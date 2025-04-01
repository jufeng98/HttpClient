package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import kotlin.Pair;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.js.JsExecutor;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.javamaster.httpclient.resolve.VariableResolver.ENV_PREFIX;
import static org.javamaster.httpclient.resolve.VariableResolver.PROPERTY_PREFIX;

public class ViewVariableForm extends DialogWrapper {
    private final Project project;
    private JPanel contentPane;
    private JBTable table;

    public ViewVariableForm(Project project) {
        super(project);
        this.project = project;
        setModal(false);
        setResizable(false);
        setOKButtonText("Close");
        setTitle("Available Variables");

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);

        List<Pair<String, Map<String, String>>> resList = collectVariableMapList();

        int rows = calRows(resList);

        Object[][] rowData = createRowData(resList, rows);

        DefaultTableModel model = new DefaultTableModel(rowData, new String[]{"key", "value"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);

        table.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
            if (value instanceof JLabel) {
                return (Component) value;
            }

            String text = (String) value;
            JBTextField textField = new JBTextField(text);

            if (column == 1 && text.length() > 100) {
                textField.setToolTipText(text);
            }

            if (isSelected && hasFocus) {
                textField.setBackground(table.getSelectionBackground());
                textField.setForeground(table.getSelectionForeground());
            } else {
                textField.setBackground(table.getBackground());
                textField.setForeground(table.getForeground());
            }

            return textField;
        });

        init();
    }

    private List<Pair<String, Map<String, String>>> collectVariableMapList() {
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        //noinspection DataFlowIssue
        VirtualFile virtualFile = selectedEditor.getFile();

        PsiFile httpFile = PsiUtil.getPsiFile(project, virtualFile);

        String selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

        List<Pair<String, Map<String, String>>> resList = Lists.newArrayList();

        JsExecutor jsExecutor = new JsExecutor(project, httpFile, "");
        VariableResolver variableResolver = new VariableResolver(jsExecutor, httpFile, selectedEnv);

        LinkedHashMap<String, String> fileGlobalVariables = variableResolver.getFileGlobalVariables();
        resList.add(new Pair<>("http file global variables(Highest priority)", fileGlobalVariables));

        Map<String, String> variableMap = variableResolver.getJsGlobalVariables();
        resList.add(new Pair<>("js global variables(middle priority)", variableMap));

        Map<String, String> envMap = EnvFileService.Companion.getEnvMap(project, false);
        resList.add(new Pair<>("environment file variables(lower priority)", envMap));

        Map<String, String> propMap = Maps.newLinkedHashMap();
        System.getProperties().forEach((key, value) -> propMap.put(PROPERTY_PREFIX + "." + key, value + ""));
        resList.add(new Pair<>("System property variables(lowest priority)", propMap));

        Map<String, String> eMap = Maps.newLinkedHashMap();
        System.getenv().forEach((key, value) -> eMap.put(ENV_PREFIX + "." + key, value));
        resList.add(new Pair<>("System environment variables(lowest priority)", eMap));

        return resList;
    }

    private int calRows(List<Pair<String, Map<String, String>>> resList) {
        int rows = 0;
        for (Pair<String, Map<String, String>> pair : resList) {
            rows++;
            Map<String, String> map = pair.getSecond();
            if (map.isEmpty()) {
                rows++;
            } else {
                rows += map.size();
            }
        }
        return rows;
    }

    private Object[][] createRowData(List<Pair<String, Map<String, String>>> resList, int rows) {
        String repeat = "-" .repeat(80);
        Object[][] rowData = new Object[rows][2];
        int i = 0;
        for (Pair<String, Map<String, String>> pair : resList) {
            String desc = pair.getFirst();

            JLabel label = new JLabel(desc);
            label.setFont(new Font(label.getFont().getName(), Font.BOLD, 13));

            rowData[i][0] = label;
            rowData[i][1] = repeat;
            i++;

            Map<String, String> map = pair.getSecond();
            if (map.isEmpty()) {
                rowData[i][0] = "No data available";
                rowData[i][1] = "";
                i++;
            } else {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    rowData[i][0] = entry.getKey();
                    rowData[i][1] = entry.getValue();
                    i++;
                }
            }
        }

        return rowData;
    }

    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
