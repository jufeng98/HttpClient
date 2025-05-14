package org.javamaster.httpclient.curl.exception

import org.javamaster.httpclient.curl.enums.CurlParseErrorType
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.net.URISyntaxException


class CurlParseException(val type: CurlParseErrorType, message: String, cause: Exception? = null) :
    RuntimeException(message, cause) {

    override fun toString(): String {
        return "${javaClass.name}, type: $type, msg: $message"
    }

    companion object {
        fun newNotCurlException(curl: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.NOT_CURL, nls("curl.is.not.curl", curl))
        }

        fun newNoUrlException(): CurlParseException {
            return CurlParseException(CurlParseErrorType.NO_URL, nls("curl.no.url"))
        }

        fun newInvalidUrlException(url: String, cause: URISyntaxException): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_URL, nls("curl.invalid.url", url), cause)
        }

        fun newInvalidMethodException(method: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_HTTP_METHOD, nls("curl.method.not.supported", method))
        }

        fun newNotSupportedOptionException(option: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.UNKNOWN_OPTION, nls("curl.unknown.option", option))
        }

        fun newNoRequiredOptionDataException(option: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INCOMPLETE_OPTION, nls("curl.incomplete.option", option))
        }

        fun newInvalidHeaderException(header: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_HEADER, nls("curl.invalid.header", header))
        }

        fun newInvalidFormDataException(formData: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_FORM_DATA, nls("curl.form.data.no.value", formData))
        }
    }

}