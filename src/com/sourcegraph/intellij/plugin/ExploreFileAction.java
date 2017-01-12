package com.sourcegraph.intellij.plugin;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExploreFileAction extends AbstractOpenFileAwareAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BrowserUtil.browse(Util.url(this.repo, this.hash, this.file));
    }
}
