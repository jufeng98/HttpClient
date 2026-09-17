package org.javamaster.httpclient.ftp

import java.net.URL
import java.net.URLClassLoader

class FtpLibClassLoader(
    urls: Array<URL>,
    parent: ClassLoader
) : URLClassLoader(urls, parent) {

    // 需要子优先加载的包前缀
    private val childFirstPrefixes = listOf(
        "org.apache.ftpserver",
    )

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // 1. 检查已加载的类
        var c = findLoadedClass(name)
        if (c != null) {
            if (resolve) resolveClass(c)
            return c
        }

        // 2. 对特定包采用子优先策略
        val isChildFirst = childFirstPrefixes.any { name.startsWith(it) }
        if (isChildFirst) {
            try {
                c = findClass(name)  // 优先从自己的 JAR 中加载
                if (resolve) resolveClass(c)
                return c
            } catch (_: ClassNotFoundException) {
                // 找不到则回退到父加载器
            }
        }

        // 3. 其他类走默认的父优先策略
        return super.loadClass(name, resolve)
    }
}