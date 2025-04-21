package org.javamaster.test

import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern

class GitTest {

    @Test
    fun push() {
        val file = File("")
        println(file.absolutePath)

        val proxyAddress = getProxyAddress()

        execCommand("cmd", "/c", "git config http.proxy $proxyAddress")

        execCommand("cmd", "/c", "git push")
    }

    @Test
    fun pull() {
        val file = File("")
        println(file.absolutePath)

        val proxyAddress = getProxyAddress()

        execCommand("cmd", "/c", "git config http.proxy $proxyAddress")

        execCommand("cmd", "/c", "git pull")
    }

    private fun getProxyAddress(): String {
        val regKey = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings"
        val res = execCommand("reg", "query", regKey)

        val split = res.split("\r\n")
        for (s in split) {
            if (s.contains("ProxyServer")) {
                val strings = s.split(Pattern.compile("\\s+"))
                return strings.last()
            }
        }

        throw IllegalArgumentException()
    }

    private fun execCommand(vararg command: String): String {
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()

        val res = getRes(process)
        println("result:$res")

        if (res.contains("fatal")) {
            return execCommand(*command)
        }

        return res
    }

    private fun getRes(process: Process): String {
        val stringBuilder = StringBuilder()

        BufferedReader(InputStreamReader(process.inputStream))
            .use {
                var line = it.readLine()
                while (line != null) {
                    stringBuilder.append(line + "\r\n")
                    line = it.readLine()
                }
            }

        BufferedReader(InputStreamReader(process.errorStream))
            .use {
                var line = it.readLine()
                while (line != null) {
                    stringBuilder.append(line + "\r\n")
                    line = it.readLine()
                }
                return stringBuilder.toString()
            }
    }
}
