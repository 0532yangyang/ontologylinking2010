package multir.eval.congle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import multir.eval.congle.PrepareForSentenceLevelAnnotationsAddXiaoResult.EntityPairMention;
import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;

public class GetOverallPrecisionRecall {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String input0 = Main.dir+"/overallcompare1";
		String output_prcurve = Main.dir+"/rphsencurve";
		String output_prcurve_umass = Main.dir+"/umasssencurve";
		DelimitedReader dr = new DelimitedReader(input0);
		String[] line;
		List<EntityPairMention> epmlist = new ArrayList<EntityPairMention>();
		while ((line = dr.read()) != null) {
			// (String guid1, String guid2, String name1,
			// String name2, int mention, String sentence)
			if(line.length<5)break;
			EntityPairMention epm = new EntityPairMention(line[0], line[1],
					line[3], line[4], Integer.parseInt(line[2]), line[5]);
			epm.ourPredictions = line[7];
			epm.ourscore = Double.parseDouble(line[8]);
			epm.umassPredictions = line[9];
			epm.theirscore = Double.parseDouble(line[10]);
			epm.truth = line[11];
			epmlist.add(epm);
		}
		dr.close();

		Collections.sort(epmlist, new Comparator<EntityPairMention>() {

			@Override
			public int compare(EntityPairMention o1, EntityPairMention o2) {
				// TODO Auto-generated method stub
				return o2.ourscore - o1.ourscore > 0 ? 1 : -1;
			}

		});

		// our precision-recall curve
		/** get Precision Recall curve */
		int[] pr = new int[5];
		final int recallAll_index = 0;
		int umasscorrect_index = 1;
		int raphaelcorrect_index = 2;
		int raphaelpred_index = 3;
		int umasspred_index = 4;

		for (EntityPairMention epm : epmlist) {
			if (!epm.truth.equals("NA")) {
				pr[recallAll_index]++;
			}
		}
		{
			DelimitedWriter dwpr = new DelimitedWriter(output_prcurve);
			int count = 0;
			for (EntityPairMention epm : epmlist) {
				count++;
				if (!epm.truth.equals("NA")) {
					if (epm.truth.equals(epm.ourPredictions))
						pr[raphaelcorrect_index]++;
				}
				if (!epm.ourPredictions.equals("NA")) {
					pr[raphaelpred_index]++;
				}

				if (pr[raphaelpred_index] > 0) {
					dwpr.write(pr[raphaelcorrect_index] * 1.0 / pr[raphaelpred_index],
							pr[raphaelcorrect_index] * 1.0 / pr[recallAll_index]);
				}
			}
			dwpr.close();
		}

		
		Collections.sort(epmlist, new Comparator<EntityPairMention>() {

			@Override
			public int compare(EntityPairMention o1, EntityPairMention o2) {
				// TODO Auto-generated method stub
				return o2.theirscore - o1.theirscore > 0 ? 1 : -1;
			}

		});
		
		{
			DelimitedWriter dwpr = new DelimitedWriter(output_prcurve_umass);
			int count = 0;
			for (EntityPairMention epm : epmlist) {
				count++;
				if (!epm.truth.equals("NA")) {
					if (epm.truth.equals(epm.umassPredictions))
						pr[umasscorrect_index]++;
				}
				if (!epm.umassPredictions.equals("NA")) {
					pr[umasspred_index]++;
				}

				if (pr[umasspred_index] > 0) {
					dwpr.write(pr[umasscorrect_index] * 1.0 / pr[umasspred_index],
							pr[umasscorrect_index] * 1.0 / pr[recallAll_index]);
				}
			}
			dwpr.close();
		}
	}
}
