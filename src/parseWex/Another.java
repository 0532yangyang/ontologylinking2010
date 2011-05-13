package parseWex;

import java.io.IOException;

import stanfordCoreWrapper.MaltiDep;

public class Another {
	public static void main(String[] args) throws IOException {
		for (int i = 0; i < Main.datas.length; i++) {
			String file = Main.fout_stanfordtext_template.replace("$DNUM$", Main.datas[i]) + i;
			MaltiDep.removeDep2(file, 2);
		}
	}
}
