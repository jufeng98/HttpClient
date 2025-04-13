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
  // requestMessagesGroup | multipartMessage
  public static boolean body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BODY, "<body>");
    r = requestMessagesGroup(b, l + 1);
    if (!r) r = multipartMessage(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // REQUEST_COMMENT
  public static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!nextTokenIs(b, REQUEST_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REQUEST_COMMENT);
    exit_section_(b, m, COMMENT, r);
    return r;
  }

  /* ********************************************************** */
  // DIRECTION_COMMENT_START directionName directionValue?
  public static boolean directionComment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionComment")) return false;
    if (!nextTokenIs(b, DIRECTION_COMMENT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTION_COMMENT, null);
    r = consumeToken(b, DIRECTION_COMMENT_START);
    p = r; // pin = 1
    r = r && report_error_(b, directionName(b, l + 1));
    r = p && directionComment_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // directionValue?
  private static boolean directionComment_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionComment_2")) return false;
    directionValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DIRECTION_NAME_PART
  public static boolean directionName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionName")) return false;
    if (!nextTokenIs(b, DIRECTION_NAME_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIRECTION_NAME_PART);
    exit_section_(b, m, DIRECTION_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // variable directionValueContent | variable | directionValueContent
  public static boolean directionValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionValue")) return false;
    if (!nextTokenIs(b, "<direction value>", DIRECTION_VALUE_PART, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTION_VALUE, "<direction value>");
    r = directionValue_0(b, l + 1);
    if (!r) r = variable(b, l + 1);
    if (!r) r = directionValueContent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // variable directionValueContent
  private static boolean directionValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionValue_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable(b, l + 1);
    r = r && directionValueContent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DIRECTION_VALUE_PART
  public static boolean directionValueContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directionValueContent")) return false;
    if (!nextTokenIs(b, DIRECTION_VALUE_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIRECTION_VALUE_PART);
    exit_section_(b, m, DIRECTION_VALUE_CONTENT, r);
    return r;
  }

  /* ********************************************************** */
  // variable filePathContent | variable | filePathContent
  public static boolean filePath(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filePath")) return false;
    if (!nextTokenIs(b, "<file path>", FILE_PATH_PART, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FILE_PATH, "<file path>");
    r = filePath_0(b, l + 1);
    if (!r) r = variable(b, l + 1);
    if (!r) r = filePathContent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // variable filePathContent
  private static boolean filePath_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filePath_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable(b, l + 1);
    r = r && filePathContent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FILE_PATH_PART
  public static boolean filePathContent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filePathContent")) return false;
    if (!nextTokenIs(b, FILE_PATH_PART)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FILE_PATH_PART);
    exit_section_(b, m, FILE_PATH_CONTENT, r);
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
  // globalScript
  public static boolean globalHandler(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalHandler")) return false;
    if (!nextTokenIs(b, GLOBAL_START_SCRIPT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = globalScript(b, l + 1);
    exit_section_(b, m, GLOBAL_HANDLER, r);
    return r;
  }

  /* ********************************************************** */
  // GLOBAL_START_SCRIPT_BRACE scriptBody END_SCRIPT_BRACE
  public static boolean globalScript(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalScript")) return false;
    if (!nextTokenIs(b, GLOBAL_START_SCRIPT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_SCRIPT, null);
    r = consumeToken(b, GLOBAL_START_SCRIPT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, scriptBody(b, l + 1));
    r = p && consumeToken(b, END_SCRIPT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // globalVariableName EQUALS globalVariableValue?
  public static boolean globalVariable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalVariable")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE, null);
    r = globalVariableName(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    p = r; // pin = 2
    r = r && globalVariable_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // globalVariableValue?
  private static boolean globalVariable_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalVariable_2")) return false;
    globalVariableValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // AT GLOBAL_NAME
  public static boolean globalVariableName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalVariableName")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_NAME, null);
    r = consumeTokens(b, 1, AT, GLOBAL_NAME);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GLOBAL_VALUE | variable
  public static boolean globalVariableValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalVariableValue")) return false;
    if (!nextTokenIs(b, "<global variable value>", GLOBAL_VALUE, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GLOBAL_VARIABLE_VALUE, "<global variable value>");
    r = consumeToken(b, GLOBAL_VALUE);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // headerField+
  public static boolean header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "header")) return false;
    if (!nextTokenIs(b, FIELD_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = headerField(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!headerField(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "header", c)) break;
    }
    exit_section_(b, m, HEADER, r);
    return r;
  }

  /* ********************************************************** */
  // headerFieldName COLON headerFieldValue?
  public static boolean headerField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerField")) return false;
    if (!nextTokenIs(b, FIELD_NAME)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HEADER_FIELD, null);
    r = headerFieldName(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && headerField_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // (FIELD_VALUE | variable | FIELD_VALUE variable | variable FIELD_VALUE)+
  public static boolean headerFieldValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldValue")) return false;
    if (!nextTokenIs(b, "<header field value>", FIELD_VALUE, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HEADER_FIELD_VALUE, "<header field value>");
    r = headerFieldValue_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!headerFieldValue_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "headerFieldValue", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FIELD_VALUE | variable | FIELD_VALUE variable | variable FIELD_VALUE
  private static boolean headerFieldValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldValue_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE);
    if (!r) r = variable(b, l + 1);
    if (!r) r = headerFieldValue_0_2(b, l + 1);
    if (!r) r = headerFieldValue_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // FIELD_VALUE variable
  private static boolean headerFieldValue_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldValue_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FIELD_VALUE);
    r = r && variable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // variable FIELD_VALUE
  private static boolean headerFieldValue_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "headerFieldValue_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable(b, l + 1);
    r = r && consumeToken(b, FIELD_VALUE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // HOST_VALUE | variable
  public static boolean host(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "host")) return false;
    if (!nextTokenIs(b, "<host>", HOST_VALUE, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HOST, "<host>");
    r = consumeToken(b, HOST_VALUE);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directionComment* globalHandler? globalVariable* requestBlock*
  static boolean httpFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = httpFile_0(b, l + 1);
    r = r && httpFile_1(b, l + 1);
    r = r && httpFile_2(b, l + 1);
    r = r && httpFile_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directionComment*
  private static boolean httpFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directionComment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "httpFile_0", c)) break;
    }
    return true;
  }

  // globalHandler?
  private static boolean httpFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile_1")) return false;
    globalHandler(b, l + 1);
    return true;
  }

  // globalVariable*
  private static boolean httpFile_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!globalVariable(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "httpFile_2", c)) break;
    }
    return true;
  }

  // requestBlock*
  private static boolean httpFile_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "httpFile_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!requestBlock(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "httpFile_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // INPUT_FILE_SIGN filePath
  public static boolean inputFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inputFile")) return false;
    if (!nextTokenIs(b, INPUT_FILE_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INPUT_FILE, null);
    r = consumeToken(b, INPUT_FILE_SIGN);
    p = r; // pin = 1
    r = r && filePath(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MESSAGE_TEXT
  public static boolean messageBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "messageBody")) return false;
    if (!nextTokenIs(b, MESSAGE_TEXT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE_TEXT);
    exit_section_(b, m, MESSAGE_BODY, r);
    return r;
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
  // header requestMessagesGroup
  public static boolean multipartField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multipartField")) return false;
    if (!nextTokenIs(b, FIELD_NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = header(b, l + 1);
    r = r && requestMessagesGroup(b, l + 1);
    exit_section_(b, m, MULTIPART_FIELD, r);
    return r;
  }

  /* ********************************************************** */
  // (MESSAGE_BOUNDARY multipartField?)+
  public static boolean multipartMessage(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multipartMessage")) return false;
    if (!nextTokenIs(b, MESSAGE_BOUNDARY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multipartMessage_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!multipartMessage_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "multipartMessage", c)) break;
    }
    exit_section_(b, m, MULTIPART_MESSAGE, r);
    return r;
  }

  // MESSAGE_BOUNDARY multipartField?
  private static boolean multipartMessage_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multipartMessage_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE_BOUNDARY);
    r = r && multipartMessage_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // multipartField?
  private static boolean multipartMessage_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multipartMessage_0_1")) return false;
    multipartField(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (STRING_LITERAL_PART | variable)+
  public static boolean myJsonValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "myJsonValue")) return false;
    if (!nextTokenIs(b, "<my json value>", START_VARIABLE_BRACE, STRING_LITERAL_PART)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MY_JSON_VALUE, "<my json value>");
    r = myJsonValue_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!myJsonValue_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "myJsonValue", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // STRING_LITERAL_PART | variable
  private static boolean myJsonValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "myJsonValue_0")) return false;
    boolean r;
    r = consumeToken(b, STRING_LITERAL_PART);
    if (!r) r = variable(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // OUTPUT_FILE_SIGN filePath
  public static boolean outputFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "outputFile")) return false;
    if (!nextTokenIs(b, OUTPUT_FILE_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OUTPUT_FILE, null);
    r = consumeToken(b, OUTPUT_FILE_SIGN);
    p = r; // pin = 1
    r = r && filePath(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (SLASH (SEGMENT | variable)?)+
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

  // SLASH (SEGMENT | variable)?
  private static boolean pathAbsolute_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathAbsolute_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SLASH);
    r = r && pathAbsolute_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SEGMENT | variable)?
  private static boolean pathAbsolute_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathAbsolute_0_1")) return false;
    pathAbsolute_0_1_0(b, l + 1);
    return true;
  }

  // SEGMENT | variable
  private static boolean pathAbsolute_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathAbsolute_0_1_0")) return false;
    boolean r;
    r = consumeToken(b, SEGMENT);
    if (!r) r = variable(b, l + 1);
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
  // preRequestScript
  public static boolean preRequestHandler(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preRequestHandler")) return false;
    if (!nextTokenIs(b, IN_START_SCRIPT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = preRequestScript(b, l + 1);
    exit_section_(b, m, PRE_REQUEST_HANDLER, r);
    return r;
  }

  /* ********************************************************** */
  // IN_START_SCRIPT_BRACE scriptBody END_SCRIPT_BRACE
  public static boolean preRequestScript(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "preRequestScript")) return false;
    if (!nextTokenIs(b, IN_START_SCRIPT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PRE_REQUEST_SCRIPT, null);
    r = consumeToken(b, IN_START_SCRIPT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, scriptBody(b, l + 1));
    r = p && consumeToken(b, END_SCRIPT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // queryParameter (AND queryParameter)*
  public static boolean query(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query")) return false;
    if (!nextTokenIs(b, "<query>", QUERY_NAME, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY, "<query>");
    r = queryParameter(b, l + 1);
    r = r && query_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // queryParameterKey EQUALS? queryParameterValue?
  public static boolean queryParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameter")) return false;
    if (!nextTokenIs(b, "<query parameter>", QUERY_NAME, START_VARIABLE_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PARAMETER, "<query parameter>");
    r = queryParameterKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, queryParameter_1(b, l + 1));
    r = p && queryParameter_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // EQUALS?
  private static boolean queryParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameter_1")) return false;
    consumeToken(b, EQUALS);
    return true;
  }

  // queryParameterValue?
  private static boolean queryParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameter_2")) return false;
    queryParameterValue(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // QUERY_NAME | variable
  public static boolean queryParameterKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameterKey")) return false;
    if (!nextTokenIs(b, "<query parameter key>", QUERY_NAME, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PARAMETER_KEY, "<query parameter key>");
    r = consumeToken(b, QUERY_NAME);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // QUERY_VALUE | variable
  public static boolean queryParameterValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryParameterValue")) return false;
    if (!nextTokenIs(b, "<query parameter value>", QUERY_VALUE, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PARAMETER_VALUE, "<query parameter value>");
    r = consumeToken(b, QUERY_VALUE);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // method requestTarget version? header? body? responseHandler? outputFile?
  public static boolean request(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request")) return false;
    if (!nextTokenIs(b, REQUEST_METHOD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REQUEST, null);
    r = method(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, requestTarget(b, l + 1));
    r = p && report_error_(b, request_2(b, l + 1)) && r;
    r = p && report_error_(b, request_3(b, l + 1)) && r;
    r = p && report_error_(b, request_4(b, l + 1)) && r;
    r = p && report_error_(b, request_5(b, l + 1)) && r;
    r = p && request_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // version?
  private static boolean request_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_2")) return false;
    version(b, l + 1);
    return true;
  }

  // header?
  private static boolean request_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_3")) return false;
    header(b, l + 1);
    return true;
  }

  // body?
  private static boolean request_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_4")) return false;
    body(b, l + 1);
    return true;
  }

  // responseHandler?
  private static boolean request_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_5")) return false;
    responseHandler(b, l + 1);
    return true;
  }

  // outputFile?
  private static boolean request_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "request_6")) return false;
    outputFile(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // comment? directionComment* preRequestHandler? request
  public static boolean requestBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestBlock")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REQUEST_BLOCK, "<request block>");
    r = requestBlock_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, requestBlock_1(b, l + 1));
    r = p && report_error_(b, requestBlock_2(b, l + 1)) && r;
    r = p && request(b, l + 1) && r;
    exit_section_(b, l, m, r, p, HttpParser::requestBlockRecover);
    return r || p;
  }

  // comment?
  private static boolean requestBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestBlock_0")) return false;
    comment(b, l + 1);
    return true;
  }

  // directionComment*
  private static boolean requestBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!directionComment(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "requestBlock_1", c)) break;
    }
    return true;
  }

  // preRequestHandler?
  private static boolean requestBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestBlock_2")) return false;
    preRequestHandler(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !(REQUEST_COMMENT)
  static boolean requestBlockRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestBlockRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, REQUEST_COMMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inputFile | messageBody
  public static boolean requestMessagesGroup(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestMessagesGroup")) return false;
    if (!nextTokenIs(b, "<request messages group>", INPUT_FILE_SIGN, MESSAGE_TEXT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUEST_MESSAGES_GROUP, "<request messages group>");
    r = inputFile(b, l + 1);
    if (!r) r = messageBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (variable | schema SCHEMA_SEPARATE) host? port? pathAbsolute? (QUESTION query)? (HASH fragment)?
  public static boolean requestTarget(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget")) return false;
    if (!nextTokenIs(b, "<request target>", SCHEMA_PART, START_VARIABLE_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUEST_TARGET, "<request target>");
    r = requestTarget_0(b, l + 1);
    r = r && requestTarget_1(b, l + 1);
    r = r && requestTarget_2(b, l + 1);
    r = r && requestTarget_3(b, l + 1);
    r = r && requestTarget_4(b, l + 1);
    r = r && requestTarget_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // variable | schema SCHEMA_SEPARATE
  private static boolean requestTarget_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable(b, l + 1);
    if (!r) r = requestTarget_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // schema SCHEMA_SEPARATE
  private static boolean requestTarget_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = schema(b, l + 1);
    r = r && consumeToken(b, SCHEMA_SEPARATE);
    exit_section_(b, m, null, r);
    return r;
  }

  // host?
  private static boolean requestTarget_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_1")) return false;
    host(b, l + 1);
    return true;
  }

  // port?
  private static boolean requestTarget_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_2")) return false;
    port(b, l + 1);
    return true;
  }

  // pathAbsolute?
  private static boolean requestTarget_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_3")) return false;
    pathAbsolute(b, l + 1);
    return true;
  }

  // (QUESTION query)?
  private static boolean requestTarget_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_4")) return false;
    requestTarget_4_0(b, l + 1);
    return true;
  }

  // QUESTION query
  private static boolean requestTarget_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUESTION);
    r = r && query(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (HASH fragment)?
  private static boolean requestTarget_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_5")) return false;
    requestTarget_5_0(b, l + 1);
    return true;
  }

  // HASH fragment
  private static boolean requestTarget_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requestTarget_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, HASH);
    r = r && fragment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // responseScript
  public static boolean responseHandler(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "responseHandler")) return false;
    if (!nextTokenIs(b, OUT_START_SCRIPT_BRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = responseScript(b, l + 1);
    exit_section_(b, m, RESPONSE_HANDLER, r);
    return r;
  }

  /* ********************************************************** */
  // OUT_START_SCRIPT_BRACE scriptBody END_SCRIPT_BRACE
  public static boolean responseScript(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "responseScript")) return false;
    if (!nextTokenIs(b, OUT_START_SCRIPT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RESPONSE_SCRIPT, null);
    r = consumeToken(b, OUT_START_SCRIPT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, scriptBody(b, l + 1));
    r = p && consumeToken(b, END_SCRIPT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  // SCRIPT_BODY_PAET
  public static boolean scriptBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scriptBody")) return false;
    if (!nextTokenIs(b, SCRIPT_BODY_PAET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SCRIPT_BODY_PAET);
    exit_section_(b, m, SCRIPT_BODY, r);
    return r;
  }

  /* ********************************************************** */
  // START_VARIABLE_BRACE variable_name variable_args? END_VARIABLE_BRACE
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    if (!nextTokenIs(b, START_VARIABLE_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE, null);
    r = consumeToken(b, START_VARIABLE_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, variable_name(b, l + 1));
    r = p && report_error_(b, variable_2(b, l + 1)) && r;
    r = p && consumeToken(b, END_VARIABLE_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // variable_args?
  private static boolean variable_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_2")) return false;
    variable_args(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // INTEGER | STRING
  public static boolean variable_arg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_arg")) return false;
    if (!nextTokenIs(b, "<variable arg>", INTEGER, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_ARG, "<variable arg>");
    r = consumeToken(b, INTEGER);
    if (!r) r = consumeToken(b, STRING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET variable_arg? (COMMA variable_arg)* RIGHT_BRACKET
  public static boolean variable_args(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_args")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_ARGS, null);
    r = consumeToken(b, LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, variable_args_1(b, l + 1));
    r = p && report_error_(b, variable_args_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // variable_arg?
  private static boolean variable_args_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_args_1")) return false;
    variable_arg(b, l + 1);
    return true;
  }

  // (COMMA variable_arg)*
  private static boolean variable_args_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_args_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!variable_args_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "variable_args_2", c)) break;
    }
    return true;
  }

  // COMMA variable_arg
  private static boolean variable_args_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_args_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && variable_arg(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DOLLAR
  public static boolean variable_builtin(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_builtin")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOLLAR);
    exit_section_(b, m, VARIABLE_BUILTIN, r);
    return r;
  }

  /* ********************************************************** */
  // variable_builtin? variable_reference
  public static boolean variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_name")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_NAME, "<variable name>");
    r = variable_name_0(b, l + 1);
    p = r; // pin = 1
    r = r && variable_reference(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // variable_builtin?
  private static boolean variable_name_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_name_0")) return false;
    variable_builtin(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_reference")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, VARIABLE_REFERENCE, r);
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
