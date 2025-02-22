package org.javamaster.httpclient.reference.support

import com.intellij.navigation.ItemPresentation
import javax.swing.Icon

/**
 * @author yudong
 */
object HttpItemPresentation : ItemPresentation {

    override fun getPresentableText(): String {
        return "搜索对应的Controller接口"
    }

    override fun getIcon(unused: Boolean): Icon? {
        return null
    }

}
