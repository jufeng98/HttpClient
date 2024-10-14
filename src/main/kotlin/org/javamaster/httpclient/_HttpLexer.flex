package org.javamaster.httpclient;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;

%%

%{
  public _HttpLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _HttpLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

PATH=(([A-E]:)|[a-z./])([\u4E00-\u9FA5\w\-./\\]+"/"?)+
URL_DESC=(https?|wss?|\{\{)[-\w+&${}()#/%?=~|!:,.;]*[-\w+,&${}()#/%=~| ]
HEADER_DESC=[a-zA-Z\-]+:[\u4E00-\u9FA5\w,${}()=;%\\\".\-*:/ ]+
REQUEST_COMMENT=#.*
LINE_COMMENT="//".*
VARIABLE_DEFINE=@[a-zA-Z_0-9]+[\w{}=]+
URL_FORM_ENCODE=[\w,&$={}():]*
JSON_TEXT=\{}|\[]|\{[\s\"][^#>]*}|\[[\s\d\"a-z{][^#>]*]
XML_TEXT=<[!\w][^♣]*[a-zA-Z_0-9]>
MULTIPART_SEPERATE=--[\w\-]+
JS_SCRIPT=\{%[^#]*%}
SPACE=[ \t\n\x0B\f\r]+

%%
<YYINITIAL> {
  {WHITE_SPACE}              { return WHITE_SPACE; }

  "<"                        { return T_LT; }
  ">"                        { return T_RT; }
  ">>"                       { return T_RT_DBL; }
  "GET"                      { return GET; }
  "POST"                     { return POST; }
  "DELETE"                   { return DELETE; }
  "PUT"                      { return PUT; }

  {PATH}                     { return PATH; }
  {URL_DESC}                 { return URL_DESC; }
  {HEADER_DESC}              { return HEADER_DESC; }
  {REQUEST_COMMENT}          { return REQUEST_COMMENT; }
  {LINE_COMMENT}             { return LINE_COMMENT; }
  {VARIABLE_DEFINE}          { return VARIABLE_DEFINE; }
  {URL_FORM_ENCODE}          { return URL_FORM_ENCODE; }
  {JSON_TEXT}                { return JSON_TEXT; }
  {XML_TEXT}                 { return XML_TEXT; }
  {MULTIPART_SEPERATE}       { return MULTIPART_SEPERATE; }
  {JS_SCRIPT}                { return JS_SCRIPT; }
  {SPACE}                    { return SPACE; }

}

[^] { return BAD_CHARACTER; }
