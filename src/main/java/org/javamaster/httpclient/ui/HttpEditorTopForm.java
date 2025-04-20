package org.javamaster.httpclient.ui;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.popup.PopupFactoryImpl;
import kotlin.Triple;
import org.javamaster.httpclient.HttpIcons;
import org.javamaster.httpclient.handler.RunFileHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Set;

import static org.javamaster.httpclient.nls.NlsBundle.INSTANCE;

/**
 * @author yudong
 */
public class HttpEditorTopForm extends JComponent {
    public static final Key<HttpEditorTopForm> KEY = Key.create("httpRequest.httpEditorTopForm");

    public final VirtualFile file;
    public JPanel mainPanel;
    private JComboBox<String> envComboBox;
    private JButton showVariableBtn;
    private JBLabel runAllLabel;
    private JBLabel addHttpLabel;
    private JLabel exampleLabel;

    private final @Nullable Module module;

    public HttpEditorTopForm(VirtualFile file, @Nullable Module module, Project project) {
        this.file = file;
        this.module = module;

        resetEnvCombo();

        Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

        envComboBox.setCursor(cursor);
        envComboBox.addActionListener(e -> DaemonCodeAnalyzer.getInstance(project).restart());

        ActionManager actionManager = ActionManager.getInstance();
        JBPopupFactory popupFactory = PopupFactoryImpl.getInstance();

        addHttpLabel.setCursor(cursor);
        addHttpLabel.setIcon(AllIcons.General.Add);
        addHttpLabel.setBorder(null);
        addHttpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionGroup group = (ActionGroup) actionManager.getAction("addToHttp");

                ListPopup listPopup = popupFactory.createActionGroupPopup(INSTANCE.nls("new"), group,
                        DataContext.EMPTY_CONTEXT, true, null, 10);

                Point point = e.getPoint();
                listPopup.show(new RelativePoint(e.getComponent(), new Point(point.x - 20, point.y + 20)));
            }
        });

        runAllLabel.setCursor(cursor);
        runAllLabel.setBorder(null);

        switchRunBtnToInitialing();

        runAllLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (runAllLabel.getIcon() == HttpIcons.STOP) {
                    switchRunBtnToInitialing();

                    RunFileHandler.INSTANCE.stopRunning();
                } else {
                    switchRunBtnToStopping();

                    RunFileHandler.INSTANCE.runRequests(project, HttpEditorTopForm.this,
                            HttpEditorTopForm.this::switchRunBtnToInitialing);
                }
            }
        });

        exampleLabel.setCursor(cursor);
        exampleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ActionGroup group = (ActionGroup) actionManager.getAction("exampleHttp");

                ListPopup listPopup = popupFactory.createActionGroupPopup(INSTANCE.nls("http.examples"), group,
                        DataContext.EMPTY_CONTEXT, true, null, 10);

                Point point = e.getPoint();
                listPopup.show(new RelativePoint(e.getComponent(), new Point(point.x - 20, point.y + 20)));
            }
        });

        showVariableBtn.setCursor(cursor);
        showVariableBtn.addActionListener(e -> {
            ViewVariableForm viewVariableForm = new ViewVariableForm(project);
            viewVariableForm.show();
        });
    }

    public void switchRunBtnToInitialing() {
        runAllLabel.setToolTipText(INSTANCE.nls("run.all.tooltip"));
        runAllLabel.setIcon(HttpIcons.RUN_ALL);
    }

    public void switchRunBtnToStopping() {
        runAllLabel.setToolTipText(INSTANCE.nls("stop.running"));
        runAllLabel.setIcon(HttpIcons.STOP);
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
