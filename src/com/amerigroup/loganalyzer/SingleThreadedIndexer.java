package com.amerigroup.loganalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;

/*
 * 12/19 - Changed to repeat threads 3 times on SOLRServerExceptions
 * 12/19 - Stopped doing commit programmatically since autoSoft commit is in the SOLR config xml. Look for related comments in the code.
 * 12/19 - Added indexer.commit property to make commit configurable
 */
public class SingleThreadedIndexer implements Runnable
{

	static final String sourcePropFormat = "log.%s.source";
	static final String destPropFormat = "log.%s.destination";

	static long frequencyInMins = 3;

	static int expireByMinOfDay = 1441; //this value should be in the range of 1 and 1440 = 24 x 60 

	static int maxOccurance = 250;

	static int pauseByMinOfDay = 1441;

	static int pauseDurationMins = 0;

	static boolean doCommit = false;

	static Logger log = Logger.getLogger("SingleThreadedIndexer");

	static String urlString;

	public static void main(String[] args) throws SolrServerException, IOException, InterruptedException
	{

		try

		{

			//HttpSolrServer server1 = new HttpSolrServer(urlString);

			//AbstractHttpClient client = (AbstractHttpClient) server1.getHttpClient();
			//client.addRequestInterceptor(new HttpRequestInterceptor()

			setProps();

			DefaultHttpClient httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager());
			httpclient.addRequestInterceptor(new HttpRequestInterceptor()
			{

				public void process(final HttpRequest request, final HttpContext context) throws HttpException,
						IOException
				{
					/*
					 * SEE HOW TO SET PRE-EMPTIVE AUTH
					 * AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

					log.info("yyyyyyyyyy CREDS yyyyyyyyyy...1");
					// If no auth scheme avaialble yet, try to initialize it
					// preemptively
					if (authState.getAuthScheme() == null)
					{
						AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
						CredentialsProvider credsProvider = (CredentialsProvider) context
								.getAttribute(ClientContext.CREDS_PROVIDER);
						HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

						log.info("yyyyyyyyyy CREDS yyyyyyyyyy...2");
						if (authScheme != null)
						{
							Credentials creds = new UsernamePasswordCredentials("user123", "pass123"); //credsProvider.getCredentials(new AuthScope(targetHost.getHostName(),
							//targetHost.getPort()));

							authState.setAuthScheme(authScheme);
							authState.setCredentials(creds);
							log.info("yyyyyyyyyy CREDS" + creds.getUserPrincipal().getName());
							request.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
						}
					}*/

					Credentials creds = new UsernamePasswordCredentials(SolrUtil.getConfig("indexer.user"), SolrUtil
							.getConfig("indexer.password"));
					request.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));

				}

			});

			//TODO: complete this

			httpclient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy()
			{
				public long getKeepAliveDuration(HttpResponse response, HttpContext context)
				{
					return 1800000L;
				}

			});

			httpclient.setReuseStrategy(new ConnectionReuseStrategy()
			{
				public boolean keepAlive(HttpResponse response, HttpContext context)
				{
					return true;
				}
			});

			ConcurrentUpdateSolrServer server1 = new ConcurrentUpdateSolrServer(urlString, httpclient, 1000, 5);
			//server1.setSoTimeout(0);
			//server1.setConnectionTimeout(0);

			//SingleThreadedIndexer indexr2 = new SingleThreadedIndexer("C:\\solr\\example\\solr\\devlogs\\output.2.log",
			//"C:\\solr\\example\\solr\\devlogs\\output.2.log.indexed", server1);

			String logs[] = SolrUtil.getConfig("logs").split(",");
			ArrayList<Thread> tList = new ArrayList<Thread>();

			for (String log : logs)
			{

				String source = String.format(sourcePropFormat, log);
				String destination = String.format(destPropFormat, log);

				SingleThreadedIndexer aIndexer = new SingleThreadedIndexer(SolrUtil.getConfig(destination),
						SolrUtil.getConfig(destination) + ".indexed", server1);
				Thread aIndexerThread = new Thread(aIndexer, log);
				tList.add(aIndexerThread);

			}

			try
			{

				for (Thread aIndexerThread : tList)
				{

					aIndexerThread.start();

				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				log.log(Level.SEVERE, "Indexing failed", e);
			}
			finally
			{
				for (Thread aIndexerThread : tList)
				{

					aIndexerThread.join();

				}

				server1.shutdown();
				//server2.shutdown();
				System.out.println("server shutdown successfull");
				log.info("server shutdown successfull");
				//coreContainer.shutdown();
				System.out.println("container shutdown successfull");
				log.info("container shutdown successfull");
				System.exit(0);
			}

		}
		catch (Throwable t)
		{

			log.log(Level.SEVERE, "Indexing  Failed", t);
		}

	}

	//instance variables

	int failCount = 0;

	String infile;

	String outfile;

	SolrUtil.Mode mode = null;

	SOLRRealTimeProcessor task;

	//long sleepTimeInMillis = 20000L;

	//multiple sharads in single node
	//http://solr.pl/en/2013/01/07/solr-4-1-solrcloud-multiple-shards-on-the-same-solr-node/

	int occurance = 0;

	static
	{

		SolrUtil.initialize(SolrUtil.Mode.INDEX);

		log.severe("severe.......");
		log.warning("warning.......");
		log.info("info.......");
		log.fine("fine.......");
		log.finer("finer.......");
		log.finest("finest......");

	}

	public static void setProps()
	{

		//log.info(SolrUtil.config.toString());
		urlString = SolrUtil.getConfig("indexer.url");

		String propsRunFreq = SolrUtil.getConfig("indexer.run.frequencyinmins");
		if (propsRunFreq != null)
		{
			frequencyInMins = Long.parseLong(propsRunFreq);
		}

		String propsMaxCycles = SolrUtil.getConfig("indexer.max.cycles");
		if (propsMaxCycles != null)
		{
			maxOccurance = Integer.parseInt(propsMaxCycles);
		}

		String propsExpireByMinOfDay = SolrUtil.getConfig("indexer.expireby.minofday");
		if (propsExpireByMinOfDay != null)
		{
			expireByMinOfDay = Integer.parseInt(propsExpireByMinOfDay);
		}

		log.info("expireByMinOfDay=" + expireByMinOfDay);

		String propsPauseByMinOfDay = SolrUtil.getConfig("indexer.pauseby.minofday");
		if (propsPauseByMinOfDay != null)
		{
			pauseByMinOfDay = Integer.parseInt(propsPauseByMinOfDay);
		}

		log.info("pauseByMinOfDay=" + pauseByMinOfDay);

		String propsPauseDuration = SolrUtil.getConfig("indexer.pauseduration.mins");
		if (propsPauseDuration != null)
		{
			pauseDurationMins = Integer.parseInt(propsPauseDuration);
		}

		log.info("pauseDurationMins=" + pauseDurationMins);

		String propsCommit = SolrUtil.getConfig("indexer.commit");
		if (propsCommit != null)
		{
			doCommit = Boolean.parseBoolean(propsCommit);
		}

		log.info("doCommit=" + doCommit);

	}

	public SingleThreadedIndexer(String inParsefile, String outParsefile)
	{
		mode = SolrUtil.Mode.RTINDEX;
		task = new SOLRRealTimeProcessor(inParsefile, outParsefile);
	}

	public SingleThreadedIndexer(String infile, String outfile, SolrServer server)
	{
		mode = SolrUtil.Mode.RTPARSE;
		task = new SOLRRealTimeProcessor(infile, outfile, mode, server);
	}

	public void execute(boolean commit) throws IOException, SolrServerException, InterruptedException,
			LogParseException
	{

		while (!isExpired())
		{

			long start = System.currentTimeMillis();
			long estEnd = start + (frequencyInMins * 60 * 1000); //start plus 30 mins

			//set false to indicate not to commit for each document add
			task.doIndexing(false);

			if (commit)
			{
				task.commit();
			}

			occurance++;
			long end = System.currentTimeMillis();
			if (end < estEnd)
			{
				log.info("Pausing for secs:" + (estEnd - end) / 1000);
				Thread.sleep(estEnd - end);
			}
		}

		log.fine("Thread: " + Thread.currentThread().getName());
		log.fine("Cycles Executed :" + occurance);
		log.fine("Normal Termination. Safe to shutdown server....");
	}

	@Override
	public void run()
	{
		String threadName = Thread.currentThread().getName();
		log.info(String.format("Thread %s running...", threadName));
		try
		{
			/*			My recommendation: 

							1) Remove all commits from your indexing application. 
							2) Configure autoCommit with values similar to that wiki page. 
							3) Configure autoSoftCommit to happen often. 

							The autoCommit must have openSearcher set to false.  For autoSoftCommit, 
							include a maxTime between 1000 and 5000 (milliseconds) and leave maxDocs 
							out. 
			*/

			//Whether or not commiting depends on the configuration indexer.commit		
			execute(doCommit);
			//resetting the failCount
			failCount = 0;
			log.info(String.format("Thread %s complete...", threadName));
		}
		catch (SolrServerException se)
		{
			failCount++;
			se.printStackTrace();
			log.log(Level.SEVERE, String.format("Thread %s failed. Fail count=%s", threadName, failCount), se);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.log(Level.SEVERE, String.format(
					"Thread %s failed unexpectedly and wont be retried. Manual intervention expected.", threadName), e);
			//resetting the failCount
			failCount = 0;
		}
		finally
		{
			//repeat the run process if the fail count is less than 3
			if (failCount > 0 && failCount <= 3)
			{
				try
				{
					log.info(String.format("Thread %s will be retried again in 30 seconds", threadName));

					Thread.sleep(30000);

					run();
				}
				catch (InterruptedException ie)
				{
					log.log(Level.SEVERE,
							String.format("Thread %s retry failed. Manual intervention expected.", threadName), ie);
				}
			}
			else if (failCount > 3)
			{
				log.log(Level.SEVERE, String.format(
						"All 3 attempts of Thread %s retry failed.  Manual intervention expected.", threadName));
			}
		}

	}

	private boolean isExpired() throws InterruptedException
	{
		boolean expired = false;

		//check first if the occurance limit has not reached.
		if (occurance > maxOccurance)
		{
			log.warning(String.format(
					"Process expired since number of cycles reached the maximum configured  limit: %s / %s", occurance,
					maxOccurance));
			return true;
		}
		else
		{
			log.info(String.format("Processing in progress. Number of cycles check: %s/%s", occurance, maxOccurance));
		}

		//check second if the expiry time has not reached.
		Calendar now = Calendar.getInstance();
		int minuteOfDay = (now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE);

		if (minuteOfDay >= expireByMinOfDay)
		{
			log.warning(String.format(
					"Process expired since it reached the configured minutes of the day expiry: %s / %s", minuteOfDay,
					expireByMinOfDay));
			return true;
		}
		else
		{
			log.info(String
					.format("Processing in progress. Min of the day check: %s/%s", minuteOfDay, expireByMinOfDay));
		}

		//check third if the pause time has not reached.
		if (minuteOfDay >= SingleThreadedIndexer.pauseByMinOfDay)
		{
			log.warning(String.format("Process going to pause for %s mins as configured",
					SingleThreadedIndexer.pauseDurationMins));
			Thread.sleep(pauseDurationMins * 60 * 1000);

		}

		return expired;
	}

}
