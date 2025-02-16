// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import org.apache.http.entity.ContentType;

public interface HttpMultipartField extends PsiElement {

  @NotNull
  List<HttpHeaderField> getHeaderFieldList();

  @NotNull
  HttpRequestMessagesGroup getRequestMessagesGroup();

  @Nullable
  ContentType getContentType();

}
