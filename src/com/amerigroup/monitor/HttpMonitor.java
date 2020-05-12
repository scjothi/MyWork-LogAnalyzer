package com.amerigroup.monitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class HttpMonitor extends HttpServlet
{

	public static void main(String args[])
	{

		HttpMonitor mon = new HttpMonitor();
		mon.mainx(null);
	}

	int rollsecs = 0;

	public String logFile = "C:\\Myspace\\test%s.log";

	public String server = "VA1ESB01";

	public String poolName = "facets";
	// Pre-emptive authentication to speed things up
	BasicHttpContext localContext = new BasicHttpContext();

	public DefaultHttpClient httpClient;

	public HttpHost targetHost;

	public HttpGet httpget;

	public void appendLog(Map<String, String> contents)
	{
		StringBuffer result = new StringBuffer("");
		try
		{

			for (Map.Entry<String, String> entry : contents.entrySet())
			{
				String key = entry.getKey();
				Object value = entry.getValue();
				//result.append(key);
				//result.append(":");
				result.append(value);
				result.append(",");
			}

			//	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			//result.append(formatter.format(new Date()));

			//removing the trailing separator char
			result.deleteCharAt(result.length() - 1);

			MonitorLogger ml = new MonitorLogger(logFile, result.toString());
			Thread t1 = new Thread(ml);
			t1.start();

		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		service(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		service(request, response);
	}

	@Override
	public void init(ServletConfig config)
	{

		BasicScheme basicAuth = new BasicScheme();
		localContext.setAttribute("preemptive-auth", basicAuth);

		HttpRequestInterceptor authInterceptor = new HttpRequestInterceptor()
		{

			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
			{
				AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

				// If no auth scheme avaialble yet, try to initialize it
				// preemptively
				if (authState.getAuthScheme() == null)
				{
					AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
					CredentialsProvider credsProvider = (CredentialsProvider) context
							.getAttribute(ClientContext.CREDS_PROVIDER);
					HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					if (authScheme != null)
					{
						Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(),
								targetHost.getPort()));
						if (creds == null)
						{
							throw new HttpException("No credentials for preemptive authentication");
						}
						authState.setAuthScheme(authScheme);
						authState.setCredentials(creds);
						request.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));
					}
				}

			}

		};

		httpClient = new DefaultHttpClient();

		HttpClientUtil.setBasicAuth(httpClient, "jsivath", "AGP%2013");
		httpClient.addRequestInterceptor(authInterceptor, 0);

		//String url = "http://vadesb01:9990/management/subsystem/datasources/data-source/WebSecurity/statistics/pool?include-runtime=true"; 

		String url = "/management/subsystem/datasources/data-source/" + poolName
				+ "/statistics/pool?include-runtime=true";

		//httpClient.execute(request)

		httpget = new HttpGet(url);

		//httpClient.execute(request)

		targetHost = new HttpHost(server, 9990, "http");

	}
	

	public void mainx(String args[])
	{

		//HttpMonitor monitor = new HttpMonitor();
		MonitorLogger
				.setHeader("AverageBlockingTime,MaxWaitTime,ActiveCount,CreatedCount,MaxCreationTime,TotalCreationTime,AvailableCount,MaxUsedCount,TimeStamp,TimedOut,TotalBlockingTime,AverageCreationTime,DestroyedCount");

		SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy");

		logFile = String.format(logFile, "_" + server + "_" + formatter.format(new Date()));
		//long st1 = System.currentTimeMillis();
		init(null);

		while (true)
		{

			//long currtime = System.currentTimeMillis();
			//System.out.println("time diff:" + (currtime - st1));
			if (isTimeToRoll())
			{
				//st1 = currtime;
				logFile = String.format(logFile, "_" + server + "_" + formatter.format(new Date()));
			}

			long st2 = System.currentTimeMillis();
			appendLog(submitRequest());
			long st3 = System.currentTimeMillis();
			System.out.println("Time taken:" + (st3 - st2));
			try
			{
				Thread.sleep(1000 * 60 * 5);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public Map<String, String> parseJsonToMap(String jsonData) throws JsonParseException, JsonMappingException,
			IOException
	{
		//converting json to Map
		byte[] mapData = jsonData.getBytes();
		Map<String, String> myMap = new HashMap<String, String>();

		ObjectMapper objectMapper = new ObjectMapper();
		//myMap = objectMapper.readValue(mapData, HashMap.class);
		System.out.println("Map is: " + myMap);

		//another way
		myMap = objectMapper.readValue(mapData, new TypeReference<HashMap<String, String>>()
		{
		});
		System.out.println("Map using TypeReference: " + myMap);

		return myMap;

	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{

		HttpMonitor monitor = new HttpMonitor();
		long st1 = System.currentTimeMillis();
		monitor.init(null);
		monitor.submitRequest();
		long st2 = System.currentTimeMillis();
		System.out.println("A Time taken:" + (st2 - st1));
		monitor.submitRequest();
		monitor.submitRequest();
		long st3 = System.currentTimeMillis();
		System.out.println("B Time taken:" + (st3 - st1));

	}

	public Map<String, String> submitRequest()
	{
		Map<String, String> result = new HashMap<String, String>();
		try
		{

			HttpResponse response = httpClient.execute(targetHost, httpget, localContext);
			HttpEntity entity = response.getEntity();

			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());

			if (entity != null)
			{
				System.out.println("Response content length: " + entity.getContentLength());

			}
			//EntityUtils.consume(entity);

			String s = EntityUtils.toString(entity);

			System.out.println(s);

			result = parseJsonToMap(s);

			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

			result.put("TimeStamp", formatter.format(new Date()));
			//result.append(formatter.format(new Date()));

			////////////////////////////////		

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;

	}

	public void writeHeader(String header)
	{
		MonitorLogger ml = new MonitorLogger(logFile, header);
		Thread t1 = new Thread(ml);
		t1.start();
	}

	private boolean isTimeToRoll()
	{
		SimpleDateFormat rollformatter = new SimpleDateFormat("HH:mm:ss");

		String rolldate = rollformatter.format(new Date());
		String[] rolltimes = rolldate.split(":");
		int n = 3600;

		for (String rtime : rolltimes)
		{
			rollsecs = rollsecs + (Integer.parseInt(rtime) * n);
			n = n / 60;
		}

		if (rollsecs > 86400)
		{
			rollsecs = 0;
			return true;
		}

		return false;

	}

}
