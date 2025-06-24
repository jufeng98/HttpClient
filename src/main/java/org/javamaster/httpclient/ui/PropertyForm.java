package org.javamaster.httpclient.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyForm extends DialogWrapper {
    private JPanel contentPane;

    private JTextField textFieldKey;
    private JTextField textFieldValue;

    public PropertyForm(Project project,String title, String key, String value) {
        super(project, false);

        textFieldKey.setText(key);
        textFieldValue.setText(value);

        setModal(true);
        setTitle(title);

        init();
    }

    public Pair<String, String> getFormData() {
        return new Pair<>(textFieldKey.getText(), textFieldValue.getText());
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        Pair<String, String> pair = getFormData();

        List<ValidationInfo> result = new ArrayList<>();
        if (StringUtils.isBlank(pair.getFirst())) {
            result.add(new ValidationInfo("Can't be blank", textFieldKey));
        }
        if (StringUtils.isBlank(pair.getSecond())) {
            result.add(new ValidationInfo("Can't be blank", textFieldValue));
        }

        return result;
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
