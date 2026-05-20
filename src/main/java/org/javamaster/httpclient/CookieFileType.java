package org.javamaster.httpclient;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yudong
 */
public class CookieFileType extends LanguageFileType {

    public static final CookieFileType INSTANCE = new CookieFileType();
    public static final String DEFAULT_EXTENSION = "cookies";

    private CookieFileType() {
        super(CookieLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "COOKIE";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Cookie file";
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
