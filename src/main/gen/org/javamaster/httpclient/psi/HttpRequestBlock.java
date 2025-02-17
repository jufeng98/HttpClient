// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpRequestBlock extends PsiElement {

  @NotNull
  HttpComment getComment();

  @NotNull
  List<HttpDirectionComment> getDirectionCommentList();

  @Nullable
  HttpPreRequestHandler getPreRequestHandler();

  @NotNull
  HttpRequest getRequest();

}
