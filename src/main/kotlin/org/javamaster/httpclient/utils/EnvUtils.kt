package org.javamaster.httpclient.utils

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.rename.RenameProcessor
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvEleLiteral
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvJsonProperty
import org.javamaster.httpclient.factory.HttpPsiFactory.createGlobalVariable
import org.javamaster.httpclient.factory.JsonPsiFactory.createBoolProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createNumberProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createStringProperty
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpGlobalVariable
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
class EnvUtils {

    companion object {

        fun resolveFileGlobalVariable(variableName: String, httpFile: PsiFile): PsiElement? {
            val fileVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

            return fileVariables
                .mapNotNull {
                    val globalVariableName = it.globalVariableName
                    if (globalVariableName?.name == variableName) {
                        return@mapNotNull globalVariableName
                    } else {
                        return@mapNotNull null
                    }
                }
                .firstOrNull()
        }

        fun modifyFileGlobalVariable(
            key: String,
            newKey: String,
            newValue: String,
            add: Boolean,
            project: Project,
        ): Boolean {
            return WriteCommandAction.runWriteCommandAction(project, Computable {
                if (add) {
                    val variable = createGlobalVariableAndInsert(newKey, newValue, project)

                    variable != null
                } else {
                    val textEditor =
                        FileEditorManager.getInstance(project).selectedTextEditor ?: return@Computable false

                    val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile
                    val children = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

                    val globalVariable = children
                        .firstOrNull { it: HttpGlobalVariable -> it.globalVariableName?.name == key }
                        ?: return@Computable false

                    if (key != newKey) {
                        val renameProcessor = RenameProcessor(
                            project, globalVariable, newKey,
                            GlobalSearchScope.projectScope(project), false, true
                        )
                        renameProcessor.run()
                    }

                    val newGlobalVariable = createGlobalVariable(newKey, newValue, project)

                    globalVariable.replace(newGlobalVariable)
                }

                true
            })
        }

        fun modifyEnvVariable(
            key: String,
            newKey: String,
            newValue: String,
            add: Boolean,
            project: Project,
        ): Boolean {
            val triple = HttpEditorTopForm.getTriple(project) ?: return false

            val selectedEnv = triple.first
            val httpFileParentPath = triple.second.parent.path

            if (add) {
                val jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, project) ?: return false

                val jsonValue = jsonProperty.value as? JsonObject ?: return false

                WriteCommandAction.runWriteCommandAction(project) {
                    val newProperty = createStringProperty(project, newKey, newValue)
                    val newComma = getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false)
                    val propertyList = jsonValue.propertyList

                    if (propertyList.isEmpty()) {
                        jsonValue.addAfter(newProperty, jsonValue.firstChild)
                    } else {
                        val psiElement = jsonValue.addAfter(newComma!!, propertyList[propertyList.size - 1])
                        jsonValue.addAfter(newProperty, psiElement)
                    }
                }
            } else {
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
            }

            return true
        }

        fun createGlobalVariableAndInsert(variableName: String, variableValue: String, project: Project): PsiElement? {
            val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
            val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile

            val newGlobalVariable = createGlobalVariable(variableName, variableValue, project)

            val directionComments = httpFile.getDirectionComments()
            val globalHandler = httpFile.getGlobalHandler()

            val elementCopy = if (directionComments.isNotEmpty()) {
                httpFile.addAfter(newGlobalVariable, directionComments.last().nextSibling)
            } else if (globalHandler != null) {
                httpFile.addAfter(newGlobalVariable, globalHandler)
            } else {
                httpFile.addBefore(newGlobalVariable, httpFile.firstChild)
            }

            val whitespace = newGlobalVariable.nextSibling
            elementCopy.add(whitespace)

            val cr = whitespace.nextSibling
            if (cr != null) {
                elementCopy.add(cr)
            }

            return elementCopy
        }
    }

}
