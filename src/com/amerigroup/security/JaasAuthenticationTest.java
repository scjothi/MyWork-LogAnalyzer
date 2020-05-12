package com.amerigroup.security;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class JaasAuthenticationTest
{

	public static void main(String[] args)
	{
		System.setProperty("java.security.auth.login.config", "JAAS.config");

		System.out.println(System.getProperty("java.util.logging.config.file"));

		//String name = "user123"; //"AGPCORP\\JSIVATH";
		//String password = "pass123"; //"AGP#2013";

		String name = "jsivath";
		String password = "AGP#2013";

		try
		{
			LoginContext lc = new LoginContext("LogAnalyzer", new TestCallbackHandler(name, password));
			lc.login();
			System.out.println("Login Successful !");
		}
		catch (LoginException e)
		{
			e.printStackTrace();
		}
	}
}
