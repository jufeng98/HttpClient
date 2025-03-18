package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import kotlin.Pair;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.js.JsExecutor;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ViewVariableForm extends DialogWrapper {
    private JPanel contentPane;
    private JTable table;

    public ViewVariableForm(Project project) {
        super(project);
        setModal(false);
        setResizable(false);

        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        //noinspection DataFlowIssue
        VirtualFile virtualFile = selectedEditor.getFile();
        PsiFile httpFile = PsiUtil.getPsiFile(project, virtualFile);
        String selectedEnv = HttpEditorTopForm.getCurrentEditorSelectedEnv(project);
        String httpFileParentPath = HttpEditorTopForm.getHttpFileParentPath();

        List<Pair<String, Map<String, String>>> resList = Lists.newArrayList();

        JsExecutor jsExecutor = new JsExecutor(project, httpFileParentPath, "");
        VariableResolver variableResolver = new VariableResolver(jsExecutor, httpFile, selectedEnv);

        LinkedHashMap<String, String> fileGlobalVariables = variableResolver.getFileGlobalVariables();
        resList.add(new Pair<>("http文件全局变量(优先级最高)", fileGlobalVariables));

        Map<String, String> variableMap = variableResolver.getJsGlobalVariables();
        resList.add(new Pair<>("js全局变量(优先级次之)", variableMap));

        Map<String, String> envMap = EnvFileService.Companion.getEnvVariables(project);
        resList.add(new Pair<>("环境文件变量(优先级最低)", envMap));

        int size = 0;
        for (Pair<String, Map<String, String>> pair : resList) {
            size++;
            Map<String, String> map = pair.getSecond();
            if (map.isEmpty()) {
                size++;
            } else {
                size += map.size();
            }
        }

        String repeat = "-".repeat(80);
        Object[][] rowData = new Object[size][2];
        int i = 0;
        for (Pair<String, Map<String, String>> pair : resList) {
            String desc = pair.getFirst();
            rowData[i][0] = desc;
            rowData[i][1] = repeat;
            i++;

            Map<String, String> map = pair.getSecond();
            if (map.isEmpty()) {
                rowData[i][0] = "   暂无数据";
                rowData[i][1] = "";
                i++;
            } else {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    rowData[i][0] = "   " + entry.getKey();
                    rowData[i][1] = entry.getValue();
                    i++;
                }
            }
        }

        DefaultTableModel model = new DefaultTableModel(rowData, new String[]{"key", "value"}) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);

        init();
    }

    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
