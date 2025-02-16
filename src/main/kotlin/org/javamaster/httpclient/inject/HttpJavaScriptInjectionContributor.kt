//package org.javamaster.httpclient.inject
//
//import com.intellij.httpClient.http.request.psi.HttpMessageBody
//import com.intellij.httpClient.http.request.psi.HttpRequestMessagesGroup
//import com.intellij.httpClient.http.request.psi.HttpScriptBody
//import com.intellij.lang.Language
//import com.intellij.lang.injection.MultiHostInjector
//import com.intellij.lang.injection.MultiHostRegistrar
//import com.intellij.lang.javascript.JavaScriptSupportLoader
//import com.intellij.openapi.util.TextRange
//import com.intellij.psi.PsiElement
//import com.intellij.psi.PsiLanguageInjectionHost
//import org.javamaster.httpclient.psi.HttpMessageBody
//import org.javamaster.httpclient.psi.HttpRequestMessagesGroup
//import org.javamaster.httpclient.psi.HttpScriptBody
//import org.javamaster.httpclient.utils.HttpUtils
//
///**
// * 根据元素类型注入相应的语言
// *
// * @author yudong
// */
//class HttpJavaScriptInjectionContributor : MultiHostInjector {
//
//    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
//        var language: Language? = null
//        var textRange: TextRange? = null
//        if (context is HttpMessageBody) {
//            val parent = context.parent
//            if (parent is HttpRequestMessagesGroup) {
//                if (parent.firstChild.text == HttpUtils.SCRIPT_INPUT_SIGN) {
//                    language = JavaScriptSupportLoader.JAVASCRIPT.language
//                    val range = innerRangeScript(context)
//                    if (range != null) {
//                        textRange = TextRange(range.startOffset, range.endOffset - 2)
//                    }
//                }
//            }
//        } else if (context is HttpScriptBody) {
//            language = JavaScriptSupportLoader.JAVASCRIPT.language
//            textRange = innerRangeScript(context)
//        }
//
//        if (language == null || textRange == null) {
//            return
//        }
//
//        registrar.startInjecting(language)
//        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
//        registrar.doneInjecting()
//    }
//
//    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
//        return mutableListOf(HttpMessageBody::class.java, HttpScriptBody::class.java)
//    }
//
//    private fun innerRangeScript(context: PsiElement): TextRange? {
//        var textRange = context.textRange
//        textRange = textRange.shiftLeft(textRange.startOffset)
//        if (textRange.endOffset == 0) {
//            return null
//        }
//
//        val textRangeTmp = TextRange(textRange.startOffset, textRange.endOffset)
//        if (textRangeTmp.endOffset == 0) {
//            return null
//        }
//
//        return textRangeTmp
//    }
//}
