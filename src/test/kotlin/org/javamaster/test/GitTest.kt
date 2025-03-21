package org.javamaster.test

import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class GitTest {

    @Test
    fun push() {
        val file = File("")
        println(file.absolutePath)

        execCommand("cmd /c git push")
    }

    @Test
    fun pull() {
        val file = File("")
        println(file.absolutePath)

        execCommand("cmd /c git pull")
    }

    private fun execCommand(command: String) {
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()

        val res = getRes(process)
        println("result:$res")

        if (res.contains("fatal")) {
            execCommand(command)
        }
    }

    private fun getRes(process: Process): String {
        val stringBuilder = StringBuilder()

        BufferedReader(InputStreamReader(process.errorStream)).use {
            var line = it.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = it.readLine()
            }
            return stringBuilder.toString()
        }
    }
}
