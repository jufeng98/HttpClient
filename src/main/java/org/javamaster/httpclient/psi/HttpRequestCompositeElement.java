package org.javamaster.httpclient.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.tree.IElementType;

public interface HttpRequestCompositeElement extends NavigatablePsiElement {
    IElementType getTokenType();
}
