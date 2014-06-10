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
	
	
	public ChangeTools(){
		
	}
	
	/**
	 * Call this method passing the filepath list from the build machine, version to set and listener to log.
	 * @param fpList
	 * @param version
	 * @param listener
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void ReplaceAllProperties(List<FilePath> fpList, String version, BuildListener listener) throws IOException, InterruptedException{		
		listener.getLogger().println("Listing files");				
		List<FilePath> fileList = getFiles(fpList, listener);		
		if(fileList.size() > 0){									
			String pattern = "Version[(]\"[\\d\\.]+\"[)]";
			for(FilePath file : fileList){			
				String content = file.readToString();				
				content = content.replaceAll(pattern, String.format("Version(\"%s\")", version));
				listener.getLogger().println(String.format("Updating file : %s", file.getRemote()));
				file.write(content, null);							
			}
		}									
	}
	
	private static List<FilePath> getFiles(List<FilePath> files, BuildListener listener) throws IOException, InterruptedException{
		List<FilePath> result = new ArrayList<FilePath>();		
		for (FilePath file : files) {						
			if(!file.getRemote().contains("svn")){					
		        if (file.isDirectory()) {
		        	result.addAll(getFiles(file.list(), listener));
		        } else{				        						
		        	if(file.getRemote().contains("AssemblyInfo.cs")){		        		
		        		result.add(file);		        		
		        	}
		        }
			}
	    }		
		return result;
	}
		
}
