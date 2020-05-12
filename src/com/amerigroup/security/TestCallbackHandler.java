package com.amerigroup.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class TestCallbackHandler implements CallbackHandler
{

	String name;
	String password;

	public TestCallbackHandler(String name, String password)
	{
		System.out.println("Callback Handler - constructor called");
		this.name = name;
		this.password = password;
	}

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
	{
		System.out.println("Callback Handler - handle called");
		for (Callback callback : callbacks)
		{
			if (callback instanceof NameCallback)
			{
				NameCallback nameCallback = (NameCallback) callback;
				nameCallback.setName(name);
			}
			else if (callback instanceof PasswordCallback)
			{
				PasswordCallback passwordCallback = (PasswordCallback) callback;
				passwordCallback.setPassword(password.toCharArray());
			}
			else
			{
				throw new UnsupportedCallbackException(callback, "The submitted Callback is unsupported");
			}
		}
	}
}
