package multir.util.delimited;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JoinDelimitedFile {

	/**
	 * @param args
	 * @throws IOException
	 */
	static List<String[]> referBuffer = new ArrayList<String[]>();
	static String[] extraRefer;

	public static void join(String mainFile, final int key1, boolean isMainFileSorted, String referFile,
			final int key2, boolean isReferFileSorted, int[] mainFileColumns, int[] referFileColumns,
			String outputfile, String outputFormat, String dir) throws IOException {

		referBuffer.clear();
		extraRefer = null;

		String sortMainFile = mainFile;
		String sortReferFile = referFile;
		{
			// Sort the two files if they are not sorted before
			if (!isMainFileSorted) {
				sortMainFile = mainFile + ".tempsort";
				Sort.sort(mainFile, sortMainFile, dir, new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						return o1[key1].compareTo(o2[key1]);
					}
				});
			}
			if (!isReferFileSorted) {
				sortReferFile = referFile + ".tempsort";
				Sort.sort(referFile, sortReferFile, dir, new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						return o1[key2].compareTo(o2[key2]);
					}
				});
			}
		}
		DelimitedReader dr1 = new DelimitedReader(sortMainFile);
		DelimitedReader dr2 = new DelimitedReader(sortReferFile);
		DelimitedWriter dw = new DelimitedWriter(outputfile);
		extraRefer = dr2.read();
		readNext(dr2,key2);
		String[] line;
		int NumMisMatch = 0;
		int ln = 0;
		while ((line = dr1.read()) != null) {
			//if(ln++ % 1000000==0)System.out.print(ln+"\r");
			String mainkey = line[key1];
			if(mainkey.equals("/m/0_00")){
				System.out.print("");
			}
			while (mainkey.compareTo(referBuffer.get(0)[key2]) > 0 && readNext(dr2, key2));
				
			if (referBuffer.size() == 0) {
				// everything is over
				NumMisMatch++;
				continue;
			} else {
				if (mainkey.equals(referBuffer.get(0)[key2])) {
					writeFile(dw,line,mainFileColumns,referFileColumns);
				} else {
					NumMisMatch++;
					//System.err.println(mainkey);
					continue;
				}
			}
		}
		dr1.close();
		dr2.close();
		dw.close();
		System.err.println(NumMisMatch);

	}

	private static void writeFile(DelimitedWriter dw, String[] mainline, 
			int[] mainFileColumns, int[] referFileColumns) throws IOException {
		if(mainFileColumns== null || mainFileColumns.length==0){
			mainFileColumns = new int[mainline.length];
			for(int i=0;i<mainFileColumns.length;i++)mainFileColumns[i]=i;
		}
		for(String []refer: referBuffer){
			String []toWrite = new String[mainFileColumns.length+referFileColumns.length];
			int k=0;
			for(int i=0;i<mainFileColumns.length;i++){
				toWrite[k++] = mainline[mainFileColumns[i]];
			}
			for(int i=0;i<referFileColumns.length;i++){
				toWrite[k++] = refer[referFileColumns[i]];
			}
			dw.write(toWrite);
		}
	}

	private static boolean readNext(DelimitedReader dr2, int key) throws IOException {
		if (extraRefer == null)
			return false;
		referBuffer.clear();
		referBuffer.add(extraRefer);
		String[] line;
		while ((line = dr2.read()) != null && line[key].equals(referBuffer.get(0)[key])) {
			referBuffer.add(line);
		}
		if (line != null) {
			extraRefer = line;
		} else {
			extraRefer = null;
		}
		return true;
	}

//	public static void join(String mainFile, int foreignKey, String referFile, int primaryKey, int[] mainFileColumns,
//			int[] referFileColumns, String outputFormat, String dir) {
//		join(mainFile, foreignKey, false, referFile, primaryKey, false, mainFileColumns, referFileColumns,
//				outputFormat, dir);
//	}

	public static void main(String[] args)throws Exception {
		// TODO Auto-generated method stub
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String referFile = dir+"/fbname.sbmid";
		String mainFile = dir+"/mid2zclid.sbmid";
		String outputfile = dir+"/aaaa";
		JoinDelimitedFile.join(mainFile, 0, true, 
				referFile, 0, true, 
				new int []{}, new int[]{1}, 
				outputfile, "", dir);
	}

}
