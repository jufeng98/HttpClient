// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.httpclient.psi.impl.*;

public interface HttpTypes {

  IElementType BODY = new HttpElementType("BODY");
  IElementType COMMENT = new HttpElementType("COMMENT");
  IElementType DIRECTION_COMMENT = new HttpElementType("DIRECTION_COMMENT");
  IElementType DIRECTION_NAME = new HttpElementType("DIRECTION_NAME");
  IElementType DIRECTION_VALUE = new HttpElementType("DIRECTION_VALUE");
  IElementType DIRECTION_VALUE_CONTENT = new HttpElementType("DIRECTION_VALUE_CONTENT");
  IElementType FILE_PATH = new HttpElementType("FILE_PATH");
  IElementType FILE_PATH_CONTENT = new HttpElementType("FILE_PATH_CONTENT");
  IElementType FRAGMENT = new HttpElementType("FRAGMENT");
  IElementType GLOBAL_HANDLER = new HttpElementType("GLOBAL_HANDLER");
  IElementType GLOBAL_SCRIPT = new HttpElementType("GLOBAL_SCRIPT");
  IElementType GLOBAL_VARIABLE = new HttpElementType("GLOBAL_VARIABLE");
  IElementType GLOBAL_VARIABLE_NAME = new HttpElementType("GLOBAL_VARIABLE_NAME");
  IElementType GLOBAL_VARIABLE_VALUE = new HttpElementType("GLOBAL_VARIABLE_VALUE");
  IElementType HEADER = new HttpElementType("HEADER");
  IElementType HEADER_FIELD = new HttpElementType("HEADER_FIELD");
  IElementType HEADER_FIELD_NAME = new HttpElementType("HEADER_FIELD_NAME");
  IElementType HEADER_FIELD_VALUE = new HttpElementType("HEADER_FIELD_VALUE");
  IElementType HISTORY_BODY_FILE = new HttpElementType("HISTORY_BODY_FILE");
  IElementType HOST = new HttpElementType("HOST");
  IElementType INPUT_FILE = new HttpElementType("INPUT_FILE");
  IElementType MESSAGE_BODY = new HttpElementType("MESSAGE_BODY");
  IElementType METHOD = new HttpElementType("METHOD");
  IElementType MULTIPART_FIELD = new HttpElementType("MULTIPART_FIELD");
  IElementType MULTIPART_MESSAGE = new HttpElementType("MULTIPART_MESSAGE");
  IElementType MY_JSON_VALUE = new HttpElementType("MY_JSON_VALUE");
  IElementType OUTPUT_FILE = new HttpElementType("OUTPUT_FILE");
  IElementType PATH_ABSOLUTE = new HttpElementType("PATH_ABSOLUTE");
  IElementType PORT = new HttpElementType("PORT");
  IElementType PRE_REQUEST_HANDLER = new HttpElementType("PRE_REQUEST_HANDLER");
  IElementType PRE_REQUEST_SCRIPT = new HttpElementType("PRE_REQUEST_SCRIPT");
  IElementType QUERY = new HttpElementType("QUERY");
  IElementType QUERY_PARAMETER = new HttpElementType("QUERY_PARAMETER");
  IElementType QUERY_PARAMETER_KEY = new HttpElementType("QUERY_PARAMETER_KEY");
  IElementType QUERY_PARAMETER_VALUE = new HttpElementType("QUERY_PARAMETER_VALUE");
  IElementType REQUEST = new HttpElementType("REQUEST");
  IElementType REQUEST_BLOCK = new HttpElementType("REQUEST_BLOCK");
  IElementType REQUEST_MESSAGES_GROUP = new HttpElementType("REQUEST_MESSAGES_GROUP");
  IElementType REQUEST_TARGET = new HttpElementType("REQUEST_TARGET");
  IElementType RESPONSE_HANDLER = new HttpElementType("RESPONSE_HANDLER");
  IElementType RESPONSE_SCRIPT = new HttpElementType("RESPONSE_SCRIPT");
  IElementType SCHEMA = new HttpElementType("SCHEMA");
  IElementType SCRIPT_BODY = new HttpElementType("SCRIPT_BODY");
  IElementType VARIABLE = new HttpElementType("VARIABLE");
  IElementType VARIABLE_ARG = new HttpElementType("VARIABLE_ARG");
  IElementType VARIABLE_ARGS = new HttpElementType("VARIABLE_ARGS");
  IElementType VARIABLE_BUILTIN = new HttpElementType("VARIABLE_BUILTIN");
  IElementType VARIABLE_NAME = new HttpElementType("VARIABLE_NAME");
  IElementType VARIABLE_REFERENCE = new HttpElementType("VARIABLE_REFERENCE");
  IElementType VERSION = new HttpElementType("VERSION");

  IElementType AND = new HttpTokenType("&");
  IElementType AT = new HttpTokenType("@");
  IElementType BLOCK_COMMENT = new HttpTokenType("BLOCK_COMMENT");
  IElementType COLON = new HttpTokenType(":");
  IElementType COMMA = new HttpTokenType(",");
  IElementType DIRECTION_COMMENT_START = new HttpTokenType("DIRECTION_COMMENT_START");
  IElementType DIRECTION_NAME_PART = new HttpTokenType("DIRECTION_NAME_PART");
  IElementType DIRECTION_VALUE_PART = new HttpTokenType("DIRECTION_VALUE_PART");
  IElementType DOLLAR = new HttpTokenType("$");
  IElementType END_SCRIPT_BRACE = new HttpTokenType("%}");
  IElementType END_VARIABLE_BRACE = new HttpTokenType("}}");
  IElementType EQUALS = new HttpTokenType("=");
  IElementType FIELD_NAME = new HttpTokenType("FIELD_NAME");
  IElementType FIELD_VALUE = new HttpTokenType("FIELD_VALUE");
  IElementType FILE_PATH_PART = new HttpTokenType("FILE_PATH_PART");
  IElementType FRAGMENT_PART = new HttpTokenType("FRAGMENT_PART");
  IElementType GLOBAL_NAME = new HttpTokenType("GLOBAL_NAME");
  IElementType GLOBAL_START_SCRIPT_BRACE = new HttpTokenType("<! {%");
  IElementType GLOBAL_VALUE = new HttpTokenType("GLOBAL_VALUE");
  IElementType HASH = new HttpTokenType("#");
  IElementType HISTORY_FILE_SIGN = new HttpTokenType("<> ");
  IElementType HOST_VALUE = new HttpTokenType("HOST_VALUE");
  IElementType HTTP_VERSION = new HttpTokenType("HTTP_VERSION");
  IElementType IDENTIFIER = new HttpTokenType("IDENTIFIER");
  IElementType INPUT_FILE_SIGN = new HttpTokenType("< ");
  IElementType INTEGER = new HttpTokenType("INTEGER");
  IElementType IN_START_SCRIPT_BRACE = new HttpTokenType("< {%");
  IElementType LEFT_BRACKET = new HttpTokenType("(");
  IElementType LINE_COMMENT = new HttpTokenType("LINE_COMMENT");
  IElementType MESSAGE_BOUNDARY = new HttpTokenType("MESSAGE_BOUNDARY");
  IElementType MESSAGE_TEXT = new HttpTokenType("MESSAGE_TEXT");
  IElementType OUTPUT_FILE_SIGN = new HttpTokenType(">> ");
  IElementType OUT_START_SCRIPT_BRACE = new HttpTokenType("> {%");
  IElementType PORT_SEGMENT = new HttpTokenType("PORT_SEGMENT");
  IElementType QUERY_NAME = new HttpTokenType("QUERY_NAME");
  IElementType QUERY_VALUE = new HttpTokenType("QUERY_VALUE");
  IElementType QUESTION = new HttpTokenType("?");
  IElementType REQUEST_COMMENT = new HttpTokenType("REQUEST_COMMENT");
  IElementType REQUEST_METHOD = new HttpTokenType("REQUEST_METHOD");
  IElementType RIGHT_BRACKET = new HttpTokenType(")");
  IElementType SCHEMA_PART = new HttpTokenType("SCHEMA_PART");
  IElementType SCHEMA_SEPARATE = new HttpTokenType("://");
  IElementType SCRIPT_BODY_PAET = new HttpTokenType("SCRIPT_BODY_PAET");
  IElementType SEGMENT = new HttpTokenType("SEGMENT");
  IElementType SLASH = new HttpTokenType("/");
  IElementType START_VARIABLE_BRACE = new HttpTokenType("{{");
  IElementType STRING = new HttpTokenType("STRING");
  IElementType STRING_LITERAL_PART = new HttpTokenType("STRING_LITERAL_PART");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BODY) {
        return new HttpBodyImpl(node);
      }
      else if (type == COMMENT) {
        return new HttpCommentImpl(node);
      }
      else if (type == DIRECTION_COMMENT) {
        return new HttpDirectionCommentImpl(node);
      }
      else if (type == DIRECTION_NAME) {
        return new HttpDirectionNameImpl(node);
      }
      else if (type == DIRECTION_VALUE) {
        return new HttpDirectionValueImpl(node);
      }
      else if (type == DIRECTION_VALUE_CONTENT) {
        return new HttpDirectionValueContentImpl(node);
      }
      else if (type == FILE_PATH) {
        return new HttpFilePathImpl(node);
      }
      else if (type == FILE_PATH_CONTENT) {
        return new HttpFilePathContentImpl(node);
      }
      else if (type == FRAGMENT) {
        return new HttpFragmentImpl(node);
      }
      else if (type == GLOBAL_HANDLER) {
        return new HttpGlobalHandlerImpl(node);
      }
      else if (type == GLOBAL_SCRIPT) {
        return new HttpGlobalScriptImpl(node);
      }
      else if (type == GLOBAL_VARIABLE) {
        return new HttpGlobalVariableImpl(node);
      }
      else if (type == GLOBAL_VARIABLE_NAME) {
        return new HttpGlobalVariableNameImpl(node);
      }
      else if (type == GLOBAL_VARIABLE_VALUE) {
        return new HttpGlobalVariableValueImpl(node);
      }
      else if (type == HEADER) {
        return new HttpHeaderImpl(node);
      }
      else if (type == HEADER_FIELD) {
        return new HttpHeaderFieldImpl(node);
      }
      else if (type == HEADER_FIELD_NAME) {
        return new HttpHeaderFieldNameImpl(node);
      }
      else if (type == HEADER_FIELD_VALUE) {
        return new HttpHeaderFieldValueImpl(node);
      }
      else if (type == HISTORY_BODY_FILE) {
        return new HttpHistoryBodyFileImpl(node);
      }
      else if (type == HOST) {
        return new HttpHostImpl(node);
      }
      else if (type == INPUT_FILE) {
        return new HttpInputFileImpl(node);
      }
      else if (type == MESSAGE_BODY) {
        return new HttpMessageBodyImpl(node);
      }
      else if (type == METHOD) {
        return new HttpMethodImpl(node);
      }
      else if (type == MULTIPART_FIELD) {
        return new HttpMultipartFieldImpl(node);
      }
      else if (type == MULTIPART_MESSAGE) {
        return new HttpMultipartMessageImpl(node);
      }
      else if (type == MY_JSON_VALUE) {
        return new HttpMyJsonValueImpl(node);
      }
      else if (type == OUTPUT_FILE) {
        return new HttpOutputFileImpl(node);
      }
      else if (type == PATH_ABSOLUTE) {
        return new HttpPathAbsoluteImpl(node);
      }
      else if (type == PORT) {
        return new HttpPortImpl(node);
      }
      else if (type == PRE_REQUEST_HANDLER) {
        return new HttpPreRequestHandlerImpl(node);
      }
      else if (type == PRE_REQUEST_SCRIPT) {
        return new HttpPreRequestScriptImpl(node);
      }
      else if (type == QUERY) {
        return new HttpQueryImpl(node);
      }
      else if (type == QUERY_PARAMETER) {
        return new HttpQueryParameterImpl(node);
      }
      else if (type == QUERY_PARAMETER_KEY) {
        return new HttpQueryParameterKeyImpl(node);
      }
      else if (type == QUERY_PARAMETER_VALUE) {
        return new HttpQueryParameterValueImpl(node);
      }
      else if (type == REQUEST) {
        return new HttpRequestImpl(node);
      }
      else if (type == REQUEST_BLOCK) {
        return new HttpRequestBlockImpl(node);
      }
      else if (type == REQUEST_MESSAGES_GROUP) {
        return new HttpRequestMessagesGroupImpl(node);
      }
      else if (type == REQUEST_TARGET) {
        return new HttpRequestTargetImpl(node);
      }
      else if (type == RESPONSE_HANDLER) {
        return new HttpResponseHandlerImpl(node);
      }
      else if (type == RESPONSE_SCRIPT) {
        return new HttpResponseScriptImpl(node);
      }
      else if (type == SCHEMA) {
        return new HttpSchemaImpl(node);
      }
      else if (type == SCRIPT_BODY) {
        return new HttpScriptBodyImpl(node);
      }
      else if (type == VARIABLE) {
        return new HttpVariableImpl(node);
      }
      else if (type == VARIABLE_ARG) {
        return new HttpVariableArgImpl(node);
      }
      else if (type == VARIABLE_ARGS) {
        return new HttpVariableArgsImpl(node);
      }
      else if (type == VARIABLE_BUILTIN) {
        return new HttpVariableBuiltinImpl(node);
      }
      else if (type == VARIABLE_NAME) {
        return new HttpVariableNameImpl(node);
      }
      else if (type == VARIABLE_REFERENCE) {
        return new HttpVariableReferenceImpl(node);
      }
      else if (type == VERSION) {
        return new HttpVersionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
