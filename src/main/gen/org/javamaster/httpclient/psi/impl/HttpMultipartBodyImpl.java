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

public class HttpMultipartBodyImpl extends ASTWrapperPsiElement implements HttpMultipartBody {

  public HttpMultipartBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitMultipartBody(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HttpHeader> getHeaderList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpHeader.class);
  }

  @Override
  @NotNull
  public HttpOrdinaryContent getOrdinaryContent() {
    return findNotNullChildByClass(HttpOrdinaryContent.class);
  }

  @Override
  @NotNull
  public PsiElement getMultipartSeperate() {
    return findNotNullChildByType(MULTIPART_SEPERATE);
  }

}
