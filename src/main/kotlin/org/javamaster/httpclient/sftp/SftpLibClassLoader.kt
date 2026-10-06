package org.javamaster.httpclient.sftp

import java.net.URL
import java.net.URLClassLoader

class SftpLibClassLoader(
    urls: Array<URL>,
    parent: ClassLoader,
) : URLClassLoader(urls, parent) {

    private val childFirstPrefixes = listOf(
        "org.apache.sshd",
    )

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        var c = findLoadedClass(name)
        if (c != null) {
            if (resolve) resolveClass(c)
            return c
        }

        if (name == "org.apache.sshd.common.util.ValidateUtils") {
            return super.loadClass(name, resolve)
        }

        val isChildFirst = childFirstPrefixes.any { name.startsWith(it) }
        if (isChildFirst) {
            try {
                c = findClass(name)
                if (resolve) resolveClass(c)
                return c
            } catch (_: ClassNotFoundException) {
            }
        }

        return super.loadClass(name, resolve)
    }
}