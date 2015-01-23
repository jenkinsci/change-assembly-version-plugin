package org.jenkinsci.plugins.changeassemblyversion;

import java.io.PrintWriter;
import java.io.StringWriter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author <a href="mailto:leonardo.kobus@hbsis.com.br">Leonardo Kobus</a>
 */
public class ChangeAssemblyVersion extends Builder {

    private final String task;
    private final String assemblyFile;
    private final String regexPattern;
    private final String replacementPattern;

    @DataBoundConstructor
    public ChangeAssemblyVersion(String task, String assemblyFile, String regexPattern, String replacementPattern) {
        this.task = task;
        this.assemblyFile = assemblyFile;
        this.regexPattern = regexPattern;
        this.replacementPattern = replacementPattern;
    }

    public String getVersionPattern() {
        return this.task;
    }

    public String getAssemblyFile() {
        return this.assemblyFile;
    }

    public String getRegexPattern() {
        return this.regexPattern;
    }

    public String getReplacementPattern() {
        return this.replacementPattern;
    }

    /**
     *
     * The perform method is gonna search all the file named "Assemblyinfo.cs"
     * in any folder below, and after found will change the version of
     * AssemblyVersion and AssemblyFileVersion in the file for the inserted
     * version (task property value).
     *
     *
     * OBS: The inserted value can be some jenkins variable like ${BUILD_NUMBER}
     * just the variable alone, but not implemented to treat
     * 0.0.${BUILD_NUMBER}.0 I think this plugin must be used with Version
     * Number Plugin.
     *
     *
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {
            String assemblyGlob = this.assemblyFile == null || this.assemblyFile.equals("") ? "**/AssemblyInfo.cs" : this.assemblyFile;

            EnvVars envVars = build.getEnvironment(listener);
            String version = new AssemblyVersion(this.task, envVars).getVersion();
            if (task == null || StringUtils.isEmpty(task))
            {
                listener.getLogger().println("Please provide a valid version pattern.");
                return false;
            }
            listener.getLogger().println(String.format("Changing the file(s) %s to version : %s", assemblyGlob, version));
            for (FilePath f : build.getWorkspace().list(assemblyGlob))
            {
                new ChangeTools(f, this.regexPattern, this.replacementPattern).Replace(version, listener);                
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            listener.getLogger().println(sw.toString());
            return false;
        }
        return true;
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Change Assembly Version";
        }
    }

}
