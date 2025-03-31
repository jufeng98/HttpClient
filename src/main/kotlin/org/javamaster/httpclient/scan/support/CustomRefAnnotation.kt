package org.javamaster.httpclient.scan.support

import org.javamaster.httpclient.enums.HttpMethod

class CustomRefAnnotation {
    val paths: MutableList<String> = mutableListOf()
    val methods: MutableList<HttpMethod> = mutableListOf()

    fun addPath(vararg paths: String) {
        this.paths.addAll(paths)
    }

    fun addMethods(vararg methods: HttpMethod) {
        this.methods.addAll(methods)
    }
}