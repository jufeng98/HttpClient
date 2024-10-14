// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.javamaster.httpclient.psi.HttpTypes.*;
import org.javamaster.httpclient.inject.HttpPsiLanguageInjectionHost;
import org.javamaster.httpclient.psi.*;

public class HttpOrdinaryContentImpl extends HttpPsiLanguageInjectionHost implements HttpOrdinaryContent {

  public HttpOrdinaryContentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HttpVisitor visitor) {
    visitor.visitOrdinaryContent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HttpVisitor) accept((HttpVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public HttpFile getFile() {
    return findChildByClass(HttpFile.class);
  }

  @Override
  @Nullable
  public PsiElement getJsonText() {
    return findChildByType(JSON_TEXT);
  }

  @Override
  @Nullable
  public PsiElement getUrlDesc() {
    return findChildByType(URL_DESC);
  }

  @Override
  @Nullable
  public PsiElement getUrlFormEncode() {
    return findChildByType(URL_FORM_ENCODE);
  }

  @Override
  @Nullable
  public PsiElement getXmlText() {
    return findChildByType(XML_TEXT);
  }

}
