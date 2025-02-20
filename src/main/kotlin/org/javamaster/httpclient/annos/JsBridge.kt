package org.javamaster.httpclient.annos

import java.lang.annotation.Inherited


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class JsBridge(val jsFun: String)
