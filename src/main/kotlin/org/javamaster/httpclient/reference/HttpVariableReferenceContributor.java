package org.javamaster.httpclient.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.psi.HttpHeaders;
import org.javamaster.httpclient.psi.HttpUrl;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpVariableReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        HttpVariablePsiReferenceProvider provider = new HttpVariablePsiReferenceProvider();

        registrar.registerReferenceProvider(PlatformPatterns.psiElement(HttpUrl.class), provider);
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(HttpHeaders.class), provider);
    }

}