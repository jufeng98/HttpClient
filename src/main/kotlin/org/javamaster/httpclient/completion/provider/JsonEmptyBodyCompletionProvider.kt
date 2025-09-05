package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.utils.DubboUtils.fillTargetDubboMethodParams
import org.javamaster.httpclient.utils.DubboUtils.findDubboServiceMethod
import org.javamaster.httpclient.utils.DubboUtils.getTargetPsiFieldClass
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.resolveUrlControllerTargetPsiClass

/**
 * @author yudong
 */
class JsonEmptyBodyCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val psiElement = parameters.position

        var targetPsiClass = resolveUrlControllerTargetPsiClass(psiElement)
        if (targetPsiClass == null) {
            val jsonProperty = PsiTreeUtil.getParentOfType(psiElement, JsonProperty::class.java)
            val parentJsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java)

            if (parentJsonProperty != null) {
                val jsonString = PsiTreeUtil.getChildOfType(parentJsonProperty, JsonStringLiteral::class.java)!!
                val filled = fillTargetDubboMethodParams(jsonString, result, "\"")
                if (filled) {
                    return
                }

                targetPsiClass = getTargetPsiFieldClass(jsonString, true)
            } else {
                val dubboServiceMethod = findDubboServiceMethod(psiElement) ?: return

                fillTargetDubboMethodParams(dubboServiceMethod, result)

                return
            }
        }

        targetPsiClass ?: return

        val prefix = result.prefixMatcher.prefix
        if (prefix.contains("\"")) {
            return
        }

        targetPsiClass.fields
            .forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                val typeText = it.type.presentableText + " " + HttpUtils.getPsiFieldDesc(it)

                val builder = LookupElementBuilder
                    .create("\"" + it.name + "\"")
                    .withTypeText(typeText, true)

                result.addElement(builder)
            }
    }

}
