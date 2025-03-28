package org.javamaster.httpclient.highlighting;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.EditorHighlighterProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.javamaster.httpclient.highlighting.support.HttpHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HttpHighlighterProvider implements EditorHighlighterProvider {

    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @NotNull FileType fileType,
                                                  @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        return new HttpHighlighter(colors);
    }
}
