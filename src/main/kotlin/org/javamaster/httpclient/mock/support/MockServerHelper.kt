package org.javamaster.httpclient.mock.support

import com.google.common.net.HttpHeaders
import com.intellij.openapi.util.text.Formats
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.application
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import org.apache.commons.lang3.time.DateFormatUtils
import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.ReqUtils
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.activation.MimetypesFileTypeMap

/**
 * @author yudong
 */
object MockServerHelper {
    private val mimetypesFileTypeMap = MimetypesFileTypeMap()

    fun printRequestInfo(exchange: HttpExchange, httpDashboardForm: HttpDashboardForm) {
        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.server.receive", exchange.remoteAddress.port) + "\n")

        httpDashboardForm.showMockServerLog("${exchange.requestMethod} ${exchange.requestURI}\n")

        exchange.requestHeaders.forEach {
            val key = it.key
            it.value.forEach {
                httpDashboardForm.showMockServerLog("${key}: ${it}\n")
            }
        }
        httpDashboardForm.showMockServerLog("\n")

        val reqBody = StreamUtils.copyToString(exchange.requestBody, StandardCharsets.UTF_8)
        httpDashboardForm.showMockServerLog(reqBody.replace(CR_LF, "\n") + "\n")
    }

    fun constructFileListResHtml(root: File, reqPath: String): String {
        val body = root.list()!!.joinToString(CR_LF) {
            val file = File(root, it)
            val filePath = file.toPath()
            val linkPath = if (reqPath.endsWith("/")) {
                reqPath + urlEncode(it)
            } else {
                reqPath + "/" + urlEncode(it)
            }

            val name: String
            val size = if (file.isFile) {
                name = "(File) $it"
                Formats.formatFileSize(Files.size(filePath))
            } else {
                name = "(Dir)  $it"
                ""
            }

            val time = DateFormatUtils.format(Files.getLastModifiedTime(filePath).toMillis(), "yyyy/MM/dd HH:mm")

            """
                <tr>
                    <td>
                        <a href='$linkPath'>${name}</a>
                    </td>
                    <td>$size</td>
                    <td>$time</td>
                </tr>
            """.trimIndent()
        }

        val bodyStr = """
            <!doctype html>
            <html lang="zh">
            <head>
                <title>Files</title>
                <style>
                    body {
                        font-family: Tahoma,Arial,sans-serif;
                    }
            
                    h1, h2, h3, b {
                        color: white;
                        background-color: #525D76;
                    }
                    
                    table {
                        width: 80%;
                        text-align: left;
                    }
            
                    .line {
                        height: 1px;
                        background-color: #525D76;
                        border: none;
                    }
                </style>
            </head>
            <body>
            <h1>Directory listing for $reqPath</h1>
            <hr class="line"/>
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Size</th>
                    <th>Last Modified</th>
                </tr>
                </thead>
                <tbody>
                    $body
                </tbody>
            </table>
            </body>
            </html>
        """.trimIndent()

        return bodyStr
    }

    fun construct404ResHtml(reqPath: String): String {
        return """
            <!doctype html>
            <html lang="zh">
                <head>
                    <title>HTTP status 404 - Not Found</title>
                    <style type="text/css">
                        body {
                            font-family: Tahoma,Arial,sans-serif;
                        }
            
                        h1, h2, h3, b {
                            color: white;
                            background-color: #525D76;
                        }   
                        
                        .line {
                            height: 1px;
                            background-color: #525D76;
                            border: none;
                        }                                                        
                    </style>
                </head>
                <body>
                    <h1>HTTP status 404 - Not Found</h1>
                    <hr class="line"/>
                    <p>
                        <b>类型</b>
                        状态报告
                    </p>
                    <p>
                        <b>消息</b>
                        path [$reqPath] not found
                    </p>
                    <hr class="line"/>
                    <h3>Java ServerSocket</h3>
                </body>
            </html>
        """.trimIndent()
    }

    fun writeHtmlResAndLog(
        resHtml: String,
        exchange: HttpExchange,
        resHeaders: Headers,
        status: Int,
        httpDashboardForm: HttpDashboardForm,
    ) {
        resHeaders.set(HttpHeaders.CONTENT_TYPE, "text/html;charset=utf-8")

        val byteArray = resHtml.toByteArray(StandardCharsets.UTF_8)
        val size = byteArray.size.toLong()

        exchange.sendResponseHeaders(status, size.toLong())

        exchange.responseBody.write(byteArray)

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.server.res") + "Content-Length ${size}b\n")
        httpDashboardForm.showMockServerLog("-----------------------------\n")
    }

    fun writeFileResBytes(
        file: File,
        exchange: HttpExchange,
        status: Int,
        resHeaders: Headers,
        httpDashboardForm: HttpDashboardForm,
    ) {
        val bytes = file.readBytes()
        val length = bytes.size
        val mimeType = mimetypesFileTypeMap.getContentType(file.name)
        val filename = urlEncode(file.name)

        resHeaders.set(HttpHeaders.CONTENT_TYPE, mimeType)
        resHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "name=\"attachment\"; filename=\"$filename\"")
        exchange.sendResponseHeaders(status, length.toLong())

        exchange.responseBody.write(bytes)

        httpDashboardForm.showMockServerLog("Write file bytes to client: $file\n")
        httpDashboardForm.showMockServerLog("-----------------------------\n")
    }

    fun writeResBody(
        pair: Pair<Any?, LinkedMultiValueMap<String, String?>>,
        exchange: HttpExchange,
        status: Int,
        resHeaders: Headers,
        httpDashboardForm: HttpDashboardForm,
    ) {
        pair.second.forEach { (t, u) ->
            u.forEach {
                resHeaders.add(t, it)
            }
        }

        val convertReqBody = ReqUtils.convertReqBody(pair.first) ?: return

        val size: Long
        if (convertReqBody is String) {
            val bodyStr = convertReqBody.toString()

            val byteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
            size = byteArray.size.toLong()

            exchange.sendResponseHeaders(status, size)

            exchange.responseBody.write(byteArray)
        } else if (convertReqBody is ByteArray) {
            size = convertReqBody.size.toLong()

            exchange.sendResponseHeaders(status, size)

            exchange.responseBody.write(convertReqBody)
        } else {
            throw UnsupportedOperationException()
        }

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.server.res") + "Content-Length $size b\n")
        httpDashboardForm.showMockServerLog("-----------------------------\n")
    }

    fun computeResBody(
        request: HttpRequest,
        variableResolver: VariableResolver,
    ): Pair<Any?, LinkedMultiValueMap<String, String?>> {
        return application.runReadAction<Pair<Any?, LinkedMultiValueMap<String, String?>>> {
            val reqBody = HttpUtils.convertToReqBody(request, variableResolver)

            val httpHeaderFields = request.header?.headerFieldList
            val reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

            Pair(reqBody, reqHeaderMap)
        }
    }

    fun resolvePort(httpMethod: HttpMethod): Int {
        val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!

        return resolvePort(requestTarget.port)
    }

    fun resolvePort(httpPort: HttpPort?): Int {
        return if (httpPort != null) {
            val firstChild = httpPort.firstChild
            val portStr = HttpPsiUtils.getNextSiblingByType(firstChild, HttpTypes.PORT_SEGMENT, false)!!.text
            return try {
                portStr.toInt()
            } catch (_: Exception) {
                -1
            }
        } else {
            80
        }
    }
}