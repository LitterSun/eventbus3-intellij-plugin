package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

import javax.swing.*;
import java.util.List;

/**
 * Created by kgmyshin on 2015/06/07.
 * <p>
 * modify by likfe ( https://github.com/likfe/ ) in 2016/09/05
 * <p>
 * add try-catch
 */
public class SenderFilter implements Filter {

    private final List<PsiClass> mEventClasses;

    SenderFilter(List<PsiClass> eventClasses) {
        this.mEventClasses = eventClasses;
    }

    private static final Icon ICON = IconLoader.getIcon("/icons/icon.png");

    private boolean isEventClass(String clazz) {
        for (PsiClass eventClass : mEventClasses) {
            if (clazz.equals(eventClass.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiReferenceExpression) {
            if ((element = element.getParent()) instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
                PsiType[] types = callExpression.getArgumentList().getExpressionTypes();
                for (PsiType type : types) {
                    if (isEventClass(PsiUtils.getClass(type).getName())) {
                        // pattern : EventBus.getDefault().post(new Event());
                        return true;
                    }
                }
                if ((element = element.getParent()) instanceof PsiExpressionStatement) {
                    if ((element = element.getParent()) instanceof PsiCodeBlock) {
                        PsiCodeBlock codeBlock = (PsiCodeBlock) element;
                        PsiStatement[] statements = codeBlock.getStatements();
                        for (PsiStatement statement : statements) {
                            if (statement instanceof PsiDeclarationStatement) {
                                PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) statement;
                                PsiElement[] elements = declarationStatement.getDeclaredElements();
                                for (PsiElement variable : elements) {
                                    if (variable instanceof PsiLocalVariable) {
                                        PsiLocalVariable localVariable = (PsiLocalVariable) variable;
                                        PsiClass psiClass = PsiUtils.getClass(localVariable.getTypeElement().getType());
                                        try {
                                            if (isEventClass(psiClass.getName())) {
                                                // pattern :
                                                //   Event event = new Event();
                                                //   EventBus.getDefault().post(event);
                                                return true;
                                            }
                                        } catch (NullPointerException e) {
                                            System.out.println(e.toString());
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
