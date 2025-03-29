package org.javamaster.httpclient.highlighting.support;

import com.google.common.collect.Maps;
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

import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class HttpSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTE_MAP = Maps.newHashMap();

    public static final TextAttributesKey HTTP_PARAMETER_NAME;
    public static final TextAttributesKey HTTP_PARAMETER_VALUE;
    public static final TextAttributesKey HTTP_PORT;
    public static final TextAttributesKey HTTP_HEADER_FIELD_NAME;
    public static final TextAttributesKey HTTP_REQUEST_COMMENT;
    public static final TextAttributesKey HTTP_KEYWORD;
    public static final TextAttributesKey HTTP_LINE_COMMENT;
    public static final TextAttributesKey HTTP_BODY;
    public static final TextAttributesKey HTTP_FILE_SIGN;
    public static final TextAttributesKey HTTP_FILE_PATH;
    public static final TextAttributesKey HTTP_MULTIPART_BOUNDARY;
    public static final TextAttributesKey HTTP_IDENTIFIER;
    public static final TextAttributesKey HTTP_VARIABLE_BRACES;
    public static final TextAttributesKey HTTP_REQUEST_NAME;

    static {
        HTTP_PARAMETER_NAME = createTextAttributesKey("HTTP_PARAMETER_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD);
        HTTP_PARAMETER_VALUE = createTextAttributesKey("HTTP_PARAMETER_VALUE", DefaultLanguageHighlighterColors.METADATA);
        HTTP_PORT = createTextAttributesKey("HTTP_PORT", DefaultLanguageHighlighterColors.NUMBER);
        HTTP_HEADER_FIELD_NAME = createTextAttributesKey("HTTP_HEADER_FIELD_NAME", DefaultLanguageHighlighterColors.STATIC_FIELD);
        HTTP_REQUEST_COMMENT = createTextAttributesKey("HTTP_REQUEST_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
        HTTP_KEYWORD = createTextAttributesKey("HTTP_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
        HTTP_LINE_COMMENT = createTextAttributesKey("HTTP_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
        HTTP_BODY = createTextAttributesKey("HTTP_BODY", EditorColors.INJECTED_LANGUAGE_FRAGMENT);
        HTTP_FILE_SIGN = createTextAttributesKey("HTTP_FILE_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
        HTTP_FILE_PATH = createTextAttributesKey("HTTP_INPUT_FILE", DefaultLanguageHighlighterColors.LABEL);
        HTTP_MULTIPART_BOUNDARY = createTextAttributesKey("HTTP_MULTIPART_BOUNDARY", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
        HTTP_IDENTIFIER = createTextAttributesKey("HTTP_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
        HTTP_VARIABLE_BRACES = createTextAttributesKey("HTTP_VARIABLE_BRACES", DefaultLanguageHighlighterColors.BRACES);
        HTTP_REQUEST_NAME = createTextAttributesKey("HTTP_REQUEST_NAME", DefaultLanguageHighlighterColors.METADATA);

        fillMap(ATTRIBUTE_MAP, HTTP_KEYWORD, HttpTypes.REQUEST_METHOD);
        fillMap(ATTRIBUTE_MAP, HTTP_PORT, HttpTypes.PORT);
        fillMap(ATTRIBUTE_MAP, HTTP_PARAMETER_NAME, HttpTypes.QUERY_NAME, HttpTypes.GLOBAL_NAME, HttpTypes.DIRECTION_NAME_PART);
        fillMap(ATTRIBUTE_MAP, HTTP_PARAMETER_VALUE, HttpTypes.QUERY_VALUE, HttpTypes.GLOBAL_VALUE, HttpTypes.DIRECTION_VALUE_PART);
        fillMap(ATTRIBUTE_MAP, HTTP_LINE_COMMENT, HttpTypes.DIRECTION_COMMENT_START, HttpTypes.BLOCK_COMMENT, HttpTypes.LINE_COMMENT);
        fillMap(ATTRIBUTE_MAP, HTTP_HEADER_FIELD_NAME, HttpTypes.FIELD_NAME);
        fillMap(ATTRIBUTE_MAP, HTTP_IDENTIFIER, HttpTypes.FIELD_VALUE);
        fillMap(ATTRIBUTE_MAP, HTTP_FILE_SIGN, HttpTypes.INPUT_FILE_SIGN, HttpTypes.OUTPUT_FILE_SIGN);
        fillMap(ATTRIBUTE_MAP, HTTP_FILE_PATH, HttpTypes.FILE_PATH_PART);
        fillMap(ATTRIBUTE_MAP, HTTP_MULTIPART_BOUNDARY, HttpTypes.MESSAGE_BOUNDARY);
        fillMap(ATTRIBUTE_MAP, HTTP_IDENTIFIER, HttpTypes.IDENTIFIER);
        fillMap(ATTRIBUTE_MAP, HTTP_REQUEST_NAME, HttpTypes.REQUEST_COMMENT);
        fillMap(ATTRIBUTE_MAP, HttpTypeSets.VARIABLE_BRACES, HTTP_VARIABLE_BRACES);
        fillMap(ATTRIBUTE_MAP, DefaultLanguageHighlighterColors.STRING, HttpTypes.STRING);
        fillMap(ATTRIBUTE_MAP, DefaultLanguageHighlighterColors.NUMBER, HttpTypes.INTEGER);
    }

    public @NotNull Lexer getHighlightingLexer() {
        return new HttpAdapter();
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTE_MAP.get(tokenType));
    }

}
