package org.javamaster.httpclient.dubbo.loader

import java.net.URL
import java.net.URLClassLoader

/**
 * @author yudong
 */
class ApiClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent) {

    override fun loadClass(name: String): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            var c = findLoadedClass(name)

            if (c != null) return c

            try {
                c = findClass(name)
            } catch (_: ClassNotFoundException) {
                c = parent.loadClass(name)
            }

            return c
        }
    }

}
