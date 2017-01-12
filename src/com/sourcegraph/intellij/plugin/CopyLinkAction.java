package com.sourcegraph.intellij.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class CopyLinkAction extends AbstractOpenFileAwareAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        CopyPasteManager.getInstance().setContents(new StringSelection(Util.url(this.repo, this.hash, this.file)));
    }
}
