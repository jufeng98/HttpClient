package org.javamaster.httpclient;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static org.javamaster.httpclient.psi.CookieTypes.*;

%%

%{
  public _CookieLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _CookieLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%state IN_DOMAIN, IN_PATH, IN_NAME, IN_VALUE, IN_DATE
%eof{
  return;
%eof}

EOL=\R

LINE_COMMENT=#.*
NEW_LINE=[\r\n]+
SEPARATOR=[\t ]+
COOKIE_TOKEN=[a-zA-Z0-9_/\-.]+
COOKIE_CONTENT=[a-zA-Z0-9\-.!#$%&*+_]+
COOKIE_DATE=[a-zA-Z0-9\-,: ]+

%%
<YYINITIAL> {
  {LINE_COMMENT}       { return LINE_COMMENT; }
  {COOKIE_TOKEN}       { yybegin(IN_DOMAIN); return COOKIE_TOKEN; }
  {NEW_LINE}           { return NEW_LINE; }
}

<IN_DOMAIN> {
  {SEPARATOR}          { yybegin(IN_PATH); return SEPARATOR; }
}

<IN_PATH> {
  {COOKIE_TOKEN}       { return COOKIE_TOKEN; }
  {SEPARATOR}          { yybegin(IN_NAME); return SEPARATOR; }
}

<IN_NAME> {
  {COOKIE_CONTENT}        { return COOKIE_NAME; }
  {SEPARATOR}             { yybegin(IN_VALUE); return SEPARATOR; }
}

<IN_VALUE> {
  {COOKIE_CONTENT}       { return COOKIE_VALUE; }
  {SEPARATOR}             { yybegin(IN_DATE); return SEPARATOR; }
}

<IN_DATE> {
  {COOKIE_DATE}           { yybegin(YYINITIAL); return COOKIE_DATE; }
}

[^] { return BAD_CHARACTER; }
