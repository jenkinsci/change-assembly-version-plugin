package org.jenkinsci.plugins.changeassemblyversion;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
            BOMInputStream inputStream = new BOMInputStream(file.read());
            String content;
            ByteOrderMark bom;
            Charset fileEncoding = Charset.defaultCharset();
            try {
                bom = inputStream.getBOM();
                if (bom != null) {
                    fileEncoding = Charset.forName(bom.getCharsetName());
                }

                content = IOUtils.toString(inputStream, fileEncoding);
            }
            finally {
                inputStream.close();
            }
            listener.getLogger().println("Updating file : %s, Replacement : %s".formatted(file.getRemote(), replacement));
            content = content.replaceAll(regexPattern, replacementPattern.formatted(replacement));
            //listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
            OutputStream os = file.write();
            try {
                if (bom != null){
                    os.write(bom.getBytes());
                }
                os.write(content.getBytes(fileEncoding));
            } finally {
                os.close();
            }
        }
        else
        {
            listener.getLogger().println("Skipping replacement because value is empty.".formatted());
        }
    }
}
