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
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author <a href="mailto:leonardo.kobus@hbsis.com.br">Leonardo Kobus</a>
 */
public class ChangeAssemblyVersion extends Builder {

    private final String versionPattern;
    private final String assemblyFile;
    private final String regexPattern;
    private final String replacementPattern;
    private final String assemblyTitle;
    private final String assemblyDescription;
    private final String assemblyCompany;
    private final String assemblyProduct;
    private final String assemblyCopyright;
    private final String assemblyTrademark;
    private final String assemblyCulture;

    @DataBoundConstructor
    public ChangeAssemblyVersion(String versionPattern, 
        String assemblyFile, 
        String regexPattern, 
        String replacementPattern, 
        String assemblyTitle,
        String assemblyDescription,
        String assemblyCompany,
        String assemblyProduct,
        String assemblyCopyright,
        String assemblyTrademark,
        String assemblyCulture
        ) {
        this.versionPattern = versionPattern;
        this.assemblyFile = assemblyFile;
        this.regexPattern = regexPattern;
        this.replacementPattern = replacementPattern;
        this.assemblyTitle = assemblyTitle;
        this.assemblyDescription = assemblyDescription;
        this.assemblyCompany = assemblyCompany;
        this.assemblyProduct = assemblyProduct;
        this.assemblyCopyright = assemblyCopyright;
        this.assemblyTrademark = assemblyTrademark;
        this.assemblyCulture = assemblyCulture;
    }

    public String getVersionPattern() {
        return this.versionPattern;
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
    
    public String getAssemblyTitle() {
        return this.assemblyTitle;
    }
    
    public String getAssemblyDescription() {
        return this.assemblyDescription;
    }
    
    public String getAssemblyCompany() {
        return this.assemblyCompany;
    }
    
    public String getAssemblyProduct() {
        return this.assemblyProduct;
    }            

    public String getAssemblyCopyright() {
        return this.assemblyCopyright;
    }

    public String getAssemblyTrademark() {
        return this.assemblyTrademark;
    }
    
    public String getAssemblyCulture() {
        return this.assemblyCulture;
    }
    
    /**
     *
     * The perform method is gonna search all the file named "Assemblyinfo.cs"
     * in any folder below, and after found will change the version of
     * AssemblyVersion and AssemblyFileVersion in the file for the inserted
     * version (versionPattern property value).
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
            String assemblyGlob = this.assemblyFile == null || this.assemblyFile.equals("") ? "**/AssemblyInfo.cs" : TokenMacro.expandAll(build, listener, this.assemblyFile);

            EnvVars envVars = build.getEnvironment(listener);
            String version = TokenMacro.expandAll(build, listener, this.versionPattern);
            if (versionPattern == null || StringUtils.isEmpty(versionPattern))
            {
                listener.getLogger().println("Please provide a valid version pattern.");
                return false;
            }
            
            // Expand env variables and token macros
            String assemblyTitle = TokenMacro.expandAll(build, listener, this.assemblyTitle);
            String assemblyDescription = TokenMacro.expandAll(build, listener, this.assemblyDescription);
            String assemblyCompany = TokenMacro.expandAll(build, listener, this.assemblyCompany);
            String assemblyProduct = TokenMacro.expandAll(build, listener, this.assemblyProduct);
            String assemblyCopyright = TokenMacro.expandAll(build, listener, this.assemblyCopyright);
            String assemblyTrademark = TokenMacro.expandAll(build, listener, this.assemblyTrademark);
            String assemblyCulture = TokenMacro.expandAll(build, listener, this.assemblyCulture);
            
            
            // Log new expanded values
            listener.getLogger().println(String.format("Changing File(s): %s", assemblyGlob));
            listener.getLogger().println(String.format("Assembly Version : %s",  version));
            listener.getLogger().println(String.format("Assembly Title : %s",  assemblyTitle));
            listener.getLogger().println(String.format("Assembly Description : %s",  assemblyDescription));
            listener.getLogger().println(String.format("Assembly Company : %s",  assemblyCompany));
            listener.getLogger().println(String.format("Assembly Product : %s",  assemblyProduct));
            listener.getLogger().println(String.format("Assembly Copyright : %s",  assemblyCopyright));
            listener.getLogger().println(String.format("Assembly Trademark : %s",  assemblyTrademark));
            listener.getLogger().println(String.format("Assembly Culture : %s",  assemblyCulture));
            
            for (FilePath f : build.getWorkspace().list(assemblyGlob))
            {
                // Update the AssemblyVerion and AssemblyFileVersion
                new ChangeTools(f, this.regexPattern, this.replacementPattern).Replace(version, listener);
                
                // Set new things, empty string being ok for them.
                // TODO: Would we need a regex for these or just blast as we are doing now?
                new ChangeTools(f, "AssemblyTitle[(]\".*\"[)]", "AssemblyTitle(\"%s\")").Replace(assemblyTitle, listener);            
                new ChangeTools(f, "AssemblyDescription[(]\".*\"[)]", "AssemblyDescription(\"%s\")").Replace(assemblyDescription, listener);
                new ChangeTools(f, "AssemblyCompany[(]\".*\"[)]", "AssemblyCompany(\"%s\")").Replace(assemblyCompany, listener);
                new ChangeTools(f, "AssemblyProduct[(]\".*\"[)]", "AssemblyProduct(\"%s\")").Replace(assemblyProduct, listener);
                new ChangeTools(f, "AssemblyCopyright[(]\".*\"[)]", "AssemblyCopyright(\"%s\")").Replace(assemblyCopyright, listener);
                new ChangeTools(f, "AssemblyTrademark[(]\".*\"[)]", "AssemblyTrademark(\"%s\")").Replace(assemblyTrademark, listener);
                new ChangeTools(f, "AssemblyCulture[(]\".*\"[)]", "AssemblyCulture(\"%s\")").Replace(assemblyCulture, listener);
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
