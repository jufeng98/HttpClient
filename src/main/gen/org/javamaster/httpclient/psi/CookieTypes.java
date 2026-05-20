// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.httpclient.psi.impl.*;

public interface CookieTypes {

  IElementType DATE = new CookieElementType("DATE");
  IElementType DOMAIN = new CookieElementType("DOMAIN");
  IElementType NAME_CK = new CookieElementType("NAME_CK");
  IElementType PATH = new CookieElementType("PATH");
  IElementType RECORD = new CookieElementType("RECORD");
  IElementType VALUE = new CookieElementType("VALUE");

  IElementType COOKIE_DATE = new CookieTokenType("COOKIE_DATE");
  IElementType COOKIE_NAME = new CookieTokenType("COOKIE_NAME");
  IElementType COOKIE_TOKEN = new CookieTokenType("COOKIE_TOKEN");
  IElementType COOKIE_VALUE = new CookieTokenType("COOKIE_VALUE");
  IElementType LINE_COMMENT = new CookieTokenType("LINE_COMMENT");
  IElementType NEW_LINE = new CookieTokenType("NEW_LINE");
  IElementType SEPARATOR = new CookieTokenType("SEPARATOR");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == DATE) {
        return new CookieDateImpl(node);
      }
      else if (type == DOMAIN) {
        return new CookieDomainImpl(node);
      }
      else if (type == NAME_CK) {
        return new CookieNameCkImpl(node);
      }
      else if (type == PATH) {
        return new CookiePathImpl(node);
      }
      else if (type == RECORD) {
        return new CookieRecordImpl(node);
      }
      else if (type == VALUE) {
        return new CookieValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
