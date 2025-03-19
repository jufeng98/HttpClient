package org.javamaster.httpclient.ui;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.utils.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.httpclient.env.EnvFileService.ENV_FILE_NAME;
import static org.javamaster.httpclient.env.EnvFileService.PRIVATE_ENV_FILE_NAME;

/**
 * @author yudong
 */
public class HttpEditorTopForm extends JComponent {
    public static final Key<HttpEditorTopForm> KEY = Key.create("httpRequest.httpEditorTopForm");
    public JPanel mainPanel;
    private JComboBox<String> envComboBox;
    private JComboBox<String> exampleComboBox;
    private JButton showVariableBtn;
    private Project project;
    private static String httpFileParentPath;

    public HttpEditorTopForm() {
        exampleComboBox.addActionListener(e -> {
            ClassLoader classLoader = getClass().getClassLoader();
            Object selectedItem = exampleComboBox.getSelectedItem();

            URL url = null;
            if (Objects.equals(selectedItem, "GET requests")) {
                url = classLoader.getResource("examples/get-requests.http");
            } else if (Objects.equals(selectedItem, "POST requests")) {
                url = classLoader.getResource("examples/post-requests.http");
            } else if (Objects.equals(selectedItem, "Request with Authorization")) {
                url = classLoader.getResource("examples/requests-with-authorization.http");
            } else if (Objects.equals(selectedItem, "Request with tests and Scripts")) {
                url = classLoader.getResource("examples/requests-with-scripts.http");
            } else if (Objects.equals(selectedItem, "Response presentations")) {
                url = classLoader.getResource("examples/responses-presentation.http");
            } else if (Objects.equals(selectedItem, "Websocket requests")) {
                url = classLoader.getResource("examples/ws-requests.http");
            } else if (Objects.equals(selectedItem, "Dubbo requests")) {
                url = classLoader.getResource("examples/dubbo-requests.http");
            } else if (Objects.equals(selectedItem, "show CryptoJS file")) {
                url = classLoader.getResource("examples/crypto-js.js");
            } else if (Objects.equals(selectedItem, "Create env.json file")) {
                createAndReInitEnvCompo(ENV_FILE_NAME, false);
            } else if (Objects.equals(selectedItem, "Create env.private.json file")) {
                createAndReInitEnvCompo(PRIVATE_ENV_FILE_NAME, true);
            }

            if (url != null) {
                VirtualFile virtualFile = VfsUtil.findFileByURL(url);
                //noinspection DataFlowIssue
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
                exampleComboBox.setSelectedIndex(0);
            }
        });
        showVariableBtn.addActionListener(e -> {
            ViewVariableForm viewVariableForm = new ViewVariableForm(project);
            viewVariableForm.show();
        });
    }

    private void createAndReInitEnvCompo(String fileName, boolean isPrivate) {
        VirtualFile envFile = EnvFileService.Companion.createEnvFile(fileName, isPrivate, project);
        exampleComboBox.setSelectedIndex(0);
        if (envFile == null) {
            NotifyUtil.INSTANCE.notifyWarn(project, fileName + "环境文件已存在!");
            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(envFile, true);

        NotifyUtil.INSTANCE.notifyInfo(project, "成功创建环境文件:" + fileName);

        try {
            Module module = ModuleUtil.findModuleForFile(envFile, project);
            if (module == null) {
                return;
            }

            FileEditor @NotNull [] allEditors = fileEditorManager.getAllEditors();
            for (FileEditor editor : allEditors) {
                HttpEditorTopForm httpEditorTopForm = editor.getUserData(HttpEditorTopForm.KEY);
                if (httpEditorTopForm == null) {
                    continue;
                }

                httpEditorTopForm.initEnvCombo(module, envFile.getParent().getPath());

            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static @Nullable String getCurrentEditorSelectedEnv(Project project) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = editorManager.getSelectedEditor();
        if (selectedEditor == null) {
            return null;
        }

        HttpEditorTopForm httpEditorTopForm = selectedEditor.getUserData(HttpEditorTopForm.KEY);
        if (httpEditorTopForm == null) {
            return null;
        }

        return httpEditorTopForm.getCurrentEditorSelectedEnv();
    }

    public static void setCurrentEditorSelectedEnv(String httpFilePath, Project project, String env) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = editorManager.getSelectedEditor();
        if (selectedEditor == null) {
            return;
        }

        VirtualFile virtualFile = selectedEditor.getFile();
        if (virtualFile == null || !Objects.equals(httpFilePath, virtualFile.getPath())) {
            return;
        }

        HttpEditorTopForm httpEditorTopForm = selectedEditor.getUserData(HttpEditorTopForm.KEY);
        if (httpEditorTopForm == null) {
            return;
        }

        httpEditorTopForm.setSelectEnv(env);
    }

    public void initEnvCombo(Module module, String httpFileParentPath) {
        project = module.getProject();
        HttpEditorTopForm.httpFileParentPath = httpFileParentPath;

        EnvFileService envFileService = EnvFileService.Companion.getService(project);
        Set<String> presetEnvSet = envFileService.getPresetEnvList(httpFileParentPath);

        presetEnvSet.forEach(it -> envComboBox.addItem(it));

        setSelectEnv("uat");
    }

    public String getCurrentEditorSelectedEnv() {
        return (String) envComboBox.getSelectedItem();
    }

    public static String getHttpFileParentPath() {
        return httpFileParentPath;
    }

    public void setSelectEnv(String env) {
        int itemCount = envComboBox.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            String item = envComboBox.getItemAt(i);
            if (!Objects.equals(item, env)) {
                continue;
            }

            envComboBox.setSelectedIndex(i);
            break;
        }
    }

}
