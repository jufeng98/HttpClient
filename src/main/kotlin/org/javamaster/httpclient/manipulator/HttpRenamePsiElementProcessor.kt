package org.javamaster.httpclient.manipulator

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import org.javamaster.httpclient.psi.HttpGlobalVariableName

/**
 * @author yudong
 */
class HttpRenamePsiElementProcessor : RenamePsiElementProcessor() {

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is HttpGlobalVariableName
    }

}
