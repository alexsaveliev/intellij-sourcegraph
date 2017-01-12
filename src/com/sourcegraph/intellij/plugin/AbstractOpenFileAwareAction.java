package com.sourcegraph.intellij.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleWalk;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Iterator;

abstract class AbstractOpenFileAwareAction extends AnAction {

    String repo;
    String hash;
    String file;

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(checkEnabled(e));
    }

    private boolean checkEnabled(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (virtualFile == null || !virtualFile.exists() || !virtualFile.isInLocalFileSystem()) {
            return false;
        }

        Project project = e.getProject();
        if (project == null) {
            return false;
        }

        File file = toLocalFile(virtualFile);
        File root = toLocalFile(project.getBaseDir());

        if (file == null || root == null || !FileUtil.isAncestor(root, file, false)) {
            return false;
        }

        String rel = FileUtil.getRelativePath(root, file);
        if (rel == null) {
            return false;
        }

        StringBuilder builder = new StringBuilder();
        for (Path component : new File(rel).toPath()) {
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(component);
        }
        this.file = builder.toString();

        File git = getGitDir(file, root);
        if (git == null) {
            return false;
        }

        try {
            FileRepository repo = new FileRepository(git);

            this.repo = getBestRemote(repo);
            if (this.repo == null) {
                return false;
            }
            this.repo = cleanup(this.repo);
            if (this.repo == null) {
                return false;
            }

            Git gitObject = new Git(repo);

            Status status = gitObject.
                    status().
                    addPath(this.file).
                    setIgnoreSubmodules(SubmoduleWalk.IgnoreSubmoduleMode.ALL).
                    call();
            if (status.hasUncommittedChanges() || !status.getUntracked().isEmpty()) {
                return false;
            }
            Iterator<RevCommit> commits = gitObject.log().setMaxCount(1).call().iterator();
            if (!commits.hasNext()) {
                return false;
            }
            RevCommit last = commits.next();
            this.hash = last.getId().getName();

        } catch (IOException | GitAPIException ex) {
            return false;
        }

        return true;
    }

    private static String cleanup(String repo) {
        try {
            String ret;
            if (repo.startsWith("git@github.com:")) {
                ret = "github.com/" + repo.substring("git@github.com:".length());
            } else {
                URI uri = new URI(repo);
                ret = uri.getHost() + uri.getPath();
            }
            if (ret.startsWith("github.com") && ret.endsWith(".git")) {
                ret = ret.substring(ret.length() - 4);
            }
            return ret;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static File toLocalFile(VirtualFile virtualFile) {
        LocalFileSystem lfs = LocalFileSystem.getInstance();
        VirtualFile localFile = lfs.findFileByPath(virtualFile.getPath());
        if (localFile == null) {
            return null;
        }
        return new File(localFile.getPath());
    }

    private static File getGitDir(File path, File root) {
        if (path.isFile()) {
            path = path.getParentFile();
        }
        do {
            File candidate = new File(path, ".git");
            if (candidate.isDirectory()) {
                return candidate;
            }
            path = path.getParentFile();
        } while (FileUtil.isAncestor(root, path, false));
        return null;
    }

    private static String getBestRemote(Repository repository) {
        String first = null;

        StoredConfig config = repository.getConfig();

        for (String remoteName : config.getSubsections("remote")) {
            String url = config.getString("remote", remoteName, "url");
            if ("origin".equals(remoteName)) {
                return url;
            }
            if (first == null) {
                first = url;
            }
        }

        return first;
    }
}