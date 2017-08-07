package org.jenkinsci.plugins.changeassemblyversion;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import java.io.File;

import java.io.IOException;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.jenkinsci.remoting.RoleChecker;

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

    public void replace(String replacement, BuildListener listener) throws IOException, InterruptedException {        
        if (replacement != null && !replacement.isEmpty()) {
            BOMInputStream bim = new BOMInputStream(file.read(), true);
            String charset = bim.getBOMCharsetName();
            String content = IOUtils.toString(bim, charset);
            listener.getLogger().println(String.format("Updating file : %s, Replacement : %s", file.getRemote(), replacement));
            listener.getLogger().println("Detected charset: "+charset);
            content = content.replaceAll(regexPattern, String.format(replacementPattern, replacement));
            bim.close();
            //listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
            file.write(content, charset);
        } else {
            listener.getLogger().println(String.format("Skipping replacement because value is empty."));
        }
    }

}
