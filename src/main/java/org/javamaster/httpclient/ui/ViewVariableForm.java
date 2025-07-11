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
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.table.JBTable;
import kotlin.Pair;
import kotlin.Triple;
import org.javamaster.httpclient.env.EnvFileService;
import org.javamaster.httpclient.js.JsExecutor;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.resolve.VariableResolver;
import org.javamaster.httpclient.utils.HttpUtils;
import org.javamaster.httpclient.utils.NotifyUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.javamaster.httpclient.resolve.VariableResolver.ENV_PREFIX;
import static org.javamaster.httpclient.resolve.VariableResolver.PROPERTY_PREFIX;

public class ViewVariableForm extends DialogWrapper {
    private final Project project;
    private JPanel contentPane;
    private JBTable table;
    private static final int TYPE_GLOBAL = 1;
    private static final int TYPE_JS = 2;
    private static final int TYPE_ENV = 3;
    private static final int TYPE_SYS = 4;

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

    private void initTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCellSelectionEnabled(true);

        List<Triple<String, Map<String, String>, Integer>> resList = collectVariableMapList();

        int rows = calRows(resList);

        Object[][] rowData = createRowData(resList, rows);

        DefaultTableModel model = new DefaultTableModel(rowData, new String[]{"key", "value", "operation"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setModel(model);

        table.setCellSelectionEnabled(true);

        table.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus,
                                                row, column) -> {
            if (value instanceof JLabel) {
                return (Component) value;
            }

            if (value instanceof JButton) {
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
        operationColumn.setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int col = table.columnAtPoint(event.getPoint());
                if (row < 0 || col != 2) {
                    return;
                }

                int type = (int) rowData[row][3];
                if (type == -1) {
                    return;
                }


                Object keyObj = rowData[row][0];

                String key;
                String value;
                String title;
                boolean add;
                if (keyObj instanceof String) {
                    key = (String) keyObj;
                    value = (String) rowData[row][1];
                    title = "Edit";
                    add = false;
                } else {
                    key = "";
                    value = "";
                    title = "Add";
                    add = true;
                }

                PropertyForm propertyForm = new PropertyForm(project, title, key, value, type == TYPE_JS, add);
                boolean b = propertyForm.showAndGet();
                if (!b) {
                    return;
                }

                Pair<String, String> pair = propertyForm.getFormData();
                String newKey = pair.getFirst();
                String newValue = pair.getSecond();

                if (!add) {
                    rowData[row][0] = newKey;
                    rowData[row][1] = newValue;
                }

                try {
                    switch (type) {
                        case TYPE_GLOBAL:
                            boolean success = HttpUtils.INSTANCE.modifyFileGlobalVariable(key, newKey, newValue, add, project);

                            if (success) {
                                PopupFactoryImpl.getInstance().createMessage("Success!").showCenteredInCurrentWindow(project);
                            }

                            break;
                        case TYPE_JS:
                            HttpUtils.INSTANCE.modifyJsVariable(newKey, newValue);

                            PopupFactoryImpl.getInstance().createMessage("Success!").showCenteredInCurrentWindow(project);

                            break;
                        case TYPE_ENV:
                            boolean success1 = HttpUtils.INSTANCE.modifyEnvVariable(key, newKey, newValue, add, project);

                            if (success1) {
                                PopupFactoryImpl.getInstance().createMessage("Success!").showCenteredInCurrentWindow(project);
                            }

                            break;
                        default:
                    }

                    initTable();
                } catch (Exception e) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    NotifyUtil.INSTANCE.notifyWarn(project, e.getMessage());
                }
            }

        });
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
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("file.global.desc"), fileGlobalVariables, TYPE_GLOBAL));

        Map<String, String> variableMap = variableResolver.getJsGlobalVariables();
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("js.variable.desc"), variableMap, TYPE_JS));

        Map<String, String> envMap = EnvFileService.Companion.getEnvMap(project, false);
        resList.add(new Triple<>(NlsBundle.INSTANCE.nls("environment.variable.desc"), envMap, TYPE_ENV));

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
                rows += map.size();
            }
        }
        return rows;
    }

    private Object[][] createRowData(List<Triple<String, Map<String, String>, Integer>> resList, int rows) {
        JButton btnAdd = new JButton(NlsBundle.INSTANCE.nls("add"));
        btnAdd.setForeground(new JBColor(new Color(64, 158, 255), new Color(64, 158, 255)));

        JButton btnEdit = new JButton(NlsBundle.INSTANCE.nls("edit"));
        btnEdit.setForeground(new JBColor(new Color(230, 162, 60), new Color(230, 162, 60)));

        String repeat = "-".repeat(160);
        Object[][] rowData = new Object[rows][4];
        int i = 0;
        for (Triple<String, Map<String, String>, Integer> triple : resList) {
            String desc = triple.getFirst();
            Integer type = triple.getThird();

            JLabel label = new JLabel(desc);
            label.setFont(new Font(label.getFont().getName(), Font.BOLD, 13));

            rowData[i][0] = label;
            rowData[i][1] = repeat;

            boolean match = type == TYPE_GLOBAL || type == TYPE_JS || type == TYPE_ENV;
            if (match) {
                rowData[i][2] = btnAdd;
                rowData[i][3] = type;
            } else {
                rowData[i][2] = "";
                rowData[i][3] = -1;
            }

            i++;

            Map<String, String> map = triple.getSecond();
            if (map.isEmpty()) {
                rowData[i][0] = NlsBundle.INSTANCE.nls("no.data.available");
                rowData[i][1] = "";
                rowData[i][2] = "";
                rowData[i][3] = -1;
                i++;
            } else {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    rowData[i][0] = key;
                    rowData[i][1] = value;

                    if (match) {
                        rowData[i][2] = btnEdit;
                        rowData[i][3] = type;
                    } else {
                        rowData[i][2] = "";
                        rowData[i][3] = -1;
                    }

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
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
