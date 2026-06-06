package org.javamaster.httpclient.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import org.javamaster.httpclient.enums.Control
import org.javamaster.httpclient.enums.SpringHttpMethod

/**
 * @author yudong
 */
object SpringUtils {
    private val controllerAnnoSet = setOf(
        Control.RestController.qualifiedName,
        Control.Controller.qualifiedName,
        SpringHttpMethod.REQUEST_MAPPING.qualifiedName
    )

    fun isSpringController(javaFile: PsiJavaFile): Boolean {
        for (psiClass in javaFile.classes) {
            // 先检查当前类
            if (hasControllerAnnotationInClass(psiClass)) {
                return true
            }

            // 再递归检测其父类
            if (hasControllerAnnotationInHierarchy(psiClass)) {
                return true
            }

            // 最后递归检测其实现的接口
            if (hasControllerAnnotationInInterfaces(psiClass)) {
                return true
            }
        }

        return false
    }

    private fun hasControllerAnnotationInClass(psiClass: PsiClass): Boolean {
        for (annotation in psiClass.annotations) {
            val qualifiedName = annotation.qualifiedName ?: continue

            if (controllerAnnoSet.contains(qualifiedName)) {
                return true
            }
        }

        return false
    }

    private fun hasControllerAnnotationInHierarchy(psiClass: PsiClass): Boolean {
        val superClass = psiClass.superClass ?: return false

        if (hasControllerAnnotationInClass(superClass)) {
            return true
        }

        return hasControllerAnnotationInHierarchy(superClass)
    }

    private fun hasControllerAnnotationInInterfaces(psiClass: PsiClass): Boolean {
        for (psiInterface in psiClass.interfaces) {
            if (hasControllerAnnotationInClass(psiInterface)) {
                return true
            }

            if (hasControllerAnnotationInInterfaces(psiInterface)) {
                return true
            }
        }
        return false
    }
}