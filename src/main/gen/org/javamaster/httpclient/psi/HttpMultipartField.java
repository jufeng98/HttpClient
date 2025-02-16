// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpMultipartField extends PsiElement {

  @NotNull
  List<HttpHeaderField> getHeaderFieldList();

  @NotNull
  HttpRequestMessagesGroup getRequestMessagesGroup();

  //WARNING: getContentType(...) is skipped
  //matching getContentType(HttpMultipartField, ...)
  //methods are not found in HttpPsiImplUtil

}
