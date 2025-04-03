package org.javamaster.httpclient.dubbo.loader

import java.net.URL
import java.net.URLClassLoader

/**
 * @author yudong
 */
class DubboClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent) {
    private val needParentLoad = setOf("org.javamaster.httpclient.map.LinkedMultiValueMap")

    override fun loadClass(name: String): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            var c = findLoadedClass(name)

            if (c != null) return c

            c = if (needParentLoad.contains(name)) {
                try {
                    parent.loadClass(name)
                } catch (e: ClassNotFoundException) {
                    findClass(name)
                }
            } else {
                try {
                    findClass(name)
                } catch (e: ClassNotFoundException) {
                    parent.loadClass(name)
                }
            }

            return c
        }
    }

}
