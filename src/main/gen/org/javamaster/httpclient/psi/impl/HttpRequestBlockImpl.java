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

public class HttpRequestBlockImpl extends ASTWrapperPsiElement implements HttpRequestBlock {

  public HttpRequestBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitRequestBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public HttpComment getComment() {
    return findNotNullChildByClass(HttpComment.class);
  }

  @Override
  @NotNull
  public List<HttpDirectionComment> getDirectionCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpDirectionComment.class);
  }

  @Override
  @Nullable
  public HttpPreRequestHandler getPreRequestHandler() {
    return findChildByClass(HttpPreRequestHandler.class);
  }

  @Override
  @NotNull
  public HttpRequest getRequest() {
    return findNotNullChildByClass(HttpRequest.class);
  }

}
