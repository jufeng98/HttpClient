package org.javamaster.httpclient.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.psi.HttpRequestCompositeElement;
import org.jetbrains.annotations.NotNull;

public class HttpRequestCompositeElementImpl extends ASTWrapperPsiElement implements HttpRequestCompositeElement {
    public HttpRequestCompositeElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public IElementType getTokenType() {
        return getNode().getElementType();
    }

    public String toString() {
        return this.getTokenType().toString();
    }
}
