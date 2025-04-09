package org.javamaster.httpclient.enums

/**
 * @author yudong
 */
enum class ParamEnum(val param: String, val desc: String) {
    CONNECT_TIMEOUT_NAME("connectTimeout", "Http and websocket connect timeout(positive number, unit: seconds)"),
    READ_TIMEOUT_NAME("readTimeout", "Http and websocket read timeout(positive number, unit: seconds)"),
    TIMEOUT_NAME("timeout", "Dubbo connect timeout(positive number, unit: milliseconds)"),
    ;

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
    }
}
