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
import java.net.http.HttpClient.Version;
import org.apache.http.entity.ContentType;

public class HttpRequestImpl extends ASTWrapperPsiElement implements HttpRequest {

  public HttpRequestImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitRequest(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpBody getBody() {
    return findChildByClass(HttpBody.class);
  }

  @Override
  @Nullable
  public HttpHeader getHeader() {
    return findChildByClass(HttpHeader.class);
  }

  @Override
  @Nullable
  public HttpHistoryBodyFileList getHistoryBodyFileList() {
    return findChildByClass(HttpHistoryBodyFileList.class);
  }

  @Override
  @NotNull
  public HttpMethod getMethod() {
    return findNotNullChildByClass(HttpMethod.class);
  }

  @Override
  @Nullable
  public HttpOutputFile getOutputFile() {
    return findChildByClass(HttpOutputFile.class);
  }

  @Override
  @Nullable
  public HttpRequestTarget getRequestTarget() {
    return findChildByClass(HttpRequestTarget.class);
  }

  @Override
  @Nullable
  public HttpResponseHandler getResponseHandler() {
    return findChildByClass(HttpResponseHandler.class);
  }

  @Override
  @Nullable
  public HttpVersion getVersion() {
    return findChildByClass(HttpVersion.class);
  }

  @Override
  public @Nullable ContentType getContentType() {
    return HttpPsiImplUtil.getContentType(this);
  }

  @Override
  public @Nullable String getContentTypeBoundary() {
    return HttpPsiImplUtil.getContentTypeBoundary(this);
  }

  @Override
  public @Nullable Integer getContentLength() {
    return HttpPsiImplUtil.getContentLength(this);
  }

  @Override
  public @NotNull Version getHttpVersion() {
    return HttpPsiImplUtil.getHttpVersion(this);
  }

  @Override
  public @NotNull String getHttpHost() {
    return HttpPsiImplUtil.getHttpHost(this);
  }

}
