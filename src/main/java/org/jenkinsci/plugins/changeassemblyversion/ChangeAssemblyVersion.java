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
import org.apache.commons.io.input.BOMInputStream;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author <a href="mailto:leonardo.kobus@hbsis.com.br">Leonardo Kobus</a>
 */
public class ChangeAssemblyVersion extends Builder {

    private final String assemblyCultureReplacementString;
    private final String assemblyTrademarkReplacementString;
    private final String assemblyCopyrightReplacementString;
    private final String assemblyProductReplacmentString;
    private final String assemblyCompanyReplacementString;
    private final String assemblyDescriptionReplacementString;
    private final String assemblyTitleReplacmentString;
    private final String assemblyFileVersionReplacementString;
    private final String assemblyInfoVersionReplacementString;
    private final String assemblyVersionReplacementString;

    /*
    private final String assemblyCultureString;
    private final String assemblyTrademarkString;
    private final String assemblyCopyrightString;
    private final String assemblyProductString;
    private final String assemblyCompanyString;
    private final String assemblyDescriptionString;
    private final String assemblyTitleString;
    private final String assemblyFileVersionString;
    private final String assemblyInfoVersionString;
    private final String assemblyVersionString; */
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
        /*
        this.assemblyCultureString = "AssemblyCulture";
        this.assemblyTrademarkString = "AssemblyTrademark";
        this.assemblyCopyrightString = "AssemblyCopyright";
        this.assemblyProductString = "AssemblyProduct";
        this.assemblyCompanyString = "AssemblyCompany";
        this.assemblyDescriptionString = "AssemblyDescription";
        this.assemblyTitleString = "AssemblyTitle";
        this.assemblyFileVersionString = "AssemblyFileVersion";
        this.assemblyInfoVersionString = "AssemblyInformationalVersion";
        this.assemblyVersionString = "AssemblyVersion"; */

        this.BasePattern = "[assembly: %s]";
        this.assemblyCultureReplacementString = String.format(BasePattern, "AssemblyCulture(\"%s\")");
        this.assemblyTrademarkReplacementString = String.format(BasePattern, "AssemblyTrademark(\"%s\")");
        this.assemblyCopyrightReplacementString = String.format(BasePattern, "AssemblyCopyright(\"%s\")");
        this.assemblyProductReplacmentString = String.format(BasePattern, "AssemblyProduct(\"%s\")");
        this.assemblyCompanyReplacementString = String.format(BasePattern, "AssemblyCompany(\"%s\")");
        this.assemblyDescriptionReplacementString = String.format(BasePattern, "AssemblyDescription(\"%s\")");
        this.assemblyTitleReplacmentString = String.format(BasePattern, "AssemblyTitle(\"%s\")");
        this.assemblyFileVersionReplacementString = String.format(BasePattern, "AssemblyFileVersion(\"%s\")");
        this.assemblyInfoVersionReplacementString = String.format(BasePattern, "AssemblyInformationalVersion(\"%s\")");
        this.assemblyVersionReplacementString = String.format(BasePattern, "AssemblyVersion(\"%s\")");

        //http://stackoverflow.com/questions/39257137/java-regex-to-filter-lines-with-comment-not-working-as-expected
        this.BASE_REGEX = "(?m)((?:\\G|^)[^\\[/\\n]*+(?:\\[(?!assembly:\\s*?%1$s\\s*?\\(\\s*?\\\".*?\\\"\\s*?\\)\\s*?\\])[^\\[/\\n]*|/(?!/)[^\\[/\\n]*)*+)\\[assembly:\\s*?%1$s\\s*?\\(\\s*?\\\".*?\\\"\\s*?\\)\\s*?\\]";

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
            String expandedAssemblyInfoVersion = TokenMacro.expandAll(build, listener, this.assemblyInformationalVersion);
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
            listener.getLogger().println(String.format("Assembly Informational Version : %s", expandedAssemblyInfoVersion));
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
                    listener.getLogger().println(String.format("Updating file : %s", f.getRemote()));
                    BOMInputStream bs = new BOMInputStream(f.read());        //removes BOM
                    String content = org.apache.commons.io.IOUtils.toString(bs);
                    content = ChangeTools.replaceOrAppend(content, assemblyVersionRegex, expandedAssemblyVersion, assemblyVersionReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyFileVersionRegex, expandedAssemblyFileVersion, assemblyFileVersionReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyInfoVersionRegex, expandedAssemblyInfoVersion, assemblyInfoVersionReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyTitleRegex, expandedAssemblyTitle, assemblyTitleReplacmentString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyDescriptionRegex, expandedAssemblyDescription, assemblyDescriptionReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyCompanyRegex, expandedAssemblyCompany, assemblyCompanyReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyProductRegex, expandedAssemblyProduct, assemblyProductReplacmentString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyCopyrightRegex, expandedAssemblyCopyright, assemblyCopyrightReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyTrademarkRegex, expandedAssemblyTrademark, assemblyTrademarkReplacementString, listener);
                    content = ChangeTools.replaceOrAppend(content, assemblyCultureRegex, expandedAssemblyCulture, assemblyCultureReplacementString, listener);
                    f.write(content, null);
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
