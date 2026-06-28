package org.javamaster.httpclient.listener

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.pom.Navigatable
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.factory.HttpPsiFactory
import org.javamaster.httpclient.psi.HttpRequestBlock

/**
 * @author yudong
 */
class DisableCookiesEditorMouseListener(
    private val inlay: Inlay<out EditorCustomElementRenderer>,
    private val requestBlock: HttpRequestBlock,
) : EditorMouseListener {

    override fun mouseClicked(e: EditorMouseEvent) {
        val point = e.mouseEvent.getPoint()
        val bounds = inlay.bounds
        if (bounds == null) {
            return
        }


        if (bounds.contains(point)) {
            val project = requestBlock.project

            WriteCommandAction.runWriteCommandAction(project) {
                val directionComment = HttpPsiFactory.createDirectionComment(project, "# @${ParamEnum.NO_COOKIE_JAR.param}\n")
                val nextSibling = directionComment.nextSibling
                val comment = requestBlock.comment
                if (comment != null) {
                    val elementNew = requestBlock.addAfter(directionComment, comment)
                    requestBlock.addAfter(nextSibling, elementNew)
                    (elementNew as Navigatable).navigate(true)
                } else {
                    val elementNew = requestBlock.addBefore(directionComment, requestBlock.firstChild)
                    requestBlock.addAfter(nextSibling, elementNew)
                    (elementNew as Navigatable).navigate(true)
                }
            }
        }
    }

}
