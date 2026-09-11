package org.javamaster.httpclient.mock

import org.apache.commons.lang3.StringUtils
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.MockFtpServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.io.File


/**
 * @author yudong
 */
@Suppress("unused")
class MockFtpServerImpl(
    private val port: Int,
    private val httpDashboardForm: HttpDashboardForm,
) : MockFtpServer {
    private var ftpServer: FtpServer? = null

    override fun startServer(paramMap: MultiValueMap<String, String>) {
        val staticFolder = checkStaticFolder(paramMap.getFirst(ParamEnum.STATIC_FOLDER.param))
        val username = paramMap["username"]?.firstOrNull()
        val password = paramMap["password"]?.firstOrNull()
        val anonymousEnable = paramMap["enableAnonymous"] != null

        val connectionConfigFactory = ConnectionConfigFactory()

        if (anonymousEnable) {
            connectionConfigFactory.isAnonymousLoginEnabled = true
        }

        val ftpServerFactory = FtpServerFactory()
        ftpServerFactory.connectionConfig = connectionConfigFactory.createConnectionConfig()

        val factory = ListenerFactory()
        factory.port = port
        ftpServerFactory.addListener("default", factory.createListener())

        val userManagerFactory = PropertiesUserManagerFactory()
        val userManager = userManagerFactory.createUserManager()

        if (anonymousEnable) {
            val anonymousUser = BaseUser()
            anonymousUser.name = "anonymous"
            anonymousUser.homeDirectory = staticFolder.absolutePath
            userManager.save(anonymousUser)
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            val user = BaseUser()
            user.name = username
            user.password = password
            user.homeDirectory = staticFolder.absolutePath
            user.authorities = listOf(WritePermission())
            userManager.save(user)
        }

        ftpServerFactory.userManager = userManager

        val server = ftpServerFactory.createServer()
        ftpServer = server
        server.start()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.ftp.server.start", port) + "\n")
    }

    override fun stopServer() {
        ftpServer?.stop()

        httpDashboardForm.showMockServerLog("Ftp Server stopped\n")
    }

    private fun checkStaticFolder(staticFolder: String?): File {
        staticFolder ?: throw RuntimeException("Must have staticFolder param")

        val file = File(staticFolder)
        if (!file.exists()) {
            throw RuntimeException(NlsBundle.nls("folder.not.exist", file.absolutePath))
        }

        if (!file.isDirectory) {
            throw RuntimeException(NlsBundle.nls("not.folder", file.absolutePath))
        }

        return file
    }
}