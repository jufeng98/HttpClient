package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.hint.HintManager
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.DiffRequestFactory
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpHistoryBodyFile
import org.javamaster.httpclient.psi.HttpHistoryBodyFileList
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.SwingConstants

/**
 * @author yudong
 */
object HttpDiffGutterIconNavigationHandler : GutterIconNavigationHandler<PsiElement> {

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val project = element.project
        val editorVirtualFile = PsiUtil.getVirtualFile(element)!!

        val currentBodyFile = element.parent as HttpHistoryBodyFile
        val currentFilePath = currentBodyFile.filePath ?: return

        val editorManager = FileEditorManager.getInstance(project)
        val hintManager = HintManager.getInstance()

        val editor = editorManager.selectedTextEditor!!
        val historyPath = editorVirtualFile.parent.path

        val currentFile = File(historyPath, currentFilePath.text)
        val currentVirtualFile = findFileByIoFile(currentFile, true)

        if (currentVirtualFile == null) {
            editor.caretModel.moveToOffset(element.textRange.endOffset)

            hintManager.showErrorHint(editor, NlsBundle.nls("file.not.exists", currentFile.name))
            return
        }

        val map = linkedMapOf<String, Pair<HttpHistoryBodyFile, File>>()

        val historyBodyFileList = currentBodyFile.parent as HttpHistoryBodyFileList

        historyBodyFileList.historyBodyFileList
            .filter { it != currentBodyFile && it.filePath != null }
            .forEach {
                val file = File(historyPath, it.filePath!!.text)

                map[file.name] = Pair(it, file)
            }

        PopupFactoryImpl.getInstance()
            .createPopupChooserBuilder(map.keys.toList())
            .setRenderer { _, value, _, _, _ ->
                val text = "    " + NlsBundle.nls("compare.with") + " " + value!! + "    "
                JBLabel(text, AllIcons.Actions.Diff, SwingConstants.CENTER)
            }
            .setItemChosenCallback {
                val pair = map[it]!!
                val chooseFile = pair.second

                val chooseVirtualFile = findFileByIoFile(chooseFile, true)
                if (chooseVirtualFile == null) {
                    editor.caretModel.moveToOffset(pair.first.textRange.endOffset)

                    hintManager.showErrorHint(
                        editor,
                        NlsBundle.nls("file.not.exists", chooseFile.name)
                    )

                    return@setItemChosenCallback
                }

                val contentFactory = DiffContentFactory.getInstance()
                val requestFactory = DiffRequestFactory.getInstance()

                val content1 = contentFactory.create(project, currentVirtualFile)
                val content2 = contentFactory.create(project, chooseVirtualFile)

                val title = requestFactory.getTitle(currentVirtualFile)

                val request = SimpleDiffRequest(title, content1, content2, null, null)

                DiffManager.getInstance().showDiff(project, request)
            }
            .createPopup()
            .showInScreenCoordinates(event.component, event.locationOnScreen)
    }

}
