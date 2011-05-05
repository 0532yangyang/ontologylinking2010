package parseWex;

import java.io.IOException;

import javatools.administrative.D;

import stanfordCoreWrapper.MaltiDep;

public class Step3_getDeps {
	public static void main(String[] args) throws IOException {
		int partId = Integer.parseInt(args[0]);
		String file = Main.fout_stanfordtext_template.replace("$DNUM$", Main.datas[partId]) + partId;
		D.p(file);
		MaltiDep.getDep(file, 2);
	}
}
