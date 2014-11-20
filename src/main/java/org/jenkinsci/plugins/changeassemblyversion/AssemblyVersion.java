package org.jenkinsci.plugins.changeassemblyversion;

import hudson.EnvVars;
import hudson.model.BuildListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	public AssemblyVersion(String version, EnvVars envVars, BuildListener listener){		
		this.envVars = envVars;
		this.version = version;		
		this.listener = listener;
		
		if(version.contains("${")){
			listener.getLogger().println("Version contains variable.");
			this.version = getEnvVersion(version);			
		}
	}
	
	public String getVersion(){
		return this.version;
	}
	
	private String getEnvVersion(String version){
		String pattern = "\\$\\{(.+)\\}";
		Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(version);	

		String result = version;		
	    if(m.find()){
			String variable = m.group(1);			
			listener.getLogger().println(variable);			
			listener.getLogger().println("->");
			
			String value = envVars.get(variable);			
			listener.getLogger().println(value);
			result = m.replaceFirst(value);
	    }
		else
		{
			listener.getLogger().println("No variable is replaced");
		}
		
		return result;
	}
	
	private String removeSyntax(){
		String result = "";
		String pattern = "[^${]+.*[^}]";		
		Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(this.version);	    
	    if(m.find()){		
		    result = this.version.substring( m.start(), m.end());
	    }
		return result;
	}
}
