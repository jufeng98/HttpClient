// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.net.http.HttpClient.Version;
import org.apache.http.entity.ContentType;

public interface HttpRequest extends PsiElement {

  @Nullable
  HttpBody getBody();

  @Nullable
  HttpHeader getHeader();

  @Nullable
  HttpHistoryBodyFileList getHistoryBodyFileList();

  @NotNull
  HttpMethod getMethod();

  @Nullable
  HttpOutputFile getOutputFile();

  @Nullable
  HttpRequestTarget getRequestTarget();

  @Nullable
  HttpResponseHandler getResponseHandler();

  @Nullable
  HttpVersion getVersion();

  @Nullable
  ContentType getContentType();

  @Nullable
  String getContentTypeBoundary();

  @Nullable
  Integer getContentLength();

  @NotNull
  Version getHttpVersion();

  @NotNull
  String getHttpHost();

}
