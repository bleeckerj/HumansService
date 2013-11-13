package com.nearfuturelaboratory.util.file;

import java.io.File;
import java.util.List;

public class FileUtils {


	public static String filenameSafeEncode(String aString) {
		//String path_alias = (String)user.get("path_alias");
		String result = (aString).replaceAll("\\s+|(\\p{Punct}*)|[\u0000-\u001f]","");
		return result;

	}
	
	
	public static void mkDirs(File root, List<String> dirs) {
		for (String s : dirs) {
			File subdir = new File(root, s);
			if(!subdir.exists()) {
				//System.out.println("Subdir "+subdir);
				subdir.mkdir();
			}
			root = subdir;
			//		    mkDirs(subdir, dirs, depth - 1);
		}
	}
	
}
