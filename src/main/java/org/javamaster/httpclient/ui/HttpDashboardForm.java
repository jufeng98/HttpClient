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
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.DocumentUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.javamaster.httpclient.action.dashboard.*;
import org.javamaster.httpclient.action.ws.*;
import org.javamaster.httpclient.consts.HttpConsts;
import org.javamaster.httpclient.enums.SimpleTypeEnum;
import org.javamaster.httpclient.exception.JsScriptException;
import org.javamaster.httpclient.key.HttpKey;
import org.javamaster.httpclient.messageBus.WsLangChangeNotifier;
import org.javamaster.httpclient.model.HttpInfo;
import org.javamaster.httpclient.nls.NlsBundle;
import org.javamaster.httpclient.utils.*;
import org.javamaster.httpclient.ws.WsRequest;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

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
        VirtualFile resBodyFile = ResUtils.INSTANCE.saveResBodyToFile(httpInfo, tabName, noLog, project);

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

    public void initWsForm(WsRequest wsRequest) {
        new WsDashboardForm(wsRequest);
    }

    public void initMockServerForm(Consumer<Editor> editorConsumer) {
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

        VirtualFile virtualFile = VirtualFileUtils.INSTANCE.createDescListVirtualFile(Lists.newArrayList(""),
                "mock-server.log", tabName, false, project);

        Document document = ReadAction.compute(() -> Objects.requireNonNull(fileDocumentManager.getDocument(virtualFile)));

        ApplicationManager.getApplication().invokeLater(() -> {
            mainPanel.remove(splitter);
            mainPanel.remove(labelLoading);
            mainPanel.setLayout(new BorderLayout());

            var editor = EditorFactory.getInstance().createEditor(document, project, virtualFile, true);
            editorList.add(editor);

            mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

            editorConsumer.accept(editor);
        });
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
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(it -> {
            if (it.isDisposed()) {
                return;
            }

            editorFactory.releaseEditor(it);
        });

        saveInputHistoryList();
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

            wsRequest.setResConsumer(res -> {
                        Editor resEditor = editorTextField.getEditor(true);
                        if (resEditor == null || resEditor.isDisposed()) {
                            System.out.println("ws res editor 已被销毁");
                            return;
                        }

                        Caret caret = resEditor.getCaretModel().getPrimaryCaret();
                        ScrollingModel scrollingModel = resEditor.getScrollingModel();
                        Document document = editorTextField.getDocument();

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
