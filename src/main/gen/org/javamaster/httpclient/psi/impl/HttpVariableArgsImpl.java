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

public class HttpVariableArgsImpl extends ASTWrapperPsiElement implements HttpVariableArgs {

  public HttpVariableArgsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitVariableArgs(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HttpVariableArg> getVariableArgList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpVariableArg.class);
  }

  @Override
  public @NotNull Object @NotNull [] toArgsList() {
    return HttpPsiImplUtil.toArgsList(this);
  }

}
