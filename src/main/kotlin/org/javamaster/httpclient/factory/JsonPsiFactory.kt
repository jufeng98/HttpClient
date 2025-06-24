package org.javamaster.httpclient.factory

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author yudong
 */
object JsonPsiFactory {

    fun createStringProperty(project: Project, key: String, value: String): JsonProperty {
        val text = """
            {
                "$key": "$value",
            }
        """.trimIndent()

        val tmpFile = createJsonFile(project, text)

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty::class.java)!!
    }

    fun createNumberProperty(project: Project, key: String, value: String): JsonProperty {
        if (!StringUtils.isNumeric(value)) {
            throw RuntimeException("$key is a numeric property!")
        }

        val text = """
            {
                "$key": $value,
            }
        """.trimIndent()

        val tmpFile = createJsonFile(project, text)

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty::class.java)!!
    }

    fun createBoolProperty(project: Project, key: String, value: String): JsonProperty {
        value.toBooleanStrictOrNull() ?: throw RuntimeException("$key is boolean property!")

        val text = """
            {
                "$key": $value,
            }
        """.trimIndent()

        val tmpFile = createJsonFile(project, text)

        return PsiTreeUtil.findChildOfType(tmpFile, JsonProperty::class.java)!!
    }

    private fun createJsonFile(project: Project, text: String): PsiFile {
        val psiFileFactory = PsiFileFactory.getInstance(project)
        return psiFileFactory.createFileFromText("dummy.json", JsonLanguage.INSTANCE, text)
    }

}
