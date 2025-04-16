package org.javamaster.httpclient.nls

import com.intellij.BundleBase.messageOrDefault
import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

internal object NlsBundle {
    @NonNls
    private const val BUNDLE = "messages.HttpClientBundle"
    private const val ZH = "zh"
    private var locale: Locale = decideLocale()

    private fun decideLocale(): Locale {
        val locale = DynamicBundle.getLocale()
        if (locale.language == ZH) {
            return locale
        }

        return Locale.getDefault()
    }

    fun nls(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
        return nls(key, locale, *params)
    }

    private fun nls(
        key: @PropertyKey(resourceBundle = BUNDLE) String,
        locale: Locale,
        vararg params: Any,
    ): @Nls String {
        return messageOrDefault(ResourceBundle.getBundle(BUNDLE, locale), key, "", *params)
    }

}