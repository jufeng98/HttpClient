package org.javamaster.httpclient.curl

import com.google.common.net.HttpHeaders
import com.intellij.openapi.project.Project
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newInvalidHeaderException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newInvalidMethodException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newInvalidPathException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newInvalidUrlException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newNoRequiredOptionDataException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newNoUrlException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newNotCurlException
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newNotSupportedOptionException
import org.javamaster.httpclient.curl.support.*
import org.javamaster.httpclient.curl.support.CurlDataOptionFactory.getCurlDataOption
import org.javamaster.httpclient.dashboard.HttpProcessHandler
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.CurlUtils
import java.net.URI
import java.net.URISyntaxException
import java.util.function.Consumer


class CurlParser(private val curl: String) {
    private var myContentType: String? = null
    private var myAuthSchemes: String? = null

    fun parseToCurlRequest(): CurlRequest {
        if (!CurlUtils.isCurlString(curl)) {
            throw newNotCurlException()
        }

        val curlRequest = CurlRequest()
        val tokens = CurlTokenizer.splitInCurlTokens(curl)
        var i = 1

        while (i < tokens.size) {
            val currentToken = tokens[i]
            if (currentToken == "\\") {
                ++i
            } else {
                var nextToken: String? = null
                if (i + 1 < tokens.size) {
                    nextToken = tokens[i + 1]
                }

                i += chooseCategory(curlRequest, currentToken, deleteBackslashes(nextToken))
            }
        }

        if (curlRequest.urlBase == null) {
            throw newNoUrlException()
        }

        if (myContentType != null) {
            addContentTypeHeaderToRequest(curlRequest)
        }

        return curlRequest
    }

    private fun chooseCategory(request: CurlRequest, currentToken: String, nextToken: String?): Int {
        var shift = 1
        if (CurlUtils.isLongOption(currentToken)) {
            shift = addLongOption(request, currentToken.substring(2), nextToken)
        } else if (CurlUtils.isShortOption(currentToken)) {
            shift = addShortOption(request, currentToken.substring(1), nextToken)
        } else {
            addURL(request, currentToken)
        }

        return shift
    }

    private fun addShortOption(request: CurlRequest, option: String, nextToken: String?): Int {
        var nextTokenTmp = nextToken
        if (CurlUtils.isAlwaysSetShortOption(option)) {
            return 1
        } else if (!CurlUtils.isKnownShortOption(option)) {
            throw newNotSupportedOptionException(option)
        } else {
            val withoutSpace: Boolean
            if (option.length > 1) {
                withoutSpace = true
                nextTokenTmp = option.substring(1)
            } else {
                withoutSpace = false
            }

            if (nextTokenTmp == null) {
                throw newNoRequiredOptionDataException(option)
            } else {
                when (option[0]) {
                    'F' -> addFormDataToRequest(request, nextTokenTmp)
                    'H' -> addHeaderToRequest(request, nextTokenTmp)
                    'X' -> addHttpMethodToRequest(request, nextTokenTmp)
                    'd' -> addDataToRequest("data", request, nextTokenTmp)
                    'u' -> addAuthorizationDataToRequest(request, nextTokenTmp)
                }

                return if (withoutSpace) 1 else 2
            }
        }
    }

    private fun addLongOption(request: CurlRequest, option: String, nextToken: String?): Int {
        if (CurlUtils.isAlwaysSetLongOption(option)) {
            return 1
        } else if (isAuthSchemeOption(request, option)) {
            return 1
        } else if (!CurlUtils.isKnownLongOption(option)) {
            throw newNotSupportedOptionException(option)
        } else if (nextToken == null) {
            throw newNoRequiredOptionDataException(option)
        } else if (option.startsWith("data")) {
            addDataToRequest(option, request, nextToken)
            return 2
        } else {
            when (option) {
                "url" -> addURL(request, nextToken)
                "request" -> addHttpMethodToRequest(request, nextToken)
                "header" -> addHeaderToRequest(request, nextToken)
                "user" -> addAuthorizationDataToRequest(request, nextToken)
                "form" -> addFormDataToRequest(request, nextToken)
            }

            return 2
        }
    }

    private fun addHeaderToRequest(request: CurlRequest, header: String) {
        val keyValueHeaderPair = getKeyValueForHeader(header)
        if (keyValueHeaderPair.key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
            myContentType = if (myContentType != null) {
                updateContentTypeIfNeeded(request, keyValueHeaderPair.value, myContentType!!)
            } else {
                keyValueHeaderPair.value
            }

            request.multipartBoundary = detectBoundary(header)
        } else {
            request.headers.add(getKeyValueForHeader(header))
        }
    }

    private fun detectBoundary(header: String): String? {
        val split = header.split(";")

        if (split.size <= 1) return null

        split.forEach {
            val keyValue = it.split("=")
            if (keyValue.size <= 1) {
                return@forEach
            }

            if (keyValue[0].trim() != "boundary") {
                return@forEach
            }

            return keyValue[1].trim()
        }

        return null
    }

    private fun addDataToRequest(optionName: String, request: CurlRequest, data: String) {
        request.httpMethod = HttpMethod.POST.name
        val curlDataOption = getCurlDataOption(optionName, data)
        curlDataOption?.apply(request)

        if (myContentType == null) {
            val header = HttpHeaders.CONTENT_TYPE + ": " + ContentType.APPLICATION_FORM_URLENCODED.mimeType

            addHeaderToRequest(request, header)
        }
    }

    private fun addAuthorizationDataToRequest(request: CurlRequest, authData: String) {
        val authScope = AuthScope(
            AuthScope.ANY_HOST,
            -1,
            AuthScope.ANY_REALM,
            if (myAuthSchemes == null) "Basic" else myAuthSchemes
        )
        var password = ""
        val colonPosition = authData.indexOf(':')
        val username: String
        if (colonPosition < 0) {
            username = authData
        } else {
            username = authData.substring(0, colonPosition)
            password = authData.substring(colonPosition + 1)
        }

        request.authData = CurlAuthData(authScope, UsernamePasswordCredentials(username, password))
    }

    private fun isAuthSchemeOption(request: CurlRequest, option: String): Boolean {
        myAuthSchemes = when (option) {
            "basic" -> "Basic"
            "digest" -> "Digest"
            "ntlm" -> "NTLM"
            "negotiate" -> "Negotiate"
            else -> return false
        }

        if (request.authData != null) {
            val authScope = AuthScope(
                AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM,
                myAuthSchemes
            )
            request.authData = CurlAuthData(authScope, request.authData!!.authCredentials)
        }

        return true
    }

    private fun addFormDataToRequest(request: CurlRequest, formData: String) {
        val curlFormData = CurlFormData(formData)

        val fieldName = curlFormData.name
        val curlFormBodyPart: CurlFormBodyPart
        if (curlFormData.hasFileContent()) {
            val file = curlFormData.file ?: throw newInvalidPathException(formData)

            val filename = file.name
            curlFormBodyPart =
                CurlFormBodyPart.create(fieldName, filename, file, curlFormData.formContentType)
                    .addHeader(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "form-data; name=\"$fieldName\"; filename=\"$filename\""
                    )
        } else {
            curlFormBodyPart =
                CurlFormBodyPart.create(fieldName, curlFormData.content, curlFormData.formContentType)
                    .addHeader(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "form-data; name=\"$fieldName\""
                    )
        }

        curlFormData.headers.forEach(Consumer { additionalHeader: CurlRequest.KeyValuePair ->
            curlFormBodyPart.addHeader(
                additionalHeader.key,
                additionalHeader.value
            )
        })

        request.formBodyPart.add(curlFormBodyPart)

        request.httpMethod = HttpMethod.POST.name
        request.isFileUpload = true

        if (request.multipartBoundary == null) {
            request.multipartBoundary = BOUNDARY
        }

        if (myContentType == null) {
            val header = HttpHeaders.CONTENT_TYPE + ": " + ContentType.MULTIPART_FORM_DATA.mimeType

            addHeaderToRequest(request, header)
        }
    }

    private fun addContentTypeHeaderToRequest(request: CurlRequest) {
        var header = "${HttpHeaders.CONTENT_TYPE}: "
        header = if (request.multipartBoundary != null) {
            "$header$myContentType; boundary=${request.multipartBoundary}"
        } else {
            header + myContentType
        }

        request.headers.add(getKeyValueForHeader(header))
    }

    companion object {
        private const val BOUNDARY = "WebAppBoundary"
        private const val MULTIPART_FORM_HEADER_VALUE = "multipart/form-data"
        private const val URLENCODED_HEADER_VALUE = "application/x-www-form-urlencoded"

        fun deleteBackslashes(data: String?): String? {
            return data?.replace("\\\\".toRegex(), "")
        }

        fun toCurlString(requestBlock: HttpRequestBlock, project: Project, consumer: Consumer<String>) {
            val request = requestBlock.request

            val editorTopForm = HttpEditorTopForm.getSelectedEditorTopForm(project)

            val httpProcessHandler = HttpProcessHandler(request.method, editorTopForm?.selectedEnv)

            httpProcessHandler.convertToCurl(consumer)
        }

        private fun updateContentTypeIfNeeded(
            request: CurlRequest,
            headerValue: String,
            contentType: String,
        ): String {
            var updatedContentType = contentType
            if (request.multipartBoundary != null) {
                if (contentType == MULTIPART_FORM_HEADER_VALUE) {
                    updatedContentType = headerValue
                }
            } else if (contentType == URLENCODED_HEADER_VALUE) {
                updatedContentType = headerValue
            } else {
                updatedContentType = "$contentType, $headerValue"
            }

            return updatedContentType
        }

        private fun addHttpMethodToRequest(request: CurlRequest, method: String) {
            if (!CurlUtils.isValidRequestOption(method)) {
                throw newInvalidMethodException(method)
            } else {
                request.httpMethod = method
            }
        }

        private fun addURL(request: CurlRequest, currentToken: String) {
            if (request.httpMethod == null) {
                addHttpMethodToRequest(request, HttpMethod.GET.name)
            }

            try {
                URI(currentToken)
                request.urlBase = currentToken
                request.urlPath = ""
            } catch (var3: URISyntaxException) {
                throw newInvalidUrlException(currentToken)
            }
        }

        private fun getKeyValueForHeader(header: String): CurlRequest.KeyValuePair {
            val colonPosition = header.indexOf(':')
            if (colonPosition < 0) {
                return CurlRequest.KeyValuePair(header.trim { it <= ' ' }.replace(";$".toRegex(), ""), "")
            }

            val name = header.substring(0, colonPosition).trim { it <= ' ' }
            if (name.isEmpty()) {
                throw newInvalidHeaderException(header)
            }

            val value = header.substring(colonPosition + 1)
            return CurlRequest.KeyValuePair(name, value.trim { it <= ' ' })
        }
    }
}