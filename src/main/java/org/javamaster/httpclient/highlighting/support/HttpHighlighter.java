package org.javamaster.httpclient.highlighting.support;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import org.jetbrains.annotations.NotNull;

public class HttpHighlighter extends LayeredLexerEditorHighlighter {

    public HttpHighlighter(@NotNull EditorColorsScheme scheme) {
        super(new HttpSyntaxHighlighter(), scheme);
    }

}
