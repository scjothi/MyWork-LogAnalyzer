package com.amerigroup.security;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.sun.security.auth.UserPrincipal;

public class LATLoginModule implements LoginModule
{

	Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private CallbackHandler handler;
	private Subject subject;
	private UserPrincipal userPrincipal;
	private RolePrincipal rolePrincipal;
	private String login;
	private List<String> userGroups;
	private Map sharedState;

	private boolean succeeded = false;
	private boolean commitSucceeded = false;

	private static final String USE_OBJECT_CREDENTIAL_OPT = "useObjectCredential";
	private static final String PRINCIPAL_DN_PREFIX_OPT = "principalDNPrefix";
	private static final String PRINCIPAL_DN_SUFFIX_OPT = "principalDNSuffix";
	private static final String ROLES_CTX_DN_OPT = "rolesCtxDN";
	private static final String USER_ROLES_CTX_DN_ATTRIBUTE_ID_OPT = "userRolesCtxDNAttributeName";
	private static final String UID_ATTRIBUTE_ID_OPT = "uidAttributeID";
	private static final String ROLE_ATTRIBUTE_ID_OPT = "roleAttributeID";
	private static final String MATCH_ON_USER_DN_OPT = "matchOnUserDN";
	private static final String ROLE_ATTRIBUTE_IS_DN_OPT = "roleAttributeIsDN";
	private static final String ROLE_NAME_ATTRIBUTE_ID_OPT = "roleNameAttributeID";
	private static final String ROLE_FILTER_OPT = "roleFilter";

	private static final String BASE_CTX_DN = "baseCtxDN";
	private static final String BIND_DN = "bindDN";
	private static final String BIND_CREDENTIAL = "bindCredential";
	private static final String BASE_FILTER_OPT = "baseFilter";
	private static final String DISTINGUISHED_NAME_ATTRIBUTE_OPT = "distinguishedNameAttribute";

	Map<String, ?> options = null;

	public boolean abort() throws LoginException
	{
		return false;
	}

	public boolean commit() throws LoginException
	{

		userPrincipal = new UserPrincipal(login);
		subject.getPrincipals().add(userPrincipal);

		if (userGroups != null && userGroups.size() > 0)
		{
			for (String groupName : userGroups)
			{
				rolePrincipal = new RolePrincipal(groupName);
				//System.out.println("rolePrincipal:" + rolePrincipal.getName());
				subject.getPrincipals().add(rolePrincipal);
			}
		}

		return true;
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options)
	{

		this.handler = callbackHandler;
		this.subject = subject;
		this.options = options;
		this.sharedState = sharedState;

		log.fine("Option values....");
		for (String option : options.keySet())
		{
			log.fine(option.toString() + ":" + options.get(option));
		}

	}

	public boolean login() throws LoginException
	{

		log.fine("here.......");
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("login");
		callbacks[1] = new PasswordCallback("password", true);
		boolean isValid = false;

		try
		{
			handler.handle(callbacks);
			String username = ((NameCallback) callbacks[0]).getName();
			String password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());

			// Here we validate the credentials against some
			// authentication/authorization provider.
			// It can be a Database, an external LDAP, 
			// a Web Service, etc.
			// For this tutorial we are just checking if 
			// user is "user123" and password is "pass123"
			if (username != null && username.equals("user123") && password != null && password.equals("pass123"))
			{

				// We store the username and roles
				// fetched from the credentials provider
				// to be used later in commit() method.
				// For this tutorial we hard coded the
				// "admin" role
				login = username;
				userGroups = new ArrayList<String>();
				userGroups.add("admin");

				log.fine("###########" + userGroups.size());
				return true;
			}

			else if (username != null && password != null)
			{
				Properties env = createLdapInitContext(username, password);

				userGroups = (List<String>) env.get("user.roles");
				//String role = (String) env.get("user.role");
				//System.out.println("role:" + role);

				login = username;

				log.fine("###########" + userGroups.size());
				return true;
			}

			// If credentials are NOT OK we throw a LoginException
			throw new LoginException("Authentication failed");

		}
		catch (IOException e)
		{
			throw new LoginException(e.getMessage());
		}
		catch (UnsupportedCallbackException e)
		{
			throw new LoginException(e.getMessage());
		}
		catch (NamingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	public boolean logout() throws LoginException
	{
		subject.getPrincipals().remove(userPrincipal);
		subject.getPrincipals().remove(rolePrincipal);
		return true;
	}

	/**
	 * Validate the inputPassword by creating a ldap InitialContext with the SECURITY_CREDENTIALS set to the password.
	 * 
	 * @param inputPassword the password to validate.
	 * @param expectedPassword ignored
	 */
	protected boolean validatePassword(String inputPassword, String expectedPassword)
	{
		boolean isValid = false;
		if (inputPassword != null)
		{
			// See if this is an empty password that should be disallowed
			if (inputPassword.length() == 0)
			{
				// Check for an allowEmptyPasswords option
				boolean allowEmptyPasswords = true;
				String flag = getStringValue(options.get("allowEmptyPasswords"));
				if (flag != null)
				{
					allowEmptyPasswords = Boolean.valueOf(flag).booleanValue();
				}
				if (allowEmptyPasswords == false)
				{
					log.fine("Rejecting empty password due to allowEmptyPasswords");
					return false;
				}
			}

			try
			{
				// Validate the password by trying to create an initial context
				String username = login;
				createLdapInitContext(username, inputPassword);
				isValid = true;
			}
			catch (NamingException e)
			{
				log.fine("Failed to validate password");
				e.printStackTrace();
			}
		}
		return isValid;
	}

	/*protected String bindDNAuthentication(InitialLdapContext ctx, String user, Object credential, String baseDN,
			String baseFilter, String distinguishedNameAttribute) throws NamingException
	{
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		//constraints.setTimeLimit(searchTimeLimit);
		String attrList[] = { distinguishedNameAttribute };
		constraints.setReturningAttributes(attrList);

		NamingEnumeration results = null;

		Object[] filterArgs = { user };
		results = ctx.search(baseDN, baseFilter, filterArgs, constraints);
		if (results.hasMore() == false)
		{
			results.close();
			throw new NamingException("Search of baseDN(" + baseDN + ") found no matches");
		}

		SearchResult sr = (SearchResult) results.next();
		String name = sr.getName();
		String userDN = null;
		Attributes attrs = sr.getAttributes();
		if (attrs != null)
		{
			Attribute dn = attrs.get(distinguishedNameAttribute);
			if (dn != null)
			{
				userDN = (String) dn.get();
			}
		}
		if (userDN == null)
		{
			if (sr.isRelative() == true)
			{
				userDN = name + ("".equals(baseDN) ? "" : "," + baseDN);
			}
			else
			{
				throw new NamingException("Can't follow referal for authentication: " + name);
			}
		}

		results.close();
		results = null;
		// SECURITY-225: don't need to authenticate again
		if (isPasswordValidated)
		{
		   // Bind as the user dn to authenticate the user
		   InitialLdapContext userCtx = constructInitialLdapContext(userDN, credential);
		   userCtx.close();
		}

		return userDN;
	}*/

	private Properties createLdapInitContext(String username, Object credential) throws NamingException
	{
		Properties env = new Properties();
		// Map all option into the JNDI InitialLdapContext env
		Iterator iter = options.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry entry = (Entry) iter.next();
			env.put(entry.getKey(), entry.getValue());
		}

		// Set defaults for key values if they are missing
		String factoryName = env.getProperty(Context.INITIAL_CONTEXT_FACTORY);
		if (factoryName == null)
		{
			factoryName = "com.sun.jndi.ldap.LdapCtxFactory";
			env.setProperty(Context.INITIAL_CONTEXT_FACTORY, factoryName);
		}
		String authType = env.getProperty(Context.SECURITY_AUTHENTICATION);
		if (authType == null)
		{
			env.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
		}
		String protocol = env.getProperty(Context.SECURITY_PROTOCOL);
		String providerURL = (String) options.get(Context.PROVIDER_URL);
		if (providerURL == null)
		{
			providerURL = "ldap://localhost:" + ((protocol != null && protocol.equals("ssl")) ? "636" : "389");
		}

		String principalDNPrefix = getStringValue(options.get(PRINCIPAL_DN_PREFIX_OPT));
		if (principalDNPrefix == null)
		{
			principalDNPrefix = "";
		}
		String principalDNSuffix = getStringValue(options.get(PRINCIPAL_DN_SUFFIX_OPT));
		if (principalDNSuffix == null)
		{
			principalDNSuffix = "";
		}

		String bindDN = (String) options.get(BIND_DN);
		String bindCredential = (String) options.get(BIND_CREDENTIAL);

		String matchType = getStringValue(options.get(MATCH_ON_USER_DN_OPT));
		boolean matchOnUserDN = Boolean.valueOf(matchType).booleanValue();

		String baseFilter = getStringValue(options.get(BASE_FILTER_OPT));
		if (baseFilter == null)
		{
			baseFilter = "(sAMAccountName={0})";
		}

		String principal = principalDNPrefix + username + principalDNSuffix;
		env.setProperty(Context.PROVIDER_URL, providerURL);
		env.setProperty(Context.SECURITY_PRINCIPAL, "AGPCORP\\" + username);
		env.put(Context.SECURITY_CREDENTIALS, credential);

		///

		///

		log.fine("Logging into LDAP server, env=" + env);
		InitialLdapContext ctx = new InitialLdapContext(env, null);
		log.fine("Logged into LDAP server, " + ctx);
		/* If a userRolesCtxDNAttributeName was speocified, see if there is a
		 user specific roles DN. If there is not, the default rolesCtxDN will
		 be used.
		 */
		String rolesCtxDN = getStringValue(options.get(ROLES_CTX_DN_OPT));
		String userRolesCtxDNAttributeName = getStringValue(options.get(USER_ROLES_CTX_DN_ATTRIBUTE_ID_OPT));
		if (userRolesCtxDNAttributeName != null)
		{
			// Query the indicated attribute for the roles ctx DN to use
			String[] returnAttribute = { userRolesCtxDNAttributeName };
			try
			{
				Attributes result = ctx.getAttributes(principal, returnAttribute);
				if (result.get(userRolesCtxDNAttributeName) != null)
				{
					rolesCtxDN = result.get(userRolesCtxDNAttributeName).get().toString();
					log.fine("Found user roles context DN: " + rolesCtxDN);
				}
			}
			catch (NamingException e)
			{
				log.fine("Failed to query userRolesCtxDNAttributeName");
				e.printStackTrace();
			}
		}

		// Search for any roles associated with the user
		if (rolesCtxDN != null)
		{
			String uidAttrName = getStringValue(options.get(UID_ATTRIBUTE_ID_OPT));
			if (uidAttrName == null)
			{
				uidAttrName = "uid";
			}
			String roleAttrName = getStringValue(options.get(ROLE_ATTRIBUTE_ID_OPT));
			if (roleAttrName == null)
			{
				roleAttrName = "roles";
			}
			BasicAttributes matchAttrs = new BasicAttributes(true);
			if (matchOnUserDN == true)
			{
				matchAttrs.put(uidAttrName, principal);
			}
			else
			{
				matchAttrs.put(uidAttrName, username);
			}

			// Is user's role attribute a DN or the role name
			String roleAttributeIsDNOption = getStringValue(options.get(ROLE_ATTRIBUTE_IS_DN_OPT));
			boolean roleAttributeIsDN = Boolean.valueOf(roleAttributeIsDNOption).booleanValue();

			// If user's role attribute is a DN, what is the role's name attribute
			// Default to 'name' (Group name attribute in Active Directory)
			String roleNameAttributeID = getStringValue(options.get(ROLE_NAME_ATTRIBUTE_ID_OPT));
			if (roleNameAttributeID == null)
			{
				roleNameAttributeID = "name";
			}

			try
			{

				String baseDN = (String) options.get(BASE_CTX_DN);

				String distinguishedNameAttribute = (String) options.get(DISTINGUISHED_NAME_ATTRIBUTE_OPT);
				if (distinguishedNameAttribute == null)
				{
					distinguishedNameAttribute = "distinguishedName";
				}

				String[] returnAttributes = new String[] { "cn", distinguishedNameAttribute, roleAttrName };

				NamingEnumeration<SearchResult> answer = executeSearch(ctx, baseDN, username, baseFilter,
						returnAttributes);

				ArrayList<String> userRoles = new ArrayList<String>();

				while (answer.hasMore())
				{
					SearchResult sr = answer.next();
					Attributes attrs = sr.getAttributes();
					Attribute roles = attrs.get(roleAttrName);
					for (int r = 0; r < roles.size(); r++)
					{
						Object value = roles.get(r);
						String roleName = null;
						if (roleAttributeIsDN == true)
						{
							// Query the roleDN location for the value of roleNameAttributeID
							String roleDN = value.toString();
							String[] returnAttribute = { roleNameAttributeID };
							log.fine("Using roleDN: " + roleDN);
							try
							{
								Attributes result = ctx.getAttributes(roleDN, returnAttribute);
								if (result.get(roleNameAttributeID) != null)
								{
									roleName = result.get(roleNameAttributeID).get().toString();
								}
							}
							catch (NamingException e)
							{
								log.fine("Failed to query roleNameAttrName");
								e.printStackTrace();
							}
						}
						else
						{
							// The role attribute value is the role name
							roleName = value.toString();
						}

						if (roleName != null)
						{
							userRoles.add(roleName);

							/*try
							{
								Principal p = super.createIdentity(roleName);
								log.fine("Assign user to role " + roleName);
								userRoles.addMember(p);
							}
							catch (Exception e)
							{
								log.fine("Failed to create principal: " + roleName);
								e.printStackTrace();
							}*/
						}
					}
				}

				env.put("user.roles", userRoles);
			}
			catch (NamingException e)
			{
				log.fine("Failed to locate roles");
				e.printStackTrace();
			}
		}
		// Close the context to release the connection
		ctx.close();
		return env;
	}

	private NamingEnumeration<SearchResult> executeSearch(InitialLdapContext ctx, String searchBase, String username,
			String basefilter, String[] returnAttributes) throws NamingException
	{

		// Create the search controls
		SearchControls searchCtls = new SearchControls();

		// Specify the attributes to return
		//if (attributes != null) {
		searchCtls.setReturningAttributes(returnAttributes); //new String[] { "cn", distinguishedName, roleAttributeID });
		// }

		// Specify the search scope
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		// Search for objects using the filter
		NamingEnumeration<SearchResult> result = ctx.search(searchBase,
				MessageFormat.format(basefilter, new Object[] { username }), searchCtls);

		return result;
	}

	private String getStringValue(Object obj)
	{
		if (obj != null)
		{
			return (String) obj;
		}

		return null;
	}

}
