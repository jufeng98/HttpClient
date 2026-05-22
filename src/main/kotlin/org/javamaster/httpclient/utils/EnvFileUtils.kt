package org.javamaster.httpclient.utils

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.rename.RenameProcessor
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvEleLiteral
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvJsonProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createBoolProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createNumberProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createStringProperty
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
class EnvFileUtils {

    companion object {

        fun addEnvVariable(newKey: String, newValue: String, project: Project): Boolean {
            val triple = HttpEditorTopForm.getTriple(project) ?: return false

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path

            val jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, project) ?: return false

            val jsonObject = jsonProperty.value as? JsonObject ?: return false

            WriteCommandAction.runWriteCommandAction(project) {
                val newProperty = createStringProperty(project, newKey, newValue)
                val newComma = getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false)
                val propertyList = jsonObject.propertyList

                if (propertyList.isEmpty()) {
                    jsonObject.addAfter(newProperty, jsonObject.firstChild)
                } else {
                    val psiElement = jsonObject.addAfter(newComma!!, propertyList[propertyList.size - 1])
                    jsonObject.addAfter(newProperty, psiElement)
                }
            }

            return true
        }

        fun delEnvVariable(key: String, project: Project): Boolean {
            val triple = HttpEditorTopForm.getTriple(project) ?: return false

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path

            val jsonLiteral = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, project) ?: return false

            val jsonProperty = jsonLiteral.parent
            if (jsonProperty !is JsonProperty) {
                return false
            }

            val jsonObject = jsonProperty.parent
            if (jsonObject !is JsonObject) {
                return false
            }

            WriteCommandAction.runWriteCommandAction(project) {
                val propertyList = jsonObject.propertyList

                if (propertyList.size == 1) {
                    jsonProperty.delete()
                } else {
                    if (jsonProperty == propertyList.last()) {
                        jsonProperty.delete()
                        propertyList[propertyList.size - 2].nextSibling.delete()
                    } else {
                        jsonProperty.nextSibling.delete()
                        jsonProperty.delete()
                    }
                }
            }

            return true
        }

        fun modifyEnvVariable(key: String, newKey: String, newValue: String, project: Project): Boolean {
            val triple = HttpEditorTopForm.getTriple(project) ?: return false

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path

            val jsonLiteral = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, project) ?: return false

            val jsonProperty = jsonLiteral.parent

            if (key != newKey) {
                val renameProcessor = RenameProcessor(
                    project, jsonProperty, newKey,
                    GlobalSearchScope.projectScope(project), false, true
                )
                renameProcessor.run()
            }

            WriteCommandAction.runWriteCommandAction(project) {
                val newProperty = when (jsonLiteral) {
                    is JsonNumberLiteral -> {
                        createNumberProperty(project, newKey, newValue)
                    }

                    is JsonBooleanLiteral -> {
                        createBoolProperty(project, newKey, newValue)
                    }

                    else -> {
                        createStringProperty(project, newKey, newValue)
                    }
                }

                jsonProperty.replace(newProperty)
            }

            return true
        }

    }

}
