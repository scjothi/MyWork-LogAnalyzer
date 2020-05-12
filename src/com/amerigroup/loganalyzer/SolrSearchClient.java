package com.amerigroup.loganalyzer;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;

public class SolrSearchClient
{

	public static void main(String args[])

	{
		try
		{
			// Pre-emptive authentication to speed things up
			BasicHttpContext localContext = new BasicHttpContext();

			BasicScheme basicAuth = new BasicScheme();
			localContext.setAttribute("preemptive-auth", basicAuth);

			//(...)

			HttpRequestInterceptor authInterceptor = new HttpRequestInterceptor()
			{

				public void process(final HttpRequest request, final HttpContext context) throws HttpException,
						IOException
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

			DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpClientUtil.setBasicAuth(httpClient, "user123", "pass123");
			httpClient.addRequestInterceptor(authInterceptor, 0);

			String url = "http://localhost:8080/loganalyzer/devlogs/"; ///collection1/select?shards=localhost:8983/solr,localhost:7574/solr";

			String url2 = "http://localhost:8080/loganalyzer/collection1/";

			////////////////
			/*HttpGet httpget = new HttpGet(
					"/loganalyzer/devlogs/select?shards=localhost:8080/loganalyzer/devlogs,localhost:8080/loganalyzer/collection1&q=id%3Ae9b59aa8-ceca-4cbf-806a-42b71e24ca65&wt=csv&indent=true");
			//"/loganalyzer/devlogs/select?q=id%3Ae9b59aa8-ceca-4cbf-806a-42b71e24ca65&wt=csv&indent=true");

			//httpClient.execute(request)

			HttpHost targetHost = new HttpHost("localhost", 8080, "http");

			HttpResponse response = httpClient.execute(targetHost, httpget, localContext);
			HttpEntity entity = response.getEntity();

			System.out.println("----------------------------------------");
			System.out.println(response.getStatusLine());
			if (entity != null)
			{
				System.out.println("Response content length: " + entity.getContentLength());
			}
			EntityUtils.consume(entity);
			*/
			////////////////////////////////

			HttpSolrServer solrServerInstance = new HttpSolrServer(url, httpClient);

			SolrQuery parameters = new SolrQuery();

			//http://localhost:8080/loganalyzer/devlogs/get?q=id%3Ae9b59aa8-ceca-4cbf-806a-42b71e24ca65&wt=json&indent=true

			//http://localhost:8080/loganalyzer/devlogs/select?q=id%3Ae9b59aa8-ceca-4cbf-806a-42b71e24ca65&wt=json&indent=true

			//parameters.set("shards", "localhost:8080/loganalyzer/devlogs,localhost:8080/loganalyzer/collection1");

			//parameters.set("collection", "devlogs,collection1");

			String queryString = "id:e9b59aa8-ceca-4cbf-806a-42b71e24ca65";
			parameters.set("q", queryString);
			parameters.set("wt", "json");
			parameters.set("indent", true);

			SolrDocumentList list = submit(url, httpClient, parameters);

			/*QueryResponse response = solrServerInstance.query(parameters);

			SolrDocumentList list = response.getResults();

			System.out.println(list.size());
			System.out.println("..............");

			queryString = "id:e9b59aa8-ceca-4cbf-806a-42b71e24ca11";
			parameters.set("q", queryString);

			response = solrServerInstance.query(parameters);
			
			list = response.getResults();
			*/

			System.out.println(list.size());

			list = submit(url2, httpClient, parameters);

			System.out.println(list.size());

			//solrServerInstance.request(request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main2(String args[])

	{

		try
		{
			System.setProperty("solr.solr.home", "C:\\solr\\example\\solr");
			CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			CoreContainer coreContainer = initializer.initialize();

			//SolrCore core = coreContainer.getCore("devlogs");

			EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "collection1");

			SolrQuery parameters = new SolrQuery();

			parameters.set("shards", "localhost:8080/loganalyzer/devlogs,localhost:8080/loganalyzer/collection1");

			//parameters.set("collection", "devlogs,collection1");

			String queryString = "id:e9b59aa8-ceca-4cbf-806a-42b71e24ca65";
			parameters.set("q", queryString);
			parameters.set("wt", "json");
			parameters.set("indent", true);

			QueryResponse response = server.query(parameters);

			SolrDocumentList list = response.getResults();

			System.out.println(list.size());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static SolrDocumentList submit(String url, HttpClient httpClient, SolrQuery q) throws IOException,
			SolrServerException
	{

		//String url = urls.get(0);
		//srsp.setShardAddress(url);
		SolrServer server = new HttpSolrServer(url, httpClient);

		//ssr.nl = server.request(req);

		QueryRequest req = new QueryRequest(q);
		req.setMethod(SolrRequest.METHOD.POST);

		NamedList<Object> nl = server.request(req);

		QueryResponse qr = new QueryResponse(nl, server);

		return qr.getResults();

	}
}
