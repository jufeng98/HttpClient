package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.DocumentUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.intellij.images.editor.impl.ImageEditorImpl;
import org.javamaster.httpclient.action.dashboard.PreviewFileAction;
import org.javamaster.httpclient.action.dashboard.SoftWrapAction;
import org.javamaster.httpclient.action.dashboard.ViewSettingsAction;
import org.javamaster.httpclient.enums.SimpleTypeEnum;
import org.javamaster.httpclient.key.HttpKey;
import org.javamaster.httpclient.mock.MockServer;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.utils.HttpUiUtils;
import org.javamaster.httpclient.utils.HttpUtils;
import org.javamaster.httpclient.utils.VirtualFileUtils;
import org.javamaster.httpclient.ws.WsRequest;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private JPanel reqVerticalToolbarPanel;
    private JPanel resVerticalToolbarPanel;
    @SuppressWarnings("unused")
    private JPanel reqPanel;
    @SuppressWarnings("unused")
    private JPanel resPanel;
    private JBSplitter splitter;

    private final String tabName;
    private final Project project;

    public HttpDashboardForm(String tabName, Project project) {
        this.tabName = tabName;
        this.project = project;

        splitter.setSplitterProportionKey("httpRequestCustomProportionKey");

        disposePreviousReqEditors();

        historyMap.put(tabName, this);
    }

    public void initHttpResContent(HttpInfo httpInfo, boolean noLog) {
        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        throwable = httpInfo.getHttpException();
        SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

        byte[] reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);

        Editor reqEditor = HttpUiUtils.INSTANCE.createEditor(reqBytes, "req.http", project, tabName,
                editorList, true, simpleTypeEnum, noLog);

        requestPanel.add(reqEditor.getComponent(), constraints);

        initVerticalToolbarPanel(reqEditor, reqVerticalToolbarPanel, null, null);

        if (throwable != null) {
            String msg = ExceptionUtils.getStackTrace(throwable);

            Editor errorEditor = HttpUiUtils.INSTANCE.createEditor(msg.getBytes(StandardCharsets.UTF_8),
                    "error.log", project, tabName, editorList, false, simpleTypeEnum, noLog);

            responsePanel.add(errorEditor.getComponent(), constraints);

            initVerticalToolbarPanel(errorEditor, resVerticalToolbarPanel, null, null);

            return;
        }

        VirtualFile responseBodyFile = saveResponseToFile(httpInfo, tabName, noLog);

        byte[] resBytes = String.join("", httpInfo.getHttpResDescList()).getBytes(StandardCharsets.UTF_8);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        Editor resEditor = HttpUiUtils.INSTANCE.createEditor(resBytes, "res.http", project, tabName,
                editorList, false, simpleTypeEnum, noLog);

        responsePanel.add(resEditor.getComponent(), constraintsRes);

        initVerticalToolbarPanel(resEditor, resVerticalToolbarPanel, simpleTypeEnum, responseBodyFile);

        if (Objects.equals(simpleTypeEnum, SimpleTypeEnum.IMAGE)) {
            ImageEditorImpl imageEditor = new ImageEditorImpl(project, responseBodyFile);

            JBScrollPane presentation = new JBScrollPane(imageEditor.getComponent());

            renderResponsePresentation(resEditor.getComponent(), presentation, constraintsRes);
        }
    }

    private void initVerticalToolbarPanel(Editor target, JPanel jPanel, SimpleTypeEnum resType, VirtualFile resBodyFile) {
        ActionManager actionManager = ActionManager.getInstance();

        AnAction viewSettingsAction = new ViewSettingsAction(target);
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup(viewSettingsAction, new SoftWrapAction(target));

        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("httpDashboardVerticalGroup");
        defaultActionGroup.addAll(actionGroup);

        if (Objects.equals(resType, SimpleTypeEnum.HTML) || Objects.equals(resType, SimpleTypeEnum.PDF)) {
            resBodyFile.putUserData(HttpKey.INSTANCE.getHttpDashboardBinaryBodyKey(), true);

            defaultActionGroup.add(new PreviewFileAction(resBodyFile));
        } else if (Objects.equals(resType, SimpleTypeEnum.IMAGE)) {
            defaultActionGroup.add(new PreviewFileAction(resBodyFile));
        }

        ActionToolbar toolbar = actionManager.createActionToolbar("httpDashboardVerticalToolbar", defaultActionGroup, false);
        toolbar.setTargetComponent(target.getComponent());

        JComponent component = toolbar.getComponent();

        jPanel.add(component);
    }

    private VirtualFile saveResponseToFile(HttpInfo httpInfo, String tabName, boolean noLog) {
        try {
            SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

            String contentType = httpInfo.getContentType();

            //noinspection DataFlowIssue
            String suffix = SimpleTypeEnum.Companion.getSuffix(simpleTypeEnum, contentType);

            String fileName = DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HHmmss") + "." + suffix;

            if (noLog) {
                LightVirtualFile lightVirtualFile = new LightVirtualFile(fileName);
                lightVirtualFile.setCharset(StandardCharsets.UTF_8);
                //noinspection DataFlowIssue
                lightVirtualFile.setBinaryContent(httpInfo.getByteArray());
                return lightVirtualFile;
            }

            File dateHistoryDir = VirtualFileUtils.INSTANCE.getDateHistoryDir(project);

            File resBodyDir = new File(dateHistoryDir, tabName);
            if (!resBodyDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                resBodyDir.mkdirs();
            }

            File file = new File(resBodyDir, fileName);

            String absolutePath = file.getAbsolutePath();

            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("已删除文件:" + absolutePath);
            }

            //noinspection DataFlowIssue
            Files.write(file.toPath(), httpInfo.getByteArray());
            System.out.println("已保存到文件:" + absolutePath);

            VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);

            httpInfo.getHttpResDescList().add(HttpUtils.CR_LF + ">> " + absolutePath + HttpUtils.CR_LF);

            return virtualFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderResponsePresentation(JComponent resComponent, JComponent presentation, GridConstraints constraintsRes) {
        Dimension size = resComponent.getSize();
        resComponent.setPreferredSize(new Dimension(size.width, 160));

        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(resComponent, BorderLayout.NORTH);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(new JLabel(NlsBundle.INSTANCE.nls("res.render.result")), BorderLayout.NORTH);
        previewPanel.add(presentation, BorderLayout.CENTER);

        jPanel.add(previewPanel, BorderLayout.CENTER);

        responsePanel.add(new JBScrollPane(jPanel), constraintsRes);
    }

    public void initWsForm(WsRequest wsRequest) {
        reqPanel.remove(reqVerticalToolbarPanel);
        resPanel.remove(resVerticalToolbarPanel);

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
                        project, tabName, editorList, false)
        );

        responsePanel.add(editor.getComponent(), constraintsRes);

        wsRequest.setResConsumer(res ->
                DocumentUtil.writeInRunUndoTransparentAction(() -> {
                            String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
                            String replace = res.replace(HttpUtils.CR_LF, "\n");
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

    public void initMockServerForm(MockServer mockServer) {
        mainPanel.remove(splitter);
        mainPanel.setLayout(new BorderLayout());

        Editor editor = WriteAction.computeAndWait(() ->
                HttpUiUtils.INSTANCE.createEditor("".getBytes(StandardCharsets.UTF_8), "mockServer.log",
                        project, tabName, editorList, false)
        );

        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

        mockServer.setResConsumer(res ->
                ApplicationManager.getApplication().invokeLater(() ->
                        DocumentUtil.writeInRunUndoTransparentAction(() -> {
                                    Document document = editor.getDocument();
                                    document.insertString(document.getTextLength(), res);

                                    Caret caret = editor.getCaretModel().getPrimaryCaret();
                                    caret.moveToOffset(document.getTextLength());

                                    ScrollingModel scrollingModel = editor.getScrollingModel();
                                    scrollingModel.scrollToCaret(ScrollType.RELATIVE);
                                }
                        ))
        );
    }

    private static @NotNull JPanel createReqPanel(WsRequest wsRequest) {
        JPanel jPanelReq = new JPanel();
        jPanelReq.setLayout(new BorderLayout());

        JTextArea jTextAreaReq = new JTextArea();
        jTextAreaReq.setToolTipText(NlsBundle.INSTANCE.nls("ws.tooltip"));
        jPanelReq.add(new JBScrollPane(jTextAreaReq), BorderLayout.CENTER);

        JButton jButtonSend = new JButton(NlsBundle.INSTANCE.nls("ws.send"));
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
        editorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);
        });
    }

    @Override
    public void dispose() {

    }
}
