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

public class HttpHeaderFieldImpl extends ASTWrapperPsiElement implements HttpHeaderField {

  public HttpHeaderFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitHeaderField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HttpHeaderFieldName getHeaderFieldName() {
    return findNotNullChildByClass(HttpHeaderFieldName.class);
  }

  @Override
  @Nullable
  public HttpHeaderFieldValue getHeaderFieldValue() {
    return findChildByClass(HttpHeaderFieldValue.class);
  }

  @Override
  @NotNull
  public String getName() {
    return HttpPsiImplUtil.getName(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return HttpPsiImplUtil.getValue(this);
  }

}
