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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

public class SOLRRealTimeProcessor
{

	static Logger log = Logger.getLogger("SOLRRealTimeIndexer");

	//multiple sharads in single node
	//http://solr.pl/en/2013/01/07/solr-4-1-solrcloud-multiple-shards-on-the-same-solr-node/

	static
	{

		//SolrUtil.initializeLogger(SolrUtil.Mode.RTINDEX);

		log.severe("severe.......");
		log.warning("warning.......");
		log.info("info.......");
		log.fine("fine.......");
		log.finer("finer.......");
		log.finest("finest......");

	}

	public static void main(String args[])
	{

		String targetFile = "C:\\solr\\example\\solr\\collection1\\output.log";
		String sourceFile = "\\\\va1esb02\\log\\server.log";
		//read last line

		try
		{

			//doParsing(sourceFile, targetFile, 26L);

			//SOLRRealTimeIndexer rtIndexer = new SOLRRealTimeIndexer();

			//rtIndexer.doParsing(sourceFile, targetFile);

			//rtIndexer.doIndexing(targetFile, targetFile + ".indexed");

		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

	}

	SolrServer server = null;

	String inIndexfile, outIndexfile, inParsefile, outParsefile;

	public long maxRecords = 40000L;

	public long lineCounter = 0L;

	//public long tsStage3, linesWritt;

	public SOLRRealTimeProcessor(String inParsefile, String outParsefile)
	{
		this.inParsefile = inParsefile;
		this.outParsefile = outParsefile;
	}

	public SOLRRealTimeProcessor(String inIndexfile, String outIndexfile, SolrUtil.Mode mode, SolrServer server)
	{

		String maxrecords = SolrUtil.getConfig("indexer.maxrecords.percycle");
		maxrecords = (maxrecords == null) ? "40000" : maxrecords.trim();
		log.fine("Using maxrecords:" + maxrecords);
		this.maxRecords = Long.parseLong(maxrecords);

		this.inIndexfile = inIndexfile;
		this.outIndexfile = outIndexfile;
		this.server = server;
	}

	public synchronized void commit() throws IOException, SolrServerException, InterruptedException, LogParseException
	{

		log.info("COMMIT COMMIT COMMIT COMMMIT.........................");
		server.commit();

		log.info("Commit suceeded...");

		//https://gist.github.com/delip/6214406

		//Thread.sleep(10000);

	}

	/**
	 * -Dloganalyzer.indexer.core=<core name, default:devlogs> -Dsolr.solr.home=<solr home dir.
	 * default:C:\\solr\\example\\solr>
	 * 
	 * @param infile
	 * @param outfile
	 * @throws IOException
	 * @throws SolrServerException
	 * @throws InterruptedException
	 * @throws LogParseException
	 */
	public void doIndexing(boolean commit) throws IOException, SolrServerException, InterruptedException,
			LogParseException
	{

		DataInputStream in = null;
		PrintWriter pw = null;
		long targetNextLine = 1L;
		long tsStartTime = System.currentTimeMillis();
		long tsStage1 = 0L, tsStage2 = 0L, tsStage3 = 0L;
		//long  = 0L;
		long fileLineCount = 1L; //, linesWritt = 0L;
		LogRecord lastEvent = null;
		CoreContainer coreContainer = null;
		lineCounter = 0L;
		//server = null;
		//PrintWriter pw = null;
		try
		{

			// Get the object of DataInputStream
			in = new DataInputStream(new FileInputStream(inIndexfile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine;
			//Read File Line By Line

			StringBuffer aLogRec = new StringBuffer();

			log.info("outfile:" + outIndexfile);

			File f = new File(outIndexfile);
			if (!f.exists())
			{
				f.createNewFile();
				targetNextLine = 1L;
			}
			else if (SolrUtil.tail(new File(outIndexfile)).indexOf('|') != -1) //[|]
			{
				LogRecord l = LogRecord.parse(SolrUtil.tail(new File(outIndexfile)));

				log.info("Pre First Event:" + l.toString());

				targetNextLine = Long.parseLong(l.line) + 2;
			}

			//pw = new PrintWriter(f, "UTF-8");			
			pw = new PrintWriter(new BufferedWriter(new FileWriter(f, true)), false);

			tsStage1 = System.currentTimeMillis();

			//initialize the indexer
			// Note that the following property could be set through JVM level arguments too

			//SolrCore core = coreContainer.getCore("devlogs");

			if (server == null)
			{
				String solrHome = System.getProperty("solr.solr.home");
				solrHome = (solrHome == null) ? "C:\\solr\\example\\solr" : solrHome.trim();
				System.setProperty("solr.solr.home", solrHome);
				log.info("Using Solr Home:" + System.getProperty("solr.solr.home"));

				//reading properties
				String core = System.getProperty("loganalyzer.indexer.core"); //==null)?"":
				core = (core == null) ? "devlogs" : core.trim();
				log.fine("Using core:" + core);
				CoreContainer.Initializer initializer = new CoreContainer.Initializer();
				coreContainer = initializer.initialize();
				server = new EmbeddedSolrServer(coreContainer, core);
			}

			server.ping();

			tsStage2 = System.currentTimeMillis();

			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

			while ((strLine = br.readLine()) != null && lineCounter <= maxRecords)
			{
				//				try
				//				{
				if (fileLineCount >= targetNextLine)
				{

					LogRecord r = LogRecord.parse(strLine);
					if (fileLineCount == 1)
					{
						pw.print(r.writeHeader());
					}
					else
					{

						indexSingleRec(docs, r);

						pw.print(r.writeRecord());
						lastEvent = r;

					}
					pw.flush();
					r = null;
					//aLogRec.delete(0, aLogRec.length());

				}
				fileLineCount++;

			}// end while

			if (docs.size() > 0)
			{

				//commit the documents to server within 5 seconds. set this value based on <autocommit> config
				server.add(docs, 5000);

				if (commit)
				{
					server.commit();

					log.fine("Commit succeeded..." + docs.size());

					tsStage3 = System.currentTimeMillis();

					//linesWritt = fileLineCount; //LogRecord.getSeq();

					//https://gist.github.com/delip/6214406

					//Thread.sleep(10000);
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();

			//server.rollback();

		}
		finally
		{
			//Close the input stream
			in.close();

			pw.close();

			if (commit)
			{

				coreContainer.shutdown();
				server.shutdown();
			}

			long tsStage4 = System.currentTimeMillis();
			/*log.info("Stage1(START):" + (tsStage1 - tsStartTime));
			log.info("Stage2(Indexer Initialization):" + (tsStage2 - tsStartTime));
			log.info("Stage3(Indexing Complete):" + (tsStage3 - tsStartTime));
			log.info("Stage4(FINISH):" + (tsStage4 - tsStartTime));
			log.info("Total Lines Read:" + fileLineCount);
			log.info("Total Lines Written:" + linesWritt);
			log.info("Last Event:" + lastEvent.toString());*/

			log.info("Finished reading " + maxRecords + " records from file:" + inIndexfile);
			log.info("	Cummulative number of events indexed:" + fileLineCount);
			if (lastEvent != null)
			{
				log.info("	Last Event:" + lastEvent.toString());
			}
			else
			{
				log.info("No Last Event.");
				//maxRecords = maxRecords + linesWritt;
				//log.info("maxRecords = " + maxRecords);
			}

		}

	}

	public void doParsing() throws IOException, LogParseException
	{
		DataInputStream in = null;
		PrintWriter pw = null;
		long targetNextLine = 1L;
		long tsStartTime = System.currentTimeMillis();
		long tsStage1 = 0L;
		long fileLineCount = 1L;
		LogRecord lastEvent = null;
		String strLine = null;
		try
		{

			log.fine("inParsefile:" + inParsefile);
			log.fine("outParsefile:" + outParsefile);

			if (inParsefile == null || outParsefile == null)
			{
				throw new LogParseException("Invalid input arguments.");
			}
			// Get the object of DataInputStream
			in = new DataInputStream(new FileInputStream(inParsefile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			//Read File Line By Line

			StringBuffer aLogRec = new StringBuffer();

			File f = new File(outParsefile);
			if (!f.exists())
			{
				f.createNewFile();
				targetNextLine = 1L;
			}
			else
			{
				LogRecord l = LogRecord.parse(SolrUtil.tail(new File(outParsefile)));

				log.info("Pre First Event:" + l.toString());

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
					if (fileLineCount >= targetNextLine)
					{
						aLogRec.append(strLine);
						LogRecord r = LogRecord.createLogRec(aLogRec.toString(), fileLineCount - 1);
						r.line = fileLineCount + "";
						if (fileLineCount == 1)
						{
							pw.print(r.writeHeader());
						}
						pw.print(r.writeRecord());
						lastEvent = r;
						r = null;
						pw.flush();

					}

					aLogRec.delete(0, aLogRec.length());
					fileLineCount++;
				}
				else
				{
					aLogRec.append(strLine);
				}

				//				}
				//				catch (Throwable e)
				//				{
				//					log.info(strLine);
				//
				//					log.info(e.getMessage());
				//				}
			}//end while

		}
		finally
		{
			//Close the input stream
			in.close();
			pw.close();

		}

	}

	public synchronized void shutdownServer() throws IOException, SolrServerException, InterruptedException,
			LogParseException
	{
		//server.getCoreContainer().shutdown();
		log.info("SHUTDOWN SHUTDOWN SHUTDOWN SHUTDOWN.........................");
		server.shutdown();

	}

	private String getlastIndexedrowId()
	{
		return "";
	}

	private void indexSingleRec(Collection<SolrInputDocument> docs, LogRecord logRec)
			throws UnsupportedEncodingException
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

		lineCounter++;

		/* UpdateRequest req = new UpdateRequest();
		    req.add(docs);
		    req.setCommitWithin(10000);
		    req.set
		    req.process(server);
		*/

		//log.info("Done.....");

	}

}
