package org.javamaster.httpclient.highlighting;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;

public class HttpHighlighter extends LayeredLexerEditorHighlighter {
    public HttpHighlighter(@NotNull EditorColorsScheme scheme) {
        super(new HttpSyntaxHighlighter(), scheme);

        registerLayer(HttpTypes.LINE_COMMENT,
                new LayerDescriptor(new HttpSyntaxHighlighter(), "", HttpSyntaxHighlighter.HTTP_REQUEST_COMMENT));
    }
}
