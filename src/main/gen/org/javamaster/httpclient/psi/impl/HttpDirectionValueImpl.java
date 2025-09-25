// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.javamaster.httpclient.psi.HttpTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.javamaster.httpclient.psi.*;
import com.intellij.psi.PsiReference;

public class HttpDirectionValueImpl extends ASTWrapperPsiElement implements HttpDirectionValue {

  public HttpDirectionValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitDirectionValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpDirectionValueContent getDirectionValueContent() {
    return findChildByClass(HttpDirectionValueContent.class);
  }

  @Override
  @Nullable
  public HttpVariable getVariable() {
    return findChildByClass(HttpVariable.class);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return HttpPsiImplUtil.getReferences(this);
  }

}
