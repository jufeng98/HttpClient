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

public class HttpGlobalVariableNameImpl extends ASTWrapperPsiElement implements HttpGlobalVariableName {

  public HttpGlobalVariableNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitGlobalVariableName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public String getName() {
    return HttpPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return HttpPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return HttpPsiImplUtil.getReferences(this);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return HttpPsiImplUtil.getNameIdentifier(this);
  }

}
