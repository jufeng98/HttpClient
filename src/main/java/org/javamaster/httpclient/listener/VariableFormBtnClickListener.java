package org.javamaster.httpclient.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.popup.PopupFactoryImpl;
import kotlin.Pair;
import org.javamaster.httpclient.js.support.jsObject.JsGlobalVariablesHolder;
import org.javamaster.httpclient.logger.HttpRequestLogger;
import org.javamaster.httpclient.parser.HttpFile;
import org.javamaster.httpclient.ui.PropertyForm;
import org.javamaster.httpclient.ui.ViewVariableForm;
import org.javamaster.httpclient.utils.EnvFileUtils;
import org.javamaster.httpclient.utils.NotifyUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author yudong
 */
public class VariableFormBtnClickListener implements ActionListener {
    private final ViewVariableForm viewVariableForm;
    private final int row;
    private final boolean del;
    private final boolean add;
    private final Object[][] rowData;
    private final Project project;

    public VariableFormBtnClickListener(ViewVariableForm viewVariableForm, int row, boolean del, boolean add,
                                        Object[][] rowData, Project project) {
        this.viewVariableForm = viewVariableForm;
        this.row = row;
        this.del = del;
        this.add = add;
        this.rowData = rowData;
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            int type = (int) rowData[row][3];
            Object keyObj = rowData[row][0];

            boolean success;
            String key = "";
            String value = "";
            if (del) {
                key = (String) keyObj;
                value = (String) rowData[row][1];

                int res = Messages.showYesNoDialog("Delete " + key + "?", "Delete", null);
                if (res != Messages.YES) {
                    return;
                }

                success = doDelete(type, key, value);
            } else {
                String title;
                if (add) {
                    title = "Add";
                } else {
                    key = (String) keyObj;
                    value = (String) rowData[row][1];
                    title = "Edit";
                }

                PropertyForm propertyForm = new PropertyForm(project, title, key, value);
                boolean b = propertyForm.showAndGet();
                if (!b) {
                    return;
                }

                Pair<String, String> pair = propertyForm.getFormData();
                String newKey = pair.getFirst();
                String newValue = pair.getSecond();

                if (add) {
                    success = doAdd(type, newKey, newValue);
                } else {
                    success = doModify(type, key, newKey, value, newValue);
                }
            }

            if (success) {
                PopupFactoryImpl.getInstance().createMessage("Success!").showCenteredInCurrentWindow(project);
                viewVariableForm.initTable();
            }
        } catch (Exception e) {
            HttpRequestLogger.INSTANCE.logWarn("error", e);

            NotifyUtil.INSTANCE.notifyError(project, e.getMessage());
        }
    }

    private boolean doDelete(int type, String key, String value) {
        return switch (type) {
            case ViewVariableForm.TYPE_FILE_VARIABLE -> HttpFile.Companion.delFileVariable(key, project);
            case ViewVariableForm.TYPE_JS_GLOBAL_VARIABLE -> JsGlobalVariablesHolder.INSTANCE.clear(key);
            case ViewVariableForm.TYPE_ENV_VARIABLE -> EnvFileUtils.Companion.delEnvVariable(key, project);
            case ViewVariableForm.TYPE_HEADER -> JsGlobalVariablesHolder.INSTANCE.getHeaders().delete(key, value);
            default -> false;
        };
    }

    private boolean doAdd(int type, String newKey, String newValue) {
        return switch (type) {
            case ViewVariableForm.TYPE_FILE_VARIABLE -> HttpFile.Companion.addFileVariable(newKey, newValue, project);
            case ViewVariableForm.TYPE_JS_GLOBAL_VARIABLE -> JsGlobalVariablesHolder.INSTANCE.set(newKey, newValue);
            case ViewVariableForm.TYPE_ENV_VARIABLE -> EnvFileUtils.Companion.addEnvVariable(newKey, newValue, project);
            case ViewVariableForm.TYPE_HEADER -> JsGlobalVariablesHolder.INSTANCE.getHeaders().add(newKey, newValue);
            default -> false;
        };
    }

    private boolean doModify(int type, String key, String newKey, String value, String newValue) {
        return switch (type) {
            case ViewVariableForm.TYPE_FILE_VARIABLE ->
                    HttpFile.Companion.changeFileVariable(key, newKey, newValue, project);
            case ViewVariableForm.TYPE_JS_GLOBAL_VARIABLE ->
                    JsGlobalVariablesHolder.INSTANCE.modify(key, newKey, newValue);
            case ViewVariableForm.TYPE_ENV_VARIABLE ->
                    EnvFileUtils.Companion.modifyEnvVariable(key, newKey, newValue, project);
            case ViewVariableForm.TYPE_HEADER ->
                    JsGlobalVariablesHolder.INSTANCE.getHeaders().modify(key, newKey, value, newValue);
            default -> false;
        };
    }
}
