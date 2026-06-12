package org.javamaster.httpclient.utils

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.HttpUtils.getTabName

object DubboUtils {
    const val INTERFACE_KEY = "Interface"
    const val INTERFACE_NAME = "Interface-Name"
    const val METHOD_KEY = "Method"
    const val VERSION = "Version"
    const val REGISTRY = "Registry"

    fun findInterface(module: Module, name: String): PsiClass? {
        val javaPsiFacade = JavaPsiFacade.getInstance(module.project)
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

        return computeReadAction { javaPsiFacade.findClass(name, scope) }
    }

    fun findInterface(project: Project, name: String): PsiClass? {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)

        return computeReadAction { javaPsiFacade.findClass(name, scope) }
    }

    fun findDubboServiceMethod(psiElement: PsiElement): PsiMethod? {
        val project = psiElement.project
        val manager = InjectedLanguageManager.getInstance(project)

        val httpMessageBody = manager.getInjectionHost(psiElement) as HttpMessageBody? ?: return null

        val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)

        val headerField = httpRequest?.header?.headerFieldList
            ?.firstOrNull { it.headerFieldName.text.equals(INTERFACE_KEY, ignoreCase = true) }
            ?: return null

        val originalFile = getOriginalFile(httpRequest) ?: return null
        val module = ModuleUtilCore.findModuleForFile(originalFile, httpRequest.project) ?: return null
        val name = headerField.headerFieldValue?.text ?: return null
        val psiClass = findInterface(module, name) ?: return null

        val headerFieldMethod = httpRequest.header?.headerFieldList
            ?.firstOrNull { it.headerFieldName.text.equals(METHOD_KEY, ignoreCase = true) }
            ?: return null

        val value = headerFieldMethod.headerFieldValue?.text ?: return null
        val methods = psiClass.findMethodsByName(value, false)
        if (methods.isEmpty()) {
            return null
        }

        if (methods.size > 1) {
            println("Founded ${methods.size} methods of the same name, get the first one")
        }

        return methods[0]
    }

    fun getOriginalFile(httpRequest: HttpRequest): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(httpRequest)

        val project = httpRequest.project
        if (HttpUtils.isFileInHistoryDir(virtualFile, project)) {
            val tabName = getTabName(httpRequest.method)

            return HttpUtils.getOriginalFile(project, tabName)
        }

        return virtualFile
    }

    fun resolveTargetPsiElement(jsonString: JsonStringLiteral): PsiElement? {
        val virtualFile = PsiUtil.getVirtualFile(jsonString)

        val psiMethod = findDubboServiceMethod(jsonString) ?: return null

        val jsonPropertyNameLevels = HttpUtils.collectJsonPropertyNameLevels(jsonString)

        val paramPsiType: PsiType?
        if (virtualFile?.name?.endsWith("res.http") == true) {
            paramPsiType = psiMethod.returnType
        } else {
            val name = jsonPropertyNameLevels.pop()
            val psiParameter = psiMethod.parameterList.parameters.firstOrNull { parameter -> parameter.name == name }

            if (psiParameter == null) {
                return null
            }

            if (jsonPropertyNameLevels.isEmpty()) {
                return psiParameter
            }

            paramPsiType = psiParameter.type
        }

        val paramPsiCls = PsiTypeUtils.resolvePsiType(paramPsiType) ?: return null

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        return HttpUtils.resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
    }

    fun fillTargetDubboMethodParams(jsonString: JsonStringLiteral, result: CompletionResultSet, wrap: String): Boolean {
        val jsonProperty = jsonString.parent
        val parentJsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java)
        if (parentJsonProperty != null) {
            return false
        }

        val psiMethod = findDubboServiceMethod(jsonString) ?: return false

        return fillTargetDubboMethodParams(psiMethod, result, wrap)
    }

    fun fillTargetDubboMethodParams(psiMethod: PsiMethod, result: CompletionResultSet, wrap: String): Boolean {
        val prefixMatcher = result.prefixMatcher
        val prefix = prefixMatcher.prefix
        if (prefix.length < 2) {
            return false
        }

        val newPrefix = prefix.substring(1)
        val completionResultSet = result.withPrefixMatcher(newPrefix)

        psiMethod.parameterList.parameters
            .forEach {
                val typeText = it.type.presentableText

                val builder = LookupElementBuilder
                    .create(wrap + it.name + wrap)
                    .withTypeText(typeText, true)

                completionResultSet.addElement(builder)
            }

        return true
    }

    fun fillTargetDubboMethodParams(psiMethod: PsiMethod, result: CompletionResultSet) {
        psiMethod.parameterList.parameters
            .forEach {
                val typeText = it.type.presentableText

                val builder = LookupElementBuilder
                    .create("\"" + it.name + "\"")
                    .withTypeText(typeText, true)

                result.addElement(builder)
            }
    }

    fun getTargetPsiFieldClass(currentJsonString: JsonStringLiteral, fromEmpty: Boolean): PsiClass? {
        val jsonString = if (fromEmpty) {
            currentJsonString
        } else {
            val jsonProperty = PsiTreeUtil.getParentOfType(currentJsonString, JsonProperty::class.java)
            val parentJsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java) ?: return null

            PsiTreeUtil.getChildOfType(parentJsonProperty, JsonStringLiteral::class.java)!!
        }

        val targetPsiElement = resolveTargetPsiElement(jsonString) ?: return null

        val psiType = if (targetPsiElement is PsiParameter) {
            targetPsiElement.type
        } else {
            val field = targetPsiElement as PsiField
            field.type
        }

        val psiClass = PsiTypeUtils.resolvePsiType(psiType)

        val isCollection = InheritanceUtil.isInheritor(psiClass, "java.util.Collection")
        return if (isCollection) {
            val parameters = (psiType as PsiClassReferenceType).parameters
            if (parameters.size > 0) {
                PsiTypeUtils.resolvePsiType(parameters[0])
            } else {
                null
            }
        } else {
            psiClass
        }
    }

}