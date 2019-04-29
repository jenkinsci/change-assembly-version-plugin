package org.jenkinsci.plugins.changeassemblyversion;

import com.sun.media.jfxmedia.track.Track;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
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
            ByteOrderMark bom = inputStream.getBOM();

            Charset fileEncoding = Charset.defaultCharset();
            if (bom != null) {
                fileEncoding = Charset.forName(bom.getCharsetName());
            }

            String content = IOUtils.toString(inputStream, fileEncoding);
            inputStream.close();
            listener.getLogger().println(String.format("Updating file : %s, Replacement : %s", file.getRemote(), replacement));
            content = content.replaceAll(regexPattern, String.format(replacementPattern, replacement));
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
            listener.getLogger().println(String.format("Skipping replacement because value is empty."));
        }
    }
}
