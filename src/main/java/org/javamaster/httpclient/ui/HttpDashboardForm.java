package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.DocumentUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.javamaster.httpclient.HttpInfo;
import org.javamaster.httpclient.enums.SimpleTypeEnum;
import org.javamaster.httpclient.utils.HttpUiUtils;
import org.javamaster.httpclient.ws.WsRequest;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpDashboardForm implements Disposable {
    private final static Map<String, HttpDashboardForm> historyMap = Maps.newHashMap();

    private final List<Editor> editorList = Lists.newArrayList();
    public JPanel mainPanel;
    public Throwable throwable;
    public JPanel requestPanel;
    public JPanel responsePanel;

    private final String tabName;
    private final Project project;

    public HttpDashboardForm(String tabName, Project project) {
        this.tabName = tabName;
        this.project = project;

        disposePreviousReqEditors();

        historyMap.put(tabName, this);
    }

    public void initHttpResContent(HttpInfo httpInfo) {
        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        throwable = httpInfo.getHttpException();

        byte[] reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);

        JComponent reqComponent = HttpUiUtils.INSTANCE.createEditorCompo(reqBytes, "req.http", project, tabName, editorList);
        requestPanel.add(reqComponent, constraints);

        if (throwable != null) {
            String msg = ExceptionUtils.getStackTrace(throwable);
            JComponent jComponent = HttpUiUtils.INSTANCE.createEditorCompo(msg.getBytes(StandardCharsets.UTF_8), "error.log",
                    project, tabName, editorList);
            responsePanel.add(jComponent, constraints);
            return;
        }

        boolean imageType = Objects.equals(httpInfo.getType(), SimpleTypeEnum.IMAGE);
        if (imageType) {
            //noinspection DataFlowIssue
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(httpInfo.getByteArray())) {
                BufferedImage bufferedImage = ImageIO.read(inputStream);

                int inputWidth = bufferedImage.getWidth();
                int inputHeight = bufferedImage.getHeight();

                int outputWidth = 500;
                int outputHeight = (int) ((double) inputHeight / inputWidth * outputWidth);

                Image newImage = bufferedImage.getScaledInstance(outputWidth, outputHeight, Image.SCALE_FAST);
                ImageIcon image = new ImageIcon(newImage);

                JLabel jlabel = new JLabel(image);
                responsePanel.add(new JBScrollPane(jlabel), constraints);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        byte[] resBytes = String.join("", httpInfo.getHttpResDescList()).getBytes(StandardCharsets.UTF_8);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        JComponent resComponent = HttpUiUtils.INSTANCE.createEditorCompo(resBytes, "res.http", project, tabName, editorList);
        responsePanel.add(resComponent, constraintsRes);
    }

    public void initWsResData(WsRequest wsRequest) {
        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);
        constraints = (GridConstraints) constraints.clone();
        int width = 200;
        constraints.myMinimumSize.width = width;
        constraints.myMaximumSize.width = width;
        constraints.myPreferredSize.width = width;

        JPanel jPanelReq = createReqPanel(wsRequest);

        requestPanel.add(jPanelReq, constraints);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        Editor editor = WriteAction.computeAndWait(() ->
                HttpUiUtils.INSTANCE.createEditor("".getBytes(StandardCharsets.UTF_8), "ws.log",
                        project, tabName, editorList)
        );

        responsePanel.add(editor.getComponent(), constraintsRes);

        wsRequest.setResConsumer(res ->
                DocumentUtil.writeInRunUndoTransparentAction(() -> {
                            String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
                            String replace = res.replace("\r\n", "\n");
                            String s = time + " - " + replace;

                            Document document = editor.getDocument();
                            document.insertString(document.getTextLength(), s);


                            Caret caret = editor.getCaretModel().getPrimaryCaret();
                            caret.moveToOffset(document.getTextLength());

                            ScrollingModel scrollingModel = editor.getScrollingModel();
                            scrollingModel.scrollToCaret(ScrollType.RELATIVE);
                        }
                )
        );
    }

    private static @NotNull JPanel createReqPanel(WsRequest wsRequest) {
        JPanel jPanelReq = new JPanel();
        jPanelReq.setLayout(new BorderLayout());

        JTextArea jTextAreaReq = new JTextArea();
        jTextAreaReq.setToolTipText("请输入ws消息");
        jPanelReq.add(new JBScrollPane(jTextAreaReq), BorderLayout.CENTER);

        JButton jButtonSend = new JButton("发送ws消息");
        jButtonSend.addActionListener(e -> {
            String text = jTextAreaReq.getText();
            wsRequest.sendWsMsg(text);
            jTextAreaReq.setText("");
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(jButtonSend);

        jPanelReq.add(btnPanel, BorderLayout.SOUTH);
        return jPanelReq;
    }

    private void disposePreviousReqEditors() {
        HttpDashboardForm previousHttpDashboardForm = historyMap.remove(tabName);
        if (previousHttpDashboardForm == null) {
            return;
        }

        previousHttpDashboardForm.disposeEditors();
    }

    private void disposeEditors() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(editorFactory::releaseEditor);
    }

    @Override
    public void dispose() {
        historyMap.values().forEach(HttpDashboardForm::disposeEditors);
        historyMap.clear();
    }
}
