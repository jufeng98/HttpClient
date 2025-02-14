// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.javamaster.httpclient.psi.HttpTypes.*;
import static org.javamaster.httpclient.parser.HttpParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class HttpParser implements PsiParser, LightPsiParser {

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
    return httpFile(b, l + 1);
  }

  /* ********************************************************** */
  // QUERY_NAME
  public static boolean QueryParameterKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryParameterKey")) return false;
    if (!nextTokenIs(b, QUERY_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUERY_NAME);
    exit_section_(b, m, QUERY_PARAMETER_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // QUERY_VALUE
  public static boolean QueryParameterValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "QueryParameterValue")) return false;
    if (!nextTokenIs(b, QUERY_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUERY_VALUE);
    exit_section_(b, m, QUERY_PARAMETER_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // FRAGMENT_PART
  public static boolean fragment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fragment")) return false;
    if (!nextTokenIs(b, FRAGMENT_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FRAGMENT_PART);
    exit_section_(b, m, FRAGMENT, r);
    return r;
  }

  /* ********************************************************** */
  // headerFieldName COLON headerFieldValue?
  public static boolean headerField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerField")) return false;
    if (!nextTokenIs(b, FIELD_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = headerFieldName(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && headerField_2(b, l + 1);
    exit_section_(b, m, HEADER_FIELD, r);
    return r;
  }

  // headerFieldValue?
  private static boolean headerField_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerField_2")) return false;
    headerFieldValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FIELD_NAME
  public static boolean headerFieldName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldName")) return false;
    if (!nextTokenIs(b, FIELD_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_NAME);
    exit_section_(b, m, HEADER_FIELD_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // FIELD_VALUE
  public static boolean headerFieldValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldValue")) return false;
    if (!nextTokenIs(b, FIELD_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE);
    exit_section_(b, m, HEADER_FIELD_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // HOST_VALUE
  public static boolean host(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "host")) return false;
    if (!nextTokenIs(b, HOST_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HOST_VALUE);
    exit_section_(b, m, HOST, r);
    return r;
  }

  /* ********************************************************** */
  // request_block*
  static boolean httpFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!request_block(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "httpFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // REQUEST_METHOD
  public static boolean method(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "method")) return false;
    if (!nextTokenIs(b, REQUEST_METHOD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REQUEST_METHOD);
    exit_section_(b, m, METHOD, r);
    return r;
  }

  /* ********************************************************** */
  // (SLASH SEGMENT)+
  public static boolean pathAbsolute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathAbsolute")) return false;
    if (!nextTokenIs(b, SLASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pathAbsolute_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!pathAbsolute_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pathAbsolute", c)) break;
    }
    exit_section_(b, m, PATH_ABSOLUTE, r);
    return r;
  }

  // SLASH SEGMENT
  private static boolean pathAbsolute_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathAbsolute_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, SLASH, SEGMENT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // COLON PORT_SEGMENT
  public static boolean port(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "port")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLON, PORT_SEGMENT);
    exit_section_(b, m, PORT, r);
    return r;
  }

  /* ********************************************************** */
  // queryParameter (AND queryParameter)*
  public static boolean query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query")) return false;
    if (!nextTokenIs(b, QUERY_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = queryParameter(b, l + 1);
    r = r && query_1(b, l + 1);
    exit_section_(b, m, QUERY, r);
    return r;
  }

  // (AND queryParameter)*
  private static boolean query_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!query_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "query_1", c)) break;
    }
    return true;
  }

  // AND queryParameter
  private static boolean query_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && queryParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // QueryParameterKey EQUALS QueryParameterValue?
  public static boolean queryParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameter")) return false;
    if (!nextTokenIs(b, QUERY_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = QueryParameterKey(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && queryParameter_2(b, l + 1);
    exit_section_(b, m, QUERY_PARAMETER, r);
    return r;
  }

  // QueryParameterValue?
  private static boolean queryParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameter_2")) return false;
    QueryParameterValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // method requestTarget headerField*
  public static boolean request(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request")) return false;
    if (!nextTokenIs(b, REQUEST_METHOD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = method(b, l + 1);
    r = r && requestTarget(b, l + 1);
    r = r && request_2(b, l + 1);
    exit_section_(b, m, REQUEST, r);
    return r;
  }

  // headerField*
  private static boolean request_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!headerField(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "request_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // schema SCHEMA_SEPARATE host port? pathAbsolute? (QUESTION query)? (HASH fragment)? version?
  public static boolean requestTarget(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget")) return false;
    if (!nextTokenIs(b, SCHEMA_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = schema(b, l + 1);
    r = r && consumeToken(b, SCHEMA_SEPARATE);
    r = r && host(b, l + 1);
    r = r && requestTarget_3(b, l + 1);
    r = r && requestTarget_4(b, l + 1);
    r = r && requestTarget_5(b, l + 1);
    r = r && requestTarget_6(b, l + 1);
    r = r && requestTarget_7(b, l + 1);
    exit_section_(b, m, REQUEST_TARGET, r);
    return r;
  }

  // port?
  private static boolean requestTarget_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_3")) return false;
    port(b, l + 1);
    return true;
  }

  // pathAbsolute?
  private static boolean requestTarget_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_4")) return false;
    pathAbsolute(b, l + 1);
    return true;
  }

  // (QUESTION query)?
  private static boolean requestTarget_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_5")) return false;
    requestTarget_5_0(b, l + 1);
    return true;
  }

  // QUESTION query
  private static boolean requestTarget_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUESTION);
    r = r && query(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (HASH fragment)?
  private static boolean requestTarget_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_6")) return false;
    requestTarget_6_0(b, l + 1);
    return true;
  }

  // HASH fragment
  private static boolean requestTarget_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HASH);
    r = r && fragment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // version?
  private static boolean requestTarget_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_7")) return false;
    version(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // request
  public static boolean request_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_block")) return false;
    if (!nextTokenIs(b, REQUEST_METHOD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = request(b, l + 1);
    exit_section_(b, m, REQUEST_BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // SCHEMA_PART
  public static boolean schema(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "schema")) return false;
    if (!nextTokenIs(b, SCHEMA_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SCHEMA_PART);
    exit_section_(b, m, SCHEMA, r);
    return r;
  }

  /* ********************************************************** */
  // HTTP_VERSION
  public static boolean version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version")) return false;
    if (!nextTokenIs(b, HTTP_VERSION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HTTP_VERSION);
    exit_section_(b, m, VERSION, r);
    return r;
  }

}
