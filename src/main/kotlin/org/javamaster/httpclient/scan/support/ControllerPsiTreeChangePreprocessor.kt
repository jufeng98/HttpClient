package org.javamaster.httpclient.scan.support

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author yudong
 */
class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    private val controllerAnnoSet = setOf(
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.stereotype.Controller",
    )

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiFile = event.file ?: return

        if (psiFile !is PsiJavaFile) {
            return
        }

        val psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass::class.java) ?: return

        val notControllerCls = psiClass.annotations
            .none {
                controllerAnnoSet.contains(it.qualifiedName)
            }

        if (notControllerCls) {
            return
        }

        ControllerPsiModificationTracker.myModificationCount.incModificationCount()
    }

}
