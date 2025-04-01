package org.javamaster.httpclient.scan.support

import com.intellij.lang.Language
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.util.PsiModificationTracker
import java.util.function.Predicate

/**
 * Only track the SpringMVC Controller class changed
 *
 * @see ControllerPsiTreeChangePreprocessor
 * @author yudong
 */
object ControllerPsiModificationTracker : PsiModificationTracker {
    val myModificationCount = SimpleModificationTracker()

    override fun getModificationCount(): Long {
        return myModificationCount.modificationCount
    }

    @Deprecated("Deprecated in Java", ReplaceWith("myModificationCount"))
    @Suppress("UnstableApiUsage")
    override fun getJavaStructureModificationTracker(): ModificationTracker {
        return myModificationCount
    }

    override fun forLanguage(language: Language): ModificationTracker {
        return myModificationCount
    }

    override fun forLanguages(condition: Predicate<in Language>): ModificationTracker {
        return myModificationCount
    }
}
