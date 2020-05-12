package com.amerigroup.loganalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import asia.redact.bracket.properties.Properties;

public class SolrUtil
{

	public static enum Mode
	{
		PARSE, INDEX, RTPARSE, RTINDEX;
	}

	public static String[] runtimeArgs;

	//2008-06-17 11:26:00.983
	public static final String[] DATETIME_FMTS = { "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" };

	private static Properties config = null;

	public static String convertToUTC(String dateAsString, String format) throws LogParseException
	{

		return toUTCString(parseDateFormats(format, dateAsString));
	}

	public static String getConfig(String property)
	{
		String propValue = System.getProperty(property);
		if (propValue != null && propValue.trim().length() > 0)
		{
			return propValue;
		}
		else if (config.get(property) != null && config.get(property).trim().length() > 0)
		{
			return config.get(property);
		}

		return null;

	}

	public static synchronized void initialize(Mode mode)
	{
		InputStream inputStream = null;
		try
		{

			java.util.Properties props = System.getProperties();
			props.list(System.out);

			System.out.println("###### LATParser.Properties ########");

			//System.out.println("parser.config: ");
			//System.out.print(System.getProperty("parser.config"));
			File file = null;
			if (System.getProperty("parser.config") != null)
			{
				file = new File(System.getProperty("parser.config"));
				config = Properties.Factory.getInstance(file, Charset.forName("UTF-8"));
				System.out.println(SolrUtil.config.toString());
			}
			else
			{
				throw new RuntimeException("No LATParser.Properties defined.");
			}

			String logFile = null;

			if (mode == Mode.INDEX)
			{
				logFile = config.get("indexer.log.file");
			}
			else if (mode == Mode.PARSE)
			{
				logFile = config.get("parser.log.file");
			}

			inputStream = new FileInputStream(new File(logFile));
			//Main.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(inputStream);

		}
		catch (FileNotFoundException e1)
		{
			System.out.println("initialization failed with error: " + e1.getMessage());
			e1.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
		finally
		{
			try
			{
				inputStream.close();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}

	/**
	 * Parses a date string in the format passed as parameter to a java.util.Date object
	 * 
	 * @param fmt format of the date string
	 * @param dateAsString a date string
	 * @return
	 * @throws LogParseException
	 */
	public static Date parseDateFormats(String fmt, String dateAsString) throws LogParseException
	{
		Date result = null;
		//for (String fmt : fmts)
		//{
		try
		{
			SimpleDateFormat df = new SimpleDateFormat(fmt, Locale.US);
			synchronized (df)
			{
				df.setLenient(false);
				result = df.parse(dateAsString);
				if (result != null)
				{
					return result;
				}
			}
		}
		catch (ParseException exc)
		{
			throw new LogParseException("can't parse date string ".concat(dateAsString), exc);
		}
		//}
		return result;
	}

	public static String tail(File file)
	{
		RandomAccessFile fileHandler = null;
		try
		{
			fileHandler = new RandomAccessFile(file, "r");
			long fileLength = file.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--)
			{
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA)
				{
					if (filePointer == fileLength)
					{
						continue;
					}
					else
					{
						break;
					}
				}
				else if (readByte == 0xD)
				{
					if (filePointer == fileLength - 1)
					{
						continue;
					}
					else
					{
						break;
					}
				}

				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		}
		catch (java.io.FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (java.io.IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if (fileHandler != null)
			{
				try
				{
					fileHandler.close();
				}
				catch (IOException e)
				{
					/* ignore */
				}
			}
		}
	}

	public static String toUTCString(Date date)
	{
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sd.setTimeZone(TimeZone.getTimeZone("UTC"));
		//TimeZone timeZone = TimeZone.getDefault();
		//Calendar cal = Calendar.getInstance(new SimpleTimeZone(timeZone.getOffset(date.getTime()), "UTC"));
		//sd.setCalendar(cal);
		return sd.format(date);
	}

}
