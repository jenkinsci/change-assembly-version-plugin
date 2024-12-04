package org.jenkinsci.plugins.changeassemblyversion;

import hudson.EnvVars;
import hudson.model.BuildListener;

public class AssemblyVersion {

    private String version;
    private EnvVars envVars;
    private BuildListener listener;

    /**
     * The instance of this class gonna return in the property version the value to be used on ChangeTools.
     * @param version
     * @param envVars
     */
    public AssemblyVersion(String version, EnvVars envVars) {
        this.envVars = envVars;

        this.version = envVars.expand(version);
    }

    public String getVersion() {
        return this.version;
    }
}
