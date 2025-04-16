package org.javamaster.httpclient.scan.support

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.enums.Control

/**
 * @author yudong
 */
class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    private val controllerAnnoSet = setOf(
        Control.RestController.qualifiedName,
        Control.Controller.qualifiedName,
    )

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiFile = event.file ?: return

        if (psiFile !is PsiJavaFile) {
            return
        }

        try {
            val code = event.code
            if (code == PsiTreeChangeEventImpl.PsiEventType.BEFORE_PROPERTY_CHANGE
                || code == PsiTreeChangeEventImpl.PsiEventType.PROPERTY_CHANGED
            ) {
                return
            }

            // 难以安全的判断改动的文件是否是 Controller 类, 先屏蔽后面的逻辑
            @Suppress("ConstantConditionIf")
            if (true) {
                ControllerPsiModificationTracker.myModificationCount.incModificationCount()
                return
            }

            val dumbService = psiFile.project.getService(DumbService::class.java)
            if (dumbService.isDumb) {
                return
            }

            if (!psiFile.isValid) {
                return
            }

            val psiClass = PsiTreeUtil.getStubChildOfType(psiFile, PsiClass::class.java) ?: return

            val notControllerCls = psiClass.annotations
                .none {
                    controllerAnnoSet.contains(it.qualifiedName)
                }

            if (notControllerCls) {
                return
            }

            ControllerPsiModificationTracker.myModificationCount.incModificationCount()
        } catch (e: Exception) {
            System.err.println(e.message)
        }

    }

}
