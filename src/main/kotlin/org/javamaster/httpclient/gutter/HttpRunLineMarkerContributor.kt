package org.javamaster.httpclient.gutter

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.action.HttpAction
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        val elementType = element.elementType
        if (elementType != HttpTypes.REQUEST_METHOD) {
            return null
        }

        val parent = element.parent
        if (parent !is HttpMethod) {
            return null
        }

        val virtualFile = PsiUtil.getVirtualFile(element)
        if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            return null
        }

        val action = HttpAction(parent)

        return Info(AllIcons.Actions.Execute, arrayOf(action)) { _ -> "执行请求" }
    }

}
