// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.httpclient.psi.impl.*;

public interface HttpTypes {

  IElementType COMMENT = new HttpElementType("COMMENT");
  IElementType DYNAMIC_VARIABLE = new HttpElementType("DYNAMIC_VARIABLE");
  IElementType FILE_PATH = new HttpElementType("FILE_PATH");
  IElementType FRAGMENT = new HttpElementType("FRAGMENT");
  IElementType GLOBAL_HANDLER = new HttpElementType("GLOBAL_HANDLER");
  IElementType GLOBAL_SCRIPT = new HttpElementType("GLOBAL_SCRIPT");
  IElementType HEADER_FIELD = new HttpElementType("HEADER_FIELD");
  IElementType HEADER_FIELD_NAME = new HttpElementType("HEADER_FIELD_NAME");
  IElementType HEADER_FIELD_VALUE = new HttpElementType("HEADER_FIELD_VALUE");
  IElementType HOST = new HttpElementType("HOST");
  IElementType INPUT_FILE = new HttpElementType("INPUT_FILE");
  IElementType MESSAGE_BODY = new HttpElementType("MESSAGE_BODY");
  IElementType METHOD = new HttpElementType("METHOD");
  IElementType MULTIPART_FIELD = new HttpElementType("MULTIPART_FIELD");
  IElementType MULTIPART_MESSAGE = new HttpElementType("MULTIPART_MESSAGE");
  IElementType OUTPUT_FILE = new HttpElementType("OUTPUT_FILE");
  IElementType OUTPUT_FILE_PATH = new HttpElementType("OUTPUT_FILE_PATH");
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
  IElementType VERSION = new HttpElementType("VERSION");

  IElementType AND = new HttpTokenType("&");
  IElementType COLON = new HttpTokenType(":");
  IElementType DIFFERENCE_FILE = new HttpTokenType("DIFFERENCE_FILE");
  IElementType DOLLAR = new HttpTokenType("$");
  IElementType DUBBO = new HttpTokenType("DUBBO");
  IElementType DYNAMIC_SIGN = new HttpTokenType("DYNAMIC_SIGN");
  IElementType END_SCRIPT_BRACE = new HttpTokenType("%}");
  IElementType END_VARIABLE_BRACE = new HttpTokenType("}}");
  IElementType EQUALS = new HttpTokenType("=");
  IElementType FIELD_NAME = new HttpTokenType("FIELD_NAME");
  IElementType FIELD_VALUE = new HttpTokenType("FIELD_VALUE");
  IElementType FRAGMENT_PART = new HttpTokenType("FRAGMENT_PART");
  IElementType GET = new HttpTokenType("GET");
  IElementType GLOBAL_START_SCRIPT_BRACE = new HttpTokenType("<! {%");
  IElementType HASH = new HttpTokenType("#");
  IElementType HOST_VALUE = new HttpTokenType("HOST_VALUE");
  IElementType HTTP = new HttpTokenType("http");
  IElementType HTTPS = new HttpTokenType("https");
  IElementType HTTP_VERSION = new HttpTokenType("HTTP_VERSION");
  IElementType IDENTIFIER = new HttpTokenType("IDENTIFIER");
  IElementType INPUT_FILE_PATH_PART = new HttpTokenType("INPUT_FILE_PATH_PART");
  IElementType INPUT_SIGN = new HttpTokenType("< ");
  IElementType IN_START_SCRIPT_BRACE = new HttpTokenType("< {%");
  IElementType LINE_COMMENT = new HttpTokenType("LINE_COMMENT");
  IElementType MESSAGE_BOUNDARY = new HttpTokenType("MESSAGE_BOUNDARY");
  IElementType MESSAGE_SEPARATOR = new HttpTokenType("MESSAGE_SEPARATOR");
  IElementType MESSAGE_TEXT = new HttpTokenType("MESSAGE_TEXT");
  IElementType OUTPUT_FILE_PATH_PART = new HttpTokenType("OUTPUT_FILE_PATH_PART");
  IElementType OUTPUT_FILE_SIGN = new HttpTokenType(">> ");
  IElementType OUT_START_SCRIPT_BRACE = new HttpTokenType("> {%");
  IElementType PORT_SEGMENT = new HttpTokenType("PORT_SEGMENT");
  IElementType POST = new HttpTokenType("POST");
  IElementType QUERY_NAME = new HttpTokenType("QUERY_NAME");
  IElementType QUERY_VALUE = new HttpTokenType("QUERY_VALUE");
  IElementType QUESTION = new HttpTokenType("?");
  IElementType REQUEST_COMMENT = new HttpTokenType("REQUEST_COMMENT");
  IElementType REQUEST_METHOD = new HttpTokenType("REQUEST_METHOD");
  IElementType SCHEMA_PART = new HttpTokenType("SCHEMA_PART");
  IElementType SCHEMA_SEPARATE = new HttpTokenType("://");
  IElementType SCHEME_SEPARATOR = new HttpTokenType("SCHEME_SEPARATOR");
  IElementType SEGMENT = new HttpTokenType("SEGMENT");
  IElementType SLASH = new HttpTokenType("/");
  IElementType SRTART_VARIABLE_BRACE = new HttpTokenType("{{");
  IElementType WEBSOCKET = new HttpTokenType("WEBSOCKET");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COMMENT) {
        return new HttpCommentImpl(node);
      }
      else if (type == DYNAMIC_VARIABLE) {
        return new HttpDynamicVariableImpl(node);
      }
      else if (type == FILE_PATH) {
        return new HttpFilePathImpl(node);
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
      else if (type == HEADER_FIELD) {
        return new HttpHeaderFieldImpl(node);
      }
      else if (type == HEADER_FIELD_NAME) {
        return new HttpHeaderFieldNameImpl(node);
      }
      else if (type == HEADER_FIELD_VALUE) {
        return new HttpHeaderFieldValueImpl(node);
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
      else if (type == OUTPUT_FILE) {
        return new HttpOutputFileImpl(node);
      }
      else if (type == OUTPUT_FILE_PATH) {
        return new HttpOutputFilePathImpl(node);
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
      else if (type == VERSION) {
        return new HttpVersionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
