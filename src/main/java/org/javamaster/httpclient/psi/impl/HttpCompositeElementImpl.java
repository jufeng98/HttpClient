package org.javamaster.httpclient.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.psi.HttpCompositeElement;
import org.jetbrains.annotations.NotNull;

public class HttpCompositeElementImpl extends ASTWrapperPsiElement implements HttpCompositeElement {
    public HttpCompositeElementImpl(@NotNull ASTNode node) {
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
