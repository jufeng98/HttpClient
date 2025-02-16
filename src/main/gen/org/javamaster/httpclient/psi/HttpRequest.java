// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

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

  //WARNING: getContentType(...) is skipped
  //matching getContentType(HttpRequest, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getContentTypeBoundary(...) is skipped
  //matching getContentTypeBoundary(HttpRequest, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getContentLength(...) is skipped
  //matching getContentLength(HttpRequest, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getHttpVersion(...) is skipped
  //matching getHttpVersion(HttpRequest, ...)
  //methods are not found in HttpPsiImplUtil

  //WARNING: getHttpHost(...) is skipped
  //matching getHttpHost(HttpRequest, ...)
  //methods are not found in HttpPsiImplUtil

}
