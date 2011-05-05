package parseWikipedia;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

public class MainMulti {
	static String input0 = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_rawtext";
	
	//static String output = "/projects/pardosa/s5/clzhang/tmp/wp/enwiki_rawtext";
	
	static void splitInput1(int parts)throws Exception{
		DelimitedWriter dw[] = new DelimitedWriter[parts];
		for(int i=1;i<=parts;i++){
			dw[i] = new DelimitedWriter(input0+".part"+i);
		}
		DelimitedReader dr = new DelimitedReader(input0);
		String []line;
		while((line = dr.read())!=null){
			int articleId = Integer.parseInt(line[0]);
			int where = articleId%parts+1;
			dw[where].write(line);
		}
		dr.close();
		for(int i=1;i<=parts;i++){
			dw[i].close();
		}
		
	}
	public static void main(String []args) throws Exception{
		splitInput1(10);
	}
}
