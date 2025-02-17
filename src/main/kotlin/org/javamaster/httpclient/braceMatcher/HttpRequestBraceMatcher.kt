package org.javamaster.httpclient.braceMatcher

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.psi.HttpTypes

class HttpRequestBraceMatcher : PairedBraceMatcherAdapter(MyPairedBraceMatcher(), HttpLanguage.INSTANCE) {

    private class MyPairedBraceMatcher : PairedBraceMatcher {
        private val pairs = arrayOf(
            BracePair(HttpTypes.START_VARIABLE_BRACE, HttpTypes.END_VARIABLE_BRACE, true),
            BracePair(HttpTypes.OUT_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true),
            BracePair(HttpTypes.GLOBAL_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true),
            BracePair(HttpTypes.IN_START_SCRIPT_BRACE, HttpTypes.END_SCRIPT_BRACE, true)
        )

        override fun getPairs(): Array<BracePair> {
            return pairs
        }

        override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
            return true
        }

        override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
            return openingBraceOffset
        }
    }

}