package org.javamaster.httpclient.ui;

import com.google.common.collect.Maps;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.js.JsScriptExecutor;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewVariableForm extends DialogWrapper {
    private JPanel contentPane;
    private JTable table;

    public ViewVariableForm(Project project) {
        super(project);
        setModal(false);

        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        //noinspection DataFlowIssue
        VirtualFile virtualFile = selectedEditor.getFile();
        PsiFile httpFile = PsiUtil.getPsiFile(project, virtualFile);
        String selectedEnv = HttpEditorTopForm.getCurrentEditorSelectedEnv(project);
        String httpFileParentPath = HttpEditorTopForm.getHttpFileParentPath();

        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("js全局变量", "---------");

        JsScriptExecutor jsScriptExecutor = new JsScriptExecutor(project, httpFileParentPath);
        VariableResolver variableResolver = new VariableResolver(jsScriptExecutor, httpFile, selectedEnv);

        Map<String, String> variableMap = variableResolver.getJsGlobalVariables();
        map.putAll(variableMap);

        LinkedHashMap<String, String> fileGlobalVariables = variableResolver.getFileGlobalVariables();
        map.put("http全局变量", "---------");
        map.putAll(fileGlobalVariables);

        map.put("环境文件变量", "---------");
        Map<String, String> envMap = EnvFileService.Companion.getEnvVariables(project);
        map.putAll(envMap);

        Object[][] rowData = new Object[map.size()][2];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            rowData[i][0] = entry.getKey();
            rowData[i][1] = entry.getValue();
            i++;
        }

        DefaultTableModel model = new DefaultTableModel(rowData, new String[]{"key", "value"}) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
