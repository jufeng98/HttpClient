package org.javamaster.httpclient.reference.support

import com.cool.request.view.tool.search.ApiAbstractGotoSEContributor
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.doc.support.CoolRequestHelper.findControllerNavigationItem
import org.javamaster.httpclient.doc.support.CoolRequestHelper.findControllerPsiMethods
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils.collectJsonPropertyNameLevels
import org.javamaster.httpclient.utils.HttpUtils.createActionEvent
import org.javamaster.httpclient.utils.HttpUtils.createProcessIndicator
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetField
import org.javamaster.httpclient.utils.HttpUtils.resolveTargetParam
import org.javamaster.httpclient.utils.PsiUtils
import org.javamaster.httpclient.utils.TooltipUtils.showTooltip
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

/**
 * @author yudong
 */
class JsonControllerMethodFieldPsiElement(
    val jsonString: JsonStringLiteral,
    val searchTxt: String,
    val module: Module,
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
        HttpBackground
            .runInBackgroundReadActionAsync {
                DubboUtils.findDubboServiceMethod(jsonString)
            }
            .finishOnUiThread {
                if (it == null) {
                    showTooltip("Tip:未能解析到对应的 Dubbo 方法,无法跳转", project)
                    return@finishOnUiThread
                }
                val paramPsiType: PsiType?

                if (virtualFile?.name?.endsWith("res.http") == true) {
                    paramPsiType = it.returnType
                } else {
                    val name = jsonPropertyNameLevels.pop()
                    val psiParameter = it.parameterList.parameters.firstOrNull { parameter -> parameter.name == name }

                    if (psiParameter == null) {
                        showTooltip("Tip:未能解析到对应的方法参数,无法跳转", project)
                        return@finishOnUiThread
                    }

                    if (jsonPropertyNameLevels.isEmpty()) {
                        psiParameter.navigate(true)
                        return@finishOnUiThread
                    }

                    paramPsiType = psiParameter.type
                }

                val paramPsiCls = PsiUtils.resolvePsiType(paramPsiType) ?: return@finishOnUiThread

                val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

                val targetField = resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
                if (targetField == null) {
                    showTooltip("Tip:未能解析对应的 Bean 属性,无法跳转", project)
                    return@finishOnUiThread
                }

                targetField.navigate(true)
            }
            .exceptionallyOnUiThread {
                showTooltip("Tip:$it", project)
            }
    }

    private fun jumpToControllerAsync(
        virtualFile: VirtualFile?,
        jsonPropertyNameLevels: LinkedList<String>,
    ) {
        val processIndicator = createProcessIndicator("Tip:正在尝试跳转到对应的 Bean 字段...", project)
        Disposer.register(Disposer.newDisposable(), processIndicator)

        val event = createActionEvent()
        val seContributor = ApiAbstractGotoSEContributor(event)

        CompletableFuture.runAsync {
            val controllers = seContributor.search(searchTxt, processIndicator)
            if (processIndicator.isCanceled) {
                processIndicator.processFinish()
                return@runAsync
            }

            processIndicator.processFinish()

            if (controllers.isEmpty()) {
                runInEdt {
                    showTooltip("Tip:未能解析到对应的 Controller,无法跳转", project)
                }
                return@runAsync
            }

            val controllerNavigationItem = findControllerNavigationItem(controllers, searchTxt)

            HttpBackground
                .runInBackgroundReadActionAsync {
                    findControllerPsiMethods(controllerNavigationItem, module)
                }
                .finishOnUiThread {
                    if (it!!.isEmpty()) {
                        showTooltip("Tip:未能解析对应的 Controller 方法,无法跳转", project)
                        return@finishOnUiThread
                    }

                    if (it.size > 1) {
                        showTooltip("Tip:解析到 ${it.size} 个 Controller 方法,无法跳转", project)
                        return@finishOnUiThread
                    }

                    val psiMethod = it[0]
                    val paramPsiType: PsiType?

                    if (virtualFile?.name?.endsWith("res.http") == true) {
                        paramPsiType = psiMethod.returnType
                    } else {
                        val psiParameter = resolveTargetParam(psiMethod)

                        paramPsiType = psiParameter?.type
                    }

                    val paramPsiCls: PsiClass = PsiUtils.resolvePsiType(paramPsiType) ?: return@finishOnUiThread

                    val classGenericParameters = (paramPsiType as PsiClassReferenceType).parameters

                    val targetField = resolveTargetField(paramPsiCls, jsonPropertyNameLevels, classGenericParameters)
                    if (targetField == null) {
                        showTooltip("Tip:未能解析对应的 Bean 字段,无法跳转", project)
                        return@finishOnUiThread
                    }

                    targetField.navigate(true)
                }
                .exceptionallyOnUiThread {
                    showTooltip("Tip:$it", project)
                }
        }
    }

    object JsonItemPresentation : ItemPresentation {

        override fun getPresentableText(): String {
            return "跳转到对应的 Controller 方法的出/入参字段"
        }

        override fun getIcon(unused: Boolean): Icon? {
            return HttpIcons.SPRING_PROPERTY
        }

    }
}

