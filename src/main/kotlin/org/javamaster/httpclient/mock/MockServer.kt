package org.javamaster.httpclient.mock

import com.google.common.net.HttpHeaders
import com.intellij.openapi.util.text.Formats
import com.intellij.util.application
import org.apache.commons.lang3.time.DateFormatUtils
import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import javax.activation.MimetypesFileTypeMap


/**
 * @author yudong
 */
class MockServer {
    lateinit var resConsumer: Consumer<String>

    fun startServerAsync(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): ServerSocket {
        val serverSocket = ServerSocket(resolvePort(request))

        val path = resolvePath(request, variableResolver)

        val staticFolder = checkStaticFolder(paramMap[ParamEnum.STATIC_FOLDER.param])

        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.start", serverSocket.localPort) + "\n"))

        CompletableFuture.supplyAsync {
            while (true) {
                serverSocket.accept().use { socket ->
                    socket.getInputStream().use { inputStream ->
                        socket.getOutputStream().use { outputStream ->
                            resConsumer.accept(appendTime(NlsBundle.nls("mock.server.receive", socket.port) + "\n"))

                            val reqStr = readAsString(inputStream)
                            if (reqStr.isNotEmpty()) {
                                resConsumer.accept(reqStr.replace(CR_LF, "\n") + "\n")

                                val reqPath = URLDecoder.decode(
                                    reqStr.split(CR_LF).first().split(" ")[1],
                                    StandardCharsets.UTF_8.toString()
                                )

                                if (staticFolder != null) {
                                    if (reqPath.startsWith(path)) {
                                        val resolvePath = reqPath.substring(path.length)
                                        val file = File(staticFolder, resolvePath)
                                        if (file.isDirectory) {
                                            val res = constructFileListResponse(file, reqPath)

                                            writeStrResAndLog(res, outputStream)
                                        } else {
                                            if (file.exists()) {
                                                writeFileResponse(file, outputStream)

                                                resConsumer.accept("Write file to client: $file\n")

                                                resConsumer.accept("-----------------------------\n")
                                            } else {
                                                val resStr = construct404Response(reqPath)

                                                writeStrResAndLog(resStr, outputStream)
                                            }
                                        }
                                    } else {
                                        val resStr = construct404Response(reqPath)

                                        writeStrResAndLog(resStr, outputStream)
                                    }
                                } else {
                                    val resStr = if (reqPath == path) {
                                        val resBody = computeResInfo(request, variableResolver, paramMap)

                                        constructResponse(resBody, paramMap)
                                    } else {
                                        construct404Response(reqPath)
                                    }

                                    writeStrResAndLog(resStr, outputStream)
                                }
                            }
                        }
                    }
                }
            }
        }.exceptionally { ex ->
            ex.printStackTrace()

            resConsumer.accept(appendTime(NlsBundle.nls("mock.server.error", ex) + "\n"))
        }

        return serverSocket
    }

    private fun writeStrResAndLog(resStr: String, outputStream: OutputStream) {
        outputStream.write(resStr.toByteArray(StandardCharsets.UTF_8))

        resConsumer.accept(appendTime(NlsBundle.nls("mock.server.res") + "\n"))

        resConsumer.accept(resStr.replace(CR_LF, "\n") + "\n")

        resConsumer.accept("-----------------------------\n")
    }

    private fun readAsString(inputStream: InputStream): String {
        val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
        val buffer = CharArray(819200000)

        val bytesRead = reader.read(buffer)
        if (bytesRead == -1) {
            return ""
        }

        val out = StringBuilder()
        out.appendRange(buffer, 0, bytesRead)

        return out.toString()
    }

    private fun computeResInfo(
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

    private fun constructResponse(
        pair: Pair<Any?, LinkedMultiValueMap<String, String>>,
        paramMap: Map<String, String>,
    ): String {
        val statusCode = paramMap[ParamEnum.RESPONSE_STATUS.param]?.toLong() ?: 200

        val list = mutableListOf("HTTP/1.1 $statusCode OK$CR_LF")

        pair.second.forEach { (t, u) ->
            u.forEach {
                list.add("$t: $it$CR_LF")
            }
        }

        var length = 0
        var bodyStr: String? = null

        val resBody = pair.first
        if (resBody != null) {
            bodyStr = resBody.toString()

            length = bodyStr.toByteArray(StandardCharsets.UTF_8).size
        }

        list.add("${HttpHeaders.CONTENT_LENGTH}: $length$CR_LF")

        list.add(CR_LF)

        if (bodyStr != null) {
            list.add(bodyStr)
        }

        return list.joinToString("")
    }

    private fun constructFileListResponse(root: File, reqPath: String): String {
        val list = mutableListOf("HTTP/1.1 200 OK$CR_LF")

        list.add("${HttpHeaders.CONTENT_TYPE}: text/html;charset=utf-8$CR_LF")
        list.add("Date: ${Date()}$CR_LF")
        list.add("Server: ServerSocket$CR_LF")

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

        val length = bodyStr.toByteArray(StandardCharsets.UTF_8).size

        list.add("${HttpHeaders.CONTENT_LENGTH}: $length$CR_LF")

        list.add(CR_LF)

        list.add(bodyStr)

        return list.joinToString("")
    }

    private fun writeFileResponse(file: File, outputStream: OutputStream) {
        val mimeType = mimetypesFileTypeMap.getContentType(file.name)
        val filename = urlEncode(file.name)

        writeStr(outputStream, "HTTP/1.1 200 OK$CR_LF")
        writeStr(outputStream, "${HttpHeaders.CONTENT_TYPE}: $mimeType$CR_LF")
        writeStr(outputStream, "${HttpHeaders.CONTENT_DISPOSITION}:name=\"attachment\"; filename=\"$filename\"$CR_LF")
        writeStr(outputStream, "Date: ${Date()}$CR_LF")
        writeStr(outputStream, "Server: ServerSocket$CR_LF")

        val bytes = file.readBytes()
        val length = bytes.size

        writeStr(outputStream, "${HttpHeaders.CONTENT_LENGTH}: $length$CR_LF")
        writeStr(outputStream, CR_LF)

        try {
            outputStream.write(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeStr(outputStream: OutputStream, str: String) {
        outputStream.write(str.toByteArray(StandardCharsets.UTF_8))
    }

    private fun construct404Response(reqPath: String): String {
        val list = mutableListOf("HTTP/1.1 404 Not Found$CR_LF")

        list.add("${HttpHeaders.CONTENT_TYPE}: text/html;charset=utf-8$CR_LF")
        list.add("Date: ${Date()}$CR_LF")
        list.add("Server: ServerSocket$CR_LF")

        val bodyStr = """
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
        val length = bodyStr.toByteArray(StandardCharsets.UTF_8).size

        list.add("${HttpHeaders.CONTENT_LENGTH}: $length$CR_LF")

        list.add(CR_LF)

        list.add(bodyStr)

        return list.joinToString("")
    }

    private fun resolvePort(request: HttpRequest): Int {
        val httpPort = request.requestTarget?.port
        return if (httpPort != null) {
            val firstChild = httpPort.firstChild
            val portStr = HttpPsiUtils.getNextSiblingByType(firstChild, HttpTypes.PORT_SEGMENT, false)!!.text
            portStr.toInt()
        } else {
            80
        }
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
