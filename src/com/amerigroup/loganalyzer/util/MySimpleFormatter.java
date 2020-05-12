package com.amerigroup.loganalyzer.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class MySimpleFormatter extends SimpleFormatter
{

	Date dat = new Date();
	private final static String format = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];

	// Line separator string.  This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private String lineSeparator = java.security.AccessController
			.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

	/**
	 * Format the given LogRecord.
	 * <p>
	 * This method can be overridden in a subclass. It is recommended to use the {@link Formatter#formatMessage}
	 * convenience method to localize and format the message field.
	 * 
	 * @param record the log record to be formatted.
	 * @return a formatted log record
	 */
	@Override
	public synchronized String format(LogRecord record)
	{
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		dat.setTime(record.getMillis());
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null)
		{
			formatter = new MessageFormat(format);
		}
		formatter.format(args, text, null);
		sb.append(text);
		sb.append(" ");

		sb.append(Thread.currentThread().getName());
		sb.append(" ");

		if (record.getSourceClassName() != null)
		{
			sb.append(record.getSourceClassName());
		}
		else
		{
			sb.append(record.getLoggerName());
		}
		if (record.getSourceMethodName() != null)
		{
			sb.append(" ");
			sb.append(record.getSourceMethodName());
		}
		sb.append(lineSeparator);
		String message = formatMessage(record);
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(message);
		sb.append(lineSeparator);
		if (record.getThrown() != null)
		{
			try
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			}
			catch (Exception ex)
			{
			}
		}
		return sb.toString();
	}

}
