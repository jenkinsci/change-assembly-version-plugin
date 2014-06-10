package org.jenkinsci.plugins.changeassemblyversion;

import hudson.EnvVars;
import hudson.model.BuildListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssemblyVersion {
	
	
	private String version;
	private EnvVars envVars;	
	
	/**
	 * The instance of this class gonna return in the property version the value to be used on ChangeTools.
	 * @param version
	 * @param envVars
	 */
	public AssemblyVersion(String version, EnvVars envVars){		
		this.envVars = envVars;
		this.version = version;		
		if(version.contains("${")){					
			this.version = removeSyntax();			
			this.version = getEnvVersion();			
		}
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	private String getEnvVersion(){
		return envVars.get(this.version);
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
