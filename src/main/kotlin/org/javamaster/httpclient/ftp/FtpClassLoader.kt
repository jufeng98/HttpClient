package org.javamaster.httpclient.ftp

import java.net.URL
import java.net.URLClassLoader

/**
 * @author yudong
 */
class FtpClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent) {
    private val needParentLoad = setOf(
        "org.javamaster.httpclient.mock.support.MockFtpServer",
        "org.javamaster.httpclient.map.MultiValueMap",
        "org.javamaster.httpclient.ui.HttpDashboardForm",
        "org.javamaster.httpclient.enums.ParamEnum",
        "org.javamaster.httpclient.nls.NlsBundle",
    )

    override fun loadClass(name: String): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            var c = findLoadedClass(name)

            if (c != null) return c

            c = if (needParentLoad.contains(name)) {
                try {
                    parent.loadClass(name)
                } catch (_: ClassNotFoundException) {
                    findClass(name)
                }
            } else {
                try {
                    findClass(name)
                } catch (_: ClassNotFoundException) {
                    parent.loadClass(name)
                }
            }

            return c
        }
    }

}
