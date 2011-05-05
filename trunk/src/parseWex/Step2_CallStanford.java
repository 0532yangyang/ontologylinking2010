package parseWex;

import java.io.File;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import stanfordCoreWrapper.CoreNlpPipeline;

public class Step2_CallStanford {
	public static void main(String[] args) throws Exception {

		int partId = Integer.parseInt(args[0]);
		CoreNlpPipeline cnp = new CoreNlpPipeline();

		String output = Main.fout_stanfordtext_template.replace("$DNUM$", Main.datas[partId]) + partId;

		// int keepGoingOn = 0;
		// if ((new File(output)).exists()) {
		// DelimitedReader dr_output = new DelimitedReader(output);
		// String []l;
		// while((l = dr_output.read())!=null){
		// keepGoingOn = Integer.parseInt(l[0]);
		// }
		// dr_output.close();
		// output = output+"_continue";
		// }
		// D.p("Keep going on from "+keepGoingOn);
		DelimitedWriter dw = new DelimitedWriter(output);

		DelimitedReader dr = new DelimitedReader(Main.fout_wexsplit_template.replace("$DNUM$", Main.datas[partId])
				+ partId);
		String[] line;
		int haspased = 0;

		while ((line = dr.read()) != null) {
			if (line.length < 2)
				continue;

			int articleId = Integer.parseInt(line[0]);
			String rawtext = line[1];

			if (rawtext.length() < 2)
				continue;

			haspased++;
			// if(haspased>5)break;

			// if(articleId < keepGoingOn)continue;
			D.p("Parsing article:" + articleId);
			// rawtext.replaceAll("[^\\p{ASCII}]", "");
			List<List<String[]>> parsed = cnp.parse(rawtext);
			for (List<String[]> s : parsed) {
				String towrite[] = new String[5];

				StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder(), sb3 = new StringBuilder();
				for (String[] w : s) {
					// if(w[0].contains(" ") || w[1].contains(" ")||
					// w[2].contains(" ")){
					// System.err.println("blank happens!!!");
					// }
					w[0] = w[0].replaceAll(" ", "_");
					w[1] = w[1].replaceAll(" ", "_");
					w[2] = w[2].replaceAll(" ", "_");
					sb1.append(w[0] + " ");
					sb2.append(w[1] + " ");
					sb3.append(w[2] + " ");
				}
				int id = 0;
				towrite[0] = articleId + "";
				towrite[1] = "sectionidNA";
				towrite[2] = sb1.toString();
				towrite[3] = sb2.toString();
				towrite[4] = sb3.toString();
				dw.write(towrite);
			}
		}
		dr.close();
		dw.close();
	}
}
