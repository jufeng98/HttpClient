package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.Consumer;
import com.intellij.util.DocumentUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javamaster.httpclient.action.dashboard.*;
import org.javamaster.httpclient.action.ws.*;
import org.javamaster.httpclient.enums.SimpleTypeEnum;
import org.javamaster.httpclient.key.HttpKey;
import org.javamaster.httpclient.messageBus.WsLangChangeNotifier;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.utils.*;
import org.javamaster.httpclient.ws.WsRequest;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class HttpDashboardForm implements Disposable {
    private final LinkedList<Pair<String, Language>> inputHistoryList = new LinkedList<>();
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
    private JLabel labelLoading;

    private final String tabName;
    private final Project project;

    public HttpDashboardForm(String tabName, Disposable parentDisposable, Project project) {
        this.tabName = tabName;
        this.project = project;

        Disposer.register(parentDisposable, this);

        splitter.setSplitterProportionKey("httpRequestCustomProportionKey");
    }

    public void initLabelLoading(String tabName, String url) {
        splitter.setVisible(false);
        labelLoading.setVisible(true);

        String str = StringUtils.substringBefore(url, "?");
        str = str.length() > 60 ? str.substring(0, 60) + "..." : str;
        labelLoading.setText(tabName + " " + labelLoading.getText() + " " + str);
    }

    public void resetDashboardForm() {
        splitter.setVisible(true);
        labelLoading.setVisible(false);
    }

    public void initHttpResContent(HttpInfo httpInfo, boolean noLog) {
        splitter.setVisible(true);
        labelLoading.setVisible(false);

        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        throwable = httpInfo.getHttpException();
        SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

        byte[] reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);

        Editor reqEditor = EditorUtils.INSTANCE.createEditor(reqBytes, "req.http", project, tabName,
                editorList, true, simpleTypeEnum, noLog);

        requestPanel.add(reqEditor.getComponent(), constraints);

        initVerticalToolbarPanel(reqEditor, reqVerticalToolbarPanel, null, null);

        if (throwable != null) {
            String msg = ExceptionUtils.getStackTrace(throwable);

            Editor errorEditor = EditorUtils.INSTANCE.createEditor(msg.getBytes(StandardCharsets.UTF_8),
                    "error.log", project, tabName, editorList, false, simpleTypeEnum, noLog);

            responsePanel.add(errorEditor.getComponent(), constraints);

            initVerticalToolbarPanel(errorEditor, resVerticalToolbarPanel, null, null);

            return;
        }

        VirtualFile responseBodyFile = saveResponseToFile(httpInfo, tabName, noLog);

        byte[] resBytes = String.join("", httpInfo.getHttpResDescList()).getBytes(StandardCharsets.UTF_8);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        Editor resEditor = EditorUtils.INSTANCE.createEditor(resBytes, "res.http", project, tabName,
                editorList, false, simpleTypeEnum, noLog);

        responsePanel.add(resEditor.getComponent(), constraintsRes);

        initVerticalToolbarPanel(resEditor, resVerticalToolbarPanel, simpleTypeEnum, responseBodyFile);

        if (Objects.equals(simpleTypeEnum, SimpleTypeEnum.IMAGE)) {
            ImageIcon imageIcon = new ImageIcon(httpInfo.getByteArray());
            JLabel jLabel = new JLabel(imageIcon);

            JBScrollPane presentation = new JBScrollPane(jLabel);

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
        String fileName = ResUtils.INSTANCE.resolveFilename(httpInfo);

        byte[] content = Objects.requireNonNull(httpInfo.getByteArray());
        VirtualFile virtualFile = VirtualFileUtils.INSTANCE.saveContent(content, tabName, fileName, noLog, project);

        httpInfo.getHttpResDescList().add(HttpUtils.CR_LF + ">> " + virtualFile.getPath() + HttpUtils.CR_LF);

        return virtualFile;
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
        new WsDashboardForm(wsRequest);
    }

    public Consumer<String> initMockServerForm() {
        mainPanel.remove(splitter);
        mainPanel.remove(labelLoading);
        mainPanel.setLayout(new BorderLayout());

        Editor editor = WriteAction.computeAndWait(() ->
                EditorUtils.INSTANCE.createEditor("".getBytes(StandardCharsets.UTF_8), "mockServer.log",
                        project, tabName, editorList, false)
        );

        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

        return log -> ApplicationManager.getApplication().invokeLater(() ->
                DocumentUtil.writeInRunUndoTransparentAction(() -> {
                            Document document = editor.getDocument();
                            document.insertString(document.getTextLength(), log);

                            Caret caret = editor.getCaretModel().getPrimaryCaret();
                            caret.moveToOffset(document.getTextLength());

                            ScrollingModel scrollingModel = editor.getScrollingModel();
                            scrollingModel.scrollToCaret(ScrollType.RELATIVE);
                        }
                ));
    }

    @Override
    public void dispose() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);
        });
    }

    public class WsDashboardForm {
        private final WsRequest wsRequest;
        private final JPanel wsReqPanel;

        private int wsInputHistoryCurrentIndex = -1;
        private final Key<String> editorTextKey = Key.create("org.javamaster.ws.WsDashboardForm");

        public WsDashboardForm(WsRequest wsRequest) {
            this.wsRequest = wsRequest;

            wsReqPanel = new JPanel();
            wsReqPanel.setLayout(new BorderLayout());

            splitter.setVisible(true);
            labelLoading.setVisible(false);

            reqPanel.remove(reqVerticalToolbarPanel);
            resPanel.remove(resVerticalToolbarPanel);

            initWsReqPanel();

            project.getMessageBus().connect(HttpDashboardForm.this).subscribe(WsLangChangeNotifier.Companion.getWS_LANG_CHANGE_TOPIC(),
                    (WsLangChangeNotifier) this::recreateWsReqEditor);

            initWsResPanel();
        }

        private void initWsReqPanel() {
            EditorTextField reqEditor = createWsReqEditor("", JsonLanguage.INSTANCE);
            wsReqPanel.add(reqEditor, BorderLayout.CENTER);

            JButton btnSend = new JButton(NlsBundle.INSTANCE.nls("ws.send"));
            btnSend.setToolTipText("Ctrl + Enter");
            btnSend.addActionListener(e -> sendWsMsg());

            JPanel btnPanel = new JPanel();
            btnPanel.add(btnSend);

            DefaultActionGroup langActionGroup = new DefaultActionGroup();
            langActionGroup.add(new ChooseWsLangAction(project, HttpDashboardForm.this));

            ActionManager actionManager = ActionManager.getInstance();
            ActionToolbar langToolbar = actionManager.createActionToolbar("langToolbar", langActionGroup, true);
            langToolbar.setTargetComponent(wsReqPanel);
            btnPanel.add(langToolbar.getComponent());

            wsReqPanel.add(btnPanel, BorderLayout.SOUTH);

            GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
            GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);
            constraints = (GridConstraints) constraints.clone();

            requestPanel.add(wsReqPanel, constraints);
        }

        private void initWsResPanel() {
            GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
            GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

            EditorTextField editorTextField = EditorUtils.INSTANCE.createEditor("", "ws-res.log", project);

            responsePanel.add(editorTextField, constraintsRes);

            Editor resEditor = Objects.requireNonNull(editorTextField.getEditor());
            Document document = editorTextField.getDocument();

            CaretModel caretModel = resEditor.getCaretModel();
            Caret caret = caretModel.getPrimaryCaret();
            ScrollingModel scrollingModel = resEditor.getScrollingModel();

            wsRequest.setResConsumer(res -> {
                        if (resEditor.isDisposed()) {
                            System.out.println("ws res editor 已被销毁");
                            return;
                        }

                        DocumentUtil.writeInRunUndoTransparentAction(() -> {
                                    String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
                                    String replace = res.replace(HttpUtils.CR_LF, "\n");
                                    String s = time + " - " + replace;

                                    document.insertString(document.getTextLength(), s);
                                    caret.moveToOffset(document.getTextLength());
                                    scrollingModel.scrollToCaret(ScrollType.RELATIVE);
                                }
                        );
                    }
            );
        }

        public void sendWsMsg() {
            EditorTextField wsReqEditor = getWsReqEditor();

            String text = wsReqEditor.getText();
            if (text.isEmpty()) {
                return;
            }

            wsRequest.sendWsMsg(text);

            wsReqEditor.setText("");

            Language language = (Language) wsReqEditor.getClientProperty("language");

            Iterator<Pair<String, Language>> iterator = inputHistoryList.iterator();
            while (iterator.hasNext()) {
                Pair<String, Language> next = iterator.next();
                if (next.getLeft().equals(text)) {
                    iterator.remove();
                    break;
                }
            }
            inputHistoryList.push(Pair.of(text, language));

            wsInputHistoryCurrentIndex = -1;

            if (inputHistoryList.size() > 50) {
                inputHistoryList.pop();
            }
        }

        private EditorTextField getWsReqEditor() {
            for (Component component : wsReqPanel.getComponents()) {
                if (component instanceof EditorTextField) {
                    return (EditorTextField) component;
                }
            }

            throw new IllegalArgumentException();
        }

        public void switchWsToPreviousInput() {
            if (inputHistoryList.isEmpty()) {
                return;
            }

            int idx = wsInputHistoryCurrentIndex + 1;
            if (idx == inputHistoryList.size()) {
                switchWsInput(wsInputHistoryCurrentIndex);
                return;
            }

            switchWsInput(++wsInputHistoryCurrentIndex);
        }

        public void switchWsToNextInput() {
            if (inputHistoryList.isEmpty()) {
                return;
            }

            if (wsInputHistoryCurrentIndex == -1) {
                return;
            }

            switchWsInput(--wsInputHistoryCurrentIndex);
        }

        private void switchWsInput(int idx) {
            EditorTextField wsReqEditor = getWsReqEditor();
            if (idx == -1) {
                wsReqEditor.setText("");
                return;
            }

            Pair<String, Language> pair = inputHistoryList.get(idx);
            String text = pair.getLeft();
            Language language = pair.getRight();

            Language currentLanguage = (Language) wsReqEditor.getClientProperty("language");
            if (currentLanguage == language) {
                wsReqEditor.setText(text);
            } else {
                language.putUserData(editorTextKey, text);

                project.getMessageBus().syncPublisher(WsLangChangeNotifier.Companion.getWS_LANG_CHANGE_TOPIC())
                        .change(language);

                language.putUserData(editorTextKey, null);
            }
        }

        public void recreateWsReqEditor(Language language) {
            EditorTextField wsReqEditor = getWsReqEditor();
            String text = language.getUserData(editorTextKey);
            if (text == null) {
                text = wsReqEditor.getText();
            }

            EditorTextField reqEditor = createWsReqEditor(text, language);
            wsReqPanel.remove(wsReqEditor);

            wsReqPanel.add(reqEditor, BorderLayout.CENTER);

            reqEditor.requestFocus();
        }

        private EditorTextField createWsReqEditor(String text, Language language) {
            EditorTextField editorTextField = EditorUtils.INSTANCE.createEditor(text, NlsBundle.INSTANCE.nls("ws.tooltip"),
                    language, project);
            editorTextField.putClientProperty("language", language);

            JComponent focusTarget = editorTextField.getFocusTarget();

            SendWsAction sendWsAction = new SendWsAction(this);
            sendWsAction.registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl ENTER"),
                    focusTarget, HttpDashboardForm.this);

            WsInputPreviousHistoryAction wsInputPreviousHistoryAction = new WsInputPreviousHistoryAction(this);
            wsInputPreviousHistoryAction.registerCustomShortcutSet(CustomShortcutSet.fromString("alt UP"),
                    focusTarget, HttpDashboardForm.this);

            WsInputNextHistoryAction wsInputNextHistoryAction = new WsInputNextHistoryAction(this);
            wsInputNextHistoryAction.registerCustomShortcutSet(CustomShortcutSet.fromString("alt DOWN"),
                    focusTarget, HttpDashboardForm.this);

            return editorTextField;
        }

    }

}
