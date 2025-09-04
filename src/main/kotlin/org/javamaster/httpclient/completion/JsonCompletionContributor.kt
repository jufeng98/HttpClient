package org.javamaster.httpclient.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import org.javamaster.httpclient.completion.provider.JsonKeyCompletionProvider

class JsonCompletionContributor : CompletionContributor() {

    init {
        this.extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(
                JsonStringLiteral::class.java
            ),
            JsonKeyCompletionProvider()
        )
    }

}
