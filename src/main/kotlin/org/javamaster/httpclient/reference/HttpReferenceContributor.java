package org.javamaster.httpclient.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.javamaster.httpclient.psi.HttpUrl;
import org.jetbrains.annotations.NotNull;

/**
 * 实现 Ctrl + 点击 url 的特定部分跳转到 Spring 对应的 Controller 方法
 *
 * @author yudong
 */
public class HttpReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(HttpUrl.class), new HttpUrlPsiReferenceProvider());
    }

}