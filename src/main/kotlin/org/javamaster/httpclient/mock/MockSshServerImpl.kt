package org.javamaster.httpclient.mock

import com.intellij.util.system.OS
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.common.keyprovider.KeyPairProvider
import org.apache.sshd.common.session.SessionContext
import org.apache.sshd.common.util.GenericUtils
import org.apache.sshd.common.util.io.IoUtils
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.command.CommandFactory
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.shell.InteractiveProcessShellFactory
import org.apache.sshd.server.shell.ProcessShellFactory
import org.apache.sshd.sftp.server.FileHandle
import org.apache.sshd.sftp.server.Handle
import org.apache.sshd.sftp.server.SftpEventListener
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.MockSshServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.KeyUtils
import org.javamaster.httpclient.utils.PluginUtils
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption
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
    private var sshServer: SshServer? = null

    override fun startServer(paramMap: MultiValueMap<String, String>) {
        val staticFolder = checkStaticFolder(paramMap.getFirst(ParamEnum.STATIC_FOLDER.param))
        val user = paramMap["username"]?.firstOrNull()
        val pwd = paramMap["password"]?.firstOrNull()

        val sshServer = SshServer.setUpDefaultServer()
        sshServer.setPort(port)

        sshServer.keyPairProvider = object : KeyPairProvider {
            override fun loadKeys(sessionContext: SessionContext): Iterable<KeyPair> {
                val file = File(PluginUtils.getPluginPath(), "lib/keyPair.dat")
                if (file.exists()) {
                    val keyPair = ObjectInputStream(FileInputStream(file)).use {
                        it.readObject() as KeyPair
                    }

                    return listOf(keyPair)
                }

                val keyPair = KeyUtils.generateKeyPair("RSA", 2048, SecureRandom())

                ObjectOutputStream(FileOutputStream(file)).use {
                    it.writeObject(keyPair)
                }

                return listOf(keyPair)
            }
        }

        sshServer.passwordAuthenticator = object : PasswordAuthenticator {
            override fun authenticate(username: String, password: String, session: ServerSession): Boolean {
                return user == username && pwd == password
            }
        }

        val fsFactory = VirtualFileSystemFactory()
        fsFactory.setUserHomeDir(user, staticFolder.toPath())
        sshServer.setFileSystemFactory(fsFactory)

        sshServer.shellFactory = InteractiveProcessShellFactory.INSTANCE

        sshServer.commandFactory = object : CommandFactory {
            override fun createCommand(channel: ChannelSession, command: String): Command {
                return if (OS.CURRENT == OS.Windows) {
                    ProcessShellFactory("cmd.exe", "/c", command).createShell(channel)
                } else {
                    ProcessShellFactory("/bin/sh", "-c", command).createShell(channel)
                }
            }
        }

        val sftpFactory = SftpSubsystemFactory()
        sftpFactory.addSftpEventListener(object : SftpEventListener {
            override fun opening(session: ServerSession?, remoteHandle: String?, localHandle: Handle) {
                if (localHandle is FileHandle) {
                    // 检查是否为写入操作
                    if (GenericUtils.containsAny<StandardOpenOption?>(
                            localHandle.openOptions,
                            IoUtils.WRITEABLE_OPEN_OPTIONS
                        )
                    ) {
                        val file = localHandle.file
                        val parent = file.parent
                        if (parent != null && !Files.exists(parent)) {
                            Files.createDirectories(parent)
                            httpDashboardForm.showMockServerLog("完成创建目录: ${parent}\n")
                        }
                    }
                }
            }

            override fun closed(
                session: ServerSession?,
                remoteHandle: String?,
                localHandle: Handle,
                thrown: Throwable?,
            ) {
                val file = localHandle.file
                httpDashboardForm.showMockServerLog("完成处理: ${file}\n")
            }
        })
        sshServer.subsystemFactories = listOf(sftpFactory)

        this.sshServer = sshServer

        sshServer.start()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.sftp.server.start", port) + "\n")
    }

    override fun stopServer() {
        sshServer?.stop()

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
}