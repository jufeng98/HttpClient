package org.javamaster.httpclient.reference.support

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpVariableArg
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpVariableArgPsiReference(variableArg: HttpVariableArg, val textRange: TextRange) :
    PsiReferenceBase<HttpVariableArg>(variableArg, textRange) {

    override fun resolve(): PsiElement? {
        val guessPath = element.value.toString()

        val httpFile = element.containingFile
        val project = httpFile.project
        val httpFileParentPath = httpFile.virtualFile?.parent?.path ?: return null

        return tryResolvePath(guessPath, httpFileParentPath, project)
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

    companion object {

        fun tryResolvePath(guessPath: String, httpFileParentPath: String, project: Project): PsiElement? {
            try {
                return HttpUtils.resolveFilePath(guessPath, httpFileParentPath, project)
            } catch (e: Exception) {
                System.err.println(e.message)
                return null
            }
        }

    }
}