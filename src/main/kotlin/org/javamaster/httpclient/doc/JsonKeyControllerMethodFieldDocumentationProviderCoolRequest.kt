package org.javamaster.httpclient.doc

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.doc.support.CoolRequestHelper
import org.javamaster.httpclient.reference.support.JsonControllerMethodFieldPsiElement
import org.javamaster.httpclient.utils.HttpUtils.collectJsonPropertyNameLevels
import org.javamaster.httpclient.utils.HttpUtils.generateAnno
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetField
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetParam
import org.javamaster.httpclient.utils.PsiUtils
import java.util.*

/**
 * 悬浮提示出入参的 json key 对应的 Controller 方法字段
 *
 * @author yudong
 */
class JsonKeyControllerMethodFieldDocumentationProviderCoolRequest : DocumentationProvider {


    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is JsonControllerMethodFieldPsiElement) {
            return null
        }

        val searchTxt = element.searchTxt
        if (searchTxt.isBlank()) {
            return null
        }

        val jsonString = element.jsonString

        val psiMethod = CoolRequestHelper.findMethod(element.module, searchTxt) ?: return null

        val jsonPropertyNameLevels = collectJsonPropertyNameLevels(jsonString)
        if (jsonPropertyNameLevels.isEmpty()) {
            return null
        }

        val virtualFile = jsonString.containingFile.virtualFile

        val field = resolveControllerField(virtualFile, psiMethod, jsonPropertyNameLevels) ?: return null

        val str = JavaDocumentationProvider.generateExternalJavadoc(field, null)

        val annotation = field.getAnnotation("io.swagger.annotations.ApiModelProperty")

        return if (annotation != null) {
            val generateAnno = generateAnno(annotation)
            str + generateAnno
        } else {
            str
        }
    }


    private fun resolveControllerField(
        virtualFile: VirtualFile?,
        psiMethod: PsiMethod,
        jsonPropertyNameLevels: LinkedList<String>,
    ): PsiField? {
        val paramPsiType: PsiType?

        if (virtualFile?.name?.endsWith("res.http") == true) {
            paramPsiType = psiMethod.returnType
        } else {
            val psiParameter = resolveTargetParam(psiMethod)

            paramPsiType = psiParameter?.type
        }

        val paramPsiCls: PsiClass = PsiUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        return resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
    }

}
