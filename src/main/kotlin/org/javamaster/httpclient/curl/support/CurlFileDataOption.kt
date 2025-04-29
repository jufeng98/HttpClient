package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import java.io.File


class CurlFileDataOption(private val myFilename: String) : CurlDataOption {

    override fun apply(curlRequest: CurlRequest) {
        if (StringUtil.isNotEmpty(curlRequest.filesToSend)) {
            curlRequest.filesToSend = curlRequest.filesToSend + File.pathSeparator + myFilename
        } else {
            curlRequest.filesToSend = myFilename
        }

        curlRequest.haveFileToSend = true
    }

}
