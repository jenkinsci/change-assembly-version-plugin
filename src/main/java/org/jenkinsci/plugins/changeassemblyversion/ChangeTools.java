package org.jenkinsci.plugins.changeassemblyversion;

import hudson.model.BuildListener;
import java.io.IOException;
import java.util.regex.Pattern;

public class ChangeTools {

    ChangeTools() {
    }

    public static String replaceOrAppend(String content, Pattern regexPattern, String replacement, String replacementPattern, BuildListener listener) throws IOException, InterruptedException {
        if (replacement != null && !replacement.isEmpty()) {
            //listener.getLogger().println(String.format("\t Replacement : %s", replacement));
            //String newContent = content.replaceAll(regexPattern.toString(), String.format(replacementPattern, replacement));
            //regexPattern.matcher(content).region(0, 0);
            content = regexPattern.matcher(content).replaceFirst(String.format(replacementPattern, replacement));
            //listener.getLogger().println(String.format("regex= %s",regexPattern.matcher(content).pattern()));
            try {
                regexPattern.matcher(content).group(); //throws illegalstate if no match was perfomred
            } catch (IllegalStateException ex) {
                listener.getLogger().println("Addidng missing value");
                content += System.lineSeparator() + String.format(replacementPattern, replacement);
            }
        } else {
            listener.getLogger().println(String.format("Skipping replacement because replacemnt value is empty."));
        }
        return content;
    }
}
