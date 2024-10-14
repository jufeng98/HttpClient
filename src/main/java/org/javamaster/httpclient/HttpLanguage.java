package org.javamaster.httpclient;

import com.intellij.lang.Language;

/**
 * @author yudong
 */
public class HttpLanguage extends Language {

    public static final HttpLanguage INSTANCE = new HttpLanguage();

    private HttpLanguage() {
        super("http");
    }

}
