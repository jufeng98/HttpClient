package org.javamaster.httpclient.doc

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.doc.support.CoolRequestHelper
import org.javamaster.httpclient.reference.support.JsonControllerMethodFieldPsiElement
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils.collectJsonPropertyNameLevels
import org.javamaster.httpclient.utils.HttpUtils.generateAnno
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetField
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetParam
import org.javamaster.httpclient.utils.PsiUtils
import java.util.*

/**
 * 提示出入参的 json key 对应的 Controller 方法字段
 *
 * @author yudong
 */
class JsonKeyDocumentationProviderCoolRequest : DocumentationProvider {


    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is JsonControllerMethodFieldPsiElement) {
            return null
        }

        val module = element.module
        val searchTxt = element.searchTxt
        val jsonString = element.jsonString
        val virtualFile = jsonString.containingFile.virtualFile

        val psiMethod = CoolRequestHelper.findMethod(module, searchTxt) ?: return null

        val jsonPropertyNameLevels = collectJsonPropertyNameLevels(jsonString)
        if (jsonPropertyNameLevels.isEmpty()) {
            return null
        }

        val field = if (searchTxt.isBlank()) {
            resolveDubboField(jsonString, virtualFile, jsonPropertyNameLevels)
        } else {
            resolveControllerField(virtualFile, psiMethod, jsonPropertyNameLevels)
        } ?: return null

        val str = JavaDocumentationProvider.generateExternalJavadoc(field, null)

        val annotation = field.getAnnotation("io.swagger.annotations.ApiModelProperty")

        return if (annotation != null) {
            val generateAnno = generateAnno(annotation)
            str + generateAnno
        } else {
            str
        }
    }

    private fun resolveDubboField(
        jsonString: JsonStringLiteral,
        virtualFile: VirtualFile?,
        jsonPropertyNameLevels: LinkedList<String>,
    ): PsiField? {
        val serviceMethod = DubboUtils.findDubboServiceMethod(jsonString) ?: return null

        val paramPsiType: PsiType?

        if (virtualFile?.name?.endsWith("res.http") == true) {
            paramPsiType = serviceMethod.returnType
        } else {
            val name = jsonPropertyNameLevels.pop()
            val psiParameter =
                serviceMethod.parameterList.parameters.firstOrNull { parameter -> parameter.name == name }

            if (psiParameter == null) {
                return null
            }

            if (jsonPropertyNameLevels.isEmpty()) {
                return null
            }

            paramPsiType = psiParameter.type
        }

        val paramPsiCls = PsiUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        return resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
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
