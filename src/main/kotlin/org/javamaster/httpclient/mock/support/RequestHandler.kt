package org.javamaster.httpclient.mock.support

import com.google.common.net.HttpHeaders
import com.intellij.openapi.util.text.Formats
import com.intellij.util.application
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.apache.commons.lang3.time.DateFormatUtils
import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.ReqUtils
import org.javamaster.httpclient.utils.StreamUtils
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Consumer
import javax.activation.MimetypesFileTypeMap

/**
 * @author yudong
 */
class RequestHandler(
    private val resConsumer: Consumer<String>,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
) : HttpHandler {

    private val path by lazy {
        resolvePath(request, variableResolver)
    }
    private val staticFolder by lazy {
        checkStaticFolder(paramMap[ParamEnum.STATIC_FOLDER.param])
    }

    override fun handle(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val reqPath = exchange.requestURI.getPath()

        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.receive", exchange.localAddress.port) + "\n"))
        resConsumer.accept("$method ${exchange.requestURI}\n")

        exchange.requestHeaders.forEach {
            resConsumer.accept("${it.key}: ${it.value}\n")
        }
        resConsumer.accept("\n")

        val reqBody = StreamUtils.copyToString(exchange.requestBody, StandardCharsets.UTF_8)
        resConsumer.accept(reqBody.replace(CR_LF, "\n") + "\n")

        if ("HEAD".equals(method, ignoreCase = true)) {
            exchange.sendResponseHeaders(200, 0)
            exchange.close()
            return
        }

        val resHeaders = exchange.responseHeaders
        val resBodyStream = exchange.responseBody
        val contentLength: Int

        if (staticFolder != null) {
            if (reqPath.startsWith(path)) {
                val resolvePath = reqPath.substring(path.length)
                val file = File(staticFolder, resolvePath)
                if (file.isDirectory) {
                    val resStr = constructFileListResHtml(file, reqPath)

                    contentLength = writeHtmlResAndLog(resStr, resBodyStream, resHeaders)
                } else {
                    if (file.exists()) {
                        contentLength = writeFileResBytes(file, resBodyStream, resHeaders)

                        resConsumer.accept("Write file bytes to client: $file\n")
                        resConsumer.accept("-----------------------------\n")
                    } else {
                        val resStr = construct404ResHtml(reqPath)

                        contentLength = writeHtmlResAndLog(resStr, resBodyStream, resHeaders)
                    }
                }
            } else {
                val resStr = construct404ResHtml(reqPath)

                contentLength = writeHtmlResAndLog(resStr, resBodyStream, resHeaders)
            }
        } else {
            if (reqPath == path) {
                val resBody = computeResBody(request, variableResolver, paramMap)

                contentLength = writeResBody(resBody, resBodyStream, resHeaders)
            } else {
                val resStr = construct404ResHtml(reqPath)

                contentLength = writeHtmlResAndLog(resStr, resBodyStream, resHeaders)
            }
        }

        resHeaders.set("Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME))
        resHeaders.set("Server", "SimpleWebServer/1.0 (Java 17)")

        exchange.sendResponseHeaders(200, contentLength.toLong())
    }


    private fun writeHtmlResAndLog(resStr: String, resBody: OutputStream, resHeaders: Headers): Int {
        val byteArray = resStr.toByteArray(StandardCharsets.UTF_8)

        resHeaders.set(HttpHeaders.CONTENT_TYPE, "text/html;charset=utf-8")
        resHeaders.set(HttpHeaders.CONTENT_LENGTH, byteArray.size.toString())

        resBody.write(byteArray)

        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.res") + "\n"))
        resConsumer.accept(resStr.replace(CR_LF, "\n") + "\n")
        resConsumer.accept("-----------------------------\n")

        return byteArray.size
    }

    private fun computeResBody(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): Pair<Any?, LinkedMultiValueMap<String, String>> {
        return application.runReadAction<Pair<Any?, LinkedMultiValueMap<String, String>>> {
            val reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

            val httpHeaderFields = request.header?.headerFieldList
            val reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

            Pair(reqBody, reqHeaderMap)
        }
    }

    private fun writeResBody(
        pair: Pair<Any?, LinkedMultiValueMap<String, String>>,
        resBodyStream: OutputStream,
        resHeaders: Headers,
    ): Int {
        pair.second.forEach { (t, u) ->
            u.forEach {
                resHeaders.add(t, it)
            }
        }

        var length = 0
        var bodyStr: String? = null

        val convertReqBody = ReqUtils.convertReqBody(pair.first) ?: return 0
        if (convertReqBody is String) {
            bodyStr = convertReqBody.toString()

            val byteArray = bodyStr.toByteArray(StandardCharsets.UTF_8)
            length = byteArray.size

            resHeaders.set(HttpHeaders.CONTENT_LENGTH, length.toString())

            resBodyStream.write(byteArray)
        } else if (convertReqBody is ByteArray) {
            length = convertReqBody.size

            resHeaders.set(HttpHeaders.CONTENT_LENGTH, length.toString())

            resBodyStream.write(convertReqBody)
        } else {
            throw UnsupportedOperationException()
        }

        return length
    }

    private fun constructFileListResHtml(root: File, reqPath: String): String {
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

    private fun writeFileResBytes(file: File, outputStream: OutputStream, resHeaders: Headers): Int {
        val bytes = file.readBytes()
        val length = bytes.size
        val mimeType = mimetypesFileTypeMap.getContentType(file.name)
        val filename = urlEncode(file.name)

        resHeaders.set(HttpHeaders.CONTENT_TYPE, mimeType)
        resHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "name=\"attachment\"; filename=\"$filename\"")
        resHeaders.set(HttpHeaders.CONTENT_LENGTH, length.toString())

        outputStream.write(bytes)

        return length
    }

    private fun construct404ResHtml(reqPath: String): String {
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

    private fun resolvePath(
        request: HttpRequest,
        variableResolver: VariableResolver,
    ): String {
        val pathAbsolute = request.requestTarget!!.pathAbsolute
        return if (pathAbsolute != null) {
            variableResolver.resolve(pathAbsolute.text)
        } else {
            "/"
        }
    }

    private fun checkStaticFolder(staticFolder: String?): File? {
        staticFolder ?: return null

        val file = File(staticFolder)
        if (!file.exists()) {
            throw RuntimeException(NlsBundle.nls("folder.not.exist", file.absolutePath))
        }

        if (!file.isDirectory) {
            throw RuntimeException(NlsBundle.nls("not.folder", file.absolutePath))
        }

        return file
    }

    private fun appendTime(msg: String): String {
        val time = DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss,SSS")
        return "$time - $msg"
    }

    companion object {
        private val mimetypesFileTypeMap = MimetypesFileTypeMap()
    }

}