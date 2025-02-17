package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import static org.javamaster.httpclient.utils.LexerUtils.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;


%%

%{
        private boolean nameFlag = true;
        int nextState;
        public int matchTimes = 0;

        public _HttpLexer() {
          this((java.io.Reader)null);
        }

        private static String zzToPrintable(CharSequence str) {
          return zzToPrintable(str.toString());
        }
%}

%public
%class _HttpLexer
%implements com.intellij.lexer.FlexLexer
%function advance
%type IElementType
%unicode
%debug
%state IN_GLOBAL_SCRIPT, IN_GLOBAL_SCRIPT_END, IN_PRE_SCRIPT, IN_PRE_SCRIPT_END, IN_DIRECTION_COMMENT
%state IN_FIRST_LINE, IN_HOST, IN_PORT, IN_PATH, IN_QUERY, IN_FRAGMENT, IN_BODY
%state IN_HEADER, IN_HEADER_FIELD_NAME, IN_HEADER_FIELD_VALUE, IN_HEADER_FIELD_VALUE_NO_SPACE
%state IN_POST_SCRIPT, IN_POST_SCRIPT_END, IN_RES_SCRIPT_BODY_PAET
%state IN_INPUT_FILE_PATH, IN_OUTPUT_FILE, IN_OUTPUT_FILE_PATH, IN_VERSION
%state IN_MULTIPART, IN_VARIABLE, IN_DINAMIC_VARIABLE, IN_GLOBAL_VARIABLE

EOL=\R
EOL_MULTI=[ ]*\R+
ONLY_SPACE=[ ]+
WHITE_SPACE=\s+
LINE_COMMENT="//".*
REQUEST_COMMENT=###.*
REQUEST_METHOD=[A-Z]+
SCHEMA_PART=https|wss|http|ws|dubbo
HOST_VALUE=[a-zA-Z0-9\-.]+
PORT_SEGMENT=[0-9]+
SEGMENT=[a-zA-Z_0-9]+
QUERY_PART=[^#&=/{\s]+
FRAGMENT_PART=[^\s]+
HTTP_VERSION=HTTP\/[0-9]+\.[0-9]+
FIELD_NAME=[a-zA-Z0-9\-]+
FIELD_VALUE=[^\r\n ]*
FILE_PATH_PART=[^\r\n<> ]+
MESSAGE_BOUNDARY=--[a-zA-Z\-]+
VARIABLE_NAME=[[a-zA-Z0-9_\-]--[$}= ]]+
GLOBAL_VARIABLE_PART=[^\r\n={}]+
DIRECTION_PART=[^\r\n ]+
%%

  {LINE_COMMENT}{EOL}         { yypushback(1); return LINE_COMMENT; }

<YYINITIAL> {
  {REQUEST_COMMENT}{EOL}      { return REQUEST_COMMENT; }
  "# @"                       { nameFlag = true; yybegin(IN_DIRECTION_COMMENT); return DIRECTION_COMMENT_START; }
  "<! {%"{EOL_MULTI}          { yybegin(IN_GLOBAL_SCRIPT); return GLOBAL_START_SCRIPT_BRACE; }
  "< {%"{EOL_MULTI}           { yybegin(IN_PRE_SCRIPT); return IN_START_SCRIPT_BRACE; }
  "@"                         { nameFlag = true; yybegin(IN_GLOBAL_VARIABLE); return AT; }
  {REQUEST_METHOD}            { yybegin(IN_FIRST_LINE); return REQUEST_METHOD; }
  {WHITE_SPACE}               { return WHITE_SPACE; }
}

<IN_GLOBAL_SCRIPT> {
  "%}"                      { yypushback(yylength()); yybegin(IN_GLOBAL_SCRIPT_END); return SCRIPT_BODY_PAET; }
  [^%]+                     {  }
  "%"                       {  }
  {WHITE_SPACE}             {  }
}

<IN_GLOBAL_SCRIPT_END> {
  "%}"{EOL_MULTI}              { yybegin(YYINITIAL); return END_SCRIPT_BRACE; }
  {WHITE_SPACE}                { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_GLOBAL_VARIABLE> {
  {GLOBAL_VARIABLE_PART}        { if(nameFlag) return GLOBAL_NAME; else return GLOBAL_VALUE; }
  "="                           { nameFlag = false; return EQUALS; }
  "{{"                          { nextState = IN_GLOBAL_VARIABLE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {ONLY_SPACE}                  { return WHITE_SPACE; }
  {EOL_MULTI}                   { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_VARIABLE> {
  {VARIABLE_NAME}        { return IDENTIFIER; }
  "$"                    { yybegin(IN_DINAMIC_VARIABLE); return DOLLAR; }
  {ONLY_SPACE}           { return WHITE_SPACE; }
  "}}"                   { yybegin(nextState); return END_VARIABLE_BRACE; }
}

<IN_DINAMIC_VARIABLE> {
  {VARIABLE_NAME}          { return IDENTIFIER; }
  {ONLY_SPACE}             { return WHITE_SPACE; }
  "}}"                     { yybegin(nextState); return END_VARIABLE_BRACE; }
}

<IN_PRE_SCRIPT> {
  "%}"                      { yypushback(yylength()); yybegin(IN_PRE_SCRIPT_END); return SCRIPT_BODY_PAET; }
  [^%]+                     {  }
  "%"                       {  }
  {WHITE_SPACE}             {  }
}

<IN_PRE_SCRIPT_END> {
  "%}"{EOL_MULTI}              { yybegin(YYINITIAL); return END_SCRIPT_BRACE; }
}

<IN_DIRECTION_COMMENT> {
  {DIRECTION_PART}              { if(nameFlag) return DIRECTION_NAME_PART; else return DIRECTION_VALUE_PART; }
  {ONLY_SPACE}                  { nameFlag = false; return WHITE_SPACE; }
  {EOL}                         { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_FIRST_LINE> {
  {SCHEMA_PART}        { return SCHEMA_PART; }
  "://"                { yybegin(IN_HOST); return SCHEMA_SEPARATE; }
  "{{"                 { nextState = IN_HOST; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {ONLY_SPACE}         { return WHITE_SPACE; }
  {EOL}                { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_HOST> {
  ":"                 { yybegin(IN_PORT); return COLON; }
  "/"                 { yybegin(IN_PATH); return SLASH; }
  "?"                 { nameFlag = true; yybegin(IN_QUERY); return QUESTION; }
  "#"                 { yybegin(IN_FRAGMENT); return HASH; }
  "{{"                { nextState = IN_HOST; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {HOST_VALUE}        { return HOST_VALUE; }
  {ONLY_SPACE}        { yybegin(IN_VERSION); return WHITE_SPACE; }
  {EOL}               { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_PORT> {
  {PORT_SEGMENT}      { yybegin(IN_PATH); return PORT_SEGMENT; }
  {ONLY_SPACE}        { yybegin(IN_VERSION); return WHITE_SPACE; }
  {EOL}               { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_PATH> {
  "/"                  { return SLASH; }
  [^?#/{\s]+           { return SEGMENT; }
  "{{"                 { nextState = IN_PATH; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  "?"                  { nameFlag = true; yybegin(IN_QUERY); return QUESTION; }
  "#"                  { yybegin(IN_FRAGMENT); return HASH; }
  {ONLY_SPACE}         { yybegin(IN_VERSION); return WHITE_SPACE; }
  {EOL}                { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_QUERY> {
  "&"                 { nameFlag = true; return AND; }
  "="                 { nameFlag = false; return EQUALS; }
  "{{"                { nextState = IN_QUERY; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {QUERY_PART}        { if(nameFlag) return QUERY_NAME; else return QUERY_VALUE; }
  "#"                 { yybegin(IN_FRAGMENT); return HASH; }
  {ONLY_SPACE}        { yybegin(IN_VERSION); return WHITE_SPACE; }
  {EOL}               { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_FRAGMENT> {
  {FRAGMENT_PART}     { return FRAGMENT_PART; }
  {ONLY_SPACE}        { yybegin(IN_VERSION); return WHITE_SPACE; }
  {EOL}               { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_VERSION> {
  {HTTP_VERSION}       { return HTTP_VERSION; }
  {ONLY_SPACE}         { return WHITE_SPACE; }
  {EOL}                { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_HEADER> {
  [^\r\n]           { yypushback(yylength()); yybegin(IN_HEADER_FIELD_NAME); }
  {EOL}             { yybegin(IN_BODY); return WHITE_SPACE; }
}

<IN_HEADER_FIELD_NAME> {
  {FIELD_NAME}               { return FIELD_NAME; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  ":"                        { yybegin(IN_HEADER_FIELD_VALUE); return COLON; }
}

<IN_HEADER_FIELD_VALUE> {
  "{{"                       { nextState = IN_HEADER_FIELD_VALUE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  [^\r\n]                    { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE_NO_SPACE); }
  {EOL}                      { yybegin(IN_HEADER); return WHITE_SPACE; }
}

<IN_HEADER_FIELD_VALUE_NO_SPACE> {
  {FIELD_VALUE}                         { yybegin(IN_HEADER_FIELD_VALUE); return FIELD_VALUE; }
  {ONLY_SPACE}                          {return WHITE_SPACE; }
}

// 未找到办法处理 body 开头和结尾的空白
<IN_BODY> {
  [^\r\n><\-#]+                      { matchTimes++; }
  "<"                                { matchTimes++; }
  ">"                                { matchTimes++; }
  "-"                                { matchTimes++; }
  "#"                                { matchTimes++; }
  {WHITE_SPACE}                      { matchTimes++; }
  "< "                               { yybegin(IN_INPUT_FILE_PATH); return INPUT_SIGN; }
  ">> "                              { yypushback(yylength()); yybegin(IN_OUTPUT_FILE_PATH); return detectBodyType(this); }
  "> {%"{EOL_MULTI}                  { yypushback(yylength()); yybegin(IN_POST_SCRIPT); return detectBodyType(this); }
  {MESSAGE_BOUNDARY}{EOL_MULTI}      { yypushback(yylength()); yybegin(IN_MULTIPART); return detectBodyType(this); }
  {REQUEST_COMMENT}{EOL}             { yypushback(yylength()); yybegin(YYINITIAL); return detectBodyType(this); }
  <<EOF>>                            { yypushback(yylength()); yybegin(YYINITIAL); return detectBodyType(this); }
}

<IN_INPUT_FILE_PATH> {
  {FILE_PATH_PART}           { return INPUT_FILE_PATH_PART; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  {EOL_MULTI}                { yybegin(IN_BODY); return WHITE_SPACE; }
}

<IN_POST_SCRIPT> {
  "> {%"                    { return OUT_START_SCRIPT_BRACE; }
  "%}"{EOL_MULTI}           { yypushback(yylength()); yybegin(IN_POST_SCRIPT_END); return SCRIPT_BODY_PAET; }
  <<EOF>>                   { yybegin(YYINITIAL); return SCRIPT_BODY_PAET; }
  [^%]+                     {  }
  "%"                       {  }
  {WHITE_SPACE}             {  }
}

<IN_POST_SCRIPT_END> {
  "%}"{EOL_MULTI}              { yybegin(IN_OUTPUT_FILE); return END_SCRIPT_BRACE; }
}

<IN_MULTIPART> {
  {MESSAGE_BOUNDARY}{EOL_MULTI}     { yybegin(detectBoundaryState(yytext())); return MESSAGE_BOUNDARY; }
}

<IN_OUTPUT_FILE> {
  ">> "                      { yybegin(IN_OUTPUT_FILE_PATH); return OUTPUT_FILE_SIGN; }
  {WHITE_SPACE}              { yybegin(YYINITIAL); return WHITE_SPACE; }
  [^]                        { yypushback(yylength()); yybegin(YYINITIAL); }
}

<IN_OUTPUT_FILE_PATH> {
  ">> "                      { return OUTPUT_FILE_SIGN; }
  {FILE_PATH_PART}           { return OUTPUT_FILE_PATH_PART; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  {EOL_MULTI}                { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^]                    { return BAD_CHARACTER; }
