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

public class HttpRequestTargetImpl extends ASTWrapperPsiElement implements HttpRequestTarget {

  public HttpRequestTargetImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitRequestTarget(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpFragment getFragment() {
    return findChildByClass(HttpFragment.class);
  }

  @Override
  @NotNull
  public HttpHost getHost() {
    return findNotNullChildByClass(HttpHost.class);
  }

  @Override
  @Nullable
  public HttpPathAbsolute getPathAbsolute() {
    return findChildByClass(HttpPathAbsolute.class);
  }

  @Override
  @Nullable
  public HttpPort getPort() {
    return findChildByClass(HttpPort.class);
  }

  @Override
  @Nullable
  public HttpQuery getQuery() {
    return findChildByClass(HttpQuery.class);
  }

  @Override
  @NotNull
  public HttpSchema getSchema() {
    return findNotNullChildByClass(HttpSchema.class);
  }

  @Override
  @Nullable
  public HttpVersion getVersion() {
    return findChildByClass(HttpVersion.class);
  }

}
