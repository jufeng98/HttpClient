package org.javamaster.httpclient.manipulator

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import com.intellij.refactoring.rename.RenameDialog
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.jsPlugin.JsFacade

/**
 * @author yudong
 */
class InjectedJsRenameDialog(
    project: Project,
    psiElement: PsiElement,
    val nameSuggestionContext: PsiElement?,
    val editor: Editor,
) : RenameDialog(project, psiElement, nameSuggestionContext, editor) {

    override fun doAction() {
        val newName = getNewName()

        close(0)

        val name = nameSuggestionContext!!.text.replace("\"", "").replace("{", "").replace("}", "")

        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = editor.document

        val host = InjectedLanguageManager.getInstance(project).getInjectionHost(psiElement)!!
        val virtualFile = host.containingFile.virtualFile
        val module = ModuleUtil.findModuleForPsiElement(host)

        val start = host.textOffset + psiElement.startOffset
        val end = host.textOffset + psiElement.endOffset

        val clz = JsFacade.getJsCallExpressionClz()
        val jsCallExpression = PsiTreeUtil.getParentOfType(psiElement, clz)!!

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(start, end, "\"${newName}\"")

            replaceAll("{{$name}}", "{{$newName}}", document)

            if (jsCallExpression.text.contains("global")) {
                val httpFiles = findModuleHttpFiles(module)

                httpFiles.removeIf { it == virtualFile }

                httpFiles.forEach {
                    val doc = fileDocumentManager.getDocument(it) ?: return@forEach

                    replaceAll("{{$name}}", "{{$newName}}", doc)
                }
            }
        }
    }

    fun replaceAll(target: String, replacement: String, document: Document) {
        val text = document.text
        val regex = Regex.escape(target).toRegex()
        val matches = regex.findAll(text).map { it.range }.toList()
        // 从后往前替换
        matches.asReversed().forEach { range ->
            document.replaceString(range.first, range.last + 1, replacement)
        }
    }

    fun findModuleHttpFiles(module: Module?): MutableList<VirtualFile> {
        val list = mutableListOf<VirtualFile>()
        if (module == null || module.isDisposed) return list
        val scope = GlobalSearchScope.moduleScope(module)
        val files = FileTypeIndex.getFiles(HttpFileType.INSTANCE, scope)
        list.addAll(files)
        return list
    }

    override fun hasPreviewButton(): Boolean {
        return false
    }

}
