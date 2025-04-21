package org.javamaster.httpclient.curl.exception

import org.javamaster.httpclient.nls.NlsBundle.nls


class CurlParseException(val type: CurlParseErrorType, message: String) : RuntimeException(message) {
    companion object {
        fun newNotCurlException(): CurlParseException {
            return CurlParseException(CurlParseErrorType.NOT_CURL, nls("curl.is.not.curl"))
        }

        fun newNoUrlException(): CurlParseException {
            return CurlParseException(CurlParseErrorType.NO_URL, nls("curl.no.url"))
        }

        fun newInvalidUrlException(url: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_URL, nls("curl.invalid.url", url))
        }

        fun newInvalidMethodException(method: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_HTTP_METHOD, nls("curl.method.not.supported", method))
        }

        fun newNotSupportedOptionException(option: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.UNKNOWN_OPTION, nls("curl.unknown.option", option))
        }

        fun newNoRequiredOptionDataException(option: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INCOMPLETE_OPTION, nls("curl.no.data", option))
        }

        fun newInvalidHeaderException(header: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_HEADER, nls("curl.invalid.header", header))
        }

        fun newInvalidPathException(formData: String): CurlParseException {
            return CurlParseException(
                CurlParseErrorType.INVALID_FORM_DATA,
                nls("curl.custom.path.in.form.data", formData)
            )
        }

        fun newInvalidFormDataException(formData: String): CurlParseException {
            return CurlParseException(CurlParseErrorType.INVALID_FORM_DATA, nls("curl.form.data.no.value", formData))
        }
    }
}