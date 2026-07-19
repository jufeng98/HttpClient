package org.javamaster.httpclient.utils

object ClassUtils {
    private val PRIMITIVE_MAP = HashMap<String?, Class<*>?>()

    init {
        PRIMITIVE_MAP.put("int", Int::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("long", Long::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("double", Double::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("float", Float::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("boolean", Boolean::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("byte", Byte::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("char", Char::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("short", Short::class.javaPrimitiveType)
        PRIMITIVE_MAP.put("void", Void.TYPE)
    }

    fun getPrimitiveClass(name: String?): Class<*>? {
        return PRIMITIVE_MAP[name]
    }

}