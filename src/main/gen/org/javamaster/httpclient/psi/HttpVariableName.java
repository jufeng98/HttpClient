// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface HttpVariableName extends PsiElement {

  @Nullable
  HttpVariableBuiltin getVariableBuiltin();

  @Nullable
  HttpVariableReference getVariableReference();

  @NotNull
  String getName();

  boolean isBuiltin();

  @NotNull
  PsiReference[] getReferences();

}
