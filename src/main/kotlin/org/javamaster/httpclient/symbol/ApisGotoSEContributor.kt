package org.javamaster.httpclient.symbol

import com.intellij.ide.actions.searcheverywhere.*
import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.DumbService.Companion.isDumb
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.psi.impl.RequestNavigationItem
import org.javamaster.httpclient.scan.ScanRequest

/**
 * @author yudong
 */
class ApisGotoSEContributor(event: AnActionEvent) : AbstractGotoSEContributor(event), PossibleSlowContributor {
    private val methodFilter: PersistentSearchEverywhereContributorFilter<HttpMethod>
    private val filterMethods: MutableSet<HttpMethod> = mutableSetOf()
    private val dumbService = myProject.getService(DumbService::class.java)

    init {
        filterMethods.addAll(HttpMethod.getMethods())

        methodFilter = PersistentSearchEverywhereContributorFilter(
            HttpMethod.getMethods(), object : ChooseByNameFilterConfiguration<HttpMethod>() {
                override fun nameForElement(type: HttpMethod): String {
                    return type.name
                }

                override fun isVisible(type: HttpMethod): Boolean {
                    return true
                }

                override fun setVisible(type: HttpMethod, value: Boolean) {
                    if (value) {
                        filterMethods.add(type)
                    } else {
                        filterMethods.remove(type)
                    }
                }
            },
            { it.name },
            { it.icon }
        )
    }

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<Any>>,
    ) {
        val fetchRunnable = Runnable {
            if (dumbService.isDumb) {
                return@Runnable
            }

            if (shouldNotProvideElements()) {
                return@Runnable
            }

            val matcher = NameUtil.buildMatcher("*$pattern", NameUtil.MatchingCaseSensitivity.NONE)

            val scope = scope.scope as GlobalSearchScope? ?: GlobalSearchScope.projectScope(myProject)

            ScanRequest.fetchRequests(myProject, scope) {
                progressIndicator.checkCanceled()

                if (it.psiElement != null && filterMethods.contains(it.method) && matcher.matches(it.path)) {
                    consumer.process(FoundItemDescriptor(RequestNavigationItem(it), 100))
                }
            }
        }

        @Suppress("UsagesOfObsoleteApi", "DEPRECATION")
        ProgressIndicatorUtils.yieldToPendingWriteActions()
        @Suppress("UsagesOfObsoleteApi", "DEPRECATION")
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator)

    }

    override fun createModel(project: Project): FilteringGotoByModel<*> {
        val model = GotoApiModel2(project, this)
        return model
    }

    override fun getActions(onChanged: Runnable): List<AnAction> {
        return doGetActions(methodFilter, null, onChanged)
    }

    private fun shouldNotProvideElements(): Boolean {
        val seManager = SearchEverywhereManager.getInstance(myProject)
        if (!seManager.isShown) {
            return true
        }

        return searchProviderId != seManager.selectedTabID
    }

    override fun getGroupName(): String {
        return "Apis"
    }

    override fun getSortWeight(): Int {
        return 800
    }

    override fun isEmptyPatternSupported(): Boolean {
        return true
    }

    class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any> {
            val seContributor = ApisGotoSEContributor(initEvent)

            return PSIPresentationBgRendererWrapper(seContributor)
        }
    }
}