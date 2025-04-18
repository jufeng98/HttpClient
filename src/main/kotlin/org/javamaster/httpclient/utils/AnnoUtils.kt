package org.javamaster.httpclient.utils

import com.intellij.lang.jvm.annotation.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiMethod

object AnnoUtils {
    private val javaMetaAnnoSet = setOf(
        "java.lang.annotation.Target",
        "java.lang.annotation.Documented",
        "java.lang.annotation.Retention",
    )

    fun getAttributeValue(attributeValue: JvmAnnotationAttributeValue?): Any? {
        if (attributeValue == null) {
            return null
        }

        when (attributeValue) {
            is JvmAnnotationConstantValue -> {
                return attributeValue.constantValue
            }

            is JvmAnnotationEnumFieldValue -> {
                return attributeValue.fieldName
            }

            is JvmAnnotationArrayValue -> {
                val values = attributeValue.values
                val list: MutableList<Any> = ArrayList(values.size)
                for (value in values) {
                    val res = getAttributeValue(value)
                    if (res != null) {
                        list.add(res)
                    } else {
                        // 如果是jar包里的JvmAnnotationConstantValue则无法正常获取值
                        try {
                            val clazz: Class<out JvmAnnotationAttributeValue> = value.javaClass

                            val myElement = clazz.superclass.getDeclaredField("myElement")
                            myElement.isAccessible = true

                            val elObj = myElement[value]

                            if (elObj is PsiExpression) {
                                list.add(elObj.text)
                            }
                        } catch (e: Exception) {
                            System.err.println(e::class.java.simpleName)
                        }
                    }
                }
                return list
            }

            is JvmAnnotationClassValue -> {
                return attributeValue.qualifiedName
            }

            else -> return null
        }
    }

    fun getClassAnnotation(psiClass: PsiClass, vararg annoNames: String): PsiAnnotation? {
        if (annoNames.isEmpty()) {
            return null
        }

        var annotation: PsiAnnotation?
        for (name in annoNames) {
            annotation = psiClass.getAnnotation(name)
            if (annotation != null) {
                return annotation
            }
        }

        val classes: MutableList<PsiClass?> = mutableListOf()

        classes.add(psiClass.superClass)

        classes.addAll(psiClass.interfaces)

        for (superPsiClass in classes) {
            if (superPsiClass == null) {
                continue
            }

            val classAnnotation = getClassAnnotation(superPsiClass, *annoNames)
            if (classAnnotation != null) {
                return classAnnotation
            }
        }

        return null
    }

    fun collectMethodAnnotations(psiMethod: PsiMethod): List<PsiAnnotation> {
        val annotations: MutableSet<PsiAnnotation> = mutableSetOf()

        annotations.addAll(psiMethod.modifierList.annotations)

        for (superMethod in psiMethod.findSuperMethods()) {
            annotations.addAll(collectMethodAnnotations(superMethod))
        }

        return annotations.toList()
    }

    fun getQualifiedAnnotation(psiAnnotation: PsiAnnotation?, qualifiedName: String): PsiAnnotation? {
        val annotationQualifiedName = psiAnnotation?.qualifiedName ?: return null

        if (qualifiedName == annotationQualifiedName) {
            return psiAnnotation
        }

        if (javaMetaAnnoSet.contains(annotationQualifiedName)) {
            return null
        }

        val element = psiAnnotation.nameReferenceElement ?: return null

        val resolve = element.resolve() as? PsiClass ?: return null

        if (!resolve.isAnnotationType) {
            return null
        }

        val annotation = resolve.getAnnotation(qualifiedName)
        if (annotation != null && qualifiedName == annotation.qualifiedName) {
            return annotation
        }

        for (classAnnotation in resolve.annotations) {
            val qualifiedAnnotation = getQualifiedAnnotation(classAnnotation, qualifiedName)

            if (qualifiedAnnotation != null) {
                return qualifiedAnnotation
            }
        }

        return null
    }

    fun findAnnotationValue(attribute: JvmAnnotationAttribute, vararg attrNames: String): Any? {
        if (attrNames.isEmpty()) {
            return null
        }

        val attributeName = attribute.attributeName
        var matchAttrName = false
        for (attrName in attrNames) {
            if (attributeName == attrName) {
                matchAttrName = true
                break
            }
        }

        if (!matchAttrName) {
            return null
        }

        val attributeValue = attribute.attributeValue

        return findAttributeValue(attributeValue)
    }

    private fun findAttributeValue(attributeValue: JvmAnnotationAttributeValue?): Any? {
        if (attributeValue == null) {
            return null
        }

        when (attributeValue) {
            is JvmAnnotationConstantValue -> {
                val constantValue = attributeValue.constantValue
                return constantValue?.toString()
            }

            is JvmAnnotationEnumFieldValue -> {
                return attributeValue.fieldName
            }

            is JvmAnnotationArrayValue -> {
                val values: MutableList<String?> = mutableListOf()
                for (value in attributeValue.values) {
                    values.add(findAttributeValue(value) as String?)
                }
                return values
            }

            else -> return null
        }
    }

}
