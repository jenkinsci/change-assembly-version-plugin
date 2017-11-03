package org.jenkinsci.plugins.changeassemblyversion;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;

import java.io.IOException;

public class ChangeTools {

    private final FilePath file;
    private final String regexPattern;
    private final String replacementPattern;

    ChangeTools(FilePath f, String regexPattern, String replacementPattern) {
        this.file = f;
        if (regexPattern != null && !regexPattern.equals("")) {
            this.regexPattern = regexPattern;
        } else {
            this.regexPattern = "Version[(]\"[\\d\\.]+\"[)]";
        }

        if (replacementPattern != null && !replacementPattern.equals("")) {
            this.replacementPattern = replacementPattern;
        } else {
            this.replacementPattern = "Version(\"%s\")";
        }
    }

    public void Replace(String replacement, TaskListener listener) throws IOException, InterruptedException {
        if (replacement != null && !replacement.isEmpty())
        {
            String content = file.readToString();  // needs to use read() instead!
            listener.getLogger().println(String.format("Updating file : %s, Replacement : %s", file.getRemote(), replacement));
            content = content.replaceAll(regexPattern, String.format(replacementPattern, replacement));
            //listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
            file.write(content, null);
        }
        else
        {
            listener.getLogger().println(String.format("Skipping replacement because value is empty."));
        }
    }
}
