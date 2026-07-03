package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import org.javamaster.httpclient.completion.support.Number2InsertHandler
import org.javamaster.httpclient.completion.support.Number3InsertHandler
import org.javamaster.httpclient.completion.support.NumberInsertHandler
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
enum class ParamEnum(val param: String, val desc: String) {
    VISUALIZE_TIMESTAMP("visualize-timestamp", NlsBundle.nls("visualize.timestamp.desc")),
    AUTO_ENCODING("auto-encoding", NlsBundle.nls("auto.encoding.desc")),
    AUTO_REDIRECT("auto-redirect", NlsBundle.nls("auto.redirect.desc")),
    NO_LOG("no-log", NlsBundle.nls("no.log.desc")),
    NO_COOKIE_JAR("no-cookie-jar", NlsBundle.nls("no.cookie.desc")),
    CONNECT_TIMEOUT_NAME("connectTimeout", NlsBundle.nls("connect.timeout.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return NumberInsertHandler
        }
    },
    READ_TIMEOUT_NAME("readTimeout", NlsBundle.nls("read.timeout.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return NumberInsertHandler
        }
    },
    TIMEOUT_NAME("timeout", NlsBundle.nls("timeout.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return Number3InsertHandler
        }
    },
    REQUIRE("require", NlsBundle.nls("require.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP
        }
    },
    IMPORT("import", NlsBundle.nls("import.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP
        }
    },
    RESPONSE_STATUS("responseStatus", NlsBundle.nls("response.status.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return Number2InsertHandler
        }
    },
    STATIC_FOLDER("staticFolder", NlsBundle.nls("static.folder.desc")) {
        override fun insertHandler(): InsertHandler<LookupElement> {
            return AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP
        }
    },
    ;

    open fun insertHandler(): InsertHandler<LookupElement>? {
        return null
    }

    companion object {
        private val map by lazy {
            val map = mutableMapOf<String, ParamEnum>()
            ParamEnum.entries
                .forEach {
                    map[it.param] = it
                }
            return@lazy map
        }

        private val filePathParamSet = setOf(
            IMPORT.param,
            STATIC_FOLDER.param,
        )


        fun getEnum(param: String): ParamEnum? {
            return map[param]
        }

        fun getRequestParams(): List<ParamEnum> {
            return listOf(
                CONNECT_TIMEOUT_NAME,
                READ_TIMEOUT_NAME,
                TIMEOUT_NAME,
                RESPONSE_STATUS,
                STATIC_FOLDER,
                AUTO_ENCODING,
                AUTO_REDIRECT,
                VISUALIZE_TIMESTAMP,
                NO_LOG,
                NO_COOKIE_JAR,
            )
        }

        fun getGlobalParams(): List<ParamEnum> {
            return listOf(
                IMPORT
            )
        }

        fun isFilePathParam(paramName: String?): Boolean {
            return filePathParamSet.contains(paramName)
        }
    }
}
