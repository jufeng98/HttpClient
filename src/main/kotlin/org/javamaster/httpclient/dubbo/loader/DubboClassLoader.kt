package org.javamaster.httpclient.dubbo.loader

import java.net.URL
import java.net.URLClassLoader

/**
 * @author yudong
 */
class DubboClassLoader(urls: Array<URL>, parent: ClassLoader) : URLClassLoader(urls, parent)
