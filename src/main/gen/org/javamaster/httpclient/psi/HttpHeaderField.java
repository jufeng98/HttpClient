// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpHeaderField extends PsiElement {

  @NotNull
  HttpHeaderFieldName getHeaderFieldName();

  @Nullable
  HttpHeaderFieldValue getHeaderFieldValue();

  @NotNull
  String getName();

  @Nullable
  String getValue();

}
