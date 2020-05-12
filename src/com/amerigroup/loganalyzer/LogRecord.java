package com.amerigroup.loganalyzer;

import java.util.UUID;
import java.util.regex.Pattern;

public class LogRecord
{

	private static Pattern p = Pattern.compile("Caused by: [a-zA-Z.]*Exception:");

	//NOTE: creates an instance from a  raw log entry that is in the standard input format.
	public static LogRecord createLogRec(String line) throws LogParseException
	{
		String[] s = line.split("[|]");
		//System.out.println(s);
		LogRecord r = new LogRecord();
		r.serverName = s[2].trim();
		r.instanceName = s[3].trim();
		r.ws = s[4].trim();
		r.transId = s[5].trim();
		r.timestamp = s[6].trim(); //
		r.tsutc = SolrUtil.convertToUTC(s[6].trim(), "yyyy-MM-dd HH:mm:ss,SSS");
		r.level = s[7].trim();
		r.thread = s[8].trim();
		r.className = s[9].trim();
		r.message = s[10];
		r.line = new Long(seq++).toString();
		r.encode();
		return r;
	}

	//NOTE: creates an instance from a  raw log entry that is in the standard input format.
	public static LogRecord createLogRec(String line, long lineNum) throws LogParseException
	{
		String[] s = line.split("[|]");
		//System.out.println(s);
		LogRecord r = new LogRecord();
		r.serverName = s[2].trim();
		r.instanceName = s[3].trim();
		r.ws = s[4].trim();
		r.transId = s[5].trim();
		r.timestamp = s[6].trim(); //
		r.tsutc = SolrUtil.convertToUTC(s[6].trim(), "yyyy-MM-dd HH:mm:ss,SSS");
		r.level = s[7].trim();
		r.thread = s[8].trim();
		r.className = s[9].trim();
		r.message = s[10];
		r.line = new Long(lineNum).toString();
		r.encode();
		return r;
	}

	public static long getSeq()
	{
		return seq;
	}

	//NOTE: creates an instance from a  log entry that is in the standard output format below(already written by the writeRecord method).
	//||id|LINE|SERVER|INSTANCE|WEBSERVICE|TIMESTAMP|UTCTIMESTAMP|LEVEL|THREAD|CLASS|MESSAGE||
	public static LogRecord parse(String line) throws LogParseException
	{

		String[] s = line.split("[|]");
		//System.out.println(s);
		LogRecord r = new LogRecord();
		r.uuid = s[2].trim();
		r.line = s[3].trim();
		r.serverName = s[4].trim();
		r.instanceName = s[5].trim();
		r.ws = s[6].trim();
		r.transId = s[7].trim();
		r.timestamp = s[8].trim(); //
		r.tsutc = s[9].trim();
		r.level = s[10].trim();
		r.thread = s[11].trim();
		r.className = s[12].trim();
		r.message = s[13];
		//to count records parsed
		seq++;

		//r.encode();
		return r;
	}

	String uuid;
	String line;
	String serverName;
	String instanceName;
	String ws;
	String timestamp;
	String tsutc;
	String level;
	String thread;
	String transId;

	String className;

	String message;

	private static long seq = 1L;

	private LogRecord()
	{
		this.uuid = UUID.randomUUID().toString();
	}

	public void decode()
	{
		message = message.replaceAll("»Caused by:", "\nCaused by:").replaceAll("¦", "\n\t");
	}

	public void encode()
	{
		/*Matcher m = LogRecord.p.matcher(message);
		while (m.find())
		{
			message = message.replaceFirst("Caused by: ", "»Caused by: ");
		}

		message = message.replaceAll("\n\t", "¦");*/

		message = message.replaceAll("Caused by: [a-zA-Z.]*Exception:", "»Caused by:").replaceAll("\n\t", "¦");
	}

	@Override
	public String toString()
	{
		return "LogRecord [serverName=" + serverName + ", instanceName=" + instanceName + ", ws=" + ws + ", transId="
				+ transId + ", timestamp=" + timestamp + ", tsutc=" + tsutc + ", level=" + level + ", thread=" + thread
				+ ", className=" + className + ", message=" + message + ", line=" + line + "]";
	}

	public String writeHeader()
	{
		return "||id|LINE|SERVER|INSTANCE|WEBSERVICE|TRANSACTION|TIMESTAMP|UTCTIMESTAMP|LEVEL|THREAD|CLASS|MESSAGE||\n";
	}

	public String writeRecord()
	{
		return "||" + uuid + "|" + line + "|" + serverName + "|" + instanceName + "|" + ws + "|" + transId + "|"
				+ timestamp + "|" + tsutc + "|" + level + "|" + thread + "|" + className + "|" + message + "||\n";
	}

}
