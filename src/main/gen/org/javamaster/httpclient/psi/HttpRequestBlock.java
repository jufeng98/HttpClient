// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface HttpRequestBlock extends PsiElement {

  @Nullable
  HttpComment getComment();

  @NotNull
  List<HttpDirectionComment> getDirectionCommentList();

  @Nullable
  HttpPreRequestHandler getPreRequestHandler();

  @Nullable
  HttpRequest getRequest();

}
