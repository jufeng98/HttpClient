package org.javamaster.httpclient.reference.support

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.utils.HttpUtils

class HttpFilePathPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val httpFilePath = element as HttpFilePath
        val rangeInElement = textRange.shiftLeft(textRange.startOffset)

        val psiReference = object : PsiReferenceBase<HttpFilePath>(httpFilePath, rangeInElement) {

            override fun resolve(): PsiElement? {
                val parentPath = element.containingFile.virtualFile.parent.path

                return HttpUtils.resolveFilePath(element.text, parentPath, element.project)
            }

        }

        return arrayOf(psiReference)
    }

}
