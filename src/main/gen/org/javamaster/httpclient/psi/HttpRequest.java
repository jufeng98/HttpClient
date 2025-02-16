// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.net.http.HttpClient.Version;
import org.apache.http.entity.ContentType;

public interface HttpRequest extends PsiElement {

  @NotNull
  List<HttpHeaderField> getHeaderFieldList();

  @NotNull
  HttpMethod getMethod();

  @Nullable
  HttpMultipartMessage getMultipartMessage();

  @Nullable
  HttpOutputFile getOutputFile();

  @Nullable
  HttpRequestMessagesGroup getRequestMessagesGroup();

  @Nullable
  HttpRequestTarget getRequestTarget();

  @Nullable
  HttpResponseHandler getResponseHandler();

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
