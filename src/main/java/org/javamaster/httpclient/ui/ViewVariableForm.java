package org.javamaster.httpclient.ui;

import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class ViewVariableForm extends DialogWrapper {
    private JPanel contentPane;
    private JTable table;

    public ViewVariableForm(Project project) {
        super(project);
        setModal(false);

        Map<String, String> map = Maps.newLinkedHashMap();

        VariableResolver variableResolver = VariableResolver.Companion.getService(project);
        Map<String, String> variableMap = variableResolver.getVariables();

        map.put("js全局变量", "---");
        map.putAll(variableMap);

        Map<String, String> envMap = EnvFileService.Companion.getEnvVariables(project);

        map.put("环境文件变量", "---");
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
