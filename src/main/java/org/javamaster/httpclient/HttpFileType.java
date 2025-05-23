package org.javamaster.httpclient;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yudong
 */
public class HttpFileType extends LanguageFileType {

    public static final HttpFileType INSTANCE = new HttpFileType();
    public static final String DEFAULT_EXTENSION = "http";

    private HttpFileType() {
        super(HttpLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "HTTP";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "HTTP request file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return HttpIcons.FILE;
    }

}
