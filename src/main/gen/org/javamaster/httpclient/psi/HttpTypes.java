// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.httpclient.psi.impl.*;

public interface HttpTypes {

  IElementType FRAGMENT = new HttpElementType("FRAGMENT");
  IElementType HEADER_FIELD = new HttpElementType("HEADER_FIELD");
  IElementType HEADER_FIELD_NAME = new HttpElementType("HEADER_FIELD_NAME");
  IElementType HEADER_FIELD_VALUE = new HttpElementType("HEADER_FIELD_VALUE");
  IElementType HOST = new HttpElementType("HOST");
  IElementType METHOD = new HttpElementType("METHOD");
  IElementType PATH_ABSOLUTE = new HttpElementType("PATH_ABSOLUTE");
  IElementType PORT = new HttpElementType("PORT");
  IElementType QUERY = new HttpElementType("QUERY");
  IElementType QUERY_PARAMETER = new HttpElementType("QUERY_PARAMETER");
  IElementType QUERY_PARAMETER_KEY = new HttpElementType("QUERY_PARAMETER_KEY");
  IElementType QUERY_PARAMETER_VALUE = new HttpElementType("QUERY_PARAMETER_VALUE");
  IElementType REQUEST = new HttpElementType("REQUEST");
  IElementType REQUEST_BLOCK = new HttpElementType("REQUEST_BLOCK");
  IElementType REQUEST_TARGET = new HttpElementType("REQUEST_TARGET");
  IElementType SCHEMA = new HttpElementType("SCHEMA");
  IElementType VERSION = new HttpElementType("VERSION");

  IElementType AND = new HttpTokenType("&");
  IElementType COLON = new HttpTokenType(":");
  IElementType EQUALS = new HttpTokenType("=");
  IElementType FIELD_NAME = new HttpTokenType("FIELD_NAME");
  IElementType FIELD_VALUE = new HttpTokenType("FIELD_VALUE");
  IElementType FRAGMENT_PART = new HttpTokenType("FRAGMENT_PART");
  IElementType HASH = new HttpTokenType("#");
  IElementType HOST_VALUE = new HttpTokenType("HOST_VALUE");
  IElementType HTTP = new HttpTokenType("http");
  IElementType HTTPS = new HttpTokenType("https");
  IElementType HTTP_VERSION = new HttpTokenType("HTTP_VERSION");
  IElementType LINE_COMMENT = new HttpTokenType("LINE_COMMENT");
  IElementType PORT_SEGMENT = new HttpTokenType("PORT_SEGMENT");
  IElementType QUERY_NAME = new HttpTokenType("QUERY_NAME");
  IElementType QUERY_VALUE = new HttpTokenType("QUERY_VALUE");
  IElementType QUESTION = new HttpTokenType("?");
  IElementType REQUEST_COMMENT = new HttpTokenType("REQUEST_COMMENT");
  IElementType REQUEST_METHOD = new HttpTokenType("REQUEST_METHOD");
  IElementType SCHEMA_PART = new HttpTokenType("SCHEMA_PART");
  IElementType SCHEMA_SEPARATE = new HttpTokenType("://");
  IElementType SEGMENT = new HttpTokenType("SEGMENT");
  IElementType SLASH = new HttpTokenType("/");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == FRAGMENT) {
        return new HttpFragmentImpl(node);
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
      else if (type == METHOD) {
        return new HttpMethodImpl(node);
      }
      else if (type == PATH_ABSOLUTE) {
        return new HttpPathAbsoluteImpl(node);
      }
      else if (type == PORT) {
        return new HttpPortImpl(node);
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
      else if (type == REQUEST_TARGET) {
        return new HttpRequestTargetImpl(node);
      }
      else if (type == SCHEMA) {
        return new HttpSchemaImpl(node);
      }
      else if (type == VERSION) {
        return new HttpVersionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
