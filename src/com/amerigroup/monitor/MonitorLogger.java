package com.amerigroup.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MonitorLogger implements Runnable
{

	public static void setHeader(String header)
	{
		sHeader = header;
	}

	String sContent;
	String sFileName;

	static String sHeader = "No Header Defined";

	public MonitorLogger(String sFileName, String sContent)
	{
		this.sContent = sContent;
		this.sFileName = sFileName;
	}

	public synchronized void appendContents()
	{
		BufferedWriter oWriter = null;
		boolean isNewFile = false;
		try
		{
			File oFile = new File(sFileName);
			if (!oFile.exists())
			{
				oFile.createNewFile();
				isNewFile = true;
			}
			//if (oFile.canWrite())
			{
				oWriter = new BufferedWriter(new FileWriter(sFileName, true));
				if (isNewFile)
				{
					oWriter.write(sHeader + "\n");
				}
				oWriter.write(sContent + "\n");
				oWriter.flush();
			}
		}
		catch (IOException oException)
		{
			throw new IllegalArgumentException("Error appending/File cannot be written: \n" + sFileName);
		}
		finally
		{
			try
			{
				oWriter.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run()
	{
		appendContents();
	}
}
