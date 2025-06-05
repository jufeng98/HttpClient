package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
enum class ParamEnum(val param: String, val desc: String) {
    CONNECT_TIMEOUT_NAME("connectTimeout", NlsBundle.nls("connect.timeout.desc")),
    READ_TIMEOUT_NAME("readTimeout", NlsBundle.nls("read.timeout.desc")),
    TIMEOUT_NAME("timeout", NlsBundle.nls("timeout.desc")),
    REQUIRE("require", NlsBundle.nls("require.desc")),
    IMPORT("import", NlsBundle.nls("import.desc")),
    RESPONSE_STATUS("responseStatus", NlsBundle.nls("response.status.desc")),
    ;

    open fun insertHandler(): InsertHandler<LookupElement>? {
        return AddSpaceInsertHandler.INSTANCE
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

        private val filePathParamSet = setOf(IMPORT.param)


        fun getEnum(param: String): ParamEnum? {
            return map[param]
        }

        fun getRequestParams(): List<ParamEnum> {
            return listOf(
                CONNECT_TIMEOUT_NAME,
                READ_TIMEOUT_NAME,
                TIMEOUT_NAME,
                RESPONSE_STATUS,
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
