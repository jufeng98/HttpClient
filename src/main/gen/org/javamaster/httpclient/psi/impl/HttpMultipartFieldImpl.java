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
import org.apache.http.entity.ContentType;

public class HttpMultipartFieldImpl extends ASTWrapperPsiElement implements HttpMultipartField {

  public HttpMultipartFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitMultipartField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HttpHeader getHeader() {
    return findNotNullChildByClass(HttpHeader.class);
  }

  @Override
  @NotNull
  public HttpRequestMessagesGroup getRequestMessagesGroup() {
    return findNotNullChildByClass(HttpRequestMessagesGroup.class);
  }

  @Override
  public @Nullable ContentType getContentType() {
    return HttpPsiImplUtil.getContentType(this);
  }

}
