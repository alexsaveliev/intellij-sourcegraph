package com.sourcegraph.intellij.plugin;

class Util  {

    static String url(String repo, String hash, String path) {
        return "https://sourcegraph.com/" + repo + '@' + hash + "/-/blob/" + path;
    }

    static String url(String repo,
                             String hash,
                             String path,
                             int startLine,
                             int startColumn,
                             int endLine,
                             int endColumn) {
        String ret = "https://sourcegraph.com/" + repo + '@' + hash + "/-/blob/" + path + "#L" +
                (startLine + 1) + ":" + (startColumn + 1);
        if (startLine != endLine || startColumn != endColumn) {
            ret += "-" + (endLine + 1) + ":" + (endColumn + 1);
        }
        return ret;
    }

}
