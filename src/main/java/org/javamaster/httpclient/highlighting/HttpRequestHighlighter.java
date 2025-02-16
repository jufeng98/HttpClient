package org.javamaster.httpclient.highlighting;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.javamaster.httpclient.psi.HttpTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpRequestHighlighter extends LayeredLexerEditorHighlighter {
    public HttpRequestHighlighter(@NotNull EditorColorsScheme scheme, @Nullable Project project, @Nullable VirtualFile file) {
        super(new HttpRequestSyntaxHighlighter(), scheme);

        registerLayer(HttpTypes.LINE_COMMENT,
                new LayerDescriptor(new HttpRequestSyntaxHighlighter(), "", HttpRequestSyntaxHighlighter.HTTP_REQUEST_COMMENT));
    }
}
