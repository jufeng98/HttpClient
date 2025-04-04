package org.javamaster.httpclient.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.javamaster.httpclient.HttpPsiFactory

/**
 * @author yudong
 */
open class HttpGlobalVariableNameBase(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {

    override fun setName(name: String): PsiElement {
        val globalVariableName = HttpPsiFactory.createGlobalVariableName(project, "@$name =")
        return replace(globalVariableName)
    }

    override fun getNameIdentifier(): PsiElement? {
        return HttpPsiUtils.getNextSiblingByType(this.firstChild, HttpTypes.GLOBAL_NAME, false)!!
    }

}
