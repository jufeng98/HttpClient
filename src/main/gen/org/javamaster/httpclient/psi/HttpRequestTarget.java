// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpRequestTarget extends PsiElement {

  @Nullable
  HttpFragment getFragment();

  @NotNull
  HttpHost getHost();

  @Nullable
  HttpPathAbsolute getPathAbsolute();

  @Nullable
  HttpPort getPort();

  @Nullable
  HttpQuery getQuery();

  @NotNull
  HttpSchema getSchema();

  @Nullable
  HttpVersion getVersion();

  //WARNING: getHttpUrl(...) is skipped
  //matching getHttpUrl(HttpRequestTarget, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getReferences(...) is skipped
  //matching getReferences(HttpRequestTarget, ...)
  //methods are not found in HttpPsiImplUtil

}
