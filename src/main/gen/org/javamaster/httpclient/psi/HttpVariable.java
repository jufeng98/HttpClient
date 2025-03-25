// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface HttpVariable extends PsiElement {

  @Nullable
  HttpVariableArgs getVariableArgs();

  @Nullable
  HttpVariableBuiltin getVariableBuiltin();

  @Nullable
  HttpVariableReference getVariableReference();

  @NotNull
  String getName();

  @NotNull
  PsiReference[] getReferences();

  boolean isBuiltin();

}
