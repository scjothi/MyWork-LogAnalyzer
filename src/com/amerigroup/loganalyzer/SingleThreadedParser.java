package com.amerigroup.loganalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServerException;

public class SingleThreadedParser implements Runnable
{

	public static final String MODE_RT_PARSE = "RTParse";

	static final String sourcePropFormat = "log.%s.source";
	static final String destPropFormat = "log.%s.destination";

	static Logger log = Logger.getLogger("SingleThreadedParser");

	//https://code.google.com/p/bracket-properties/

	static int maxOccurance = 79;

	static long frequencyInMins = 10;

	static int expireByMinOfDay = 1441; //this value should be in the range of 1 and 1440 = 24 x 60 

	static int pauseByMinOfDay = 1441;

	static int pauseDurationMins = 0;

	int occurance = 0;

	//multiple sharads in single node
	//http://solr.pl/en/2013/01/07/solr-4-1-solrcloud-multiple-shards-on-the-same-solr-node/

	static
	{

		SolrUtil.initialize(SolrUtil.Mode.PARSE);

		log.severe("severe.......");
		log.warning("warning.......");
		log.info("info.......");
		log.fine("fine.......");
		log.finer("finer.......");
		log.finest("finest......");

	}

	public static void main(String[] args) throws SolrServerException, IOException, InterruptedException
	{

		try
		{
			log.info("Initializing...");
			String logs[] = SolrUtil.getConfig("logs").split(",");
			ArrayList<Thread> tList = new ArrayList<Thread>();

			//set the properties
			setProps();

			for (String log : logs)
			{

				String source = String.format(sourcePropFormat, log);
				String destination = String.format(destPropFormat, log);

				SingleThreadedParser aParser = new SingleThreadedParser(SolrUtil.getConfig(source),
						SolrUtil.getConfig(destination));
				Thread aParserThread = new Thread(aParser, log);
				tList.add(aParserThread);

			}

			log.info("Creating Threads...");
			for (Thread parserThread : tList)
			{

				parserThread.start();

			}

			try
			{

				for (Thread parserThread : tList)
				{

					parserThread.join();

				}

			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.log(Level.SEVERE, "Uncaught exception", e);
			}

			log.info("Parsing complete.....");

		}

		catch (Throwable t)
		{
			log.log(Level.SEVERE, "Parsing Failed", t);
		}

	}

	public static void setProps()
	{
		String propsRunFreq = SolrUtil.getConfig("parser.run.frequencyinmins");
		if (propsRunFreq != null)
		{
			frequencyInMins = Long.parseLong(propsRunFreq);
		}

		String propsMaxCycles = SolrUtil.getConfig("parser.max.cycles");
		if (propsMaxCycles != null)
		{
			maxOccurance = Integer.parseInt(propsMaxCycles);
		}

		String propsExpireByMinOfDay = SolrUtil.getConfig("parser.expireby.minofday");
		if (propsExpireByMinOfDay != null)
		{
			expireByMinOfDay = Integer.parseInt(propsExpireByMinOfDay);
		}

		log.info("expireByMinOfDay=" + expireByMinOfDay);

		String propsPauseByMinOfDay = SolrUtil.getConfig("parser.pauseby.minofday");
		if (propsPauseByMinOfDay != null)
		{
			pauseByMinOfDay = Integer.parseInt(propsPauseByMinOfDay);
		}

		log.info("pauseByMinOfDay=" + pauseByMinOfDay);

		String propsPauseDuration = SolrUtil.getConfig("parser.pauseduration.mins");
		if (propsPauseDuration != null)
		{
			pauseDurationMins = Integer.parseInt(propsPauseDuration);
		}

		log.info("pauseDurationMins=" + pauseDurationMins);
	}

	String infile;
	String outfile;

	String mode = "";

	SOLRRealTimeProcessor task;

	public SingleThreadedParser(String inParsefile, String outParsefile)
	{
		mode = MODE_RT_PARSE;
		task = new SOLRRealTimeProcessor(inParsefile, outParsefile);
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

		try
		{

			while (!isExpired())
			{
				//SOLRRealTimeIndexer rtIndexer = new SOLRRealTimeIndexer();
				//rtIndexer.doIndexing(infile, outfile);
				long start = System.currentTimeMillis();
				long estEnd = start + (frequencyInMins * 60 * 1000); //1800000L; //start plus 30 mins
				task.doParsing();
				occurance++;
				long end = System.currentTimeMillis();
				if (end < estEnd)
				{
					log.info("Pausing for secs:" + (estEnd - end) / 1000);
					Thread.sleep(estEnd - end);
				}
			}

			log.info("thread complete...");

		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.severe(e.getMessage());
			log.log(Level.SEVERE, "Thread failed", e);
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
			log.info(String.format("Processing in progress: %s/%s", occurance, maxOccurance));
		}

		//check second if the expiry time has not reached.
		Calendar now = Calendar.getInstance();
		int minuteOfDay = (now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE);

		if (minuteOfDay >= expireByMinOfDay)
		{
			log.warning(String.format("Process expired since it reached the configured time of expiry: %s / %s",
					minuteOfDay, expireByMinOfDay));
			return true;
		}
		else
		{
			log.info(String.format("Processing in progress: %s/%s", minuteOfDay, expireByMinOfDay));
		}

		//check third if the pause time has not reached.
		if (minuteOfDay >= SingleThreadedParser.pauseByMinOfDay)
		{
			log.warning(String.format("Process going to pause for %s mins as configured",
					SingleThreadedParser.pauseDurationMins));
			Thread.sleep(pauseDurationMins * 60 * 1000);

		}

		return expired;
	}
}
