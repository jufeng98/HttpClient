package org.javamaster.httpclient.utils

import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.env.EnvFileService

/**
 * @author yudong
 */
object IntentUtil {

    fun checkHasCertificatePassphrase(editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null) {
            return false
        }

        if (file !is JsonFile) {
            return false
        }

        if (!EnvFileService.Companion.ENV_FILE_NAMES.contains(file.name)) {
            return false
        }

        val element = file.findElementAt(editor.caretModel.primaryCaret.offset)?.parent ?: return false
        if (element !is JsonStringLiteral) {
            return false
        }

        if (element.value != "hasCertificatePassphrase") {
            return false
        }

        val property = element.parent
        if (property !is JsonProperty) {
            return false
        }

        val value = property.value
        if (value !is JsonBooleanLiteral) {
            return false
        }

        return value.value
    }

}