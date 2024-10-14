package org.javamaster.httpclient.ui;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javamaster.httpclient.HttpInfo;
import org.javamaster.httpclient.utils.VirtualFileUtils;
import org.javamaster.httpclient.ws.WsRequest;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author yudong
 */
public class HttpExecutionConsoleToolWindow implements Disposable {
    private final List<Editor> editorList = Lists.newArrayList();
    public JPanel mainPanel;
    public Throwable throwable;
    private JPanel responsePanel;
    private JPanel requestPanel;

    public void initPanelData(HttpInfo httpInfo, Throwable throwable, String tabName, Project project, Disposable parentDisposer) {
        Disposer.register(parentDisposer, this);

        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        if (throwable != null) {
            String msg = ExceptionUtils.getStackTrace(throwable);
            JComponent jComponent = createEditor(msg.getBytes(StandardCharsets.UTF_8), "error.log", project, tabName);
            requestPanel.add(jComponent, constraints);
            this.throwable = throwable;
            return;
        }

        boolean imageType = Objects.equals(httpInfo.getType(), "image");
        Exception httpException = httpInfo.getHttpException();
        boolean hasError = httpException != null;

        this.throwable = httpException;

        byte[] reqBytes;
        if (imageType || hasError) {
            List<String> list = Lists.newArrayList();
            list.addAll(httpInfo.getHttpReqDescList());
            list.addAll(httpInfo.getHttpResDescList());
            reqBytes = String.join("", list).getBytes(StandardCharsets.UTF_8);
        } else {
            reqBytes = String.join("", httpInfo.getHttpReqDescList()).getBytes(StandardCharsets.UTF_8);
        }

        JComponent reqComponent = createEditor(reqBytes, "req.http", project, tabName);
        requestPanel.add(reqComponent, constraints);

        if (hasError) {
            String msg = ExceptionUtils.getStackTrace(httpException);
            JComponent jComponent = createEditor(msg.getBytes(StandardCharsets.UTF_8), "error.log", project, tabName);
            responsePanel.add(jComponent, constraints);
            return;
        }

        if (imageType) {
            //noinspection DataFlowIssue
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(httpInfo.getByteArray())) {
                BufferedImage bufferedImage = ImageIO.read(inputStream);

                int inputWidth = bufferedImage.getWidth();
                int inputHeight = bufferedImage.getHeight();

                int outputWidth = 400;
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

        byte[] resBytes = String.join("", httpInfo.getHttpResDescList())
                .getBytes(StandardCharsets.UTF_8);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);
        JComponent resComponent = createEditor(resBytes, "res.http", project, tabName);
        responsePanel.add(resComponent, constraintsRes);
    }

    public JComponent createEditor(byte[] bytes, String suffix, Project project, String tabName) {
        VirtualFile virtualFile = VirtualFileUtils.createHttpVirtualFileFromText(bytes, suffix, project, tabName);
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile psiFile = PsiUtil.getPsiFile(project, virtualFile);
        Document document = psiDocumentManager.getDocument(psiFile);

        EditorFactory editorFactory = EditorFactory.getInstance();
        @SuppressWarnings("DataFlowIssue")
        Editor editor = editorFactory.createEditor(document, project, virtualFile, true);
        editorList.add(editor);

        return editor.getComponent();
    }

    public void initPanelWsData(WsRequest wsRequest) {
        GridLayoutManager layout = (GridLayoutManager) requestPanel.getParent().getLayout();
        GridConstraints constraints = layout.getConstraintsForComponent(requestPanel);

        JPanel jPanelReq = new JPanel();
        jPanelReq.setLayout(new BorderLayout());

        JTextArea jTextAreaReq = new JTextArea();
        jTextAreaReq.setToolTipText("请输入ws消息");
        jPanelReq.add(jTextAreaReq, BorderLayout.CENTER);

        JButton jButtonSend = new JButton("发送ws消息");
        JButton jButtonAbort = new JButton("关闭ws连接");

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout());
        btnPanel.add(jButtonSend);
        btnPanel.add(jButtonAbort);

        jPanelReq.add(btnPanel, BorderLayout.SOUTH);

        requestPanel.add(jPanelReq, constraints);

        GridLayoutManager layoutRes = (GridLayoutManager) responsePanel.getParent().getLayout();
        GridConstraints constraintsRes = layoutRes.getConstraintsForComponent(responsePanel);

        JTextArea jTextAreaRes = new JTextArea();
        jTextAreaRes.setLineWrap(true);
        jTextAreaRes.setEditable(false);
        JBScrollPane jScrollPane = new JBScrollPane(jTextAreaRes);
        responsePanel.add(jScrollPane, constraintsRes);

        jButtonSend.addActionListener(e -> {
            String text = jTextAreaReq.getText();
            wsRequest.sendWsMsg(text);
            jTextAreaReq.setText("");
        });

        jButtonAbort.addActionListener(e -> wsRequest.abortConnect());

        wsRequest.setConsumer(jTextAreaRes::append);
    }

    @Override
    public void dispose() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorList.forEach(editorFactory::releaseEditor);
    }
}
