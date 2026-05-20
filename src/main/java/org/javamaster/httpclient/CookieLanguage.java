package org.javamaster.httpclient;

import com.intellij.lang.Language;

import static org.javamaster.httpclient.CookieFileType.DEFAULT_EXTENSION;

/**
 * @author yudong
 */
public class CookieLanguage extends Language {

    public static final CookieLanguage INSTANCE = new CookieLanguage();

    private CookieLanguage() {
        super(DEFAULT_EXTENSION);
    }

}
