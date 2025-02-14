package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;


%%

%{
  private boolean queryNameFlag = true;
  public _HttpLexer() {
    this((java.io.Reader)null);
  }

  private static String zzToPrintable(CharSequence str) {
      return zzToPrintable(str.toString());
  }
%}

%public
%class _HttpLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%debug
%state IN_HTTP_REQUEST, IN_DOMAIN, IN_PORT, IN_PATH, IN_QUERY, IN_FRAGMENT
%state IN_HEADER, IN_HEADER_FIELD_VALUE, IN_HEADER_END

EOL=\R
ONLY_SPACE=[ ]+
WHITE_SPACE=\s+
LINE_COMMENT="//".*
REQUEST_COMMENT=###.*
REQUEST_METHOD=[A-Z]+
SCHEMA_PART = https|wss|http|ws
HOST_VALUE = [a-zA-Z0-9\-.]+
PORT_SEGMENT=[0-9]+
SEGMENT=[a-zA-Z_0-9]+
QUERY_PART=[^#&=/\s]+
FRAGMENT_PART=[^\s]+
HTTP_VERSION=HTTP\/[0-9]+\.[0-9]+
FIELD_NAME=[a-zA-Z0-9\-]+
FIELD_VALUE=[^\r\n ]*

%%

<YYINITIAL> {
  {LINE_COMMENT}              { return LINE_COMMENT; }
  {REQUEST_COMMENT}           { return REQUEST_COMMENT; }
  {REQUEST_METHOD}            { yybegin(IN_HTTP_REQUEST); return REQUEST_METHOD; }
  {WHITE_SPACE}               { return WHITE_SPACE; }
}

<IN_HTTP_REQUEST> {
  {SCHEMA_PART}        { return SCHEMA_PART; }
  "://"                { yybegin(IN_DOMAIN); return SCHEMA_SEPARATE; }
  {HTTP_VERSION}       { return HTTP_VERSION; }
  {EOL}                { yybegin(IN_HEADER); return WHITE_SPACE; }
  {WHITE_SPACE}        { return WHITE_SPACE; }
}

<IN_DOMAIN> {
  ":"                 { yybegin(IN_PORT); return COLON; }
  "/"                 { yybegin(IN_PATH); return SLASH; }
  "?"                 { yybegin(IN_QUERY); return QUESTION; }
  "#"                 { yybegin(IN_FRAGMENT); return HASH; }
  {HOST_VALUE}        { return HOST_VALUE; }
  {WHITE_SPACE}       { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); }
}

<IN_PORT> {
  {PORT_SEGMENT}      { yybegin(IN_PATH);return PORT_SEGMENT; }
}

<IN_PATH> {
  "/"                  { return SLASH; }
  [^?#/\s]+            { return SEGMENT; }
  "?"                  { yybegin(IN_QUERY); return QUESTION; }
  "#"                  { yybegin(IN_FRAGMENT); return HASH; }
  {WHITE_SPACE}       { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); }
}

<IN_QUERY> {
  "&"                 { queryNameFlag=true; return AND; }
  "="                 { queryNameFlag=false; return EQUALS; }
  {QUERY_PART}        { if(queryNameFlag) return QUERY_NAME; else return QUERY_VALUE; }
  "#"                 { yybegin(IN_FRAGMENT); return HASH; }
  {WHITE_SPACE}       { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); }
}

<IN_FRAGMENT> {
  {FRAGMENT_PART}     { return FRAGMENT_PART; }
  {WHITE_SPACE}       { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); }
}

<IN_HEADER> {
  {FIELD_NAME}        { return FIELD_NAME; }
  ":"                 { yybegin(IN_HEADER_FIELD_VALUE); return COLON; }
  {EOL}               { if(yylength() >= 2) { yybegin(IN_HEADER_END); return WHITE_SPACE; } else { yybegin(IN_HEADER); return WHITE_SPACE; } }
  {ONLY_SPACE}        { return WHITE_SPACE; }
}

<IN_HEADER_FIELD_VALUE> {
  {FIELD_VALUE}        { yybegin(IN_HEADER); return FIELD_VALUE; }
  {ONLY_SPACE}         { return WHITE_SPACE; }
}

<IN_HEADER_END> {
  .*                   {  }
}

[^]                    { return BAD_CHARACTER; }
