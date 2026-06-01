package org.javamaster.httpclient.messageBus

import com.intellij.lang.Language
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel

@FunctionalInterface
interface WsLangChangeNotifier {

    fun change(newLanguage: Language)

    companion object {
        @ProjectLevel
        val WS_LANG_CHANGE_TOPIC: Topic<WsLangChangeNotifier> =
            Topic.create<WsLangChangeNotifier>("Ws lang change", WsLangChangeNotifier::class.java)
    }

}