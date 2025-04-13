package org.javamaster.test

import com.intellij.lang.LanguageParserDefinitions
import com.intellij.mock.MockApplication
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.impl.ProgressManagerImpl
import com.intellij.openapi.util.Disposer
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.parser.HttpAdapter
import org.javamaster.httpclient.parser.HttpParserDefinition
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement.Companion.parse
import org.javamaster.httpclient.utils.StreamUtils
import org.junit.Test
import java.nio.charset.StandardCharsets

class LexerTest {

    @Test
    fun testLexer() {
        val stream = checkNotNull(javaClass.classLoader.getResourceAsStream("test.http"))
        val bytes = StreamUtils.copyToByteArray(stream)
        val str = String(bytes, StandardCharsets.UTF_8)

        val httpAdapter = HttpAdapter()
        httpAdapter.start(str)

        while (true) {
            val tokenType = httpAdapter.tokenType
            println(tokenType)

            httpAdapter.advance()
            if (tokenType == null) {
                break
            }
        }
    }

    private fun initApplication() {
        val application: MockApplication = object : MockApplication(Disposer.newDisposable()) {
            init {
                registerService(
                    ProgressManager::class.java,
                    ProgressManagerImpl::class.java
                )
            }
        }
        @Suppress("UnstableApiUsage")
        ApplicationManager.setApplication(application)
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(HttpLanguage.INSTANCE, HttpParserDefinition())
    }

    @Test
    fun testLexer1() {
        initApplication()

        var str = "this is a {{\$requestName}} and {{age}} good."

        var element = parse(str)
        println(element.variableList)

        str = "{{requestName}}"
        element = parse(str)
        println(element.variableList)
    }
}
