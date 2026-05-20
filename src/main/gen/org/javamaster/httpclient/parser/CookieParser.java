// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.javamaster.httpclient.psi.CookieTypes.*;
import static org.javamaster.httpclient.parser.CookieParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CookieParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return cookieFile(b, l + 1);
  }

  /* ********************************************************** */
  // cookieRecord*
  static boolean cookieFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cookieFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!cookieRecord(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "cookieFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // NEW_LINE* record NEW_LINE*
  static boolean cookieRecord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cookieRecord")) return false;
    if (!nextTokenIs(b, "", COOKIE_TOKEN, NEW_LINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cookieRecord_0(b, l + 1);
    r = r && record(b, l + 1);
    r = r && cookieRecord_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEW_LINE*
  private static boolean cookieRecord_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cookieRecord_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEW_LINE)) break;
      if (!empty_element_parsed_guard_(b, "cookieRecord_0", c)) break;
    }
    return true;
  }

  // NEW_LINE*
  private static boolean cookieRecord_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cookieRecord_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, NEW_LINE)) break;
      if (!empty_element_parsed_guard_(b, "cookieRecord_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COOKIE_DATE
  public static boolean date(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "date")) return false;
    if (!nextTokenIs(b, COOKIE_DATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COOKIE_DATE);
    exit_section_(b, m, DATE, r);
    return r;
  }

  /* ********************************************************** */
  // COOKIE_TOKEN
  public static boolean domain(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "domain")) return false;
    if (!nextTokenIs(b, COOKIE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COOKIE_TOKEN);
    exit_section_(b, m, DOMAIN, r);
    return r;
  }

  /* ********************************************************** */
  // COOKIE_NAME
  public static boolean name_ck(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_ck")) return false;
    if (!nextTokenIs(b, COOKIE_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COOKIE_NAME);
    exit_section_(b, m, NAME_CK, r);
    return r;
  }

  /* ********************************************************** */
  // COOKIE_TOKEN
  public static boolean path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "path")) return false;
    if (!nextTokenIs(b, COOKIE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COOKIE_TOKEN);
    exit_section_(b, m, PATH, r);
    return r;
  }

  /* ********************************************************** */
  // domain SEPARATOR path SEPARATOR name_ck SEPARATOR value SEPARATOR date
  public static boolean record(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record")) return false;
    if (!nextTokenIs(b, COOKIE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = domain(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && path(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && name_ck(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && value(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && date(b, l + 1);
    exit_section_(b, m, RECORD, r);
    return r;
  }

  /* ********************************************************** */
  // COOKIE_VALUE
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    if (!nextTokenIs(b, COOKIE_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COOKIE_VALUE);
    exit_section_(b, m, VALUE, r);
    return r;
  }

}
