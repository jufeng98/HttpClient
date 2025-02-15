package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.utils.LexerUtils;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;


%%

%{
    private boolean queryNameFlag = true;
    int nextState;
    StringBuilder body = new StringBuilder();

    public _HttpLexer() {
      this((java.io.Reader)null);
    }

    private static String zzToPrintable(CharSequence str) {
        return zzToPrintable(str.toString());
    }

    private static boolean moreTwo(CharSequence str) {
        return LexerUtils.moreTwoLineBreak(str);
    }
%}

%public
%class _HttpLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode
%debug
%state IN_HTTP_REQUEST, IN_DOMAIN, IN_PORT, IN_PATH, IN_QUERY, IN_FRAGMENT
%state IN_HEADER, IN_HEADER_FIELD_VALUE, IN_BODY, IN_RES_SCRIPT, IN_RES_SCRIPT_END, IN_RES_SCRIPT_BODY

EOL=\R
EOL_MULTI=(\R|[ ])+
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
  "> {%"{EOL}          { yybegin(IN_RES_SCRIPT); return START_SCRIPT_BRACE; }
  {EOL}                { yybegin(IN_HEADER); return WHITE_SPACE; }
  {EOL_MULTI}          { if(moreTwo(yytext())) yybegin(IN_BODY); return WHITE_SPACE; }
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
  {WHITE_SPACE}        { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); }
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
  {EOL_MULTI}         { if(moreTwo(yytext())) yybegin(IN_BODY); else yybegin(IN_HEADER); return WHITE_SPACE; }
  {ONLY_SPACE}        { return WHITE_SPACE; }
}

<IN_HEADER_FIELD_VALUE> {
  {FIELD_VALUE}        { yybegin(IN_HEADER); return FIELD_VALUE; }
  {ONLY_SPACE}         { return WHITE_SPACE; }
}

<IN_BODY> {
  [^\r\n]+            { body.append(yytext()); }
  {WHITE_SPACE}       { body.append(yytext()); }
  "> {%"{EOL}         { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); return LexerUtils.createMessageText(body); }
}

<IN_RES_SCRIPT> {
  "%}"                      { yypushback(yylength()); yybegin(IN_RES_SCRIPT_END); return LexerUtils.createScriptBody(body); }
  [^%]+                     { body.append(yytext()); }
  "%"                       { body.append(yytext()); }
  {WHITE_SPACE}             { body.append(yytext()); }
}

<IN_RES_SCRIPT_END> {
  "%}"                      { yybegin(IN_HTTP_REQUEST); return END_SCRIPT_BRACE; }
}



[^]                    { return BAD_CHARACTER; }
