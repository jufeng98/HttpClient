// This is a generated file. Not intended for manual editing.
package org.javamaster.httpclient.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.httpclient.psi.impl.*;

public interface HttpTypes {

  IElementType BODY = new HttpElementType("BODY");
  IElementType FILE = new HttpElementType("FILE");
  IElementType FILE_PATH = new HttpElementType("FILE_PATH");
  IElementType GLOBAL_SCRIPT = new HttpElementType("GLOBAL_SCRIPT");
  IElementType GLOBAL_VARIABLE_DEFINITION = new HttpElementType("GLOBAL_VARIABLE_DEFINITION");
  IElementType HEADER = new HttpElementType("HEADER");
  IElementType HEADERS = new HttpElementType("HEADERS");
  IElementType METHOD = new HttpElementType("METHOD");
  IElementType MULTIPART_BODY = new HttpElementType("MULTIPART_BODY");
  IElementType MULTIPART_CONTENT = new HttpElementType("MULTIPART_CONTENT");
  IElementType ORDINARY_CONTENT = new HttpElementType("ORDINARY_CONTENT");
  IElementType OUTPUT_FILE = new HttpElementType("OUTPUT_FILE");
  IElementType REQUEST = new HttpElementType("REQUEST");
  IElementType SCRIPT = new HttpElementType("SCRIPT");
  IElementType URL = new HttpElementType("URL");
  IElementType VERSION = new HttpElementType("VERSION");

  IElementType DELETE = new HttpTokenType("DELETE");
  IElementType GET = new HttpTokenType("GET");
  IElementType HEADER_DESC = new HttpTokenType("HEADER_DESC");
  IElementType JSON_TEXT = new HttpTokenType("JSON_TEXT");
  IElementType JS_SCRIPT = new HttpTokenType("JS_SCRIPT");
  IElementType LINE_COMMENT = new HttpTokenType("LINE_COMMENT");
  IElementType MULTIPART_SEPERATE = new HttpTokenType("MULTIPART_SEPERATE");
  IElementType PATH = new HttpTokenType("PATH");
  IElementType POST = new HttpTokenType("POST");
  IElementType PUT = new HttpTokenType("PUT");
  IElementType REQUEST_COMMENT = new HttpTokenType("REQUEST_COMMENT");
  IElementType T_LT = new HttpTokenType("<");
  IElementType T_RT = new HttpTokenType(">");
  IElementType T_RT_DBL = new HttpTokenType(">>");
  IElementType URL_DESC = new HttpTokenType("URL_DESC");
  IElementType URL_FORM_ENCODE = new HttpTokenType("URL_FORM_ENCODE");
  IElementType VARIABLE_DEFINE = new HttpTokenType("VARIABLE_DEFINE");
  IElementType XML_TEXT = new HttpTokenType("XML_TEXT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BODY) {
        return new HttpBodyImpl(node);
      }
      else if (type == FILE) {
        return new HttpFileImpl(node);
      }
      else if (type == FILE_PATH) {
        return new HttpFilePathImpl(node);
      }
      else if (type == GLOBAL_SCRIPT) {
        return new HttpGlobalScriptImpl(node);
      }
      else if (type == GLOBAL_VARIABLE_DEFINITION) {
        return new HttpGlobalVariableDefinitionImpl(node);
      }
      else if (type == HEADER) {
        return new HttpHeaderImpl(node);
      }
      else if (type == HEADERS) {
        return new HttpHeadersImpl(node);
      }
      else if (type == METHOD) {
        return new HttpMethodImpl(node);
      }
      else if (type == MULTIPART_BODY) {
        return new HttpMultipartBodyImpl(node);
      }
      else if (type == MULTIPART_CONTENT) {
        return new HttpMultipartContentImpl(node);
      }
      else if (type == ORDINARY_CONTENT) {
        return new HttpOrdinaryContentImpl(node);
      }
      else if (type == OUTPUT_FILE) {
        return new HttpOutputFileImpl(node);
      }
      else if (type == REQUEST) {
        return new HttpRequestImpl(node);
      }
      else if (type == SCRIPT) {
        return new HttpScriptImpl(node);
      }
      else if (type == URL) {
        return new HttpUrlImpl(node);
      }
      else if (type == VERSION) {
        return new HttpVersionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
