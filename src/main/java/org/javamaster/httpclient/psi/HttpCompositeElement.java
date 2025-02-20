package org.javamaster.httpclient.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.tree.IElementType;

public interface HttpCompositeElement extends NavigatablePsiElement {
    IElementType getTokenType();
}
