// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

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

  @NotNull
  String getHttpUrl();

  @NotNull
  PsiReference[] getReferences();

}
