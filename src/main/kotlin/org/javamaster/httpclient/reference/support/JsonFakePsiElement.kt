package org.javamaster.httpclient.reference.support

import com.cool.request.view.tool.search.ApiAbstractGotoSEContributor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.createProcessIndicator
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.findControllerPsiMethods
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.getControllerNavigationItem
import org.javamaster.httpclient.reference.support.HttpFakePsiElement.Companion.showTip
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.PsiUtils
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
class JsonFakePsiElement(
    private val jsonString: JsonStringLiteral,
    private val searchTxt: String,
    private val module: Module,
) :
    ASTWrapperPsiElement(jsonString.node) {

    override fun getPresentation(): ItemPresentation {
        return JsonItemPresentation
    }

    override fun navigate(requestFocus: Boolean) {
        val virtualFile = jsonString.containingFile.virtualFile

        val jsonPropertyNameLevels = collectJsonPropertyNameLevels(jsonString)
        if (jsonPropertyNameLevels.isEmpty()) {
            return
        }

        if (searchTxt.isBlank()) {
            jumpToDubbo(virtualFile, jsonPropertyNameLevels)
            return
        }

        jumpToControllerAsync(virtualFile, jsonPropertyNameLevels)
    }

    private fun jumpToDubbo(
        virtualFile: VirtualFile?,
        jsonPropertyNameLevels: LinkedList<String>,
    ) {
        val serviceMethod = DubboUtils.findDubboServiceMethod(jsonString)
        if (serviceMethod == null) {
            showTip("Tip:未能解析到对应的Dubbo方法,无法跳转", project)
            return
        }

        val paramPsiType: PsiType?

        if (virtualFile?.name?.endsWith("res.http") == true) {
            paramPsiType = serviceMethod.returnType
        } else {
            val name = jsonPropertyNameLevels.pop()
            val psiParameter = serviceMethod.parameterList.parameters
                .firstOrNull { it.name == name }

            if (psiParameter == null) {
                showTip("Tip:未能解析到对应的方法参数,无法跳转", project)
                return
            }

            if (jsonPropertyNameLevels.isEmpty()) {
                psiParameter.navigate(true)
                return
            }

            paramPsiType = psiParameter.type
        }

        val paramPsiCls = PsiUtils.resolvePsiType(paramPsiType) ?: return

        val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

        val targetField = resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
        if (targetField == null) {
            showTip("Tip:未能解析对应的Bean属性,无法跳转", project)
            return
        }

        targetField.navigate(true)

    }

    private fun jumpToControllerAsync(
        virtualFile: VirtualFile?,
        jsonPropertyNameLevels: LinkedList<String>,
    ) {
        val processIndicator = createProcessIndicator("Tip:正在尝试跳转到对应的Bean字段...", project)
        Disposer.register(Disposer.newDisposable(), processIndicator)

        val event = HttpFakePsiElement.createEvent()
        val seContributor = ApiAbstractGotoSEContributor(event)

        CompletableFuture.runAsync {
            val list = seContributor.search(searchTxt, processIndicator)
            if (processIndicator.isCanceled) {
                processIndicator.processFinish()
                return@runAsync
            }

            processIndicator.processFinish()

            runInEdt {
                if (list.isEmpty()) {
                    showTip("Tip:未能解析到对应的controller mapping,无法跳转", project)
                    return@runInEdt
                }

                val controllerNavigationItem = getControllerNavigationItem(list, searchTxt)

                runReadAction {
                    val psiMethods = findControllerPsiMethods(controllerNavigationItem, module)
                    if (psiMethods.isEmpty()) {
                        showTip("Tip:未能解析对应的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    if (psiMethods.size > 1) {
                        showTip("Tip:解析到${psiMethods.size}个的controller方法,无法跳转", project)
                        return@runReadAction
                    }

                    val psiMethod = psiMethods[0]
                    val paramPsiType: PsiType?

                    if (virtualFile?.name?.endsWith("res.http") == true) {
                        paramPsiType = psiMethod.returnType
                    } else {
                        val psiParameter = resolveTargetParam(psiMethod)

                        paramPsiType = psiParameter?.type
                    }

                    val paramPsiCls: PsiClass = PsiUtils.resolvePsiType(paramPsiType) ?: return@runReadAction

                    val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

                    val targetField = resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
                    if (targetField == null) {
                        showTip("Tip:未能解析对应的Bean属性,无法跳转", project)
                        return@runReadAction
                    }

                    targetField.navigate(true)
                }
            }
        }
    }

    private fun resolveTargetParam(psiMethod: PsiMethod): PsiParameter? {
        val superPsiMethods = psiMethod.findSuperMethods(false)
        val psiParameters = psiMethod.parameterList.parameters
        var psiParameter: PsiParameter? = null
        val requestBodyAnnoName = "org.springframework.web.bind.annotation.RequestBody"

        for ((index, psiParam) in psiParameters.withIndex()) {
            var hasAnno = psiParam.hasAnnotation(requestBodyAnnoName)
            if (hasAnno) {
                psiParameter = psiParam
                break
            }

            for (superPsiMethod in superPsiMethods) {
                val superPsiParam = superPsiMethod.parameterList.parameters[index]
                hasAnno = superPsiParam.hasAnnotation(requestBodyAnnoName)
                if (hasAnno) {
                    psiParameter = psiParam
                    break
                }
            }
        }

        return psiParameter
    }

    private fun resolveTargetField(
        paramPsiCls: PsiClass,
        jsonPropertyNameLevels: LinkedList<String>,
        classGenericParameters: Array<PsiType>,
    ): PsiField? {
        var psiField: PsiField? = null

        try {
            var fieldTypeCls: PsiClass
            var propertyName = jsonPropertyNameLevels.pop()

            val isCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection")
            if (isCollection) {
                if (classGenericParameters.isEmpty()) {
                    return null
                }

                // 取得泛型参数类型
                fieldTypeCls = PsiUtils.resolvePsiType(classGenericParameters[0]) ?: return null
            } else {
                fieldTypeCls = paramPsiCls
            }


            while (true) {
                psiField = fieldTypeCls.findFieldByName(propertyName, true) ?: return null
                if (psiField.type !is PsiClassType) {
                    return psiField
                }

                val psiType = psiField.type as PsiClassType

                val parameters = psiType.parameters
                if (parameters.isNotEmpty()) {
                    // 取得泛型参数类型
                    fieldTypeCls = PsiUtils.resolvePsiType(parameters[0]) ?: return null
                } else {
                    val psiFieldTypeCls = PsiUtils.resolvePsiType(psiType) ?: return null
                    if (psiFieldTypeCls is PsiTypeParameter && classGenericParameters.isNotEmpty()) {
                        // 参数本身是泛型类型,如 T, 直接取第一个
                        val genericActualType = classGenericParameters[0] as PsiClassType
                        if (genericActualType.parameters.isNotEmpty()) {
                            val psiFieldGenericTypeCls =
                                PsiUtils.resolvePsiType(genericActualType.parameters[0]) ?: return null
                            fieldTypeCls = psiFieldGenericTypeCls
                        } else {
                            fieldTypeCls = PsiUtils.resolvePsiType(genericActualType) ?: return null
                        }
                    } else {
                        fieldTypeCls = psiFieldTypeCls
                    }
                }

                propertyName = jsonPropertyNameLevels.pop()
            }
        } catch (_: NoSuchElementException) {
        }

        return psiField
    }

    private fun collectJsonPropertyNameLevels(jsonString: JsonStringLiteral): LinkedList<String> {
        val beanFieldLevels = LinkedList<String>()

        var jsonProperty = PsiTreeUtil.getParentOfType(jsonString, JsonProperty::class.java)
        while (jsonProperty != null) {
            val propertyName = getJsonPropertyName(jsonProperty)
            beanFieldLevels.push(propertyName)
            jsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java)
        }

        return beanFieldLevels
    }

    private fun getJsonPropertyName(jsonProperty: JsonProperty): String {
        val nameElement = jsonProperty.nameElement
        val name = nameElement.text
        return name.substring(1, name.length - 1)
    }
}
