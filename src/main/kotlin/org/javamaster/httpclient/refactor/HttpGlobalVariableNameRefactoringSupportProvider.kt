package org.javamaster.httpclient.refactor

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.psi.HttpGlobalVariableName


class HttpGlobalVariableNameRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
        return element is HttpGlobalVariableName
    }

}
