// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpHeader extends PsiElement {

  @NotNull
  List<HttpHeaderField> getHeaderFieldList();

  @Nullable HttpHeaderField getContentTypeField();

  @Nullable HttpHeaderField getInterfaceField();

  @Nullable String getContentDispositionName();

  @Nullable String getContentDispositionFileName();

}
