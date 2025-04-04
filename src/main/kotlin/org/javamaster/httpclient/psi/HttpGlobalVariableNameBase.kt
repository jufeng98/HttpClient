package org.javamaster.httpclient.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.javamaster.httpclient.HttpPsiFactory

/**
 * @author yudong
 */
open class HttpGlobalVariableNameBase(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {

    override fun setName(name: String): PsiElement {
        val globalVariable = HttpPsiFactory.createGlobalVariable(project, "@$name =")
        return replace(globalVariable.globalVariableName)
    }

}
