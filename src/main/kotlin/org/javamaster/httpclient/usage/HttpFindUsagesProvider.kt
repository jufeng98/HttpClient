package org.javamaster.httpclient.usage

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpGlobalVariableName
import org.javamaster.httpclient.psi.HttpVariableName

/**
 * @author yudong
 */
class HttpFindUsagesProvider : FindUsagesProvider {

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
         return psiElement is PsiNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return if (element is HttpVariableName) {
            if (element.isBuiltin) {
                "Builtin variable"
            } else {
                "Variable"
            }
        } else if (element is HttpGlobalVariableName) {
            "Global variable"
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is HttpVariableName -> {
                StringUtil.notNullize(element.text)
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
            is HttpVariableName -> {
                StringUtil.notNullize(element.text)
            }

            is HttpGlobalVariableName -> {
                element.name
            }

            else -> {
                ""
            }
        }
    }
}
