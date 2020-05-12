package com.amerigroup.loganalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

@Deprecated
public class SOLRIndexer
{

	public static void doParsing(String infile, String outfile) throws IOException, LogParseException
	{
		DataInputStream in = null;
		PrintWriter pw = null;
		long targetNextLine = 1L;
		long tsStartTime = System.currentTimeMillis();
		long tsStage1 = 0L;
		long lineCt = 1L;
		LogRecord lastEvent = null;
		String strLine = null;
		try
		{
			// Get the object of DataInputStream
			in = new DataInputStream(new FileInputStream(infile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			//Read File Line By Line

			StringBuffer aLogRec = new StringBuffer();
			System.out.println("outfile:" + outfile);

			File f = new File(outfile);
			if (!f.exists())
			{
				f.createNewFile();
				targetNextLine = 1L;
			}
			else
			{
				LogRecord l = LogRecord.parse(SolrUtil.tail(new File(outfile)));

				System.out.println("Pre First Event:" + l.toString());

				targetNextLine = Long.parseLong(l.line) + 1;
			}

			//pw = new PrintWriter(f, "UTF-8"); //("C:/solr/example/solr/collection1/", "UTF-8");
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));

			tsStage1 = System.currentTimeMillis();

			while ((strLine = br.readLine()) != null)
			{
				//try
				//{
				if (SolrMain.endsWith(strLine, "||"))
				{
					if (lineCt >= targetNextLine)
					{
						aLogRec.append(strLine);
						LogRecord r = LogRecord.createLogRec(aLogRec.toString());
						r.line = lineCt + "";
						if (lineCt == 1)
						{
							pw.print(r.writeHeader());
						}
						pw.print(r.writeRecord());
						lastEvent = r;
						r = null;
						pw.flush();

					}

					aLogRec.delete(0, aLogRec.length());
					lineCt++;
				}
				else
				{
					aLogRec.append(strLine);
				}

				//				}
				//				catch (Throwable e)
				//				{
				//					System.out.println(strLine);
				//
				//					System.out.println(e.getMessage());
				//				}
			}//end while

		}
		finally
		{
			//Close the input stream
			in.close();
			pw.close();
			long tsStage2 = System.currentTimeMillis();
			System.out.println("Stage1:" + (tsStage1 - tsStartTime));
			System.out.println("Stage2:" + (tsStage2 - tsStartTime));
			System.out.println("Total Lines Read:" + lineCt);
			System.out.println("Total Lines Written:" + LogRecord.getSeq());
			System.out.println("Last Event:" + lastEvent.toString());

		}

	}

	public static void main(String args[])
	{

		String targetFile = "C:\\solr\\example\\solr\\collection1\\output.log";
		String sourceFile = "\\\\va1esb02\\log\\server.log";
		//read last line

		try
		{

			//doParsing(sourceFile, targetFile, 26L);

			doParsing(sourceFile, targetFile);

		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

	}

	public void doIndexing(String infile, String outfile) throws IOException, SolrServerException,
			InterruptedException, LogParseException
	{

		DataInputStream in = null;
		List<LogRecord> logList = new ArrayList<LogRecord>();
		PrintWriter pw = null;
		long targetNextLine = 1L;
		long tsStartTime = System.currentTimeMillis();
		long tsStage1 = 0L;
		long lineCt = 1L;
		LogRecord lastEvent = null;
		CoreContainer coreContainer = null;
		EmbeddedSolrServer server = null;
		//PrintWriter pw = null;
		try
		{

			// Get the object of DataInputStream
			in = new DataInputStream(new FileInputStream(infile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine;
			//Read File Line By Line

			StringBuffer aLogRec = new StringBuffer();

			System.out.println("outfile:" + outfile);

			File f = new File(outfile);
			if (!f.exists())
			{
				f.createNewFile();
				targetNextLine = 1L;
			}
			else
			{
				LogRecord l = LogRecord.parse(SolrUtil.tail(new File(outfile)));

				System.out.println("Pre First Event:" + l.toString());

				targetNextLine = Long.parseLong(l.line) + 1;
			}

			//pw = new PrintWriter(f, "UTF-8");			
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));

			tsStage1 = System.currentTimeMillis();

			//initialize the indexer
			// Note that the following property could be set through JVM level arguments too
			System.setProperty("solr.solr.home", "C:\\solr\\example\\solr");
			CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			coreContainer = initializer.initialize();
			server = new EmbeddedSolrServer(coreContainer, "");

			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

			while ((strLine = br.readLine()) != null)
			{
				//				try
				//				{
				if (lineCt >= targetNextLine)
				{

					LogRecord r = LogRecord.parse(strLine);
					if (lineCt == 1)
					{
						pw.print(r.writeHeader());
					}

					indexSingleRec(docs, r);

					pw.print(r.writeRecord());
					r = null;
					//aLogRec.delete(0, aLogRec.length());

					pw.flush();

				}
				lineCt++;

				//				}
				//				catch (Throwable e)
				//				{
				//					System.out.println(strLine);
				//
				//					System.out.println(e.getMessage());
				//				}
			}// end while

			//commit the documents to server
			server.add(docs);
			server.commit(false, false, true);

			//https://gist.github.com/delip/6214406

			Thread.sleep(10000);

		}
		finally
		{
			//Close the input stream
			in.close();
			pw.close();

			coreContainer.shutdown();
			server.shutdown();

		}

	}

	private String getlastIndexedrowId()
	{
		return "";
	}

	private void indexSingleRec(Collection<SolrInputDocument> docs, LogRecord logRec)
	{

		//||id|LINE|SERVER|INSTANCE|WEBSERVICE|TIMESTAMP|UTCTIMESTAMP|LEVEL|THREAD|CLASS|MESSAGE||

		//http://wiki.apache.org/solr/Solrj

		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", logRec.uuid);
		doc.addField("LINE", logRec.line);
		doc.addField("SERVER", logRec.serverName);
		doc.addField("INSTANCE", logRec.instanceName);
		doc.addField("WEBSERVICE", logRec.ws);
		doc.addField("TRANSACTION", logRec.transId);
		doc.addField("TIMESTAMP", logRec.timestamp);
		doc.addField("UTCTIMESTAMP", logRec.tsutc);
		doc.addField("LEVEL", logRec.level);
		doc.addField("THREAD", logRec.thread);
		doc.addField("CLASS", logRec.className);
		doc.addField("MESSAGE", logRec.message);

		docs.add(doc);

		/* UpdateRequest req = new UpdateRequest();
		    req.add(docs);
		    req.setCommitWithin(10000);
		    req.set
		    req.process(server);
		*/

		//System.out.println("Done.....");

	}

}
