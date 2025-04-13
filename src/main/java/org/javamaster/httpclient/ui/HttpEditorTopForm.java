package org.javamaster.httpclient.ui;

import com.google.common.collect.Maps;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import kotlin.Triple;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.handler.RunFileHandler;
import org.javamaster.httpclient.utils.NotifyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.httpclient.env.EnvFileService.ENV_FILE_NAME;
import static org.javamaster.httpclient.env.EnvFileService.PRIVATE_ENV_FILE_NAME;

/**
 * @author yudong
 */
public class HttpEditorTopForm extends JComponent {
    public static final Key<HttpEditorTopForm> KEY = Key.create("httpRequest.httpEditorTopForm");

    private static final LinkedHashMap<String, String> optionMap = Maps.newLinkedHashMap();

    static {
        optionMap.put("Examples And Environments", null);
        optionMap.put("Create env.json file", ENV_FILE_NAME);
        optionMap.put("Create env.private.json file", PRIVATE_ENV_FILE_NAME);
        optionMap.put("GET requests", "examples/get-requests.http");
        optionMap.put("POST requests", "examples/post-requests.http");
        optionMap.put("Request with Authorization", "examples/requests-with-authorization.http");
        optionMap.put("Request with tests and Scripts", "examples/requests-with-scripts.http");
        optionMap.put("Response presentations", "examples/responses-presentation.http");
        optionMap.put("Websocket requests", "examples/ws-requests.http");
        optionMap.put("Dubbo requests", "examples/dubbo-requests.http");
        optionMap.put("show CryptoJS file", "examples/crypto-js.js");
    }

    public final VirtualFile file;
    public JPanel mainPanel;
    private JComboBox<String> envComboBox;
    private JComboBox<String> exampleComboBox;
    private JButton showVariableBtn;
    private JButton runAllBtn;

    private final @Nullable Module module;
    private final Project project;

    public HttpEditorTopForm(VirtualFile file, @Nullable Module module, Project project) {
        this.file = file;
        this.module = module;
        this.project = project;

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addAll(optionMap.keySet());
        exampleComboBox.setModel(model);
        exampleComboBox.setSelectedIndex(0);

        runAllBtn.setBorder(null);

        switchRunBtnToInitialing();

        runAllBtn.addActionListener(event -> {
            if (runAllBtn.getIcon() == HttpIcons.STOP) {
                switchRunBtnToInitialing();

                RunFileHandler.INSTANCE.stopRunning();
            } else {
                switchRunBtnToStopping();

                RunFileHandler.INSTANCE.runRequests(project, this, this::switchRunBtnToInitialing);
            }
        });

        exampleComboBox.addActionListener(e -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String selectedItem = (String) exampleComboBox.getSelectedItem();

            exampleComboBox.setSelectedIndex(0);

            String option = optionMap.get(selectedItem);
            if (option == null) {
                return;
            }

            if (ENV_FILE_NAME.equals(option)) {
                createAndReInitEnvCompo(false);
            } else if (PRIVATE_ENV_FILE_NAME.equals(option)) {
                createAndReInitEnvCompo(true);
            } else {
                URL url = classLoader.getResource(option);
                VirtualFile virtualFile = VfsUtil.findFileByURL(url);
                //noinspection DataFlowIssue
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }
        });

        showVariableBtn.addActionListener(e -> {
            ViewVariableForm viewVariableForm = new ViewVariableForm(project);
            viewVariableForm.show();
        });
    }

    public void switchRunBtnToInitialing() {
        runAllBtn.setToolTipText("Run all requests of the file(think time is 2 seconds)");
        runAllBtn.setIcon(HttpIcons.RUN_ALL);
    }

    public void switchRunBtnToStopping() {
        runAllBtn.setToolTipText("Stop running");
        runAllBtn.setIcon(HttpIcons.STOP);
    }

    public void createAndReInitEnvCompo(boolean isPrivate) {
        String envFileName = isPrivate ? PRIVATE_ENV_FILE_NAME : ENV_FILE_NAME;

        VirtualFile envFile = EnvFileService.Companion.createEnvFile(envFileName, isPrivate, project);
        exampleComboBox.setSelectedIndex(0);
        if (envFile == null) {
            NotifyUtil.INSTANCE.notifyWarn(project, envFileName + " Environment file already exist!");
            return;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        fileEditorManager.openFile(envFile, true);

        NotifyUtil.INSTANCE.notifyInfo(project, "The environment file was successfully created: " + envFileName);

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

                LinkedHashSet<String> set = new LinkedHashSet<>();
                set.add("dev");
                set.add("uat");
                set.add("pro");
                httpEditorTopForm.initEnvCombo(set);
            }
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void initEnvCombo(Set<String> presetEnvSet) {
        presetEnvSet.forEach(it -> envComboBox.addItem(it));

        setSelectEnv("uat");
    }

    public String getSelectedEnv() {
        return (String) envComboBox.getSelectedItem();
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

    public static @Nullable String getSelectedEnv(Project project) {
        HttpEditorTopForm httpEditorTopForm = getSelectedEditorTopForm(project);
        if (httpEditorTopForm == null) {
            return null;
        }

        return httpEditorTopForm.getSelectedEnv();
    }

    public static @Nullable Triple<String, VirtualFile, @Nullable Module> getTriple(Project project) {
        HttpEditorTopForm topForm = getSelectedEditorTopForm(project);
        if (topForm == null) {
            return null;
        }

        return new Triple<>(topForm.getSelectedEnv(), topForm.file, topForm.module);
    }

    public static @Nullable HttpEditorTopForm getSelectedEditorTopForm(Project project) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = editorManager.getSelectedEditor();
        if (selectedEditor == null) {
            return null;
        }

        return selectedEditor.getUserData(HttpEditorTopForm.KEY);
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
}
