package org.javamaster.httpclient.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.parser.HttpAdapter;
import org.javamaster.httpclient.psi.HttpTypeSets;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();
    public static final TextAttributesKey HTTP_REQUEST_PARAMETER_NAME = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_PARAMETER_NAME");
    public static final TextAttributesKey HTTP_REQUEST_PARAMETER_VALUE = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_PARAMETER_VALUE");
    public static final TextAttributesKey HTTP_REQUEST_PORT = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_PORT");
    public static final TextAttributesKey HTTP_HEADER_FIELD_NAME;
    public static final TextAttributesKey HTTP_HEADER_FIELD_VALUE;
    public static final TextAttributesKey HTTP_REQUEST_COMMENT;
    public static final TextAttributesKey HTTP_REQUEST_METHOD_TYPE;
    public static final TextAttributesKey HTTP_REQUEST_SEPARATOR;
    public static final TextAttributesKey HTTP_REQUEST_BODY;
    public static final TextAttributesKey HTTP_REQUEST_INPUT_SIGN;
    public static final TextAttributesKey HTTP_REQUEST_INPUT_FILE;
    public static final TextAttributesKey HTTP_REQUEST_DIFFERENCE_SIGN;
    public static final TextAttributesKey HTTP_REQUEST_DIFFERENCE_FILE;
    public static final TextAttributesKey HTTP_REQUEST_MULTIPART_BOUNDARY;
    public static final TextAttributesKey HTTP_REQUEST_ENVIRONMENT_VARIABLE;
    public static final TextAttributesKey HTTP_REQUEST_VARIABLE_BRACES;
    public static final TextAttributesKey HTTP_REQUEST_DOC_COMMENT_TAG;
    public static final TextAttributesKey HTTP_REQUEST_NAME;

    public @NotNull Lexer getHighlightingLexer() {
        return new HttpAdapter();
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }

    static {
        HTTP_HEADER_FIELD_NAME = TextAttributesKey.createTextAttributesKey("HTTP_HEADER_FIELD_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD);
        HTTP_HEADER_FIELD_VALUE = TextAttributesKey.createTextAttributesKey("HTTP_HEADER_FIELD_VALUE", DefaultLanguageHighlighterColors.IDENTIFIER);
        HTTP_REQUEST_COMMENT = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
        HTTP_REQUEST_METHOD_TYPE = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
        HTTP_REQUEST_SEPARATOR = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_SEPARATOR", DefaultLanguageHighlighterColors.LINE_COMMENT);
        HTTP_REQUEST_BODY = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_MESSAGE_BODY", EditorColors.INJECTED_LANGUAGE_FRAGMENT);
        HTTP_REQUEST_INPUT_SIGN = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_INPUT_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
        HTTP_REQUEST_INPUT_FILE = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_INPUT_FILE");
        HTTP_REQUEST_DIFFERENCE_SIGN = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_DIFFERENCE_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
        HTTP_REQUEST_DIFFERENCE_FILE = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_DIFFERENCE_FILE", DefaultLanguageHighlighterColors.STATIC_METHOD);
        HTTP_REQUEST_MULTIPART_BOUNDARY = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_MULTIPART_BOUNDARY", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
        HTTP_REQUEST_ENVIRONMENT_VARIABLE = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_ENVIRONMENT_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
        HTTP_REQUEST_VARIABLE_BRACES = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_VARIABLE_BRACES", DefaultLanguageHighlighterColors.BRACES);
        HTTP_REQUEST_DOC_COMMENT_TAG = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_DOC_COMMENT_TAG", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
        HTTP_REQUEST_NAME = TextAttributesKey.createTextAttributesKey("HTTP_REQUEST_NAME", DefaultLanguageHighlighterColors.METADATA);
        fillMap(ATTRIBUTES, HTTP_REQUEST_METHOD_TYPE, HttpTypes.REQUEST_METHOD);
        fillMap(ATTRIBUTES, HTTP_REQUEST_PORT, HttpTypes.PORT);
        fillMap(ATTRIBUTES, HTTP_REQUEST_PARAMETER_NAME, HttpTypes.QUERY_NAME);
        fillMap(ATTRIBUTES, HTTP_REQUEST_PARAMETER_VALUE, HttpTypes.QUERY_VALUE);
        fillMap(ATTRIBUTES, HTTP_REQUEST_SEPARATOR, HttpTypes.REQUEST_COMMENT);
        fillMap(ATTRIBUTES, HTTP_HEADER_FIELD_NAME, HttpTypes.FIELD_NAME);
        fillMap(ATTRIBUTES, HTTP_HEADER_FIELD_VALUE, HttpTypes.FIELD_VALUE);
        fillMap(ATTRIBUTES, HTTP_REQUEST_INPUT_SIGN, HttpTypes.INPUT_SIGN, HttpTypes.OUTPUT_FILE_SIGN);
        fillMap(ATTRIBUTES, HTTP_REQUEST_INPUT_FILE, HttpTypes.INPUT_FILE_PATH_PART, HttpTypes.OUTPUT_FILE_PATH_PART);
        fillMap(ATTRIBUTES, HTTP_REQUEST_MULTIPART_BOUNDARY, HttpTypes.MESSAGE_BOUNDARY);
        fillMap(ATTRIBUTES, HTTP_REQUEST_ENVIRONMENT_VARIABLE, HttpTypes.IDENTIFIER);
        fillMap(ATTRIBUTES, HttpTypeSets.VARIABLE_BRACES, HTTP_REQUEST_VARIABLE_BRACES);
    }
}
