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

            // 再递归检查其父类
            if (hasControllerAnnotationInHierarchy(psiClass)) {
                return true
            }

            // 最后递归检查其实现的接口
            if (hasControllerAnnotationInInterfaces(psiClass)) {
                return true
            }
        }

        return false
    }

    /**
     * Spring MVC 风格的路径模式匹配
     * @param pattern 控制器定义的路径模式，如 /api/user/{id}
     * @param path 用户请求的实际路径，如 /api/user/123
     */
    fun matchPath(pattern: String, path: String): Boolean {
        val patternSegments = pattern.split("/")
        val pathSegments = path.split("/")

        // 段数必须相等（不考虑 ** 通配符的简化版本）
        if (patternSegments.size != pathSegments.size) {
            return false
        }

        for (i in patternSegments.indices) {
            val patternSegment = patternSegments[i]
            val pathSegment = pathSegments[i]

            // 路径变量匹配 {xxx} 或 {xxx:regex}
            if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                // 提取变量名和可选的正则约束
                val variableDef = patternSegment.substring(1, patternSegment.length - 1)
                val colonIndex = variableDef.indexOf(':')

                if (colonIndex > 0) {
                    // 有正则约束
                    val regex = variableDef.substring(colonIndex + 1)
                    if (!pathSegment.matches(regex.toRegex())) {
                        return false
                    }
                }
                // 无正则约束，直接匹配成功
                continue
            }

            // 普通路径段必须完全匹配
            if (patternSegment != pathSegment) {
                return false
            }
        }

        return true
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