// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.javamaster.httpclient.psi.CookieTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.javamaster.httpclient.psi.*;

public class CookieNameCkImpl extends ASTWrapperPsiElement implements CookieNameCk {

  public CookieNameCkImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CookieVisitor visitor) {
    visitor.visitNameCk(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CookieVisitor) accept((CookieVisitor)visitor);
    else super.accept(visitor);
  }

}
