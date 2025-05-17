package org.javamaster.httpclient.nls

import com.intellij.BundleBase.messageOrDefault
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

internal object NlsBundle {
    @NonNls
    private const val BUNDLE = "messages.HttpClientBundle"

     val lang by lazy {
        val locale = ResourceBundle.getBundle(BUNDLE).locale
        if (locale == Locale.CHINESE) {
            return@lazy "zh"
        } else {
            return@lazy "en"
        }
    }

     val region by lazy {
        val locale = ResourceBundle.getBundle(BUNDLE).locale
        if (locale == Locale.CHINESE) {
            return@lazy "zh-CN"
        } else {
            return@lazy "en-US"
        }
    }

    fun nls(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
        return messageOrDefault(ResourceBundle.getBundle(BUNDLE), key, "", *params)
    }

}