package com.sourcegraph.intellij.plugin;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;

public class ExploreAtCursorAction extends AbstractOpenFileAwareAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return;
        }

        Caret caret = editor.getCaretModel().getCurrentCaret();
        LogicalPosition start = editor.visualToLogicalPosition(caret.getSelectionStartPosition());
        LogicalPosition end = editor.visualToLogicalPosition(caret.getSelectionEndPosition());
        BrowserUtil.browse(Util.url(this.repo, this.hash, this.file, start.line, start.column, end.line, end.column));
    }
}
