package org.javamaster.httpclient.curl.support

import org.javamaster.httpclient.curl.enums.QuotesOutside

object CurlTokenizer {

    fun splitInCurlTokens(curlString: String): List<String> {
        val tokensList: MutableList<String> = mutableListOf()
        val curlStringLength = curlString.length
        var quotesOutside = QuotesOutside.NONE
        val curToken = StringBuilder()

        var i = 0
        while (i < curlStringLength) {
            when (val c = curlString[i]) {
                '"' -> if (quotesOutside == QuotesOutside.SINGLE) {
                    curToken.append(c)
                } else if (quotesOutside == QuotesOutside.DOUBLE && curToken.isEmpty()) {
                    tokensList.add("")
                } else {
                    addTokenToList(tokensList, curToken)
                    quotesOutside = updateQuotesState(quotesOutside, '"')
                }

                '\'' -> if (quotesOutside == QuotesOutside.DOUBLE) {
                    curToken.append(c)
                } else if (quotesOutside == QuotesOutside.SINGLE && curToken.isEmpty()) {
                    tokensList.add("")
                } else {
                    addTokenToList(tokensList, curToken)
                    quotesOutside = updateQuotesState(quotesOutside, '\'')
                }

                '\\' -> if (curlStringLength <= i + 1 || curlString[i + 1] != '"' && curlString[i + 1] != '\'') {
                    curToken.append("\\")
                } else {
                    curToken.append(curlString[i + 1])
                    ++i
                }

                else -> if (quotesOutside != QuotesOutside.NONE) {
                    curToken.append(c)
                } else if (Character.isWhitespace(c)) {
                    addTokenToList(tokensList, curToken)
                } else {
                    curToken.append(c)
                }
            }

            ++i
        }

        addTokenToList(tokensList, curToken)

        return tokensList
    }

    private fun updateQuotesState(curQuotesState: QuotesOutside, curCharacter: Char): QuotesOutside {
        if (curQuotesState == QuotesOutside.NONE) {
            if (curCharacter == '"') {
                return QuotesOutside.DOUBLE
            }

            if (curCharacter == '\'') {
                return QuotesOutside.SINGLE
            }
        }

        return QuotesOutside.NONE
    }

    private fun addTokenToList(tokensList: MutableList<String>, token: StringBuilder) {
        if (token.isNotEmpty()) {
            tokensList.add(token.toString())
            token.setLength(0)
        }
    }
}
