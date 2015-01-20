/*
 * The MIT License
 *
 * Copyright 2014 BELLINSALARIN.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.changeassemblyversion;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

/**
 *
 * @author BELLINSALARIN
 */
public class ReplacementsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testResolveEnvironmentVariables() throws InterruptedException, IOException, Exception {

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("PREFIX", "1.1.0");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                    BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child("AssemblyVersion.cs").write("using System.Reflection;\n" +
"\n" +
"[assembly: AssemblyVersion(\"13.1.1.976\")]", "UTF-8");
                return true;
            }
        });
        ChangeAssemblyVersion builder = new ChangeAssemblyVersion("$PREFIX.${BUILD_NUMBER}", "AssemblyVersion.cs", "", "");
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //String s = FileUtils.readFileToString(build.getLogFile());
        String content = build.getWorkspace().child("AssemblyVersion.cs").readToString();
        assertTrue(content.contains("AssemblyVersion(\"1.1.0."));
        assertTrue(builder.getVersionPattern().equals("$PREFIX.${BUILD_NUMBER}"));
    }
    
    @Test
    public void testResolveEnvironmentVariables_recursively_excludingSvn() throws InterruptedException, IOException, Exception {

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("PREFIX", "1.1.0");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        final String f1 = "myassembly/properties/AssemblyInfo.cs";
        final String f2 = ".svn/myassembly/properties/AssemblyInfo.cs";
        final String c = "using System.Reflection;\n" +
"\n" +
"[assembly: AssemblyVersion(\"13.1.1.976\")]";
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                    BuildListener listener) throws InterruptedException, IOException {
                build.getWorkspace().child(f1).write(c, "UTF-8");
                build.getWorkspace().child(f2).write(c, "UTF-8");
                return true;
            }
        });
        ChangeAssemblyVersion builder = new ChangeAssemblyVersion("$PREFIX.${BUILD_NUMBER}", "", "", "");
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //String s = FileUtils.readFileToString(build.getLogFile());
        String content = build.getWorkspace().child(f1).readToString();
        assertTrue(content.contains("AssemblyVersion(\"1.1.0."));
        content = build.getWorkspace().child(f2).readToString();
        assertTrue(content.contains("AssemblyVersion(\"13.1.1.976"));
        assertTrue(builder.getVersionPattern().equals("$PREFIX.${BUILD_NUMBER}"));
    }
}
