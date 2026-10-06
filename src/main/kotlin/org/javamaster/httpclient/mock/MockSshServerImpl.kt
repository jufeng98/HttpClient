package org.javamaster.httpclient.mock

import com.intellij.util.system.OS
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.MockSshServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.sftp.SftpJars
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.KeyUtils
import org.javamaster.httpclient.utils.PluginUtils
import org.javamaster.httpclient.utils.ReflectionUtils
import java.io.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.security.KeyPair
import java.security.SecureRandom

/**
 * @author yudong
 */
@Suppress("unused")
class MockSshServerImpl(
    private val port: Int,
    private val httpDashboardForm: HttpDashboardForm,
) : MockSshServer {
    private var sshServer: Any? = null

    override fun startServer(paramMap: MultiValueMap<String, String>) {
        val staticFolder = checkStaticFolder(paramMap.getFirst(ParamEnum.STATIC_FOLDER.param))
        val user = paramMap["username"]?.firstOrNull()
        val pwd = paramMap["password"]?.firstOrNull()

        val classLoader = SftpJars.sftpLibClassLoader

        val sshServerClass = classLoader.loadClass("org.apache.sshd.server.SshServer")
        val setUpDefaultServerMethod = sshServerClass.getMethod("setUpDefaultServer")
        val sshServerInstance = setUpDefaultServerMethod.invoke(null)

        ReflectionUtils.findMethod(sshServerClass, "setPort", Int::class.javaPrimitiveType)
            .invoke(sshServerInstance, port)

        val keyPairProviderClass = classLoader.loadClass("org.apache.sshd.common.keyprovider.KeyPairProvider")
        val keyPairProviderProxy =
            Proxy.newProxyInstance(classLoader, arrayOf(keyPairProviderClass)) { proxy, method, args ->
                if (method.name == "loadKeys") {
                    val file = File(PluginUtils.getPluginPath(), "lib/keyPair.dat")
                    if (file.exists()) {
                        val keyPair = ObjectInputStream(FileInputStream(file)).use {
                            it.readObject() as KeyPair
                        }
                        return@newProxyInstance listOf(keyPair)
                    }

                    val keyPair = KeyUtils.generateKeyPair("RSA", 2048, SecureRandom())
                    ObjectOutputStream(FileOutputStream(file)).use {
                        it.writeObject(keyPair)
                    }

                    return@newProxyInstance listOf(keyPair)
                }

                handleDefaultProxyMethod(proxy, method, args, keyPairProviderClass)
            }
        ReflectionUtils.findMethod(sshServerClass, "setKeyPairProvider", keyPairProviderClass)
            .invoke(sshServerInstance, keyPairProviderProxy)

        val passwordAuthenticatorClass =
            classLoader.loadClass("org.apache.sshd.server.auth.password.PasswordAuthenticator")
        val passwordAuthenticatorProxy =
            Proxy.newProxyInstance(classLoader, arrayOf(passwordAuthenticatorClass)) { proxy, method, args ->
                if (method.name == "authenticate") {
                    val username = args[0] as String
                    val password = args[1] as String
                    return@newProxyInstance user == username && pwd == password
                }
                handleDefaultProxyMethod(proxy, method, args, passwordAuthenticatorClass)
            }
        ReflectionUtils.findMethod(sshServerClass, "setPasswordAuthenticator", passwordAuthenticatorClass)
            .invoke(sshServerInstance, passwordAuthenticatorProxy)

        val virtualFileSystemFactoryClass =
            classLoader.loadClass("org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory")
        val fsFactory = virtualFileSystemFactoryClass.getDeclaredConstructor().newInstance()
        ReflectionUtils.findMethod(
            virtualFileSystemFactoryClass,
            "setUserHomeDir",
            String::class.java,
            java.nio.file.Path::class.java
        ).invoke(fsFactory, user, staticFolder.toPath())

        val fileSystemFactoryClass = classLoader.loadClass("org.apache.sshd.common.file.FileSystemFactory")
        ReflectionUtils.findMethod(sshServerClass, "setFileSystemFactory", fileSystemFactoryClass)
            .invoke(sshServerInstance, fsFactory)

        val interactiveProcessShellFactoryClass =
            classLoader.loadClass("org.apache.sshd.server.shell.InteractiveProcessShellFactory")
        val shellFactoryInstance = interactiveProcessShellFactoryClass.getField("INSTANCE").get(null)
        val shellFactoryClass = classLoader.loadClass("org.apache.sshd.server.shell.ShellFactory")
        ReflectionUtils.findMethod(sshServerClass, "setShellFactory", shellFactoryClass)
            .invoke(sshServerInstance, shellFactoryInstance)

        val commandFactoryClass = classLoader.loadClass("org.apache.sshd.server.command.CommandFactory")
        val commandFactoryProxy =
            Proxy.newProxyInstance(classLoader, arrayOf(commandFactoryClass)) { proxy, method, args ->
                if (method.name == "createCommand") {
                    val channel = args[0]
                    val command = args[1] as String
                    val processShellFactoryClass =
                        classLoader.loadClass("org.apache.sshd.server.shell.ProcessShellFactory")
                    val factory = if (OS.CURRENT == OS.Windows) {
                        processShellFactoryClass.getConstructor(String::class.java, Array<String>::class.java)
                            .newInstance("cmd.exe", arrayOf("/c", command))
                    } else {
                        processShellFactoryClass.getConstructor(String::class.java, Array<String>::class.java)
                            .newInstance("/bin/sh", arrayOf("-c", command))
                    }
                    val channelSessionClass = classLoader.loadClass("org.apache.sshd.server.channel.ChannelSession")
                    val createShellMethod =
                        ReflectionUtils.findMethod(processShellFactoryClass, "createShell", channelSessionClass)
                    return@newProxyInstance createShellMethod.invoke(factory, channel)
                }
                handleDefaultProxyMethod(proxy, method, args, commandFactoryClass)
            }
        ReflectionUtils.findMethod(sshServerClass, "setCommandFactory", commandFactoryClass)
            .invoke(sshServerInstance, commandFactoryProxy)

        val sftpSubsystemFactoryClass = classLoader.loadClass("org.apache.sshd.sftp.server.SftpSubsystemFactory")
        val sftpFactory = sftpSubsystemFactoryClass.getDeclaredConstructor().newInstance()

        val sftpEventListenerClass = classLoader.loadClass("org.apache.sshd.sftp.server.SftpEventListener")
        val sftpEventListenerProxy =
            Proxy.newProxyInstance(classLoader, arrayOf(sftpEventListenerClass)) { proxy, method, args ->
                when (method.name) {
                    "opening" -> {
                        val localHandle = args[2]
                        val fileHandleClass = classLoader.loadClass("org.apache.sshd.sftp.server.FileHandle")
                        if (fileHandleClass.isInstance(localHandle)) {
                            val openOptions = ReflectionUtils.findMethod(fileHandleClass, "getOpenOptions")
                                .invoke(localHandle) as? Collection<*>

                            val ioUtilsClass = classLoader.loadClass("org.apache.sshd.common.util.io.IoUtils")
                            val writeableOptions =
                                ioUtilsClass.getField("WRITEABLE_OPEN_OPTIONS").get(null) as? Collection<*>

                            if (openOptions != null && writeableOptions != null && openOptions.any { it in writeableOptions }) {
                                val file = ReflectionUtils.findMethod(fileHandleClass, "getFile")
                                    .invoke(localHandle) as? java.nio.file.Path
                                if (file != null) {
                                    val parent = file.parent
                                    if (parent != null && !Files.exists(parent)) {
                                        Files.createDirectories(parent)
                                        httpDashboardForm.showMockServerLog("完成创建目录: ${parent}\n")
                                    }
                                }
                            }
                        }
                        null
                    }

                    "closed" -> {
                        val localHandle = args[2]
                        val fileHandleClass = classLoader.loadClass("org.apache.sshd.sftp.server.FileHandle")
                        if (fileHandleClass.isInstance(localHandle)) {
                            val file = ReflectionUtils.findMethod(fileHandleClass, "getFile")
                                .invoke(localHandle) as? java.nio.file.Path
                            if (file != null) {
                                httpDashboardForm.showMockServerLog("完成处理: ${file}\n")
                            }
                        }
                        null
                    }

                    else -> handleDefaultProxyMethod(proxy, method, args, sftpEventListenerClass)
                }
            }

        ReflectionUtils.findMethod(sftpSubsystemFactoryClass, "addSftpEventListener", sftpEventListenerClass)
            .invoke(sftpFactory, sftpEventListenerProxy)

        ReflectionUtils.findMethod(sshServerClass, "setSubsystemFactories", List::class.java)
            .invoke(sshServerInstance, listOf(sftpFactory))

        this.sshServer = sshServerInstance

        ReflectionUtils.findMethod(sshServerClass, "start").invoke(sshServerInstance)

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.sftp.server.start", port) + "\n")
    }

    override fun stopServer() {
        if (sshServer != null) {
            ReflectionUtils.findMethod(sshServer!!.javaClass, "stop").invoke(sshServer)
        }
        httpDashboardForm.showMockServerLog("Sftp Server stopped\n")
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

    private fun handleDefaultProxyMethod(
        proxy: Any,
        method: Method,
        args: Array<Any>?,
        actualClz: Class<*>,
    ): Any? {
        return when (method.name) {
            "toString" -> "${actualClz.name}Proxy"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.get(0)
            else -> {
                if (method.isDefault) {
                    InvocationHandler.invokeDefault(proxy, method, *(args ?: emptyArray<Any?>()))
                } else {
                    null
                }
            }
        }
    }
}