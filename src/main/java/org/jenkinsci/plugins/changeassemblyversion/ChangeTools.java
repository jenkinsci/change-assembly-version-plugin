package org.jenkinsci.plugins.changeassemblyversion;

import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ChangeTools {
	
	
	private final String fileName;
	private final String regexPattern;
	private final String replacementPattern;
	
	public ChangeTools(String fileName,String regexPattern,String replacementPattern){
		if(!fileName.equals("")){
			this.fileName = fileName;	
		} else{
			this.fileName = "AssemblyInfo.cs";
		}
		
		if(!regexPattern.equals("")){
			this.regexPattern = regexPattern;	
		} else{
			this.regexPattern = "Version[(]\"[\\d\\.]+\"[)]";
		}
		
		if(!replacementPattern.equals("")){
			this.replacementPattern = replacementPattern;	
		} else{
			this.replacementPattern = "Version(\"%s\")";
		}		
	}
	
	/**
	 * Call this method passing the filepath list from the build machine, version to set and listener to log.
	 * @param fpList
	 * @param version
	 * @param listener
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void ReplaceAllProperties(List<FilePath> fpList, String version, BuildListener listener) throws IOException, InterruptedException{		
		listener.getLogger().println("Listing files");				
		List<FilePath> fileList = getFiles(fpList, listener);		
		if(fileList.size() > 0){									
			String pattern = regexPattern;
			for(FilePath file : fileList){			
				String content = file.readToString();				
				content = content.replaceAll(pattern, String.format(replacementPattern, version));
				listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
				file.write(content, null);							
			}
		}									
	}
	
	private List<FilePath> getFiles(List<FilePath> files, BuildListener listener) throws IOException, InterruptedException{
		List<FilePath> result = new ArrayList<FilePath>();		
		for (FilePath file : files) {						
			if(!file.getRemote().contains("svn")){					
		        if (file.isDirectory()) {
		        	result.addAll(getFiles(file.list(), listener));
		        } else{				        					
		        	String[] filesName = this.fileName.split(",");
		        	for(String fileName : filesName){
		        		if(file.getRemote().contains(fileName.trim())){		        		
			        		result.add(file);		        		
			        	}
		        	}
		        
		        }
			}
	    }		
		return result;
	}
		
}
