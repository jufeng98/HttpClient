package org.javamaster.httpclient.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.JavaDocTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.scan.support.Request
import javax.swing.Icon

/**
 * @author yudong
 */
class RequestNavigationItem(val request: Request) :
    FakePsiElement() {
    private val psiMethod = request.psiElement!!

    override fun getPresentation(): ItemPresentation {
        return RequestItemPresentation(request)
    }

    override fun getIcon(open: Boolean): Icon {
        return request.method.icon
    }

    override fun canNavigate(): Boolean {
        return psiMethod.canNavigate()
    }

    override fun navigate(requestFocus: Boolean) {
        psiMethod.navigate(requestFocus)
    }

    override fun getName(): String {
        return request.path
    }

    override fun getContext(): PsiElement? {
        return psiMethod.context
    }

    override fun getParent(): PsiElement {
        return psiMethod
    }

    override fun getNavigationElement(): PsiElement {
        return psiMethod.navigationElement
    }

    override fun isValid(): Boolean {
        return psiMethod.isValid
    }

    class RequestItemPresentation(val request: Request) : ItemPresentation {
        override fun getPresentableText(): String {
            val psiMethod = request.psiElement!!

            var str = ""

            val docComment = psiMethod.docComment
            if (docComment != null) {
                val comment =
                    HttpPsiUtils.getNextSiblingByType(docComment.firstChild, JavaDocTokenType.DOC_COMMENT_DATA, false)
                        ?.text?.trim()
                str += comment
            }

            val annotation = psiMethod.getAnnotation("io.swagger.annotations.ApiOperation")
            if (annotation != null) {
                val desc = annotation.findAttributeValue("value")?.text?.trim()
                str += " $desc "
            }

            str = if (str.isNotEmpty()) {
                "($str)"
            } else {
                ""
            }

            return request.path + str
        }

        override fun getIcon(unused: Boolean): Icon {
            return request.method.icon
        }

    }

}
