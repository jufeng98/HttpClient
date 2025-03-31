package org.javamaster.httpclient.symbol

import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.DumbService.Companion.isDumb
import com.intellij.openapi.project.modules
import com.intellij.util.Processor
import org.javamaster.httpclient.psi.impl.RequestNavigationItem
import org.javamaster.httpclient.scan.ScanRequest
import javax.swing.ListCellRenderer

class ApiWeightedSearchEverywhereContributor(initEvent: AnActionEvent) :
    WeightedSearchEverywhereContributor<RequestNavigationItem> {
    private val project = initEvent.project!!

    override fun getElementsRenderer(): ListCellRenderer<in RequestNavigationItem> {
        return SearchEverywherePsiRenderer(this)
    }

    override fun getGroupName(): String {
        return "Apis"
    }

    override fun getSortWeight(): Int {
        return 900
    }

    override fun isEmptyPatternSupported(): Boolean {
        return true
    }

    override fun isShownInSeparateTab(): Boolean {
        return true
    }

    override fun isDumbAware(): Boolean {
        return isDumb(project)
    }

    override fun processSelectedItem(selected: RequestNavigationItem, modifiers: Int, searchText: String): Boolean {
        selected.request.psiElement?.navigate(true)
        return true
    }

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<RequestNavigationItem>>,
    ) {
        progressIndicator.checkCanceled()

        val fetchRunnable = Runnable {
            if (isDumbAware) {
                return@Runnable
            }

            project.modules
                .forEach { module ->
                    progressIndicator.checkCanceled()

                    val requestMap = ScanRequest.getCacheRequestMap(module, progressIndicator)

                    if (pattern.isEmpty()) {
                        requestMap.entries
                            .take(50)
                            .forEach {
                                it.value
                                    .filter { request -> request.psiElement != null }
                                    .forEach { request ->
                                        consumer.process(FoundItemDescriptor(RequestNavigationItem(request, module), 1))
                                    }
                            }
                        return@Runnable
                    }

                    requestMap.entries
                        .filter {
                            progressIndicator.checkCanceled()

                            it.key.contains(pattern)
                        }
                        .forEach {
                            it.value
                                .filter { request -> request.psiElement != null }
                                .forEach { request ->
                                    consumer.process(FoundItemDescriptor(RequestNavigationItem(request, module), 10))
                                }
                        }
                }
        }

        @Suppress("UsagesOfObsoleteApi", "DEPRECATION")
        ProgressIndicatorUtils.yieldToPendingWriteActions()
        @Suppress("UsagesOfObsoleteApi", "DEPRECATION")
        ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator)
    }

    override fun getSearchProviderId(): String {
        return javaClass.simpleName
    }

    override fun showInFindResults(): Boolean {
        return false
    }

    class Factory : SearchEverywhereContributorFactory<RequestNavigationItem> {

        override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<RequestNavigationItem> {
            return ApiWeightedSearchEverywhereContributor(initEvent)
        }

    }
}
