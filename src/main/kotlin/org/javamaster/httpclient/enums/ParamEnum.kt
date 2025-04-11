package org.javamaster.httpclient.enums

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import org.javamaster.httpclient.completion.support.QuoteInsertHandler

/**
 * @author yudong
 */
enum class ParamEnum(val param: String, val desc: String) {
    CONNECT_TIMEOUT_NAME("connectTimeout", "Http and websocket connect timeout(positive number, unit: seconds)"),
    READ_TIMEOUT_NAME("readTimeout", "Http and websocket read timeout(positive number, unit: seconds)"),
    TIMEOUT_NAME("timeout", "Dubbo connect timeout(positive number, unit: milliseconds)"),
    IMPORT("import", "Import file, like javascript file") {

        override fun insertHandler(): InsertHandler<LookupElement> {
            return QuoteInsertHandler
        }

    },
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


        fun getEnum(param: String): ParamEnum? {
            return map[param]
        }

        fun getRequestParams(): List<ParamEnum> {
            return listOf(
                CONNECT_TIMEOUT_NAME,
                READ_TIMEOUT_NAME,
                TIMEOUT_NAME,
            )
        }

        fun getGlobalParams(): List<ParamEnum> {
            return listOf(
                IMPORT
            )
        }
    }
}
