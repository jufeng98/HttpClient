package org.javamaster.httpclient.utils;

import com.intellij.psi.tree.IElementType;
import org.javamaster.httpclient.psi.HttpElementType;

public class LexerUtils {

    public static boolean moreTwoLineBreak(CharSequence str) {
        if (str.length() < 2) {
            return false;
        }

        int times1 = 0;
        int times2 = 0;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c == '\r') {
                times1++;
            } else if (c == '\n') {
                times2++;
            }
        }

        return times1 >= 2 || times2 >= 2;
    }

    public static IElementType createMessageText(StringBuilder body) {
        String text = body.toString();
        body.setLength(0);
        return new HttpElementType("MESSAGE_TEXT",text);
    }

    public static IElementType createScriptBody(StringBuilder body) {
        String text = body.toString();
        body.setLength(0);
        return new HttpElementType("SCRIPT_BODY",text);
    }
}
