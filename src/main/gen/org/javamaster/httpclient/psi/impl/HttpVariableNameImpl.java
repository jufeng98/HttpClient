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

public class HttpVariableNameImpl extends ASTWrapperPsiElement implements HttpVariableName {

  public HttpVariableNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitVariableName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpVariableBuiltin getVariableBuiltin() {
    return findChildByClass(HttpVariableBuiltin.class);
  }

  @Override
  @NotNull
  public HttpVariableReference getVariableReference() {
    return findNotNullChildByClass(HttpVariableReference.class);
  }

  @Override
  public @NotNull String getName() {
    return HttpPsiImplUtil.getName(this);
  }

  @Override
  public boolean isBuiltin() {
    return HttpPsiImplUtil.isBuiltin(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return HttpPsiImplUtil.getReferences(this);
  }

}
