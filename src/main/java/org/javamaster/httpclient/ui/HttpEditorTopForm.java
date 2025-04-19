package org.javamaster.httpclient.ui;

import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.PopupFactoryImpl;
import kotlin.Triple;
import org.apache.commons.compress.utils.Lists;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.handler.RunFileHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.httpclient.nls.NlsBundle.INSTANCE;

/**
 * @author yudong
 */
public class HttpEditorTopForm extends JComponent {
    public static final Key<HttpEditorTopForm> KEY = Key.create("httpRequest.httpEditorTopForm");

    private final List<String> options = Lists.newArrayList();
    private final LinkedHashMap<Integer, String> optionIndexMap = Maps.newLinkedHashMap();

    {
        options.add(INSTANCE.nls("http.examples"));
        optionIndexMap.put(0, null);
        options.add(INSTANCE.nls("get.requests"));
        optionIndexMap.put(1, "examples/get-requests.http");
        options.add(INSTANCE.nls("post.requests"));
        optionIndexMap.put(2, "examples/post-requests.http");
        options.add(INSTANCE.nls("request.with.authorization"));
        optionIndexMap.put(3, "examples/requests-with-authorization.http");
        options.add(INSTANCE.nls("request.with.tests.and.scripts"));
        optionIndexMap.put(4, "examples/requests-with-scripts.http");
        options.add(INSTANCE.nls("response.presentations"));
        optionIndexMap.put(5, "examples/responses-presentation.http");
        options.add(INSTANCE.nls("websocket.requests"));
        optionIndexMap.put(6, "examples/ws-requests.http");
        options.add(INSTANCE.nls("dubbo.requests"));
        optionIndexMap.put(7, "examples/dubbo-requests.http");
        options.add(INSTANCE.nls("show.cryptojs.file"));
        optionIndexMap.put(8, "examples/crypto-js.js");
    }

    public final VirtualFile file;
    public JPanel mainPanel;
    private JComboBox<String> envComboBox;
    private JComboBox<String> exampleComboBox;
    private JButton showVariableBtn;
    private JButton runAllBtn;
    private JButton addBtn;

    private final @Nullable Module module;

    public HttpEditorTopForm(VirtualFile file, @Nullable Module module, Project project) {
        this.file = file;
        this.module = module;

        resetEnvCombo();

        addBtn.setIcon(AllIcons.General.Add);
        addBtn.setBorder(null);
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionManager actionManager = ActionManager.getInstance();
                ActionGroup group = (ActionGroup) actionManager.getAction("addToHttp");

                JBPopupFactory popupFactory = PopupFactoryImpl.getInstance();
                ListPopup listPopup = popupFactory.createActionGroupPopup(INSTANCE.nls("new"), group,
                        DataContext.EMPTY_CONTEXT, true, null, 10);

                Point point = e.getPoint();
                listPopup.show(new RelativePoint(e.getComponent(), new Point(point.x - 20, point.y + 20)));
            }
        });

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addAll(options);
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

            String option = optionIndexMap.get(exampleComboBox.getSelectedIndex());

            exampleComboBox.setSelectedIndex(0);

            if (option == null) {
                return;
            }

            URL url = classLoader.getResource(option);
            VirtualFile virtualFile = VfsUtil.findFileByURL(url);
            //noinspection DataFlowIssue
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        });

        showVariableBtn.addActionListener(e -> {
            ViewVariableForm viewVariableForm = new ViewVariableForm(project);
            viewVariableForm.show();
        });
    }

    public void switchRunBtnToInitialing() {
        runAllBtn.setToolTipText(INSTANCE.nls("run.all.tooltip"));
        runAllBtn.setIcon(HttpIcons.RUN_ALL);
    }

    public void switchRunBtnToStopping() {
        runAllBtn.setToolTipText(INSTANCE.nls("stop.running"));
        runAllBtn.setIcon(HttpIcons.STOP);
    }

    private void resetEnvCombo() {
        int itemCount = envComboBox.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            envComboBox.removeItemAt(i);
        }

        envComboBox.addItem(INSTANCE.nls("no.env"));
    }

    public void initEnvCombo(Set<String> presetEnvSet) {
        resetEnvCombo();

        presetEnvSet.forEach(it -> envComboBox.addItem(it));

        setSelectEnv("uat");
    }

    public @Nullable String getSelectedEnv() {
        int selectedIndex = envComboBox.getSelectedIndex();
        if (selectedIndex == 0) {
            return null;
        }

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
