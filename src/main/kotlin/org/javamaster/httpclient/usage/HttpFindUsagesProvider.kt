package org.javamaster.httpclient.usage

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
class HttpFindUsagesProvider : FindUsagesProvider {

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is HttpVariable || psiElement is HttpGlobalVariable
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return if (element is HttpVariable) {
            if (element.variableName?.isBuiltin == true) {
                "Builtin variable"
            } else {
                "Variable"
            }
        } else if (element is HttpGlobalVariable) {
            "Global variable"
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is HttpVariable -> {
                StringUtil.notNullize(element.variableName?.text)
            }

            is HttpGlobalVariable -> {
                StringUtil.notNullize(element.text)
            }

            else -> {
                ""
            }
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return when (element) {
            is HttpVariable -> {
                StringUtil.notNullize(element.variableName?.text)
            }

            is HttpGlobalVariable -> {
                element.globalVariableName.name
            }

            else -> {
                ""
            }
        }
    }
}
