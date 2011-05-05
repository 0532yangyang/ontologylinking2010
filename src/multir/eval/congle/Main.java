package multir.eval.congle;

import java.io.IOException;

public class Main {
	static String dir = "/projects/pardosa/s5/clzhang/tmp/seb/cameraready";
	
	public static void main(String []args) throws Exception{
		GetPredictions.main(null);
		PrepareForSentenceLevelAnnotationsAddXiaoResult.main(null);
		GetOverallPrecisionRecall.main(null);
	}
}
