package org.javamaster.httpclient.mock.support

import com.google.common.net.HttpHeaders
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.apache.http.HttpStatus
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.mock.support.MockServerHelper.computeResBody
import org.javamaster.httpclient.mock.support.MockServerHelper.writeHtmlResAndLog
import org.javamaster.httpclient.mock.support.MockServerHelper.writeResBody
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @author yudong
 */
class RequestHandler(
    private val resConsumer: Consumer<String>,
    private val request: HttpRequest,
    private val variableResolver: VariableResolver,
    private val paramMap: Map<String, String>,
) : HttpHandler {

    private val path = resolvePath(request, variableResolver)

    private val staticFolder = checkStaticFolder(paramMap[ParamEnum.STATIC_FOLDER.param])

    private val responseStatus = paramMap[ParamEnum.RESPONSE_STATUS.param]?.toInt()

    private val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong()

    override fun handle(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val reqPath = exchange.requestURI.path

        MockServerHelper.printRequestInfo(exchange, resConsumer)

        if (readTimeout != null) {
            resConsumer.accept("Sleeping $readTimeout s......\n")
            TimeUnit.SECONDS.sleep(readTimeout)
        }

        if (HttpMethod.HEAD.name == method) {
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
            return
        }

        val resHeaders = exchange.responseHeaders
        resHeaders.set(HttpHeaders.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME))
        resHeaders.set(HttpHeaders.SERVER, "MockWebServer/1.0 (Java 17)")

        if (staticFolder != null) {
            if (reqPath.startsWith(path)) {
                val resolvePath = reqPath.substring(path.length)
                val file = File(staticFolder, resolvePath)
                if (file.isDirectory) {
                    val resHtml = MockServerHelper.constructFileListResHtml(file, reqPath)

                    val status = responseStatus ?: HttpStatus.SC_OK
                    writeHtmlResAndLog(resHtml, exchange, resHeaders, status, resConsumer)
                } else {
                    if (file.exists()) {
                        val status = responseStatus ?: HttpStatus.SC_OK
                        MockServerHelper.writeFileResBytes(file, exchange, status, resHeaders, resConsumer)
                    } else {
                        val resStr = MockServerHelper.construct404ResHtml(reqPath)

                        val status = responseStatus ?: HttpStatus.SC_NOT_FOUND
                        writeHtmlResAndLog(resStr, exchange, resHeaders, status, resConsumer)
                    }
                }
            } else {
                val resStr = MockServerHelper.construct404ResHtml(reqPath)

                val status = responseStatus ?: HttpStatus.SC_NOT_FOUND
                writeHtmlResAndLog(resStr, exchange, resHeaders, status, resConsumer)
            }
        } else {
            if (reqPath == path) {
                val resBody = computeResBody(request, variableResolver, paramMap)

                val status = responseStatus ?: HttpStatus.SC_OK
                writeResBody(resBody, exchange, status, resHeaders, resConsumer)
            } else {
                val resStr = MockServerHelper.construct404ResHtml(reqPath)

                val status = responseStatus ?: HttpStatus.SC_NOT_FOUND
                writeHtmlResAndLog(resStr, exchange, resHeaders, status, resConsumer)
            }
        }

        exchange.close()
    }

    private fun resolvePath(request: HttpRequest, variableResolver: VariableResolver): String {
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

}