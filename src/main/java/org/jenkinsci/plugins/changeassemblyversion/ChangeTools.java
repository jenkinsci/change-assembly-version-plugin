package org.jenkinsci.plugins.changeassemblyversion;

import hudson.FilePath;
import hudson.model.BuildListener;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.commons.io.input.BOMInputStream;

public class ChangeTools {

    ChangeTools() {
    }

    public static void replaceOrAppend(FilePath file, Pattern regexPattern, String replacement, String replacementPattern,BuildListener listener) throws IOException, InterruptedException {
        if (replacement != null && !replacement.isEmpty())
        {
            BOMInputStream bs = new BOMInputStream(file.read());        //removes BOM
            String content = org.apache.commons.io.IOUtils.toString(bs);
            //String content = file.readToString();  // needs to use read() instead!
            listener.getLogger().println(String.format("Updating file : %s, Replacement : %s", file.getRemote(), replacement));
            //String newContent = content.replaceAll(regexPattern.toString(), String.format(replacementPattern, replacement));
            String newContent = regexPattern.matcher(content).replaceFirst(String.format(replacementPattern, replacement));
            listener.getLogger().println(String.format("regex= %s",regexPattern.matcher(content).pattern()));
            if(content.equals(newContent)){
                newContent+=System.lineSeparator()+String.format(replacementPattern, replacement);
            }
            
            //listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
            file.write(newContent, null);
        }
        else
        {
            listener.getLogger().println(String.format("Skipping replacement because replacemnt value is empty."));
        }
    }
}
