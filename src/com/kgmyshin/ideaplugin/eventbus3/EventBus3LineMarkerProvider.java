package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kgmyshin on 15/06/08.
 * <p>
 * modify by likfe ( https://github.com/likfe/ ) in 2016/09/05
 * <p>
 * 1.fix package name for EventBus
 * <p>
 * 2. try use `GlobalSearchScope.projectScope(project)` to just search for project,but get NullPointerException,
 * the old use `GlobalSearchScope.allScope(project)` ,it will search in project and libs,so slow
 */
public class EventBus3LineMarkerProvider implements LineMarkerProvider {

    public static final Icon ICON = IconLoader.getIcon("/icons/icon.png");

    public static final int MAX_USAGES = 100;

    private static GutterIconNavigationHandler<PsiElement> SHOW_SENDERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethod) {
                        Project project = psiElement.getProject();
                        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                        PsiClass eventBusClass = javaPsiFacade.findClass("org.greenrobot.eventbus.EventBus", GlobalSearchScope.allScope(project));
                        //PsiClass eventBusClass = javaPsiFacade.findClass("org.greenrobot.eventbus.EventBus", GlobalSearchScope.projectScope(project));
                        PsiMethod postMethod = eventBusClass.findMethodsByName("post", false)[0];
                        PsiMethod method = (PsiMethod) psiElement;
                        PsiClass eventClass = ((PsiClassType) method.getParameterList().getParameters()[0].getTypeElement().getType()).resolve();
                        List<PsiClass> allClasses = searchSubClasses(eventClass);
                        allClasses.add(eventClass);
                        new ShowUsagesAction(new SenderFilter(allClasses)).startFindUsages(postMethod, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES);
                    }
                }
            };

    private static GutterIconNavigationHandler<PsiElement> SHOW_RECEIVERS =
            new GutterIconNavigationHandler<PsiElement>() {
                @Override
                public void navigate(MouseEvent e, PsiElement psiElement) {
                    if (psiElement instanceof PsiMethodCallExpression) {
                        PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
                        PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                        if (expressionTypes.length > 0) {
                            PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                            if (eventClass != null) {
                                List<PsiElement> generatedDeclarations = new ArrayList<>();
                                generatedDeclarations.add(PsiUtils.getClass(expressionTypes[0]));
                                for (PsiType superType : expressionTypes[0].getSuperTypes()) {
                                    generatedDeclarations.add(PsiUtils.getClass(superType));
                                }
                                new ShowUsagesAction(new ReceiverFilter()).startFindUsages(generatedDeclarations, eventClass, new RelativePoint(e), PsiUtilBase.findEditor(psiElement), MAX_USAGES);
                            }
                        }
                    }
                }
            };

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        if (PsiUtils.isEventBusPost(psiElement)) {
            return new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), ICON,
                    Pass.UPDATE_ALL, null, SHOW_RECEIVERS,
                    GutterIconRenderer.Alignment.LEFT);
        } else if (PsiUtils.isEventBusReceiver(psiElement)) {
            return new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), ICON,
                    Pass.UPDATE_ALL, null, SHOW_SENDERS,
                    GutterIconRenderer.Alignment.LEFT);
        }
        return null;
    }


    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
    }

    @NotNull
    public static Collection<UsageInfo> search(@NotNull PsiElement element) {
        FindUsagesManager findUsagesManager = ((FindManagerImpl) FindManager.getInstance(element.getProject())).getFindUsagesManager();
        FindUsagesHandler findUsagesHandler = findUsagesManager.getFindUsagesHandler(element, false);
        final FindUsagesOptions options = findUsagesHandler.getFindUsagesOptions();
        final CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<UsageInfo>();
        for (PsiElement primaryElement : findUsagesHandler.getPrimaryElements()) {
            findUsagesHandler.processElementUsages(primaryElement, processor, options);
        }
        for (PsiElement secondaryElement : findUsagesHandler.getSecondaryElements()) {
            findUsagesHandler.processElementUsages(secondaryElement, processor, options);
        }
        return processor.getResults();
    }

    @NotNull
    private static List<PsiClass> searchSubClasses(@NotNull PsiClass element) {
        Collection<UsageInfo> usageInfos = search(element);
        List<PsiClass> subClasses = new ArrayList<>();
        for (UsageInfo usageInfo : usageInfos) {
            PsiElement psiElement = usageInfo.getElement();
            if (psiElement.getParent() instanceof PsiReferenceList) {
                PsiReferenceList referenceListElement = (PsiReferenceList) psiElement.getParent();
                if (referenceListElement.getRole() == PsiReferenceList.Role.EXTENDS_LIST) {
                    if (referenceListElement.getParent() instanceof PsiClass) {
                        subClasses.add((PsiClass) referenceListElement.getParent());
                    }
                }
            }
        }
        return subClasses;
    }
}
