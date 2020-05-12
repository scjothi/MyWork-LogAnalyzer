package com.amerigroup.loganalyzer;

import java.util.UUID;
import java.util.regex.Pattern;

public class AccessLogRecord
{

	private static Pattern p = Pattern.compile("Caused by: [a-zA-Z.]*Exception:");

	//NOTE: creates an instance from a  raw log entry that is in the standard input format.
	public static AccessLogRecord createLogRec(String line) throws LogParseException
	{
		String[] s = line.split(" ");
		//System.out.println(s);
		AccessLogRecord r = new AccessLogRecord();
		r.address = s[0].trim();
		r.timestamp = s[1].trim() + " " + s[2].trim();
		r.type = s[3].trim();
		r.port = s[4].trim();
		r.url = s[5].trim();
		r.respCode = s[6].trim();
		r.respTime = s[7].trim();
		/*r.serverName = s[2].trim();
		r.instanceName = s[3].trim();
		r.ws = s[4].trim();
		r.timestamp = s[5].trim(); //
		r.tsutc = SolrUtil.convertToUTC(s[5].trim(), "yyyy-MM-dd HH:mm:ss,SSS");
		r.level = s[6].trim();
		r.thread = s[7].trim();
		r.className = s[8].trim();
		r.message = s[9];*/
		r.line = new Long(seq++).toString();
		r.tsutc = SolrUtil.convertToUTC(s[1].trim().substring(1), "yyyy-MM-dd HH:mm:ss,SSS");
		r.encode();
		return r;
	}

	//NOTE: creates an instance from a  raw log entry that is in the standard input format.
	public static AccessLogRecord createLogRec(String line, long lineNum) throws LogParseException
	{
		String[] s = line.split("[|]");
		//System.out.println(s);
		AccessLogRecord r = new AccessLogRecord();
		r.serverName = s[2].trim();
		r.instanceName = s[3].trim();
		r.ws = s[4].trim();
		r.timestamp = s[5].trim(); //
		r.tsutc = SolrUtil.convertToUTC(s[5].trim(), "yyyy-MM-dd HH:mm:ss,SSS");
		r.level = s[6].trim();
		r.thread = s[7].trim();
		r.className = s[8].trim();
		r.message = s[9];
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
	public static AccessLogRecord parse(String line) throws LogParseException
	{

		String[] s = line.split("[|]");
		//System.out.println(s);
		AccessLogRecord r = new AccessLogRecord();
		r.uuid = s[2].trim();
		r.line = s[3].trim();
		r.serverName = s[4].trim();
		r.instanceName = s[5].trim();
		r.ws = s[6].trim();
		r.timestamp = s[7].trim(); //
		r.tsutc = s[8].trim();
		r.level = s[9].trim();
		r.thread = s[10].trim();
		r.className = s[11].trim();
		r.message = s[12];
		//to count records parsed
		seq++;

		//r.encode();
		return r;
	}

	String uuid;
	String line;
	String address;
	String timestamp;
	String type;
	String port;
	String tsutc;
	String url;
	String respCode;
	String respTime;
	//String className;
	//String message;

	private static long seq = 1L;

	private AccessLogRecord()
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
		return "AccessLogRecord [address=" + address + ", timestamp=" + timestamp + ", type=" + type + ", port=" + port
				+ ", url=" + url + ", respCode=" + respCode + ", respTime=" + respTime + ", line=" + line + "]";
	}

	public String writeHeader()
	{
		return "||id|LINE|IPADDRESS|TIMESTAMP|HTTPTYPE|PORT|URL|RESPONSECODE|RESPONSETIME||\n";
	}

	public String writeRecord()
	{
		return "||" + uuid + "|" + line + "|" + address + "|" + timestamp + "|" + type + "|" + port + "|" + url + "|"
				+ respCode + "|" + respTime + "||\n";
	}

}
