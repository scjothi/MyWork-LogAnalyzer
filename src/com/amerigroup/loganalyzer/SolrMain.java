package com.amerigroup.loganalyzer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.util.Base64;

public class SolrMain
{

	public static boolean endsWith(String line, String delim)
	{
		int lineLen = line.length();
		int delimLen = delim.length();
		if (lineLen - delimLen >= 0)
		{
			return line.substring(lineLen - delimLen).equals(delim);
		}

		return false;

	}

	//-Denv=http://localhost:7070 -Durl=/solr-example/collection1/update/csv -Dsrc=C:/solr/example/solr/collection1/  "2013-06-15", "2013-06-16"
	public static void index(String args[])
	{
		String[] dates = args[0].split(",");

		HashMap<String, String> params = new HashMap<String, String>();
		//stream.contentType=text/plain
		params.put("stream.contentType", "text/plain");
		//charset=utf-8
		params.put("charset", "utf-8");
		//commit=true
		//params.put("commit", "true");
		//committed no later than 10 seconds later 
		//params.put("commitWithin", "60000");
		//trim=true
		params.put("separator", "|");

		params.put("overwrite", "false");

		Properties p = System.getProperties();
		if (p == null || p.size() == 0)
		{
			p.setProperty("env", "http://localhost:7070"); //PROD, DEV
			p.setProperty("url", "/solr-example/collection1/update/csv"); //http://localhost:7070/solr-example/collection1/update/csv
			p.setProperty("src", "C:/solr/example/solr/collection1/"); //C:/solr/example/solr/collection1/
		}

		String requestURL = p.getProperty("env") + p.getProperty("url");
		//C:/solr/example/solr/collection1/
		String file = p.getProperty("src");

		ArrayList<String> paths = new ArrayList<String>();
		for (String date : dates)
		{

			String path = file + "server.log";
			if (date.trim().length() != 0)
			{
				path = file + "server.log." + date;
			}

			File f = new File(path);
			if (f.canRead())
			{
				paths.add(f.getAbsolutePath());
			}
			else
			{
				System.out.println("Can't read path: " + path);
			}

		}

		for (String path : paths)
		{
			params.put("stream.file", path);

			try
			{
				System.out.println("Indexing File..." + path);
				StringBuffer sb = sendPostRequest(requestURL, params);
				System.out.println(sb.toString());
				System.out.println("Indexing Successfull..." + path);
			}
			catch (IOException ie)
			{
				System.out.println(ie.getMessage());
				System.out.println("Indexing failed..." + path);
				System.setProperty("ERRORLEVEL", "1");
			}
			File f = new File(path);
			f.renameTo(new File(path + ".indexed"));
		}

		//C:/solr/example/solr/collection1/server.log.2013-07-03
		//file = file + "server.log." + date;
		//stream.file=C:/solr/example/solr/collection1/server.log.2013-07-03

	}

	public static List<LogRecord> insertUTCDate(String infile, String outfile) throws IOException, LogParseException
	{
		DataInputStream in = null;
		List<LogRecord> logList = new ArrayList<LogRecord>();
		PrintWriter pw = null;
		//PrintWriter pw = null;
		try
		{
			// Open the file that is the first 
			// command line parameter
			//pw = new PrintWriter(outfile);
			// Get the object of DataInputStream
			in = new DataInputStream(new FileInputStream(infile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine;
			//Read File Line By Line

			StringBuffer aLogRec = new StringBuffer();
			String serverAndFile[] = resolveServerAndFile(infile);

			outfile = outfile + "/" + serverAndFile[0] + "/" + serverAndFile[1];
			System.out.println("outfile:" + outfile);

			File f = new File(outfile);
			if (!f.exists())
			{
				f.createNewFile();
			}

			pw = new PrintWriter(f, "UTF-8"); //("C:/solr/example/solr/collection1/", "UTF-8");

			int lineCt = 1;
			while ((strLine = br.readLine()) != null)
			{
				try
				{
					if (endsWith(strLine, "||"))
					{
						aLogRec.append(strLine);
						//logList.add(LogRecord.createLogRec(aLogRec.toString()));
						LogRecord r = LogRecord.createLogRec(aLogRec.toString());
						if (lineCt == 1)
						{
							pw.print(r.writeHeader());
						}
						pw.print(r.writeRecord());
						r = null;
						aLogRec.delete(0, aLogRec.length());
						lineCt++;

						if (lineCt % 1000 == 0)
						{
							;
						}
						{
							pw.flush();
						}
					}
					else
					{
						aLogRec.append(strLine);
					}
				}
				catch (Exception e)
				{
					System.out.println(strLine);
					System.out.println(e.getMessage());
				}
			}

		}
		finally
		{
			//Close the input stream
			in.close();
			pw.close();
		}

		return logList;
	}

	public static void main(String args[])
	{

		try
		{
			//-Dopt=index -Denv=http://localhost:7070 -Durl=/solr-example/collection1/update/csv -Dsrc=C:/solr/example/solr/collection1/  "2013-06-05"
			System.out.println("Mode" + System.getProperty("opt"));
			long t = System.currentTimeMillis();

			if (System.getProperty("opt").equalsIgnoreCase("index"))
			{
				index(args);
			}
			//-Dopt=parse -Denv=\\\\VAQESB01 -Dsrc=\\log-A\\ -Dtar=C:/solr/example/solr/collection1/  "2013-06-05"
			else if (System.getProperty("opt").equalsIgnoreCase("parse"))
			{
				parse(args);
			}
			else
			{
				System.out.println("ERROR: Mode unknown...");
			}

			t = (System.currentTimeMillis() - t) / (1000 * 60);
			System.out.println("Time taken(mins) : " + t);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(1);
		}

	}

	public static void parse(String args[])
	{

		/*ArrayList<LogRecord> lst = new ArrayList<LogRecord>();
		lst.add(LogRecord
				.createLogRec("|| vaqesb01 | QA-A | DelegatedAdminWS-v309 | 2013-06-03 13:55:49,284 | INFO  | http--10.100.112.162-8080-16 | com.amerigroup.namevalue.NameValue.getListNameValuePairs(NameValue.java:248) | >getListNameValuePairs() ||"));
		lst.add(LogRecord
				.createLogRec("|| vaqesb01 | QA-A | DelegatedAdminWS-v309 | 2013-06-03 13:55:49,284 | INFO  | http--10.100.112.162-8080-16 | com.amerigroup.namevalue.NameValue.getListNameValuePairs(NameValue.java:248) | >getListNameValuePairs() ||"));
		lst.add(LogRecord
				.createLogRec("|| vaqesb01 | QA-A | PegaWS-v204 | 2013-06-03 14:54:11,055 | ERROR | http--10.100.112.162-8080-1 | com.amerigroup.umiload.UmiLoader.writeUmiKeywords(UmiLoader.java:130) | Rolling back due to exception: com.amerigroup.exception.DAONeedRollbackException: Unable to perform create - rollback needed - SQL: INSERT INTO AGP.UMI_AUTHKEYWORD_UMUM_STAGING ( UMUM_UPDATE_CD, RECTYPE, UMUM_REF_ID, MEME_SFX, MEME_REL, MEME_ID_NAME, GRGR_ID, SBSB_ID, SGSG_ID, CMCM_ID, UMUM_CREATE_USID, IDCD_ID, UMUM_USID_PRI, UMUM_USID_SEC, UMUM_ACD_IND, UMUM_ACD_DTM, UMUM_ACD_STATE, UMUM_OTHER_BN_IND, UMUM_MCTR_RISK, UMUM_MCTR_QUAL, UMUM_MCTR_MECM, UMUM_FINAL_CARE_DT, UMUM_DECEASED_DTM, UMUM_REF_ID_LINK, UMUM_MCTR_LINK, PDDS_MCTR_BCAT, USUS_ID, UMAC_ACTIV_DT, UMAC_MCTR_REAS, UMAC_MCTR_CPLX, UMAC_USID_ROUTE, I_AUTH_ID, I_SOURCE, I_SOURCE_AUTH_ID, I_SBSB_CK, I_FIELD1, I_FIELD2, I_FIELD3) 	VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n\t"

						+ "at com.amerigroup.umiload.dao.UmiAuthKeywordDaoImpl.writeUmum(UmiAuthKeywordDaoImpl.java:2694)\n\t"
						+ "at com.amerigroup.umiload.dao.UmiAuthKeywordDao.writeUmum(UmiAuthKeywordDao.java:364)\n ||\n"));*/

		String[] dates = args[0].split(",");

		/*new String[] { "2013-06-15", "2013-06-16", "2013-06-17", "2013-06-18", "2013-06-19",
		"2013-06-20", "2013-06-21", "2013-06-22", "2013-06-23", "2013-06-24", "2013-06-25", "2013-06-26",
		"2013-06-27", "2013-06-28", "2013-06-29", "2013-06-30" };AGP*/

		Properties p = System.getProperties();
		if (p == null || p.size() == 0)
		{
			p.setProperty("env", "VAQESB01"); //PROD, DEV
			p.setProperty("src", "\\log-A\\,\\log-B\\"); // I:/ 
			p.setProperty("tar", "C:/solr/example/solr/collection1/"); //C:/solr/example/solr/collection1/
		}

		System.out.println(p.getProperty("env"));
		System.out.println(p.getProperty("src"));
		System.out.println(p.getProperty("tar"));
		System.out.println(args);

		String[] envs = p.getProperty("env").split(",");
		String[] sources = p.getProperty("src").split(",");

		ArrayList<String> paths = new ArrayList<String>();
		for (String env : envs)
		{
			for (String src : sources)
			{

				for (String date : dates)
				{
					String path = "\\\\" + env + src + "server.log";
					if (date.trim().length() != 0)
					{
						path = "\\\\" + env + src + "server.log." + date;
					}

					File f = new File(path);
					//if (f.canRead())
					//{
					paths.add(f.getAbsolutePath());
					//}
					//else
					//{
					//	System.out.println("Can't read path: " + path);
					//}
				}
			}
		}

		//\\VA1ESB02\log\server.log.2013-08-08

		for (String path : paths)
		{

			try
			{
				System.out.println("Parsing started..." + path);

				List<LogRecord> lst = insertUTCDate(path, p.getProperty("tar")); // + "server.log." + date);

				System.out.println("Parsing Successful..." + path);
			}
			catch (Exception ie)
			{
				System.out.println("Parsing  Failed.." + path);
				System.out.println("Parsing  Failed.." + p.getProperty("tar"));
				System.exit(1);
			}
		}

		//writeRec(lst);
	}

	//-Denv=\\\\VAQESB01 -Dsrc=\\log-A\\ -Dtar=C:/solr/example/solr/collection1/  "2013-06-15", "2013-06-16"
	public static String[] resolveServerAndFile(String _path)
	{
		File f = new File(_path);
		String serverAndFile[] = new String[2];

		if (f.canRead())
		{
			String path = f.getAbsolutePath();
			path = path.substring(path.indexOf("\\") + 2);
			serverAndFile[0] = path.substring(0, path.indexOf("\\"));
			serverAndFile[1] = path.substring(path.lastIndexOf("\\") + 1);
		}
		else
		{
			System.out.println("Can't resolve server: " + _path);
		}

		return serverAndFile;

	}

	public static StringBuffer sendPostRequest(String requestURL, Map<String, String> params) throws IOException
	{
		System.out.println("requestURL:" + requestURL);
		URL url = new URL(requestURL);
		StringBuffer response = new StringBuffer();
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		OutputStreamWriter writer = null;
		BufferedReader reader = null;

		try
		{
			httpConn.setUseCaches(false);
			httpConn.setDoInput(true); // true indicates the server returns response
			httpConn.setRequestMethod("GET");

			if (url.getUserInfo() != null)
			{
				System.out.println("UI:" + url.getUserInfo());
				String basicAuth = "Basic " + new String(Base64.encode(url.getUserInfo().getBytes()));
				httpConn.setRequestProperty("Authorization", basicAuth);
			}

			StringBuffer requestParams = new StringBuffer();

			if (params != null && !params.isEmpty())
			{

				httpConn.setDoOutput(true); // true indicates POST request

				// creates the params string, encode them using URLEncoder
				Iterator<String> paramIterator = params.keySet().iterator();
				while (paramIterator.hasNext())
				{
					String key = paramIterator.next();
					String value = params.get(key);
					requestParams.append(key); //URLEncoder.encode(key, "UTF-8"));
					requestParams.append('=').append(value); //URLEncoder.encode(value, "UTF-8"));
					requestParams.append('&');
				}

				System.out.println("params:" + requestParams.toString());

				// sends POST data
				writer = new OutputStreamWriter(httpConn.getOutputStream());
				writer.write(requestParams.toString());
				writer.flush();
			}

			InputStream inputStream = httpConn.getInputStream();

			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line = "";
			while (true)
			{
				line = reader.readLine();
				if (line == null)
				{
					break;
				}
				response.append(line.trim());
			}

		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
			if (writer != null)
			{
				writer.close();
			}
		}

		return response;
	}

	public static void writeRec(List<LogRecord> recs) throws IOException
	{
		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter("C:/solr/example/solr/collection1/server.log.2013-07-01", "UTF-8");
			int lineCt = 1;

			for (LogRecord rec : recs)
			{
				// Print the content on the console
				if (lineCt == 1)
				{
					pw.println(rec.writeHeader());
				}
				else
				{
					pw.println(rec.writeRecord());
				}
				pw.flush();
				lineCt++;
			}
		}
		finally
		{//Catch exception if any
			pw.close();
		}
	}

}
