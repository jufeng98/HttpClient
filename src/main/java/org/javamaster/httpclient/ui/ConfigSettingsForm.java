package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.CollectionComboBoxModel;
import kotlin.Pair;
import org.javamaster.httpclient.action.ChooseEnvironmentAction;
import org.javamaster.httpclient.env.EnvFileService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;

public class ConfigSettingsForm {
    private JPanel mainPanel;

    private JComboBox<String> envComboBox;
    private TextFieldWithBrowseButton httpFileBtn;

    public void initForm(String env, String httpFilePath, @NotNull Project project) {
        File httpFile = new File(httpFilePath);

        initEnvComboBox(env, httpFile, project);

        initFileBtn(httpFilePath);
    }

    public Pair<String, String> getPair() {
        String env = envComboBox.getSelectedItem() + "";
        if (env.equals(ChooseEnvironmentAction.Companion.getNoEnv())) {
            env = null;
        }

        String fileName = httpFileBtn.getText();

        return new Pair<>(env, fileName);
    }

    private void initEnvComboBox(String env, File httpFile, Project project) {
        EnvFileService envFileService = EnvFileService.Companion.getService(project);

        Set<String> presetEnvSet = envFileService.getPresetEnvSet(httpFile.getParentFile().getAbsolutePath());

        List<String> envNameList = Lists.newArrayList(ChooseEnvironmentAction.Companion.getNoEnv());
        envNameList.addAll(presetEnvSet);

        envComboBox.setModel(new CollectionComboBoxModel<>(envNameList));

        envComboBox.setSelectedItem(env);
    }

    private void initFileBtn(String httpFilePath) {
        httpFileBtn.setText(httpFilePath);

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();

        httpFileBtn.addBrowseFolderListener(new TextBrowseFolderListener(descriptor) {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);

                httpFileBtn.setText(httpFileBtn.getText().replaceAll("\\\\", "/"));
            }

        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
