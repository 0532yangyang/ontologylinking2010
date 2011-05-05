package parseWikipedia;

import java.io.File;
import java.io.IOException;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;


public class Step2_split {
	
	public static void main(String []args) throws IOException{
		String []splitfiles = new String[Main.datas.length];
		DelimitedWriter dw[] = new DelimitedWriter[Main.datas.length];
		                                           
		for(int i =0;i<Main.datas.length;i++){
			String dir = Main.dir_blikitext_template.replace("$DNUM$", Main.datas[i]);
			if(!(new File(dir)).exists()){
				(new File(dir)).mkdirs();
			}
			splitfiles[i] = dir+"/"+Main.file_blikitext_template+i;
			dw[i] = new DelimitedWriter(splitfiles[i]);
		}
		
		DelimitedReader dr = new DelimitedReader(Main.file_blikitext_all_clean);
		String []line;
		int num = 0;
		while((line = dr.read())!=null){
			num++;
			int articleId = Integer.parseInt(line[0]);
			dw[articleId%Main.datas.length].write(line);
		}
		dr.close();
		for(DelimitedWriter dw0: dw){
			dw0.close();
		}
	}
}
