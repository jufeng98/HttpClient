package org.javamaster.httpclient.utils

import com.intellij.execution.RunManager
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.utils.HttpUtils.getTabName
import org.javamaster.httpclient.utils.HttpUtils.isFileInIdeaDir
import java.io.File

object DubboUtils {
    const val INTERFACE_KEY = "Interface"
    const val INTERFACE_NAME = "Interface-Name"
    const val METHOD_KEY = "Method"

    fun findInterface(module: Module, name: String): PsiClass? {
        val javaPsiFacade = JavaPsiFacade.getInstance(module.project)
        val scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)

        return javaPsiFacade.findClass(name, scope)
    }

    fun findDubboServiceMethod(jsonString: JsonStringLiteral): PsiMethod? {
        val httpMessageBody =
            InjectedLanguageManager.getInstance(jsonString.project).getInjectionHost(jsonString) as HttpMessageBody
        val httpRequest = PsiTreeUtil.getParentOfType(httpMessageBody, HttpRequest::class.java)

        val headerField = httpRequest?.header?.headerFieldList
            ?.firstOrNull { it.headerFieldName.text == INTERFACE_KEY }
            ?: return null

        val module = getOriginalModule(httpRequest) ?: return null
        val name = headerField.headerFieldValue?.text ?: return null
        val psiClass = findInterface(module, name) ?: return null

        val headerFieldMethod = httpRequest.header?.headerFieldList
            ?.firstOrNull { it.headerFieldName.text == METHOD_KEY }
            ?: return null

        val value = headerFieldMethod.headerFieldValue?.text ?: return null
        val methods = psiClass.findMethodsByName(value, false)
        if (methods.isEmpty()) {
            return null
        }

        return methods[0]
    }

    private fun getOriginalFile(headerFieldValue: HttpHeaderFieldValue): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(headerFieldValue)
        if (!isFileInIdeaDir(virtualFile)) {
            return virtualFile
        }

        val httpRequest = PsiTreeUtil.getParentOfType(headerFieldValue, HttpRequest::class.java) ?: return null

        return getOriginalFile(httpRequest)
    }

    private fun getOriginalFile(httpRequest: HttpRequest): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(httpRequest)
        if (!isFileInIdeaDir(virtualFile)) {
            return virtualFile
        }

        val tabName = getTabName(httpRequest.method)

        val runManager = RunManager.getInstance(httpRequest.project)
        val configurationSettings = runManager.allSettings
            .firstOrNull {
                it.configuration is HttpRunConfiguration && it.configuration.name == tabName
            }
        if (configurationSettings == null) {
            return null
        }

        val httpRunConfiguration = configurationSettings.configuration as HttpRunConfiguration

        return VfsUtil.findFileByIoFile(File(httpRunConfiguration.httpFilePath), true)
    }

    fun getOriginalModule(httpRequest: HttpRequest): Module? {
        val project = httpRequest.project

        val virtualFile = getOriginalFile(httpRequest) ?: return null

        return ModuleUtilCore.findModuleForFile(virtualFile, project)
    }

    fun getOriginalModule(headerFieldValue: HttpHeaderFieldValue): Module? {
        val project = headerFieldValue.project

        val virtualFile = getOriginalFile(headerFieldValue) ?: return null

        return ModuleUtilCore.findModuleForFile(virtualFile, project)
    }

}