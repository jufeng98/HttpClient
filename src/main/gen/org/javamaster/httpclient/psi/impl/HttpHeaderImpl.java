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

public class HttpHeaderImpl extends ASTWrapperPsiElement implements HttpHeader {

  public HttpHeaderImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitHeader(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HttpHeaderField> getHeaderFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpHeaderField.class);
  }

  @Override
  public @Nullable HttpHeaderField getContentTypeField() {
    return HttpPsiImplUtil.getContentTypeField(this);
  }

  @Override
  public @Nullable HttpHeaderField getInterfaceField() {
    return HttpPsiImplUtil.getInterfaceField(this);
  }

  @Override
  public @Nullable String getContentDispositionName() {
    return HttpPsiImplUtil.getContentDispositionName(this);
  }

  @Override
  public @Nullable String getContentDispositionFileName() {
    return HttpPsiImplUtil.getContentDispositionFileName(this);
  }

}
