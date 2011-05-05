package parseWikipedia;

import java.io.File;
import java.util.Comparator;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.filehandlers.Sort;

public class Step4_merge {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int sentenceId = 0;
		int sectionId = 0;
		try {
			
			DelimitedWriter dw = new DelimitedWriter(Main.output);
			for (int i = 0; i < Main.datas.length; i++) {
				String file = Main.file_stanfordtext_template.replace("$DNUM$", Main.datas[i]) + i;
				D.p(file);
				// D.p(file);
				try {
					DelimitedReader dr = new DelimitedReader(file);
					String[] line;
					int lasttempsectionid = -1;
					while ((line = dr.read()) != null) {
						if(line.length<5)continue;
						sentenceId++;
						int tempsectionId = Integer.parseInt(line[1]);
						if (tempsectionId != lasttempsectionid) {
							sectionId++;
							lasttempsectionid = tempsectionId;
						}
						dw.write(sentenceId,line[0],sectionId,line[2],line[3],line[4]);
					}
					dr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			String tempdir = "/projects/pardosa/s5/clzhang/tmp/wp/";
			Sort.sort(Main.output,Main.output+".sortbyArtid",tempdir, new Comparator<String[]>(){

				@Override
				public int compare(String[] o1, String[] o2) {
					// TODO Auto-generated method stub
					int a1 = Integer.parseInt(o1[1]);
					int a2 = Integer.parseInt(o2[1]);
					return a1-a2;
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
