package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import org.javamaster.httpclient.consts.HttpConsts.Companion.WEB_BOUNDARY
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.CookieUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.constructMultipartBodyCurl
import org.javamaster.httpclient.utils.HttpUtils.handleOrdinaryContentCurl
import org.javamaster.httpclient.utils.ReqUtils
import java.util.function.Consumer

/**
 * @author yudong
 */
class CurlProcessHandler(
    httpMethod: HttpMethod, selectedEnv: String?,
    private val raw: Boolean,
    private val consumer: Consumer<String>,
) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    override fun startProcess() {
        var url = resolveAndHandleUrl()

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        val preJsResList = executePreJs(url, reqInfo, reqHeaderMap)
        println("js执行结果:$preJsResList")

        url = variableResolver.resolve(url)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.encodeUrl(url)
        }

        CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)

        reqHeaderMap.addAll(GlobalHeaders.dataHolder)

        handleCurl(raw, url, reqHeaderMap)
    }

    private fun handleCurl(
        raw: Boolean,
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
    ) {
        val list = mutableListOf<String>()

        if (raw) {
            val tabName = HttpUtils.getTabName(request.method)
            list.add("### $tabName$CR_LF")
        }

        list.add(
            if (raw) {
                "${request.method.text} $url$CR_LF"
            } else {
                "curl -X ${request.method.text} --location \"$url\""
            }
        )

        reqHeaderMap.forEach {
            val name = it.key
            for (value in it.value) {
                list.add(
                    if (raw) {
                        "$name: $value$CR_LF"
                    } else {
                        "    -H \"$name: ${value}\""
                    }
                )
            }
        }

        if (raw) {
            list.add(CR_LF)
        }

        val header = request.header
        val body = request.body
        val requestMessagesGroup = body?.requestMessagesGroup
        val httpMultipartMessage = body?.multipartMessage

        if (requestMessagesGroup != null) {
            val content = handleOrdinaryContentCurl(requestMessagesGroup, variableResolver, header, raw)

            list.add(
                if (raw) {
                    content
                } else {
                    "    -d '${content}'"
                }
            )
        } else if (httpMultipartMessage != null) {
            val boundary = request.contentTypeBoundary ?: WEB_BOUNDARY

            val contents = constructMultipartBodyCurl(httpMultipartMessage, variableResolver, boundary, raw)

            list.addAll(contents)
        }

        val str = if (raw) {
            list.joinToString("")
        } else {
            list.joinToString(" \\${CR_LF}")
        }

        runInEdt {
            consumer.accept(str)
        }
    }
}