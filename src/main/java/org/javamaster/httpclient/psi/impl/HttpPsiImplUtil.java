package org.javamaster.httpclient.psi.impl;

import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.javamaster.httpclient.psi.HttpHeaders;
import org.javamaster.httpclient.psi.HttpUrl;
import org.jetbrains.annotations.NotNull;

/**
 * @author yudong
 */
public class HttpPsiImplUtil {

    public static PsiReference @NotNull [] getReferences(HttpUrl param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }

    public static PsiReference @NotNull [] getReferences(HttpHeaders param) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(param);
    }
}
