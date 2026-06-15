package org.javamaster.httpclient.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.rename.RenameProcessor
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.factory.HttpPsiFactory.createFileVariable
import org.javamaster.httpclient.psi.*

/**
 * @author yudong
 */
class HttpFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HttpLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return HttpFileType.INSTANCE
    }

    fun getGlobalHandler(): HttpGlobalHandler? {
        return PsiTreeUtil.getChildOfType(this, HttpGlobalHandler::class.java)
    }

    fun getFileVariables(): List<HttpFileVariable> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpFileVariable::class.java)
    }

    fun getRequestBlocks(): List<HttpRequestBlock> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpRequestBlock::class.java)
    }

    fun getRequests(): List<HttpRequest> {
        return getRequestBlocks().mapNotNull { PsiTreeUtil.getChildOfType(it, HttpRequest::class.java) }
    }

    fun getDirectionComments(): List<HttpDirectionComment> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpDirectionComment::class.java)
    }

    fun getGlobalImports(): List<HttpGlobalImport> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpGlobalImport::class.java)
    }

    fun getHttpMethods(): List<HttpMethod> {
        return getRequests().mapNotNull { PsiTreeUtil.getChildOfType(it, HttpMethod::class.java) }
    }

    fun modifyFileVariable(key: String, newKey: String, newValue: String): PsiElement? {
        val children = PsiTreeUtil.findChildrenOfType(this, HttpFileVariable::class.java)

        val fileVariable = children
            .firstOrNull { it: HttpFileVariable -> it.fileVariableName?.name == key }
            ?: return null

        if (key != newKey) {
            val renameProcessor = RenameProcessor(
                project, fileVariable.fileVariableName!!, newKey,
                GlobalSearchScope.projectScope(project), false, false
            )
            renameProcessor.run()
        }

        return WriteCommandAction.runWriteCommandAction(project, Computable {
            val newFileVariable = createFileVariable(newKey, newValue, project)

            fileVariable.replace(newFileVariable)

            newFileVariable
        })
    }

    fun delFileVariable(key: String): Boolean {
        return WriteCommandAction.runWriteCommandAction(project, Computable {
            val children = PsiTreeUtil.findChildrenOfType(this, HttpFileVariable::class.java)

            val fileVariable = children
                .firstOrNull { it: HttpFileVariable -> it.fileVariableName?.name == key }
                ?: return@Computable false

            fileVariable.delete()

            true
        })
    }

    fun createFileVariableAndInsert(variableName: String, variableValue: String): PsiElement? {
        return WriteCommandAction.runWriteCommandAction(project, Computable {
            val newGlobalVariable = createFileVariable(variableName, variableValue, project)

            val directionComments = getDirectionComments()
            val globalHandler = getGlobalHandler()
            val globalImports = getGlobalImports()

            val elementNew = if (directionComments.isNotEmpty()) {
                addAfter(newGlobalVariable, directionComments.last().nextSibling)
            } else if (globalImports.isNotEmpty()) {
                addAfter(newGlobalVariable, globalImports.last().nextSibling)
            } else if (globalHandler != null) {
                addAfter(newGlobalVariable, globalHandler.nextSibling)
            } else {
                addBefore(newGlobalVariable, firstChild)
            }

            val cr = newGlobalVariable.nextSibling
            addAfter(cr, elementNew)

            elementNew
        })
    }

    override fun toString(): String {
        return "HTTP File"
    }

    companion object {

        fun resolveFileGlobalVariable(variableName: String, httpFile: PsiFile): PsiElement? {
            val fileVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpFileVariable::class.java)

            return fileVariables
                .mapNotNull {
                    val globalVariableName = it.fileVariableName
                    if (globalVariableName?.name == variableName) {
                        return@mapNotNull globalVariableName
                    } else {
                        return@mapNotNull null
                    }
                }
                .firstOrNull()
        }

        fun delFileVariable(key: String, project: Project): Boolean {
            val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return false
            val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile
            return httpFile.delFileVariable(key)
        }

        fun changeFileVariable(key: String, newKey: String, newValue: String, project: Project): Boolean {
            val variable = modifyFileVariable(key, newKey, newValue, project)
            return variable != null
        }

        fun modifyFileVariable(key: String, newKey: String, newValue: String, project: Project): PsiElement? {
            val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
            val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile
            return httpFile.modifyFileVariable(key, newKey, newValue)
        }

        fun addFileVariable(name: String, value: String, project: Project): Boolean {
            val variable = createFileVariableAndInsert(name, value, project)
            return variable != null
        }

        fun createFileVariableAndInsert(variableName: String, variableValue: String, project: Project): PsiElement? {
            val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
            val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile
            return httpFile.createFileVariableAndInsert(variableName, variableValue)
        }

    }
}
