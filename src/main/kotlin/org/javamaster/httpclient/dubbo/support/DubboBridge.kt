package org.javamaster.httpclient.dubbo.support

import org.javamaster.httpclient.ui.HttpDashboardForm

/**
 * @author yudong
 */
class DubboBridge(private val httpDashboardForm: HttpDashboardForm) {

    fun showMockServerLog(msg: String) {
        httpDashboardForm.showMockServerLog(msg)
    }

}
