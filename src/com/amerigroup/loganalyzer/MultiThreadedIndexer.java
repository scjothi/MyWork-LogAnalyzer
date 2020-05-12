package com.amerigroup.loganalyzer;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

public class MultiThreadedIndexer implements Runnable
{

	static Logger log = Logger.getLogger("MultiThreadedIndexer");

	//multiple sharads in single node
	//http://solr.pl/en/2013/01/07/solr-4-1-solrcloud-multiple-shards-on-the-same-solr-node/

	static
	{

		//SolrUtil.initializeLogger();

		log.severe("severe.......");
		log.warning("warning.......");
		log.info("info.......");
		log.fine("fine.......");
		log.finer("finer.......");
		log.finest("finest......");

	}

	public static void main(String[] args) throws SolrServerException, IOException, InterruptedException
	{
		String mode = args[0];
		//String inFiles = args[1];
		//String outFiles = args[2];
		//CoreContainer coreContainer = null

		if (mode == null)
		{
			log.severe("Invalid Argument Error. Valid arguments: RTParse, RTIndex");
		}

		if (mode.equals(SolrUtil.Mode.RTINDEX))
		{
			//System.getProperty("loganalyzer.index.url");
			/*String solrHome = System.getProperty("solr.solr.home");
			//System.out.println("")
			solrHome = (solrHome == null) ? "C:\\solr\\example\\solr" : solrHome.trim();
			System.setProperty("solr.solr.home", solrHome);
			System.out.println("Using Solr Home:" + System.getProperty("solr.solr.home"));

			CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			CoreContainer coreContainer = initializer.initialize();*/

			//CoreDescriptor cd = new CoreDescriptor(coreContainer, "devlogs", "C:/solr/example/solr/devlogs");
			//coreContainer.create(cd);

			System.setProperty("solr.solr.home", "C:\\solr\\example\\solr");
			log.info("Using Solr Home:" + System.getProperty("solr.solr.home"));
			//CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			//CoreContainer coreContainer = initializer.initialize();
			//EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");

			//SolrCore sc = coreContainer.getCore("devlogs");

			String urlString = "http://localhost:8080/loganalyzer/devlogs/";
			//SolrServer solr = new HttpSolrServer(urlString);

			HttpSolrServer server1 = new HttpSolrServer(urlString); //new EmbeddedSolrServer(coreContainer, "devlogs");		

			AbstractHttpClient client = (AbstractHttpClient) server1.getHttpClient();
			client.addRequestInterceptor(new HttpRequestInterceptor()
			{

				public void process(final HttpRequest request, final HttpContext context) throws HttpException,
						IOException
				{
					/*AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

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

					Credentials creds = new UsernamePasswordCredentials("user123", "pass123");
					request.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));

				}

			});

			//MultiThreadedIndexer indexr1 = new MultiThreadedIndexer("C:\\solr\\example\\solr\\devlogs\\output.1.log",
			//"C:\\solr\\example\\solr\\devlogs\\output.1.log.indexed", server1);
			//EmbeddedSolrServer server2 = new EmbeddedSolrServer(coreContainer, "devlogs");
			MultiThreadedIndexer indexr2 = new MultiThreadedIndexer("C:\\solr\\example\\solr\\devlogs\\output.2.log",
					"C:\\solr\\example\\solr\\devlogs\\output.2.log.indexed", server1);

			//Thread t1 = new Thread(indexr1);
			//t1.start();
			Thread t2 = new Thread(indexr2);
			t2.start();

			try
			{
				//t1.join();
				t2.join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			server1.commit(); //false, false, true);

			//server1.optimize(true, false);

			Thread.sleep(60000);

			System.out.println("Commit suceeded...");

			System.out.println("All threads have completed. Safe to shutdown...");

			//server1.commit();
			//server2.commit(false, false, true);
			//System.out.println("commit successfull");
			Thread.sleep(10000);
			server1.shutdown();
			//server2.shutdown();
			System.out.println("server shutdown successfull");
			//coreContainer.shutdown();
			System.out.println("container shutdown successfull");
			System.exit(0);

		}
		else if (mode.equals(SolrUtil.Mode.RTPARSE))
		{

			MultiThreadedIndexer p1 = new MultiThreadedIndexer( //"\\\\va1esb01\\log\\server.log",
					"\\\\VAQESB01\\log-A\\server.log", "C:\\solr\\example\\solr\\devlogs\\output.1.log");
			MultiThreadedIndexer p2 = new MultiThreadedIndexer(//"\\\\va1esb02\\log\\server.log",
					"\\\\VAQESB02\\log-A\\server.log", "C:\\solr\\example\\solr\\devlogs\\output.2.log");

			MultiThreadedIndexer p5 = new MultiThreadedIndexer( //"\\\\va1esb01\\log\\server.log",
					"\\\\VAQESB01\\log-A\\server.log", "C:\\solr\\example\\solr\\devlogs\\output.5.log");
			MultiThreadedIndexer p6 = new MultiThreadedIndexer(//"\\\\va1esb02\\log\\server.log",
					"\\\\VAQESB02\\log-A\\server.log", "C:\\solr\\example\\solr\\devlogs\\output.6.log");

			Thread t1 = new Thread(p1);
			t1.start();

			Thread t2 = new Thread(p2);
			t2.start();

			Thread t5 = new Thread(p5);
			t5.start();

			Thread t6 = new Thread(p6);
			t6.start();

			try
			{

				t1.join();
				t2.join();
				t5.join();
				t6.join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Parsing complete.....");

		}

	}

	String infile;
	String outfile;

	SolrUtil.Mode mode = null;

	SOLRRealTimeProcessor task;

	public MultiThreadedIndexer(String inParsefile, String outParsefile)
	{
		mode = SolrUtil.Mode.RTPARSE;
		task = new SOLRRealTimeProcessor(inParsefile, outParsefile);
	}

	public MultiThreadedIndexer(String infile, String outfile, SolrServer server)
	{
		mode = SolrUtil.Mode.RTINDEX;
		task = new SOLRRealTimeProcessor(infile, outfile, mode, server);
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

		try
		{
			if (mode.equals(SolrUtil.Mode.RTPARSE))
			{

				//SOLRRealTimeIndexer rtIndexer = new SOLRRealTimeIndexer();
				//rtIndexer.doIndexing(infile, outfile);
				task.doParsing();

			}
			else if (mode.equals(SolrUtil.Mode.RTINDEX))
			{

				//SOLRRealTimeIndexer rtIndexer = new SOLRRealTimeIndexer();
				//rtIndexer.doParsing(infile, outfile);
				task.doIndexing(false);
				//task.commit();

			}
			else
			{
				throw new LogParseException("Invalid Mode");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("thread complete...");

	}

}
