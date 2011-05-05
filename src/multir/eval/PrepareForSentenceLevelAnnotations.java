package multir.eval;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.util.delimited.DelimitedReader;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

/*
 * Prepare Excel file which contains sample of 5000 sentences.
 * 
 *  space for label      sentence (arguments in brackets)    features (top-k)
 *  
 */

/*
 * VBA CODE FOR HIGHLIGHTING IN EXCEL

Sub Test()

   For i = 1 To 2000
      s = ActiveSheet.Cells(i, 2).Value
      j = InStr(1, s, "[")
      Do While j > 0
        e = InStr(j + 1, s, "]")
        ActiveSheet.Cells(i, 2).Characters(j + 1, e - j - 1).Font.Color = RGB(0, 0, 255)
        ActiveSheet.Cells(i, 2).Characters(e + 1, 1).Font.Color = RGB(255, 0, 0)
        j = InStr(e + 1, s, "[")
      Loop
   Next i

End Sub


 * 
 * 
 */


public class PrepareForSentenceLevelAnnotations {

	static String input1 = "/projects/pardosa/s5/clzhang/liminyao/heldout_relations/test-Multiple.pb";

	//static String input1 = "/projects/pardosa/data14/raphaelh/riedel0506/test-all-newlabel-Multiple.pb";
	static String input4 = "/projects/pardosa/data16/raphaelh/tmp/seb/guid2name.all";
	
	static String output = "/projects/pardosa/data16/raphaelh/camera/all";
	
	public static void main(String[] args) throws IOException {
		// read guid2name
		HashMap<String,String> guid2name = new HashMap<String,String>();
		{
			DelimitedReader r = new DelimitedReader(input4);
			String[] t = null;
			while ((t = r.read())!= null)
				guid2name.put(t[0], t[1]);
			r.close();
		}

		Map<RelationMentionRef,Relation> map = new HashMap<RelationMentionRef,Relation>();
		List<RelationMentionRef> all = new ArrayList<RelationMentionRef>();
        InputStream is = new FileInputStream(input1);
        Relation r = null;
        
        while ((r = Relation.parseDelimitedFrom(is))!=null) {
        	for (int i=0; i < r.getMentionCount(); i++) {
        		RelationMentionRef rmf = r.getMention(i);
        		all.add(rmf);
        		map.put(rmf, r);
        	}
        }
		is.close();
		
		// sample
		
		List<Integer> ids = new ArrayList<Integer>();
		for (int i=0; i < all.size(); i++) ids.add(i);
		
		Collections.shuffle(ids);
		
		// write sample
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter
				(new FileOutputStream(output), "utf-8"));
		for (int i=0; i < all.size() /*Math.min(10000, all.size())*/; i++) {
			int id = ids.get(i);
			RelationMentionRef rmf = all.get(ids.get(i));
			r = map.get(rmf);
        	String name1 = guid2name.get(r.getSourceGuid());
        	String name2 = guid2name.get(r.getDestGuid());
        	if (name1 == null) name1 = "";
        	if (name2 == null) name2 = "";

    		// our prediction
    		String snt = rmf.getSentence();
    		
    		StringBuilder sb = new StringBuilder();
    		for (int j=0; j < Math.min(5, rmf.getFeatureList().size()); j++)
    			sb.append(rmf.getFeatureList().get(j) + ",");

			String sentence = snt;					
			sentence = sentence.replaceAll(name1, "[" + name1 + "]1");
			sentence = sentence.replaceAll(name2, "[" + name2 + "]2");
    		
			//w.write("\t" + name1 + ", " + name2 + "\t" +  annSnt + "\t" + sb.toString() + "\t" + snt + "\n");
			
			w.write(id + "\t" + sentence + "\t" + "" + "\t" + name1 + "\t" + name2 + "\t" + sb.toString() + "\t" + snt + "\n" );

		}
		w.close();
		
	}	
}

