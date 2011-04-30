package javatools.webapi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.parsers.PlingStemmer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


import cc.factorie.protobuf.DocumentProtos.Entity;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

public class FBSearchEngine {

	static String tmpDir = "/projects/pardosa/data14/raphaelh/tmp/seb";

	static String input1 = "/projects/pardosa/data04/xiaoling/riedel/kb_manual/trainEntities.pb";
	static String input2 = "/projects/pardosa/data14/raphaelh/riedel0506/trainHeldOutAll.pb";
	static String input3 = "/projects/pardosa/data14/raphaelh/riedel0506/testHeldOutAll.pb";
	static String input4 = "/projects/pardosa/data16/raphaelh/tmp/fb/names.red.unique";

	static String output1 = tmpDir + "/guids";

	public static List<String> query2(String q, int k) {
		// "http://api.freebase.com/api/service/search?query=madonna"
		try {
			D.p(q);
			Thread.sleep(2000);
			String queries[] = null;
			String q2 = PlingStemmer.stem(q);
			if(!q2.equals(q)){
				queries = new String[]{q,q2};
			}else{
				queries = new String[]{q};
			}
			List<String> res = new ArrayList<String>();
			for(String q0: queries){
				URL yahoo = new URL("http://api.freebase.com/api/service/search?query=" + q0.replaceAll("\\s", "%20"));
				URLConnection yc = yahoo.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
				String inputLine;
				StringBuilder sb = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
				in.close();

				JSONObject o = new JSONObject(sb.toString());
				System.out.println(o.toString());

				JSONArray a = o.getJSONArray("result");
				
				for (int i = 0; i < a.length() && i < k; i++) {
					JSONObject ar = a.getJSONObject(i);
					if(ar == null)continue;
					String urlstuff = ar.get("id").toString();
					//String mid = urlstuff.replace("{\"id\":\"", "").replace("\"}", "");
					//if(mid.startsWith("/m/"))res.add(mid);
					res.add(urlstuff);
				}
			}
			return res;
			// if (a.length() > 0) {
			// JSONObject ar = a.getJSONObject(0);
			// String midstuff = ar.get("article").toString();
			// String mid = midstuff.replace("{\"id\":\"", "").replace("\"}",
			// "");
			// System.out.println(mid);
			// }
		} catch (Exception e) {
			return null;
		}

	}

	public static void getType(String enid) throws Exception{
		{
			//"http://api.freebase.com/api/trans/notable_types_2?id=/en/dinah_washington"
			URL yahoo = new URL("http://api.freebase.com/api/trans/notable_types_2?id=" + enid);
			URLConnection yc = yahoo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			StringBuilder sb = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}
			in.close();
			JSONObject o = new JSONObject(sb.toString());
			//System.out.println(o.toString());
			JSONArray jsonarray = (JSONArray)o.getJSONObject(enid).getJSONObject("result").get("notable_for");
			//D.p(subo.toString());
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject ar = jsonarray.getJSONObject(i);
				if(ar == null)continue;
				String urlstuff = ar.get("types").toString();
				//String mid = urlstuff.replace("{\"id\":\"", "").replace("\"}", "");
				//if(mid.startsWith("/m/"))res.add(mid);
				D.p(urlstuff);
			}
			
		}
	}
	//
	// public static void query(String q) throws Exception {
	// HttpClient httpclient = new DefaultHttpClient();
	// String[] t = null;
	// int count = 0;
	// String url = "https://api.freebase.com/api/service/mqlread?query=";
	// url += URLEncoder.encode(q, "utf-8");
	//
	// HttpGet httpget = new HttpGet(url);
	//
	// // Create a response handler
	// ResponseHandler<String> responseHandler = new BasicResponseHandler();
	//
	// String responseBody = httpclient.execute(httpget, responseHandler);
	//
	// JSONObject o = new JSONObject(responseBody);
	// System.out.println(o.toString());
	//
	// JSONArray a = o.getJSONArray("result");
	// if (a.length() > 0) {
	// JSONObject ar = a.getJSONObject(0);
	// String mid = ar.get("mid").toString();
	// System.out.println(mid);
	// System.out.println("  " + mid);
	// }
	// httpclient.getConnectionManager().shutdown();
	// }

	public static void main(String[] args) throws Exception {
//		List<String >x = query2("Honey of the Nile",10);
//		System.out.println(x);
		getType("/en/dinah_washington");
	}

	private static void blah(HashMap<String, String> guid2mid, HashMap<String, List<String>> m, String input,
			DelimitedWriter w) throws Exception {

		{
			InputStream is = new FileInputStream(input);
			Relation r = null;
			while ((r = Relation.parseDelimitedFrom(is)) != null) {
				RelationMentionRef rmf = r.getMention(0);
				String mid1 = guid2mid.get(r.getSourceGuid());
				if (mid1 != null) {
					List<String> l1 = m.get(mid1);
					if (l1 != null)
						for (int i = 0; i < l1.size(); i++) {
							if (rmf.getSentence().indexOf(l1.get(i)) >= 0) {
								System.out.println(r.getSourceGuid() + "\t" + l1.get(i));
								w.write(r.getSourceGuid(), l1.get(i));
							}
						}
				}
				String mid2 = guid2mid.get(r.getDestGuid());
				if (mid2 != null) {
					List<String> l1 = m.get(mid2);
					if (l1 != null)
						for (int i = 0; i < l1.size(); i++) {
							if (rmf.getSentence().indexOf(l1.get(i)) >= 0) {
								System.out.println(r.getDestGuid() + "\t" + l1.get(i));
								w.write(r.getDestGuid(), l1.get(i));
							}
						}
				}
			}
			is.close();
		}

	}

	private static Map<String, String> argumentTypes(String input) throws Exception {
		Map<String, String> af = new HashMap<String, String>();
		Entity e = null;
		{
			InputStream is = new FileInputStream(input);
			while ((e = Entity.parseDelimitedFrom(is)) != null)
				af.put(e.getGuid(), e.getName());
		}
		return af;
	}

}
