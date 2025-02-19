package org.javamaster.httpclient.utils;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient._HttpLexer;
import org.javamaster.httpclient.psi.HttpTypes;

public class LexerUtils {

    public static int detectBoundaryState(CharSequence matchText) {
        int length = matchText.length();
        if (length < 3) {
            return _HttpLexer.IN_HEADER;
        }

        int idx = 0;
        for (int i = length; i > 0; i--) {
            int index = i - 1;
            char c = matchText.charAt(index);
            if (c == '\r' || c == '\n') {
                continue;
            }

            idx = index;
            break;
        }

        if (matchText.charAt(idx) == '-') {
            return _HttpLexer.IN_BODY;
        } else {
            return _HttpLexer.IN_HEADER;
        }
    }

    public static IElementType detectBodyType(_HttpLexer lexer) {
        int matchTimes = lexer.matchTimes;

        lexer.matchTimes = 0;

        if (matchTimes > 0) {
            return HttpTypes.MESSAGE_TEXT;
        } else {
            return TokenType.WHITE_SPACE;
        }
    }

    public static boolean endsWithLineBreak(_HttpLexer lexer) {
        CharSequence lastMatch = lexer.lastMatch;
        int length = lastMatch.length();
        if (length == 0) {
            return true;
        }

        char c = lastMatch.charAt(length - 1);
        boolean b = c == '\r' || c == '\n';

        if (b) {
            lexer.lastMatch = "";
        }

        return b;
    }
}
