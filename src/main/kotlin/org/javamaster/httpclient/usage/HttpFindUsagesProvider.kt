package org.javamaster.httpclient.usage

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpFileVariableName
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
                NlsBundle.nls("builtin.variable")
            } else {
                NlsBundle.nls("variable")
            }
        } else if (element is HttpFileVariableName) {
            NlsBundle.nls("file.variable")
        } else {
            ""
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is HttpVariableName -> {
                StringUtil.notNullize(element.text)
            }

            is HttpFileVariableName -> {
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

            is HttpFileVariableName -> {
                element.name
            }

            else -> {
                ""
            }
        }
    }
}
