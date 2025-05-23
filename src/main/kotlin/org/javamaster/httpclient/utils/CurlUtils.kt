package org.javamaster.httpclient.utils


object CurlUtils {
    private val validRequestOptions = setOf("HEAD", "POST", "PUT", "GET", "DELETE", "PATCH", "OPTIONS")
    private val alwaysSetShortOptions = setOf("i", "v", "L")
    private val alwaysSetLongOptions = setOf("verbose", "include", "location", "compressed")
    private val knownLongOptions = setOf(
        "url",
        "request",
        "header",
        "user",
        "form",
        "data",
        "data-raw",
        "data-binary",
        "data-ascii",
        "data-urlencode"
    )
    private val knownShortOptions = setOf('X', 'H', 'u', 'F', 'd')

    fun isKnownLongOption(longOption: String): Boolean {
        return knownLongOptions.contains(longOption)
    }

    fun isKnownShortOption(shortOption: String): Boolean {
        return shortOption.isNotEmpty() && knownShortOptions.contains(shortOption[0])
    }

    fun isAlwaysSetLongOption(longOption: String): Boolean {
        return alwaysSetLongOptions.contains(longOption)
    }

    fun isAlwaysSetShortOption(shortOption: String): Boolean {
        return alwaysSetShortOptions.contains(shortOption)
    }

    fun isValidRequestOption(reqOption: String): Boolean {
        return validRequestOptions.contains(reqOption)
    }

    fun isLongOption(option: String): Boolean {
        return option.length > 2 && option.startsWith("--")
    }

    fun isShortOption(option: String): Boolean {
        return option.length > 1 && option.startsWith("-") && !isLongOption(option)
    }

    fun isCurlString(string: String): Boolean {
        val len = string.length
        val correctFirstWord = "curl"
        if (len < 5) {
            return false
        }

        var pos = 0
        while (pos < len && string[pos] <= ' ') {
            ++pos
        }

        var correctFirstWordIndex = 0

        while (pos < len && string[pos] == correctFirstWord[correctFirstWordIndex]) {
            ++pos
            ++correctFirstWordIndex
            if (correctFirstWordIndex == 4) {
                if (pos < len && string[pos] <= ' ') {
                    return true
                }
                break
            }
        }

        return false
    }

    fun createCurlStringComment(curlString: String): String {
        val str = curlString.trim { it <= ' ' }.replace("\r\n".toRegex(), "\n")

        return "%s\n".format(str)
    }
}