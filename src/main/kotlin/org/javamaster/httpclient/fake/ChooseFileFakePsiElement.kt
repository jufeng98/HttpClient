package org.javamaster.httpclient.fake

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.apache.commons.lang3.StringUtils
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.factory.HttpPsiFactory
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpDirectionValue
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.psi.HttpTypes
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * @author yudong
 */
class ChooseFileFakePsiElement(private val myElement: PsiElement, private val directionImport: Boolean) :
    FakePsiElement() {

    override fun canNavigate(): Boolean {
        return true
    }

    override fun navigate(requestFocus: Boolean) {
        val project = myElement.project
        val virtualFile = myElement.containingFile.virtualFile

        val chooserDescriptor = if (myElement.elementType == HttpTypes.IMPORT) {
            FileChooserDescriptorFactory.createSingleFileDescriptor(HttpFileType.INSTANCE)
        } else if (directionImport) {
            FileChooserDescriptorFactory.createSingleFileDescriptor("js")
        } else {
            FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
        }

        val file = FileChooser.chooseFile(chooserDescriptor, project, virtualFile)
        if (file == null) {
            return
        }

        val parent = if (directionImport) myElement.parent?.parent else myElement.parent
        if (parent == null) {
            return
        }

        val currentElement = if (directionImport) {
            PsiTreeUtil.findChildOfType(parent, HttpDirectionValue::class.java)
        } else {
            PsiTreeUtil.findChildOfType(parent, HttpFilePath::class.java)
        }

        val path = file.toNioPath()

        var targetPath: Path
        try {
            targetPath = virtualFile.parent.toNioPath().relativize(path)
            val count = StringUtils.countMatches(targetPath.absolutePathString(), "..")
            if (count > 3) {
                targetPath = path
            }
        } catch (e: Exception) {
            logWarn("Relativize path failed", e)
            targetPath = path
        }

        val element = if (directionImport) {
            HttpPsiFactory.createDirectionValue(project, targetPath.toString())
        } else {
            HttpPsiFactory.createFilePath(project, targetPath.toString())
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val newEle = if (currentElement != null) {
                currentElement.replace(element)
            } else {
                parent.addAfter(element, myElement)
            }

            (newEle as Navigatable).navigate(requestFocus)
        }
    }

    override fun getName(): String {
        return nls("choose.tip")
    }

    override fun getParent(): PsiElement? {
        return myElement.parent
    }

}