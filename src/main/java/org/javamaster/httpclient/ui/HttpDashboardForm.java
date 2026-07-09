package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.Formats;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javamaster.httpclient.action.dashboard.*;
import org.javamaster.httpclient.action.ws.*;
import org.javamaster.httpclient.consts.HttpConsts;
import org.javamaster.httpclient.enums.SimpleTypeEnum;
import org.javamaster.httpclient.exception.JsScriptException;
import org.javamaster.httpclient.js.support.JsExecuteResult;
import org.javamaster.httpclient.key.HttpKey;
import org.javamaster.httpclient.logger.HttpRequestLogger;
import org.javamaster.httpclient.messageBus.WsLangChangeNotifier;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.psi.HttpRequestBlock;
import org.javamaster.httpclient.renderer.TextEditorCustomElementRenderer;
import org.javamaster.httpclient.utils.*;
import org.javamaster.httpclient.ws.WsRequest;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

public class HttpDashboardForm implements Disposable {
    private final LinkedList<Pair<String, Language>> inputHistoryList = new LinkedList<>();
    private final List<Editor> editorList = Lists.newArrayList();

    public JPanel mainPanel;
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
    private String labelLoadingTxt;
    private String resContentLengthDesc;
    private JProgressBar progressBar;

    private final String tabName;
    private final Project project;
    private Editor mockServerEditor;

    public HttpDashboardForm(String tabName, Disposable parentDisposable, Project project) {
        this.tabName = tabName;
        this.project = project;

        labelLoading.setVisible(false);
        progressBar.setVisible(false);

        Disposer.register(parentDisposable, this);

        splitter.setSplitterProportionKey("httpRequestCustomProportionKey");
    }

    public void initLabelLoading(String tabName, String url) {
        splitter.setVisible(false);
        labelLoading.setVisible(true);
        progressBar.setVisible(true);

        String str = StringUtils.substringBefore(url, "?");
        str = str.length() > 60 ? str.substring(0, 60) + "..." : str;

        labelLoadingTxt = tabName + " " + labelLoading.getText() + " " + str;

        labelLoading.setText("0 s " + labelLoadingTxt);
    }

    public void updateLabelLoading(int elapseTime) {
        labelLoading.setText(elapseTime + " s " + labelLoadingTxt);
    }

    public void initProgress(int resContentLength) {
        if (resContentLength != -1) {
            resContentLengthDesc = Formats.formatFileSize(resContentLength);

            progressBar.setMaximum(resContentLength);
        } else {
            progressBar.setIndeterminate(true);
        }
    }

    public void updateProgress(int byteLength, int resContentLength) {
        int receivedByteLength = byteLength / 1024;

        String str;
        if (resContentLength != -1) {
            int totalLength = resContentLength / 1024;
            int percent = (int) ((double) receivedByteLength / totalLength * 100.0);

            progressBar.setValue(byteLength);

            str = NlsBundle.INSTANCE.nls("progress.hint.total", receivedByteLength, resContentLengthDesc, percent);
        } else {
            str = NlsBundle.INSTANCE.nls("progress.hint", receivedByteLength);
        }

        progressBar.setString(str);
    }

    public void resetDashboardForm() {
        splitter.setVisible(true);
        labelLoading.setVisible(false);
        progressBar.setVisible(false);
    }

    public void initHttpResContent(HttpInfo httpInfo, boolean noLog, HttpRequestBlock requestBlock) {
        kotlin.Pair<VirtualFile, Boolean> pair = ResUtils.INSTANCE.saveResBodyToFile(httpInfo, tabName, noLog, project);

        VirtualFile resBodyFile = pair.getFirst();
        Boolean renderResBodyFileName = pair.getSecond();

        VirtualFile reqVirtualFile = VirtualFileUtils.INSTANCE.createDescListVirtualFile(httpInfo.getHttpReqDescList(),
                "req.http", tabName, noLog, project);

        Document reqDocument = ResUtils.INSTANCE.getDocument(reqVirtualFile);

        VirtualFile resVirtualFile;
        Throwable throwable = httpInfo.getHttpException();
        if (throwable != null) {
            if (throwable instanceof JsScriptException error && error.getBefore()) {
                resVirtualFile = VirtualFileUtils.INSTANCE.createDescListVirtualFile(httpInfo.getHttpResDescList(),
                        "res.http", tabName, noLog, project);
            } else {
                String msg = ExceptionUtils.getStackTrace(throwable);
                resVirtualFile = VirtualFileUtils.INSTANCE.createDescListVirtualFile(Lists.newArrayList(msg),
                        "error.log", tabName, noLog, project);
            }
        } else {
            resVirtualFile = VirtualFileUtils.INSTANCE.createDescListVirtualFile(httpInfo.getHttpResDescList(),
                    "res.http", tabName, noLog, project);
        }

        Document resDocument = ResUtils.INSTANCE.getDocument(resVirtualFile);

        ApplicationManager.getApplication().invokeLater(() -> {
            resetDashboardForm();

            GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
            GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

            SimpleTypeEnum simpleTypeEnum = httpInfo.getType();

            Editor reqEditor = EditorUtils.INSTANCE.createEditor(reqVirtualFile, reqDocument, true,
                    project, true, simpleTypeEnum, editorList);

            requestPanel.add(reqEditor.getComponent(), constraints);

            initVerticalToolbarPanel(reqEditor, reqVerticalToolbarPanel, null, null);

            Editor resEditor = EditorUtils.INSTANCE.createEditor(resVirtualFile, resDocument, true,
                    project, false, simpleTypeEnum, editorList);

            List<TextEditorCustomElementRenderer> reqRenderers = Lists.newArrayList();

            Long reqContentLength = httpInfo.getReqContentLength();
            if (reqContentLength != null) {
                TextEditorCustomElementRenderer render = RenderUtils.INSTANCE.createReqDescRender(resEditor, reqContentLength);

                reqRenderers.add(render);
            }

            JsExecuteResult jsBeforeExecuteResult = httpInfo.getJsBeforeExecuteResult();
            if (jsBeforeExecuteResult != null) {
                List<TextEditorCustomElementRenderer> rendererList = RenderUtils.INSTANCE
                        .createJsExecuteResultRender(reqEditor, jsBeforeExecuteResult);

                reqRenderers.addAll(rendererList);
            }

            RenderUtils.INSTANCE.renderTop(reqEditor, reqRenderers);

            List<TextEditorCustomElementRenderer> resRenderers = Lists.newArrayList();

            Integer statusCode = httpInfo.getStatusCode();
            if (statusCode != null) {
                TextEditorCustomElementRenderer render = RenderUtils.INSTANCE.createResDescRender(resEditor, statusCode,
                        httpInfo.getCostTimes(), httpInfo.getContentLength());

                resRenderers.add(render);
            }

            JsExecuteResult jsAfterExecuteResult = httpInfo.getJsAfterExecuteResult();
            if (jsAfterExecuteResult != null) {
                List<TextEditorCustomElementRenderer> rendererList = RenderUtils.INSTANCE
                        .createJsExecuteResultRender(resEditor, jsAfterExecuteResult);

                resRenderers.addAll(rendererList);
            }

            RenderUtils.INSTANCE.renderTop(resEditor, resRenderers);

            if (renderResBodyFileName) {
                RenderUtils.INSTANCE.renderResBodyFileName(resEditor, resDocument, resBodyFile, project, requestBlock);
            }

            kotlin.Pair<String, VirtualFile> cookieSavePair = httpInfo.getCookieSavePair();
            if (cookieSavePair != null) {
                RenderUtils.INSTANCE.renderCookieFilePath(resEditor, resDocument, cookieSavePair, project, requestBlock);
            }

            GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
            GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

            responsePanel.add(resEditor.getComponent(), constraintsRes);

            initVerticalToolbarPanel(resEditor, resVerticalToolbarPanel, simpleTypeEnum, resBodyFile);

            if (Objects.equals(simpleTypeEnum, SimpleTypeEnum.IMAGE)) {
                ImageIcon imageIcon = new ImageIcon(httpInfo.getByteArray());
                JLabel jLabel = new JLabel(imageIcon);

                JBScrollPane presentation = new JBScrollPane(jLabel);

                renderResponsePresentation(resEditor.getComponent(), presentation, constraintsRes);
            }
        });
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

    public WsDashboardForm initWsForm() {
        return new WsDashboardForm();
    }

    public void initMockServerForm(kotlin.Pair<VirtualFile, Document> pair) {
        mainPanel.remove(splitter);
        mainPanel.remove(labelLoading);
        mainPanel.remove(progressBar);
        mainPanel.setLayout(new BorderLayout());

        var editor = EditorFactory.getInstance().createEditor(pair.getSecond(), project, pair.getFirst(), true);
        editorList.add(editor);

        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

        this.mockServerEditor = editor;
    }

    public void showMockServerLog(String log) {
        DocUtils.INSTANCE.appendLog(mockServerEditor, log);
    }

    public void saveInputHistoryList() {
        List<Map<String, String>> list = inputHistoryList.stream()
                .map(it -> {
                    Map<String, String> map = Maps.newHashMap();
                    String content = it.getKey();
                    Language language = it.getValue();
                    map.put("id", language.getID());
                    map.put("content", content);
                    return map;
                })
                .toList();

        String json = JsonUtils.INSTANCE.getGsonNotPretty().toJson(list);

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        propertiesComponent.setValue(HttpConsts.HTTP_CLIENT + ":http:" + tabName, json);
    }

    public void restoreInputHistoryList() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
        String json = propertiesComponent.getValue(HttpConsts.HTTP_CLIENT + ":http:" + tabName);
        if (StringUtils.isBlank(json)) {
            return;
        }

        Type listType = new TypeToken<List<Map<String, String>>>() {
        }.getType();

        List<Map<String, String>> list = JsonUtils.INSTANCE.getGsonNotPretty().fromJson(json, listType);

        List<Pair<String, Language>> pairs = list.stream()
                .map(it -> {
                    String id = it.get("id");
                    String content = it.get("content");
                    return Pair.of(content, Language.findLanguageByID(id));
                })
                .toList();

        inputHistoryList.addAll(pairs);
    }

    @Override
    public void dispose() {
        HttpRequestLogger.INSTANCE.logInfo("开始销毁 editor");
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);

            HttpRequestLogger.INSTANCE.logInfo("完成销毁 editor:" + it);
        });

        saveInputHistoryList();
    }

    public class WsDashboardForm {
        private WsRequest wsRequest;

        private final Editor resEditor;
        private final JPanel wsReqPanel;

        private int wsInputHistoryCurrentIndex = -1;
        private final Key<String> editorTextKey = Key.create("org.javamaster.ws.WsDashboardForm");

        public WsDashboardForm() {
            wsReqPanel = new JPanel();
            wsReqPanel.setLayout(new BorderLayout());

            splitter.setVisible(true);
            labelLoading.setVisible(false);
            progressBar.setVisible(false);

            reqPanel.remove(reqVerticalToolbarPanel);
            resPanel.remove(resVerticalToolbarPanel);

            initWsReqPanel();

            project.getMessageBus()
                    .connect(HttpDashboardForm.this)
                    .subscribe(
                            WsLangChangeNotifier.Companion.getWS_LANG_CHANGE_TOPIC(),
                            (WsLangChangeNotifier) this::recreateWsReqEditor
                    );

            this.resEditor = initWsResPanel();
        }

        public void setWsRequest(WsRequest wsRequest) {
            this.wsRequest = wsRequest;
        }

        public void showReceiveWsMsg(String wsMsg) {
            if (resEditor.isDisposed()) {
                HttpRequestLogger.INSTANCE.logInfo("ws res editor 已被销毁");
                return;
            }

            DocUtils.INSTANCE.appendLog(resEditor, wsMsg);
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

        private Editor initWsResPanel() {
            GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
            GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

            String fileName = DateFormatUtils.format(new Date(), "hhmmss") + "-ws-res.log";

            Editor resEditor = EditorUtils.INSTANCE.createEditor("", fileName, project, editorList);

            responsePanel.add(resEditor.getComponent(), constraintsRes);

            return resEditor;
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
