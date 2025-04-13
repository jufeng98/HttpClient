package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.completion.support.SlashInsertHandler
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.psi.HttpDirectionComment
import org.javamaster.httpclient.psi.HttpDirectionValue
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.reference.support.HttpVariableNamePsiReference
import java.io.File

/**
 * @author yudong
 */
class HttpFilePathCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val psiElement = parameters.position
        val parent = psiElement.parent.parent
        val parentParent = parent.parent

        if (parentParent is HttpDirectionComment && parentParent.directionName?.text != ParamEnum.IMPORT.param) {
            return
        }

        val virtualFile = PsiUtil.getVirtualFile(psiElement) ?: return

        val variable = findVariable(parent)
        if (variable != null) {
            val variableName = variable.variableName ?: return

            val psiFile = PsiUtil.getPsiFile(parentParent.project, virtualFile)

            val psiDirectory = HttpVariableNamePsiReference.tryResolveVariable(
                variableName.name,
                variableName.isBuiltin,
                psiFile,
                false
            )

            if (psiDirectory !is PsiDirectory) {
                return
            }

            fillRootPaths(psiDirectory, variable.text, result)
        } else {
            val root = virtualFile.parent ?: return

            File.listRoots().forEach {
                result.addElement(LookupElementBuilder.create(it, it.path))
            }

            fillRootPaths(root, result)
        }
    }

    private fun fillRootPaths(psiDirectory: PsiDirectory, variableText: String, result: CompletionResultSet) {
        val prefix = result.prefixMatcher.prefix
        val root = psiDirectory.virtualFile

        val start = variableText.length
        var end = prefix.lastIndexOf("/")
        if (end == -1 || end < start) {
            end = start
        }

        val path = prefix.substring(start, end)

        val relativeFile = VfsUtil.findFileByIoFile(File(root.path + path), false)

        val prefixPath: String
        val virtualFile = if (relativeFile != null) {
            prefixPath = path
            relativeFile
        } else {
            prefixPath = ""
            root
        }

        fillPaths(virtualFile, variableText, prefixPath, result)
    }

    private fun fillPaths(
        virtualFile: VirtualFile,
        variableText: String,
        prefixPath: String,
        result: CompletionResultSet,
    ) {
        for (file in virtualFile.children) {
            if (file.name.startsWith(".")) {
                continue
            }

            val relativize = variableText + prefixPath + file.path.substring(virtualFile.path.length)
            if (file.isDirectory) {
                result.addElement(LookupElementBuilder.create(file, relativize).withInsertHandler(SlashInsertHandler))
            } else {
                result.addElement(LookupElementBuilder.create(file, relativize))
            }
        }
    }

    private fun fillRootPaths(root: VirtualFile, result: CompletionResultSet) {
        var num = 0

        VfsUtil.iterateChildrenRecursively(root, null, {
            num++
            val relativize = it.path.substring(root.path.length + 1)
            if (it.isDirectory) {
                result.addElement(LookupElementBuilder.create(it, relativize).withInsertHandler(SlashInsertHandler))
            } else {
                result.addElement(LookupElementBuilder.create(it, relativize))
            }

            if (num > 600) false else true
        }, VirtualFileVisitor.SKIP_ROOT)
    }

    private fun findVariable(parent: PsiElement): HttpVariable? {
        var variable: HttpVariable? = null
        if (parent is HttpFilePath) {
            variable = parent.variable
        } else if (parent is HttpDirectionValue) {
            variable = parent.variable
        }

        return variable
    }

}
