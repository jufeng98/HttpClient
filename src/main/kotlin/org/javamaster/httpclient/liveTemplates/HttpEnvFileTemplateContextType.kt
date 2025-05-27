package org.javamaster.httpclient.liveTemplates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.env.EnvFileService


/**
 * @author yudong
 */
class HttpEnvFileTemplateContextType : TemplateContextType("Http env file") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return inContext(templateActionContext.file)
    }

    private fun inContext(file: PsiFile): Boolean {
        return EnvFileService.ENV_FILE_NAMES.contains(file.name)
    }
}
