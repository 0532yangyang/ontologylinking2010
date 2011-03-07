package multir.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileOperations {

	public static void move(String from, String to) throws IOException {
		File fromFile = new File(from);
		File toFile = new File(to);
		
		// if target exists delete
		if (toFile.exists()) toFile.delete();
		
		// try if renaming works (e.g. if on same drive, platform-dependent)
		fromFile.renameTo(toFile);		
		if (toFile.exists()) return;
		
		// must copy file
		System.out.println("couldn't move, must copy");
        FileChannel inChannel = new
        	FileInputStream(from).getChannel();
        FileChannel outChannel = new
        	FileOutputStream(to).getChannel();
	    try {
           // magic number for Windows, 64Mb - 32Kb)
           int maxCount = (64 * 1024 * 1024) - (32 * 1024);
           long size = inChannel.size();
           long position = 0;
           while (position < size) {
              position += 
                inChannel.transferTo(position, maxCount, outChannel);
           }
	    } 
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	    fromFile.delete();
	}
	
	public static void copy(String from, String to) throws IOException {
		File toFile = new File(to);
		
		// if target exists delete
		if (toFile.exists()) toFile.delete();
		
		// must copy file
        FileChannel inChannel = new
        	FileInputStream(from).getChannel();
        FileChannel outChannel = new
        	FileOutputStream(to).getChannel();
	    try {
           // magic number for Windows, 64Mb - 32Kb)
           int maxCount = (64 * 1024 * 1024) - (32 * 1024);
           long size = inChannel.size();
           long position = 0;
           while (position < size) {
              position += 
                inChannel.transferTo(position, maxCount, outChannel);
           }
	    } 
	    catch (IOException e) {
	        throw e;
	    }
	    finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	    }
	}

	public static void rmr(String dir) throws IOException {
		File f = new File(dir);
		if (!f.exists()) return;
			
		if (!f.isDirectory()) {
			f.delete();
			return;
		}
		
		for (File fi : f.listFiles())
			rmr(fi.getAbsolutePath());
		
		f.delete();
	}
	
	public static void remove(String file)  throws IOException {
		File f = new File(file);
		if (!f.exists()) return;
		f.delete();
	}
}
