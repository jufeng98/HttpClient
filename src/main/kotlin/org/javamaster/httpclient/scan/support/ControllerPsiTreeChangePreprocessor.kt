package org.javamaster.httpclient.scan.support

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.util.application
import org.javamaster.httpclient.enums.Control
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ControllerPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    private val controllerAnnoSet = setOf(
        Control.RestController.qualifiedName,
        Control.Controller.qualifiedName,
    )

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        val psiFile = event.file ?: return

        if (psiFile !is PsiJavaFile) {
            return
        }

        val code = event.code
        if (code == PsiTreeChangeEventImpl.PsiEventType.BEFORE_PROPERTY_CHANGE
            || code == PsiTreeChangeEventImpl.PsiEventType.PROPERTY_CHANGED
        ) {
            return
        }

        scheduledFuture?.cancel(false)

        scheduledFuture = executor.schedule({
            scheduleControllerCheck(psiFile)
        }, 1, TimeUnit.SECONDS)
    }

    private fun scheduleControllerCheck(psiFile: PsiJavaFile) {
        val project = psiFile.project
        val dumbService = DumbService.getInstance(project)

        dumbService.runWhenSmart {
            application.executeOnPooledThread {
                runReadAction {
                    try {
                        if (!psiFile.isValid) {
                            return@runReadAction
                        }

                        if (isSpringController(psiFile)) {
                            ControllerPsiModificationTracker.myModificationCount.incModificationCount()
                        }
                    } catch (t: Throwable) {
                        System.err.println(t.message)
                    }
                }
            }
        }
    }

    private fun isSpringController(psiFile: PsiJavaFile): Boolean {
        for (psiClass in psiFile.classes) {
            if (checkClassForController(psiClass)) {
                return true
            }
        }
        return false
    }

    private fun checkClassForController(psiClass: PsiClass): Boolean {
        if (hasControllerAnnotationDirectly(psiClass)) {
            return true
        }

        if (hasControllerAnnotationInHierarchy(psiClass, 0)) {
            return true
        }

        return false
    }

    private fun hasControllerAnnotationDirectly(psiClass: PsiClass): Boolean {
        for (annotation in psiClass.annotations) {
            val qualifiedName = annotation.qualifiedName ?: continue

            if (controllerAnnoSet.contains(qualifiedName)) {
                return true
            }
        }

        return false
    }

    private fun hasControllerAnnotationInHierarchy(psiClass: PsiClass, depth: Int): Boolean {
        if (depth >= 3) {
            return false
        }

        val superClass = psiClass.superClass ?: return false

        if (hasControllerAnnotationDirectly(superClass)) {
            return true
        }

        return hasControllerAnnotationInHierarchy(superClass, depth + 1)
    }

}