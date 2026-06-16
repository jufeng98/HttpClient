package org.javamaster.httpclient.highlighting.support;

import com.google.common.collect.Maps;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.parser.CookieAdapter;
import org.javamaster.httpclient.psi.CookieTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * @author yudong
 */
public class CookieSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTE_MAP = Maps.newHashMap();

    public static final TextAttributesKey COOKIE_LINE_COMMENT;
    public static final TextAttributesKey COOKIE_TOKEN;

    static {
        COOKIE_LINE_COMMENT = createTextAttributesKey("COOKIE_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
        COOKIE_TOKEN = TextAttributesKey.createTextAttributesKey("COOKIE_TOKEN", DefaultLanguageHighlighterColors.IDENTIFIER);

        fillMap(ATTRIBUTE_MAP, COOKIE_LINE_COMMENT, CookieTypes.LINE_COMMENT);
        fillMap(ATTRIBUTE_MAP, COOKIE_TOKEN, CookieTypes.COOKIE_TOKEN);
    }

    public @NotNull Lexer getHighlightingLexer() {
        return new CookieAdapter();
    }

    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTE_MAP.get(tokenType));
    }
}
