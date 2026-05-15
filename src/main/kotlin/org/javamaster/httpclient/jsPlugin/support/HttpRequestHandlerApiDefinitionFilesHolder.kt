package org.javamaster.httpclient.jsPlugin.support

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.impl.LightFilePointer

/**
 * @author yudong
 */
object HttpRequestHandlerApiDefinitionFilesHolder {

    val commonLibraryFilePointer by lazy {
        val url = javaClass.classLoader.getResource("types/http-request.common.d.ts")!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        LightFilePointer(virtualFile)
    }

    val responseLibraryFilePointer by lazy {
        val url = javaClass.classLoader.getResource("types/http-request.d.ts")!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        LightFilePointer(virtualFile)
    }

    val preRequestLibraryFilePointer by lazy {
        val url = javaClass.classLoader.getResource("types/http-request.pre-request.d.ts")!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        LightFilePointer(virtualFile)
    }

    val cryptoLibraryFilePointer by lazy {
        val url = javaClass.classLoader.getResource("types/http-request.crypto.d.ts")!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        LightFilePointer(virtualFile)
    }

    val dynamicVariablesFilePointer by lazy {
        val url = javaClass.classLoader.getResource("types/http-request.dynamic-variables.d.ts")!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        LightFilePointer(virtualFile)
    }
}
