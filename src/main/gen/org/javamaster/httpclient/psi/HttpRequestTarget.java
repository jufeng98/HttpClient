// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpRequestTarget extends PsiElement {

  @Nullable
  HttpFragment getFragment();

  @Nullable
  HttpHost getHost();

  @Nullable
  HttpPathAbsolute getPathAbsolute();

  @Nullable
  HttpPort getPort();

  @Nullable
  HttpQuery getQuery();

  @Nullable
  HttpSchema getSchema();

  @Nullable
  HttpVariable getVariable();

  @Nullable
  HttpVersion getVersion();

  //WARNING: getHttpUrl(...) is skipped
  //matching getHttpUrl(HttpRequestTarget, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getReferences(...) is skipped
  //matching getReferences(HttpRequestTarget, ...)
  //methods are not found in HttpPsiImplUtil

}
