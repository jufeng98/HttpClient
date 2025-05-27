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

public class HttpGlobalVariableValueImpl extends ASTWrapperPsiElement implements HttpGlobalVariableValue {

  public HttpGlobalVariableValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitGlobalVariableValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpVariable getVariable() {
    return findChildByClass(HttpVariable.class);
  }

  @Override
  @Nullable
  public String getValue() {
    return HttpPsiImplUtil.getValue(this);
  }

}
