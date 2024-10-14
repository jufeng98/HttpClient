// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpRequest extends PsiElement {

  @Nullable
  HttpBody getBody();

  @Nullable
  HttpHeaders getHeaders();

  @NotNull
  HttpMethod getMethod();

  @Nullable
  HttpOutputFile getOutputFile();

  @Nullable
  HttpScript getScript();

  @Nullable
  HttpUrl getUrl();

  @Nullable
  HttpVersion getVersion();

}
