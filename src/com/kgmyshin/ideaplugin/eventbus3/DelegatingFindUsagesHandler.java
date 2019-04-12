package com.kgmyshin.ideaplugin.eventbus3;

import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DelegatingFindUsagesHandler extends FindUsagesHandler {
    private List<FindUsagesHandler> delegates;

    public DelegatingFindUsagesHandler(@NotNull PsiElement psiElement, List<FindUsagesHandler> delegates) {
        super(psiElement);
        this.delegates = delegates;
    }

    @NotNull
    @Override
    public PsiElement[] getPrimaryElements() {
        List<PsiElement> result = new ArrayList<PsiElement>();
        for (FindUsagesHandler delegate : delegates) {
            result.addAll(Arrays.asList(delegate.getPrimaryElements()));
        }
        return result.toArray(new PsiElement[result.size()]);
    }

    @NotNull
    @Override
    public PsiElement[] getSecondaryElements() {
        List<PsiElement> result = new ArrayList<PsiElement>();
        for (FindUsagesHandler delegate : delegates) {
            result.addAll(Arrays.asList(delegate.getSecondaryElements()));
        }
        return result.toArray(new PsiElement[result.size()]);
    }

    @Override
    public boolean processElementUsages(@NotNull PsiElement element, @NotNull Processor<UsageInfo> processor, @NotNull FindUsagesOptions options) {
        boolean result = true;
        for (FindUsagesHandler delegate : delegates) {
            result = result && delegate.processElementUsages(element, processor, options);
        }
        return result;
    }

    @Override
    public boolean processUsagesInText(@NotNull PsiElement element, @NotNull Processor<UsageInfo> processor, @NotNull GlobalSearchScope searchScope) {
        boolean result = true;
        for (FindUsagesHandler delegate : delegates) {
            result = result && delegate.processUsagesInText(element, processor, searchScope);
        }
        return result;
    }

    @Override
    public Collection<PsiReference> findReferencesToHighlight(@NotNull PsiElement target, @NotNull SearchScope searchScope) {
        List<PsiReference> result = new ArrayList<PsiReference>();
        for (FindUsagesHandler delegate : delegates) {
            result.addAll((delegate.findReferencesToHighlight(target, searchScope)));
        }
        return result;
    }
}
