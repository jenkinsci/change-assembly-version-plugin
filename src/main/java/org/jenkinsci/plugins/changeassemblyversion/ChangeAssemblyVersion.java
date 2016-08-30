package org.jenkinsci.plugins.changeassemblyversion;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author <a href="mailto:leonardo.kobus@hbsis.com.br">Leonardo Kobus</a>
 */
public class ChangeAssemblyVersion extends Builder {

    private final String assemblyCultureString;
    private final String assemblyTrademarkString;
    private final String assemblyCopyrightString;
    private final String assemblyProductString;
    private final String assemblyCompanyString;
    private final String assemblyDescriptionString;
    private final String assemblyTitleString;
    private final String assemblyFileVersionString;
    private final String assemblyInfoVersionString;
    private final String assemblyVersionString;

    private final String BASE_REGEX;

    private final String BasePattern;

    private final String assemblyFile;
    private final String assemblyVersion;
    private final String assemblyFileVersion;
    private final String assemblyInformationalVersion;
    private final String assemblyTitle;
    private final String assemblyDescription;
    private final String assemblyCompany;
    private final String assemblyProduct;
    private final String assemblyCopyright;
    private final String assemblyTrademark;
    private final String assemblyCulture;

    private final Pattern assemblyVersionRegex;
    private final Pattern assemblyInfoVersionRegex;
    private final Pattern assemblyFileVersionRegex;

    private final Pattern assemblyTitleRegex;
    private final Pattern assemblyDescriptionRegex;
    private final Pattern assemblyCompanyRegex;
    private final Pattern assemblyProductRegex;
    private final Pattern assemblyCopyrightRegex;
    private final Pattern assemblyTrademarkRegex;
    private final Pattern assemblyCultureRegex;

    @DataBoundConstructor
    public ChangeAssemblyVersion(
            String assemblyVersion,
            String assemblyFileVersion,
            String assemblyInformationalVersion,
            String assemblyFile,
            String assemblyTitle,
            String assemblyDescription,
            String assemblyCompany,
            String assemblyProduct,
            String assemblyCopyright,
            String assemblyTrademark,
            String assemblyCulture
    ) {
        this.BasePattern = "[assembly: %s]";
        this.assemblyCultureString = String.format(BasePattern, "AssemblyCulture (\"%s\")");
        this.assemblyTrademarkString = String.format(BasePattern, "AssemblyTrademark (\"%s\")");
        this.assemblyCopyrightString = String.format(BasePattern, "AssemblyCopyright (\"%s\")");
        this.assemblyProductString = String.format(BasePattern, "AssemblyProduct (\"%s\")");
        this.assemblyCompanyString = String.format(BasePattern, "AssemblyCompany (\"%s\")");
        this.assemblyDescriptionString = String.format(BasePattern, "AssemblyDescription (\"%s\")");
        this.assemblyTitleString = String.format(BasePattern, "AssemblyTitle (\"%s\")");
        this.assemblyFileVersionString = String.format(BasePattern, "AssemblyFileVersion (\"%s\")");
        this.assemblyInfoVersionString = String.format(BasePattern, "AssemblyInformationalVersion (\"%s\")");
        this.assemblyVersionString = String.format(BasePattern, "AssemblyVersion (\"%s\")");

        this.BASE_REGEX = "\\s*\\[\\s*assembly:\\s*%s\\s*\\(\\s*\\\".*\\\"\\s*\\)\\s*\\]"; //ok
        this.assemblyCultureRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyCulture"));
        this.assemblyTrademarkRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyTrademark"));
        this.assemblyCopyrightRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyCopyright"));
        this.assemblyProductRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyProduct"));
        this.assemblyCompanyRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyCompany"));
        this.assemblyDescriptionRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyDescription"));
        this.assemblyTitleRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyTitle"));
        this.assemblyFileVersionRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyFileVersion"));
        this.assemblyInfoVersionRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyInformationalVersion"));
        this.assemblyVersionRegex = Pattern.compile(String.format(BASE_REGEX, "AssemblyVersion"));

        this.assemblyVersion = assemblyVersion;
        this.assemblyFileVersion = assemblyFileVersion;
        this.assemblyInformationalVersion = assemblyInformationalVersion;

        this.assemblyFile = assemblyFile;
        this.assemblyTitle = assemblyTitle;
        this.assemblyDescription = assemblyDescription;
        this.assemblyCompany = assemblyCompany;
        this.assemblyProduct = assemblyProduct;
        this.assemblyCopyright = assemblyCopyright;
        this.assemblyTrademark = assemblyTrademark;
        this.assemblyCulture = assemblyCulture;
    }
    
    public String getAssemblyVersion() {
        return this.assemblyVersion;
    }
    
    public String getAssemblyFileVersion() {
        return this.assemblyFileVersion;
    }
    
    public String getAssemblyInformationalVersion() {
        return this.assemblyInformationalVersion;
    }

    public String getAssemblyFile() {
        return this.assemblyFile;
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
            String assemblyGlob = this.assemblyFile == null || this.assemblyFile.isEmpty() ? "**/AssemblyInfo.cs" : TokenMacro.expandAll(build, listener, this.assemblyFile);

            // Expand env variables and token macros
            String expandedAssemblyVersion = TokenMacro.expandAll(build, listener, this.assemblyVersion);
            String expandedAssemblyFileVersion = TokenMacro.expandAll(build, listener, this.assemblyFileVersion);
            String expandedAssemblyInformationalVersion = TokenMacro.expandAll(build, listener, this.assemblyInformationalVersion);
            String expandedAssemblyTitle = TokenMacro.expandAll(build, listener, this.assemblyTitle);
            String expandedAssemblyDescription = TokenMacro.expandAll(build, listener, this.assemblyDescription);
            String expandedAssemblyCompany = TokenMacro.expandAll(build, listener, this.assemblyCompany);
            String expandedAssemblyProduct = TokenMacro.expandAll(build, listener, this.assemblyProduct);
            String expandedAssemblyCopyright = TokenMacro.expandAll(build, listener, this.assemblyCopyright);
            String expandedAssemblyTrademark = TokenMacro.expandAll(build, listener, this.assemblyTrademark);
            String expandedAssemblyCulture = TokenMacro.expandAll(build, listener, this.assemblyCulture);

            // Log new expanded values
            listener.getLogger().println(String.format("Changing File(s): %s", assemblyGlob));
            listener.getLogger().println(String.format("Assembly Version : %s", expandedAssemblyVersion));
            listener.getLogger().println(String.format("Assembly File Version : %s", expandedAssemblyFileVersion));
            listener.getLogger().println(String.format("Assembly Informational Version : %s", expandedAssemblyInformationalVersion));
            listener.getLogger().println(String.format("Assembly Title : %s", expandedAssemblyTitle));
            listener.getLogger().println(String.format("Assembly Description : %s", expandedAssemblyDescription));
            listener.getLogger().println(String.format("Assembly Company : %s", expandedAssemblyCompany));
            listener.getLogger().println(String.format("Assembly Product : %s", expandedAssemblyProduct));
            listener.getLogger().println(String.format("Assembly Copyright : %s", expandedAssemblyCopyright));
            listener.getLogger().println(String.format("Assembly Trademark : %s", expandedAssemblyTrademark));
            listener.getLogger().println(String.format("Assembly Culture : %s", expandedAssemblyCulture));

            FilePath workspace = build.getWorkspace();
            if (workspace == null) {
                throw new AbortException("Unable to retrieve workspace");
            } else {
                for (FilePath f : workspace.list(assemblyGlob)) {
                    ChangeTools.replaceOrAppend(f, assemblyTitleRegex, expandedAssemblyVersion, assemblyVersionString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyTitleRegex, expandedAssemblyFileVersion, assemblyFileVersionString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyTitleRegex, expandedAssemblyInformationalVersion, assemblyInfoVersionString, listener);

                    // Set new things, empty string being ok for them.
                    // TODO: Would we need a regex for these or just blast as we are doing now?
                    ChangeTools.replaceOrAppend(f, assemblyTitleRegex, expandedAssemblyTitle, assemblyTitleString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyDescriptionRegex, expandedAssemblyDescription, assemblyDescriptionString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyCompanyRegex, expandedAssemblyCompany, assemblyCompanyString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyProductRegex, expandedAssemblyProduct, assemblyProductString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyCopyrightRegex, expandedAssemblyCopyright, assemblyCopyrightString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyTrademarkRegex, expandedAssemblyTrademark, assemblyTrademarkString, listener);
                    ChangeTools.replaceOrAppend(f, assemblyCultureRegex, expandedAssemblyCulture, assemblyCultureString, listener);
                }
            }
        } catch (IOException | InterruptedException | MacroEvaluationException ex) {
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
