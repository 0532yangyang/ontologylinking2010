package multir.exp;

import java.io.IOException;

import multir.preprocess.SplitIntoChunks;

public class Step0 {

	static String dir = "/projects/pardosa/data14/raphaelh/t/raw";

	static String input1 = dir + "/sentences";
	
	public static void main(String[] args) throws IOException {
		
		// split
		SplitIntoChunks.split(input1, input1 + ".chunk", 100);
		
		System.out.println("next call (for each X = 0..99):\n" +
				"java -cp .:../lib/opennlp-tools-1.5.0.jar:../lib/maxent-3.0.0.jar:../lib/log4j-1.2.15.jar:../lib/liblinear-1.33-with-deps.jar:../lib/stanford-ner.jar " +
				"multir.preprocess.PreprocessChunk " + input1 + ".chunkX");
		
		System.out.println("then call:\n" +
				"java multir.preprocess.MergeChunks " + input1);
		
	}
	
}
