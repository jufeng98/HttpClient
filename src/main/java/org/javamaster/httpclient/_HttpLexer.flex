package org.javamaster.httpclient;

import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.utils.LexerUtils;
import static org.javamaster.httpclient.utils.LexerUtils.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.javamaster.httpclient.psi.HttpTypes.*;


%%

%{
        public boolean nameFlag;
        private int nextState;

        public int matchTimes;
        public CharSequence lastMatch;

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
//%debug
%state IN_GLOBAL_SCRIPT, IN_GLOBAL_SCRIPT_END, IN_PRE_SCRIPT, IN_PRE_SCRIPT_END, IN_DIRECTION_NAME, IN_DIRECTION_VALUE
%state IN_FIRST_LINE, IN_HOST, IN_PORT, IN_QUERY, IN_FRAGMENT, IN_BODY, IN_TRIM_PREFIX_SPACE, IN_TRIM_PREFIX_ONLY_SPACE
%xstate IN_PATH, IN_JSON_VALUE
%state IN_HEADER, IN_HEADER_FIELD_NAME, IN_HEADER_FIELD_VALUE, IN_HEADER_FIELD_VALUE_NO_SPACE
%state IN_POST_SCRIPT, IN_POST_SCRIPT_END
%state IN_INPUT_FILE_PATH, IN_OUTPUT_FILE, IN_OUTPUT_FILE_PATH, IN_VERSION, IN_HISTORY_BODY_FILE_PART
%state IN_MULTIPART, IN_VARIABLE, IN_DYNAMIC_VARIABLE, IN_DYNAMIC_VARIABLE_ARGS, IN_GLOBAL_VARIABLE, IN_GLOBAL_VARIABLE_VALUE
%eof{
  return;
%eof}

EOL=\R
EOL_MULTI=[ ]*\R+
ONLY_SPACE=[ ]+
WHITE_SPACE=\s+
LINE_COMMENT="//"[^\r\n]*
BLOCK_COMMENT="/*" !([^]* "*/" [^]*) ("*/")?
REQUEST_COMMENT=###.*
REQUEST_METHOD=[A-Z_]+
SCHEMA_PART=https|wss|http|ws|dubbo
HOST_VALUE=[a-zA-Z0-9\-.]+
PORT_SEGMENT=[0-9]+
SEGMENT=[a-zA-Z_0-9]+
QUERY_PART=[^#&=/{\s]+
FRAGMENT_PART=[^\s]+
HTTP_VERSION=HTTP\/[0-9]+\.[0-9]+
FIELD_NAME=[a-zA-Z0-9\-]+
FIELD_VALUE=[^\r\n{ ]+
FILE_PATH_PART=[^\r\n<>{}#]+
MESSAGE_BOUNDARY=--[a-zA-Z0-9\-]+
VARIABLE_NAME=[[a-zA-Z0-9_\-.]--[$}= ]]+
GLOBAL_VARIABLE_NAME=[^\r\n={} ]+
DIRECTION_NAME_PART=[^\r\n ]+
DIRECTION_VALUE_PART=[^\r\n{]+
INTEGER=[0-9]+
STRING=('([^'])*'|\"([^\"])*\")
%%

<YYINITIAL> {
  {REQUEST_COMMENT}{EOL}      { return REQUEST_COMMENT; }
  "# @"                       { yybegin(IN_DIRECTION_NAME); return DIRECTION_COMMENT_START; }
  "<! {%"{EOL_MULTI}          { yybegin(IN_GLOBAL_SCRIPT); return GLOBAL_START_SCRIPT_BRACE; }
  "< {%"{EOL_MULTI}           { yybegin(IN_PRE_SCRIPT); return IN_START_SCRIPT_BRACE; }
  "@"                         { yybegin(IN_GLOBAL_VARIABLE); return AT; }
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
  {GLOBAL_VARIABLE_NAME}        { return GLOBAL_NAME; }
  "="                           { nextState = IN_GLOBAL_VARIABLE_VALUE; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return EQUALS; }
  {ONLY_SPACE}                  { return WHITE_SPACE; }
  {EOL_MULTI}                   { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_GLOBAL_VARIABLE_VALUE> {
  [^\r\n{]+                     { return GLOBAL_VALUE; }
  "{{"                          { nextState = IN_GLOBAL_VARIABLE_VALUE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {EOL}                         { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_VARIABLE> {
  {VARIABLE_NAME}        { return IDENTIFIER; }
  "$"                    { yybegin(IN_DYNAMIC_VARIABLE); return DOLLAR; }
  {ONLY_SPACE}           { return WHITE_SPACE; }
  "}}"                   { yybegin(nextState); return END_VARIABLE_BRACE; }
}

<IN_DYNAMIC_VARIABLE> {
  {VARIABLE_NAME}          { return IDENTIFIER; }
  "("                      { yybegin(IN_DYNAMIC_VARIABLE_ARGS); return LEFT_BRACKET; }
  {ONLY_SPACE}             { return WHITE_SPACE; }
  "}}"                     { yybegin(nextState); return END_VARIABLE_BRACE; }
}

<IN_DYNAMIC_VARIABLE_ARGS> {
  {INTEGER}                    { return INTEGER; }
  {STRING}                     { return STRING; }
  ","                          { return COMMA; }
  ")"                          { yybegin(IN_DYNAMIC_VARIABLE); return RIGHT_BRACKET; }
  {ONLY_SPACE}                 { return WHITE_SPACE; }
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

<IN_DIRECTION_NAME> {
  {DIRECTION_NAME_PART}         { return DIRECTION_NAME_PART; }
  {ONLY_SPACE}                  { yybegin(IN_DIRECTION_VALUE); return WHITE_SPACE; }
  {EOL}                         { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_DIRECTION_VALUE> {
  "{{"                          { nextState = IN_DIRECTION_VALUE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {DIRECTION_VALUE_PART}        { return DIRECTION_VALUE_PART; }
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
  [^?#/{}\s]+          { return SEGMENT; }
  "{"                  { return BAD_CHARACTER; }
  "}"                  { return BAD_CHARACTER; }
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
  [^\r\n]              { yypushback(yylength()); yybegin(IN_HEADER_FIELD_NAME); }
  [ ]*{EOL}            { matchTimes = 0; lastMatch = ""; yybegin(IN_BODY); return WHITE_SPACE; }
  {REQUEST_COMMENT}    { yypushback(yylength()); yybegin(YYINITIAL); }
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
  <<EOF>>                    { yybegin(YYINITIAL); return FIELD_VALUE; }
}

<IN_HEADER_FIELD_VALUE_NO_SPACE> {
  {FIELD_VALUE}                         { }
  "{"                                   { }
  {ONLY_SPACE}                          { }
  "{{"                                  { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE); return FIELD_VALUE; }
  [ ]*{EOL}                             { yypushback(yylength()); yybegin(IN_HEADER_FIELD_VALUE); return FIELD_VALUE; }
  <<EOF>>                               { yybegin(YYINITIAL); return FIELD_VALUE; }
}

<IN_BODY> {
  [^\r\n><\-#]+                      { lastMatch = yytext(); matchTimes++; }
  "<"                                { lastMatch = yytext(); matchTimes++; }
  ">"                                { lastMatch = yytext(); matchTimes++; }
  "-"                                { lastMatch = yytext(); matchTimes++; }
  "#"                                { lastMatch = yytext(); matchTimes++; }
  {WHITE_SPACE}                      { lastMatch = yytext(); matchTimes++; }
  "< "                               { nextState = IN_INPUT_FILE_PATH; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return INPUT_FILE_SIGN; }
  {EOL_MULTI}">> "                   { nextState = IN_OUTPUT_FILE_PATH; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  ">> "                              { nextState = IN_OUTPUT_FILE_PATH; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  {EOL_MULTI}"<> "                   { nextState = IN_HISTORY_BODY_FILE_PART; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  "<> "                              { nextState = IN_HISTORY_BODY_FILE_PART; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  {EOL_MULTI}"> {%"{EOL_MULTI}       { nextState = IN_POST_SCRIPT; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  "> {%"{EOL_MULTI}                  { if(LexerUtils.endsWithLineBreak(this)) { nextState = IN_POST_SCRIPT; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); } }
  {EOL_MULTI}{MESSAGE_BOUNDARY}\s*   { nextState = IN_MULTIPART; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  {MESSAGE_BOUNDARY}\s*              { if(LexerUtils.endsWithLineBreak(this)) { nextState = IN_MULTIPART; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); } }
  {EOL_MULTI}{REQUEST_COMMENT}{EOL}  { nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  {REQUEST_COMMENT}{EOL}             { if(LexerUtils.endsWithLineBreak(this)) { nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); } }
  {EOL_MULTI}{LINE_COMMENT}{EOL}     { nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); }
  {LINE_COMMENT}{EOL}                { if(LexerUtils.endsWithLineBreak(this)) { nextState = YYINITIAL; yypushback(yylength()); yybegin(IN_TRIM_PREFIX_SPACE); return detectBodyType(this); } }
  <<EOF>>                            { yybegin(YYINITIAL); return detectBodyType(this); }
}

<IN_TRIM_PREFIX_SPACE> {
  \s+                         { return WHITE_SPACE; }
  [^]                         { yypushback(yylength()); yybegin(nextState); }
}

<IN_TRIM_PREFIX_ONLY_SPACE> {
  [ ]+                        { return WHITE_SPACE; }
  [^]                         { yypushback(yylength()); yybegin(nextState); }
}

<IN_INPUT_FILE_PATH> {
  {FILE_PATH_PART}           { return FILE_PATH_PART; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  {EOL_MULTI}                { matchTimes = 0; lastMatch = ""; yybegin(IN_BODY); return WHITE_SPACE; }
}

<IN_POST_SCRIPT> {
  "> {%"{EOL_MULTI}                { return OUT_START_SCRIPT_BRACE; }
  "%}"\s*                          { yypushback(yylength()); yybegin(IN_POST_SCRIPT_END); return SCRIPT_BODY_PAET; }
  <<EOF>>                          { yybegin(YYINITIAL); return SCRIPT_BODY_PAET; }
  [^%]+                            {  }
  "%"                              {  }
  {WHITE_SPACE}                    {  }
}

<IN_POST_SCRIPT_END> {
  "%}"\s*                   { yybegin(IN_OUTPUT_FILE); return END_SCRIPT_BRACE; }
}

<IN_MULTIPART> {
  {MESSAGE_BOUNDARY}\s*     { yybegin(detectBoundaryState(yytext(), this)); return MESSAGE_BOUNDARY; }
}

<IN_OUTPUT_FILE> {
  ">> "                      { nextState = IN_OUTPUT_FILE_PATH; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return OUTPUT_FILE_SIGN; }
  "<> "                      { nextState = IN_HISTORY_BODY_FILE_PART; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return HISTORY_FILE_SIGN; }
  {WHITE_SPACE}              { yybegin(YYINITIAL); return WHITE_SPACE; }
  [^]                        { yypushback(yylength()); yybegin(YYINITIAL); }
}

<IN_OUTPUT_FILE_PATH> {
  ">> "                      { nextState = IN_OUTPUT_FILE_PATH; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return OUTPUT_FILE_SIGN; }
  "{{"                       { nextState = IN_OUTPUT_FILE_PATH; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {FILE_PATH_PART}           { return FILE_PATH_PART; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  {EOL_MULTI}                { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_HISTORY_BODY_FILE_PART> {
  "<> "                      { nextState = IN_HISTORY_BODY_FILE_PART; yybegin(IN_TRIM_PREFIX_ONLY_SPACE); return HISTORY_FILE_SIGN; }
  "{{"                       { nextState = IN_HISTORY_BODY_FILE_PART; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
  {FILE_PATH_PART}           { return FILE_PATH_PART; }
  {ONLY_SPACE}               { return WHITE_SPACE; }
  {EOL}                      { return WHITE_SPACE; }
  [^]                        { yypushback(yylength()); yybegin(YYINITIAL); }
  <<EOF>>                    { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<IN_JSON_VALUE> {
  [^{}]+                     { return STRING_LITERAL_PART; }
  "{"                        { return STRING_LITERAL_PART; }
  "}"                        { return STRING_LITERAL_PART; }
  "{{"                       { nextState = IN_JSON_VALUE; yybegin(IN_VARIABLE); return START_VARIABLE_BRACE; }
}

  {LINE_COMMENT}{EOL}         { yypushback(1); return LINE_COMMENT; }
  {BLOCK_COMMENT}{EOL}        { yypushback(1); return BLOCK_COMMENT; }

  [^]                         { return BAD_CHARACTER; }
