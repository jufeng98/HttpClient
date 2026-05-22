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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import kotlin.Triple;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.js.JsExecutor;
import org.javamaster.httpclient.js.support.jsObject.JsGlobalVariablesHolder;
import org.javamaster.httpclient.listener.VariableFormBtnClickListener;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.javamaster.httpclient.resolve.VariableResolver.ENV_PREFIX;
import static org.javamaster.httpclient.resolve.VariableResolver.PROPERTY_PREFIX;

public class ViewVariableForm extends DialogWrapper {
    private final Project project;
    private JPanel contentPane;
    private JBTable table;

    private static final JBColor addColor = new JBColor(new Color(64, 158, 255), new Color(64, 158, 255));
    private static final JBColor editColor = new JBColor(new Color(230, 162, 60), new Color(230, 162, 60));
    private static final JBColor delColor = new JBColor(new Color(232, 47, 47), new Color(232, 47, 47));
    private static final JLabel emptyLabel = new JLabel("");

    public static final int TYPE_FILE_VARIABLE = 1;
    public static final int TYPE_JS_GLOBAL_VARIABLE = 2;
    public static final int TYPE_ENV_VARIABLE = 3;
    public static final int TYPE_SYS = 4;
    public static final int TYPE_HEADER = 5;

    public ViewVariableForm(Project project) {
        super(project);
        this.project = project;
        setModal(false);
        setResizable(false);
        setOKButtonText(NlsBundle.INSTANCE.nls("close"));
        setTitle(NlsBundle.INSTANCE.nls("available.variables"));

        initTable();

        init();
    }

    public void initTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);

        List<Triple<String, Map<String, String>, Integer>> resList = collectVariableMapList();

        int rows = calRows(resList);

        Object[][] rowData = createRowData(resList, rows);

        DefaultTableModel model = new DefaultTableModel(rowData, new String[]{"key", "value", "operation"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        table.setModel(model);

        table.setCellSelectionEnabled(true);

        table.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus,
                                                row, column) -> {
            if (value instanceof Component) {
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

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(280);
        columnModel.getColumn(1).setPreferredWidth(720);

        TableColumn operationColumn = columnModel.getColumn(2);
        operationColumn.setMinWidth(200);
        operationColumn.setMaxWidth(200);
        operationColumn.setPreferredWidth(200);
        operationColumn.setCellRenderer((table, value, isSelected, hasFocus, row, column) -> (Component) rowData[row][2]);
        operationColumn.setCellEditor(new OperationCellEditor(rowData));
    }

    private List<Triple<String, Map<String, String>, Integer>> collectVariableMapList() {
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        //noinspection DataFlowIssue
        VirtualFile virtualFile = selectedEditor.getFile();

        PsiFile httpFile = PsiUtil.getPsiFile(project, virtualFile);

        String selectedEnv = HttpEditorTopForm.getSelectedEnv(project);

        List<Triple<String, Map<String, String>, Integer>> resList = Lists.newArrayList();

        JsExecutor jsExecutor = new JsExecutor(project, httpFile, "");
        VariableResolver variableResolver = new VariableResolver(jsExecutor, httpFile, selectedEnv, project);

        LinkedHashMap<String, String> fileGlobalVariables = variableResolver.getFileGlobalVariables();
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("file.global.desc"), fileGlobalVariables, TYPE_FILE_VARIABLE));

        Map<String, String> jsGlovalVariableMap = JsGlobalVariablesHolder.INSTANCE.getJsGlobalVariables();
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("js.variable.desc"), jsGlovalVariableMap, TYPE_JS_GLOBAL_VARIABLE));

        Map<String, String> envMap = EnvFileService.Companion.getEnvMap(project, false);
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("environment.variable.desc"), envMap, TYPE_ENV_VARIABLE));

        @SuppressWarnings("rawtypes")
        Map globalHeaders = JsGlobalVariablesHolder.INSTANCE.getHeaders().getDataHolder();
        //noinspection rawtypes,unchecked
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("global.headers.desc"), globalHeaders, TYPE_HEADER));

        Map<String, String> propMap = Maps.newLinkedHashMap();
        System.getProperties().forEach((key, value) -> propMap.put(PROPERTY_PREFIX + "." + key, value + ""));
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("system.desc"), propMap, TYPE_SYS));

        Map<String, String> eMap = Maps.newLinkedHashMap();
        System.getenv().forEach((key, value) -> eMap.put(ENV_PREFIX + "." + key, value));
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("env.desc"), eMap, TYPE_SYS));

        return resList;
    }

    private int calRows(List<Triple<String, Map<String, String>, Integer>> resList) {
        int rows = 0;
        for (Triple<String, Map<String, String>, Integer> triple : resList) {
            rows++;
            Map<String, String> map = triple.getSecond();
            if (map.isEmpty()) {
                rows++;
            } else {
                if (map instanceof LinkedMultiValueMap) {
                    //noinspection rawtypes
                    rows += ((LinkedMultiValueMap) map).valueSize();
                } else {
                    rows += map.size();
                }
            }
        }
        return rows;
    }

    private Object[][] createRowData(List<Triple<String, Map<String, String>, Integer>> resList, int rows) {
        String repeat = "-".repeat(142);
        Object[][] rowData = new Object[rows][4];
        int i = 0;
        for (Triple<String, Map<String, String>, Integer> triple : resList) {
            String desc = triple.getFirst();
            Integer type = triple.getThird();

            JLabel label = new JLabel(desc);
            label.setFont(new Font(label.getFont().getName(), Font.BOLD, 13));

            rowData[i][0] = label;
            rowData[i][1] = repeat;

            boolean match = type == TYPE_FILE_VARIABLE || type == TYPE_JS_GLOBAL_VARIABLE || type == TYPE_ENV_VARIABLE || type == TYPE_HEADER;
            if (match) {
                rowData[i][2] = createPanelAdd(i, rowData, project);
                rowData[i][3] = type;
            } else {
                rowData[i][2] = emptyLabel;
                rowData[i][3] = -1;
            }

            i++;

            @SuppressWarnings("rawtypes")
            Map map = triple.getSecond();
            if (map.isEmpty()) {
                rowData[i][0] = NlsBundle.INSTANCE.nls("no.data.available");
                rowData[i][1] = "";
                rowData[i][2] = emptyLabel;
                rowData[i][3] = -1;
                i++;
            } else {
                for (Object obj : map.entrySet()) {
                    @SuppressWarnings("rawtypes")
                    Map.Entry entry = (Map.Entry) obj;
                    String key = (String) entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String) {
                        rowData[i][0] = key;
                        rowData[i][1] = value;

                        if (match) {
                            rowData[i][2] = createPanelOther(i, rowData, project);
                            rowData[i][3] = type;
                        } else {
                            rowData[i][2] = emptyLabel;
                            rowData[i][3] = -1;
                        }

                        i++;
                    } else if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> headerValues = (List<String>) value;
                        for (String headerValue : headerValues) {
                            rowData[i][0] = key;
                            rowData[i][1] = headerValue;

                            if (match) {
                                rowData[i][2] = createPanelOther(i, rowData, project);
                                rowData[i][3] = type;
                            } else {
                                rowData[i][2] = emptyLabel;
                                rowData[i][3] = -1;
                            }

                            i++;
                        }
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }

        return rowData;
    }

    public JPanel createPanelAdd(int row, Object[][] rowData, Project project) {
        JButton btnAdd = new JButton(NlsBundle.INSTANCE.nls("add"));
        btnAdd.setForeground(addColor);
        btnAdd.addActionListener(new VariableFormBtnClickListener(this, row, false, true, rowData, project));
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(btnAdd);
        return panel;
    }

    private JPanel createPanelOther(int row, Object[][] rowData, Project project) {
        JButton btnEdit = new JButton(NlsBundle.INSTANCE.nls("edit"));
        btnEdit.setForeground(editColor);
        btnEdit.addActionListener(new VariableFormBtnClickListener(this, row, false, false, rowData, project));

        JButton btnDel = new JButton(NlsBundle.INSTANCE.nls("del"));
        btnDel.setForeground(delColor);
        btnDel.addActionListener(new VariableFormBtnClickListener(this, row, true, false, rowData, project));

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(btnEdit);
        panel.add(btnDel);
        return panel;
    }

    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private static class OperationCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final Object[][] rowData;

        public OperationCellEditor(Object[][] rowData) {
            this.rowData = rowData;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            return (Component) rowData[row][2];
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
    }
}
