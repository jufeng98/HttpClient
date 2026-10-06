package org.javamaster.httpclient.mock

import org.apache.commons.lang3.StringUtils
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.ftp.FtpJars
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.MockFtpServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.ReflectionUtils
import java.io.File

/**
 * @author yudong
 */
@Suppress("unused")
class MockFtpServerImpl(
    private val port: Int,
    private val httpDashboardForm: HttpDashboardForm,
) : MockFtpServer {
    private var ftpServer: Any? = null

    override fun startServer(paramMap: MultiValueMap<String, String>) {
        val staticFolder = checkStaticFolder(paramMap.getFirst(ParamEnum.STATIC_FOLDER.param))
        val username = paramMap["username"]?.firstOrNull()
        val password = paramMap["password"]?.firstOrNull()
        val anonymousEnable = paramMap["enableAnonymous"] != null

        val classLoader = FtpJars.ftpLibClassLoader

        val connectionConfigFactoryClass = classLoader.loadClass("org.apache.ftpserver.ConnectionConfigFactory")
        val connectionConfigFactory = connectionConfigFactoryClass.getDeclaredConstructor().newInstance()

        if (anonymousEnable) {
            val setAnonymousLoginEnabledMethod = ReflectionUtils.findMethod(
                connectionConfigFactoryClass, "setAnonymousLoginEnabled", Boolean::class.javaPrimitiveType
            )
            setAnonymousLoginEnabledMethod.invoke(connectionConfigFactory, true)
        }

        val createConnectionConfigMethod =
            ReflectionUtils.findMethod(connectionConfigFactoryClass, "createConnectionConfig")
        val connectionConfig = createConnectionConfigMethod.invoke(connectionConfigFactory)

        val ftpServerFactoryClass = classLoader.loadClass("org.apache.ftpserver.FtpServerFactory")
        val ftpServerFactory = ftpServerFactoryClass.getDeclaredConstructor().newInstance()

        val connectionConfigInterface = classLoader.loadClass("org.apache.ftpserver.ConnectionConfig")
        val setConnectionConfigMethod = ReflectionUtils.findMethod(
            ftpServerFactoryClass, "setConnectionConfig", connectionConfigInterface
        )
        setConnectionConfigMethod.invoke(ftpServerFactory, connectionConfig)

        val listenerFactoryClass = classLoader.loadClass("org.apache.ftpserver.listener.ListenerFactory")
        val listenerFactory = listenerFactoryClass.getDeclaredConstructor().newInstance()

        val setPortMethod =
            ReflectionUtils.findMethod(listenerFactoryClass, "setPort", Int::class.javaPrimitiveType)
        setPortMethod.invoke(listenerFactory, port)

        val createListenerMethod = ReflectionUtils.findMethod(listenerFactoryClass, "createListener")
        val listener = createListenerMethod.invoke(listenerFactory)

        val listenerInterface = classLoader.loadClass("org.apache.ftpserver.listener.Listener")
        val addListenerMethod = ReflectionUtils.findMethod(
            ftpServerFactoryClass, "addListener", String::class.java, listenerInterface
        )
        addListenerMethod.invoke(ftpServerFactory, "default", listener)

        val userManagerFactoryClass =
            classLoader.loadClass("org.apache.ftpserver.usermanager.PropertiesUserManagerFactory")
        val userManagerFactory = userManagerFactoryClass.getDeclaredConstructor().newInstance()
        val createUserManagerMethod = ReflectionUtils.findMethod(userManagerFactoryClass, "createUserManager")
        val userManager = createUserManagerMethod.invoke(userManagerFactory)

        val baseUserClass = classLoader.loadClass("org.apache.ftpserver.usermanager.impl.BaseUser")
        val writePermissionClass = classLoader.loadClass("org.apache.ftpserver.usermanager.impl.WritePermission")
        val userInterface = classLoader.loadClass("org.apache.ftpserver.ftplet.User")

        val saveMethod = ReflectionUtils.findMethod(userManager.javaClass, "save", userInterface)

        if (anonymousEnable) {
            val anonymousUser = baseUserClass.getDeclaredConstructor().newInstance()
            ReflectionUtils.findMethod(baseUserClass, "setName", String::class.java)
                .invoke(anonymousUser, "anonymous")
            ReflectionUtils.findMethod(baseUserClass, "setHomeDirectory", String::class.java)
                .invoke(anonymousUser, staticFolder.absolutePath)
            saveMethod.invoke(userManager, anonymousUser)
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            val user = baseUserClass.getDeclaredConstructor().newInstance()
            ReflectionUtils.findMethod(baseUserClass, "setName", String::class.java).invoke(user, username)
            ReflectionUtils.findMethod(baseUserClass, "setPassword", String::class.java).invoke(user, password)
            ReflectionUtils.findMethod(baseUserClass, "setHomeDirectory", String::class.java)
                .invoke(user, staticFolder.absolutePath)

            val writePermission = writePermissionClass.getDeclaredConstructor().newInstance()
            val authoritiesList = arrayListOf(writePermission)
            ReflectionUtils.findMethod(baseUserClass, "setAuthorities", List::class.java)
                .invoke(user, authoritiesList)

            saveMethod.invoke(userManager, user)
        }

        val userManagerInterface = classLoader.loadClass("org.apache.ftpserver.ftplet.UserManager")
        val setUserManagerMethod =
            ReflectionUtils.findMethod(ftpServerFactoryClass, "setUserManager", userManagerInterface)
        setUserManagerMethod.invoke(ftpServerFactory, userManager)

        val createServerMethod = ReflectionUtils.findMethod(ftpServerFactoryClass, "createServer")
        val server = createServerMethod.invoke(ftpServerFactory)
        ftpServer = server

        val startMethod = ReflectionUtils.findMethod(server.javaClass, "start")
        startMethod.invoke(server)

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.ftp.server.start", port) + "\n")
    }

    override fun stopServer() {
        if (ftpServer != null) {
            val stopMethod = ReflectionUtils.findMethod(ftpServer!!.javaClass, "stop")
            stopMethod.invoke(ftpServer)
        }
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