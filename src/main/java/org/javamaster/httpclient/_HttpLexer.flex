package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.utils.LexerUtils;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;


%%

%{
    private boolean queryNameFlag = true;
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
%state IN_GLOBAL_SCRIPT, IN_GLOBAL_SCRIPT_END, IN_REQ_SCRIPT, IN_REQ_SCRIPT_END
%state IN_HTTP_REQUEST, IN_DOMAIN, IN_PORT, IN_PATH, IN_QUERY, IN_FRAGMENT
%state IN_LINE_BREAK, IN_HEADER_FIELD_VALUE, IN_HEADER_FIELD_VALUE_H, IN_TWO_LINE_BREAK, IN_BODY
%state IN_RES_SCRIPT, IN_RES_SCRIPT_END, IN_RES_SCRIPT_BODY
%state IN_INPUT_FILE_PATH, IN_OUTPUT_FILE_PATH
%state IN_MULTIPART
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
FIELD_VALUE=[^\r\n]*
FILE_PATH_PART=[^\r\n ]*
MESSAGE_BOUNDARY=--[a-zA-Z\-]+

%%

<YYINITIAL> {
  {LINE_COMMENT}              { return LINE_COMMENT; }
  {REQUEST_COMMENT}           { return REQUEST_COMMENT; }
  "<! {%"{EOL}                { yybegin(IN_GLOBAL_SCRIPT); return GLOBAL_START_SCRIPT_BRACE; }
  "< {%"{EOL}                 { yybegin(IN_REQ_SCRIPT); return IN_START_SCRIPT_BRACE; }
  {REQUEST_METHOD}            { yybegin(IN_HTTP_REQUEST); return REQUEST_METHOD; }
  {WHITE_SPACE}               { return WHITE_SPACE; }
}

<IN_HTTP_REQUEST> {
  {SCHEMA_PART}        { return SCHEMA_PART; }
  "://"                { yybegin(IN_DOMAIN); return SCHEMA_SEPARATE; }
  {HTTP_VERSION}       { return HTTP_VERSION; }
  "> {%"{EOL}          { yybegin(IN_RES_SCRIPT); return OUT_START_SCRIPT_BRACE; }
  {MESSAGE_BOUNDARY}   { yybegin(IN_MULTIPART); return MESSAGE_BOUNDARY; }
  {EOL}                { yybegin(IN_LINE_BREAK); return WHITE_SPACE; }
  {EOL_MULTI}          { if(moreTwo(yytext())) yybegin(IN_TWO_LINE_BREAK); return WHITE_SPACE; }
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

<IN_LINE_BREAK> {
  ">> "               { yybegin(IN_OUTPUT_FILE_PATH); return OUTPUT_FILE_SIGN; }
  {FIELD_NAME}        { return FIELD_NAME; }
  ":"                 { yybegin(IN_HEADER_FIELD_VALUE); return COLON; }
  {EOL_MULTI}         { if(moreTwo(yytext())) yybegin(IN_TWO_LINE_BREAK); else yybegin(IN_LINE_BREAK); return WHITE_SPACE; }
  {ONLY_SPACE}        { return WHITE_SPACE; }
}

<IN_HEADER_FIELD_VALUE> {
  {ONLY_SPACE}               { return WHITE_SPACE; }
  [^]                        { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE_H); }
}

<IN_HEADER_FIELD_VALUE_H> {
  {FIELD_VALUE}              { yybegin(IN_LINE_BREAK); return FIELD_VALUE; }
}

<IN_TWO_LINE_BREAK> {
  "< "                      { yybegin(IN_INPUT_FILE_PATH); return INPUT_SIGN; }
  ">> "                     { yybegin(IN_OUTPUT_FILE_PATH); return OUTPUT_FILE_SIGN; }
  {MESSAGE_BOUNDARY}        { yybegin(IN_MULTIPART); return MESSAGE_BOUNDARY; }
  [^]                       { yypushback(yylength()); yybegin(IN_BODY); }
}

<IN_MULTIPART> {
  {EOL_MULTI}         { if(moreTwo(yytext())) yybegin(IN_TWO_LINE_BREAK); else yybegin(IN_LINE_BREAK); return WHITE_SPACE; }
}

<IN_BODY> {
  [^\r\n]+                   { body.append(yytext()); }
  {WHITE_SPACE}              { body.append(yytext()); }
  "> {%"{EOL}                { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); return LexerUtils.createMessageText(body); }
  {MESSAGE_BOUNDARY}{EOL}    { yypushback(yylength()); yybegin(IN_HTTP_REQUEST); return LexerUtils.createMessageText(body); }
}

<IN_INPUT_FILE_PATH> {
  {FILE_PATH_PART}           { return INPUT_FILE_PATH_PART; }
  {WHITE_SPACE}              { yybegin(IN_HTTP_REQUEST); return WHITE_SPACE; }
}

<IN_OUTPUT_FILE_PATH> {
  {FILE_PATH_PART}            { return OUTPUT_FILE_PATH_PART; }
  {WHITE_SPACE}               { yybegin(IN_HTTP_REQUEST); return WHITE_SPACE; }
}

<IN_GLOBAL_SCRIPT> {
  "%}"                      { yypushback(yylength()); yybegin(IN_GLOBAL_SCRIPT_END); return LexerUtils.createScriptBody(body); }
  [^%]+                     { body.append(yytext()); }
  "%"                       { body.append(yytext()); }
  {WHITE_SPACE}             { body.append(yytext()); }
}

<IN_GLOBAL_SCRIPT_END> {
  "%}"                      { yybegin(YYINITIAL); return END_SCRIPT_BRACE; }
}

<IN_REQ_SCRIPT> {
  "%}"                      { yypushback(yylength()); yybegin(IN_REQ_SCRIPT_END); return LexerUtils.createScriptBody(body); }
  [^%]+                     { body.append(yytext()); }
  "%"                       { body.append(yytext()); }
  {WHITE_SPACE}             { body.append(yytext()); }
}

<IN_REQ_SCRIPT_END> {
  "%}"                      { yybegin(YYINITIAL); return END_SCRIPT_BRACE; }
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
