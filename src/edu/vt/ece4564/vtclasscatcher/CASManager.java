package edu.vt.ece4564.vtclasscatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.protocol.HTTP;


import android.util.Log;

/***
 * CASManager
 * 
 * @author Brian Hession
 *
 * Class that manages a session on the Virginia Tech CAS. This is not 100% secure.
 * It is vulnerable to DNS hacks. Use at your own risk.
 */

public class CASManager {
    
	// Static strings
    private static final String CAS_LOGIN_URL			= "https://auth.vt.edu/login";
    private static final String CAS_LOGOUT_URL			= "https://auth.vt.edu/logout";
    private static final String USER_AGENTS				= "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) Gecko/20100101 Firefox/11.0";
    
    // Member variables
    private char[] username_;
    private char[] password_;
    private boolean validCredentials_;
    private String cookie_;
    
	public CASManager(char[] username, char[] password) throws LoginException {
		username_ = new char[username.length];
		password_ = new char[password.length];
		validCredentials_ = false;
		
		// Copy over credentials
		for(int i = 0; i < username.length; ++i) username_[i] = username[i];
		for(int i = 0; i < password.length; ++i) password_[i] = password[i];
		
		// Clear temporary credentials
		for(int i = 0; i < username.length; ++i) username[i] = 0;
		for(int i = 0; i < password.length; ++i) password[i] = 0;
		
		try {
			validCredentials_ = login();
		} catch (LoginException e) {
			clearCredentials();
			throw new LoginException();
		}
	}
	
	

	/***
	 * getCookie()
	 * 
	 * @return the cookie for the session
	 */
	public String getCookie() {
		return cookie_;
	}
	
	
	
	/***
	 * validCredentials()
	 * 
	 * @return whether credentials are valid
	 */
	public boolean validCredentials() {
		return validCredentials_;
	}
	
	

	/***
	 * endSession()
	 * 
	 * Ends a session on the CAS and clears the credentials from memory
	 * 
	 * @return whether successful or not
	 */
	public boolean endSession() {
		boolean ret = logout();
		clearCredentials();
		return ret;
	}
	
	

	/***
	 * refreshSession()
	 * 
	 * Refreshes the session on CAS
	 * 
	 * @return whether successful or not
	 * @throws LoginException
	 */
	public boolean refreshSession() throws LoginException {
		boolean ret = false;
		ret = logout();
		ret = login();
		return ret;
	}
	

	
	/***
	 * login()
	 * 
	 * Starts a session with CAS
	 * 
	 * @return whether the login was successful
	 * @throws LoginException
	 */
	private boolean login() throws LoginException {
		try {
			String url;
			HttpsURLConnection conn;
			
			// Connect to URL and grab cookie
			HttpsURLConnection.setFollowRedirects(false);
			conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
			conn.connect();
			cookie_ = getCookie(conn);
			
			// If redirected
			if(conn.getResponseCode() == 302 || conn.getResponseCode() == 301) {
				conn.disconnect();
				url = conn.getHeaderField("Location");
				HttpsURLConnection.setFollowRedirects(true);
				conn = (HttpsURLConnection) new URL(url).openConnection();
				conn.setRequestProperty("Cookie", cookie_);
				conn.connect();
			}
			
			// Grab divs
			Map<String, String> divs = getDivs(getHtml(conn));
			
			// Disconnect
			conn.disconnect();
			
			// Print stuff to LogCat
			Log.d("ClassCatcher", "Cookie: " + cookie_);
			Log.d("ClassCatcher", "Divs: " + divs.toString());
			
			// Encode data
			String data = "username=" + URLEncoder.encode(divs.get("username"),HTTP.UTF_8) + 
						  "&password=" + URLEncoder.encode(divs.get("password"),HTTP.UTF_8) +
						  "&lt=" + divs.get("lt") +
						  "&execution=" + divs.get("execution") +
						  "&_eventId=" + divs.get("_eventId") +
						  "&submit=" + divs.get("submit");
			url = CAS_LOGIN_URL + "?" + data;
			
			// Send login
			HttpsURLConnection.setFollowRedirects(true);
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Cookie", cookie_);
			conn.connect();
			
			// Validate Login
			if(!validateLogin(getHtml(conn))) throw new LoginException();
			
			// Get cookie
			cookie_ = getCookie(conn);
							
			// Print login attempt
			Log.d("ClassCatcher", "Cookie: " + getCookie(conn));
			
			return true;
		} catch (LoginException e) {
			throw new LoginException();
		} catch (IOException e) {
			e.printStackTrace(); 
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	
	
	/***
	 * logout()
	 * 
	 * Ends a session on the CAS
	 * 
	 * @return whether successful or not
	 */
	private boolean logout() {
		try {
			HttpsURLConnection conn = (HttpsURLConnection) new URL(CAS_LOGOUT_URL).openConnection();
			conn.setRequestProperty("Cookie", cookie_);
			conn.connect();
			if(validateLogout(getHtml(conn))) return true;
			else return false;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	/***
	 * clearCredentials()
	 * 
	 * Clears the credentials in memory
	 */
	private void clearCredentials() {
		for(int i = 0; i < username_.length; ++i) username_[i] = 0;
		for(int i = 0; i < password_.length; ++i) password_[i] = 0;
	}
	
	
	
	/***
	 * getCookie()
	 * 
	 * Extracts the cookie
	 * 
	 * @param conn - The HTTP Connection object
	 * @return the cookie in a string
	 */
	private String getCookie(HttpsURLConnection conn) {
		String ret = conn.getHeaderField("Set-Cookie");
		if(ret != null)
			ret = ret.substring(0,ret.indexOf(';')).trim();
		return ret;
	}
	
	
	
	/***
	 * getHtml()
	 * 
	 * Builds the HTML string
	 * 
	 * @param conn - The HTTP Connection object
	 * @return the HTML in a string 
	 * @throws IOException
	 */
	private String getHtml(HttpsURLConnection conn) throws IOException {
		// Build the html string
		StringBuilder html = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String buffer;
		while((buffer = in.readLine()) != null)
			html.append(buffer);
		return html.toString();
	}

	
	
	/***
	 * getDivs()
	 * 
	 * Parses the HTML and grabs the lt and execution value.
	 * 
	 * @param html - String containing all of the HTML
	 * @return HashMap of the data to send
	 */
	private Map<String, String> getDivs(String html) {
		Map<String, String> divs = new HashMap<String, String>();
		
		// Take the login-form out of the html
		String form = html.substring(html.indexOf("<form id=\"login-form\""));
		form = form.substring(0,form.indexOf("</form>") + "</form>".length());
		
		// Find the lt and execution values
		String ltValue = form.substring(form.indexOf("<input type=\"hidden\" name=\"lt\" value=\"") + 
				"<input type=\"hidden\" name=\"lt\" value=\"".length());
		ltValue = ltValue.substring(0,ltValue.indexOf("\" />"));
		String executionValue = form.substring(form.indexOf("<input type=\"hidden\" name=\"execution\" value=\"") +
				"<input type=\"hidden\" name=\"execution\" value=\"".length());
		executionValue = executionValue.substring(0,executionValue.indexOf("\" />"));
		
		// Populate the divs map
		divs.put("lt", ltValue);
		divs.put("execution", executionValue);
		divs.put("_eventId", "submit");
		divs.put("submit", "_submit");
		divs.put("username", String.copyValueOf(username_));
		divs.put("password", String.copyValueOf(password_));

		return divs;
	}

	
	
	/***
	 * validateLogin()
	 * 
	 * Ensures the the login was successful.
	 * 
	 * @param html - String containing the HTML response
	 * @return whether the login was successful
	 */
	private boolean validateLogin(String html) {
		return (html.indexOf("Login Successful") >= 0);
	}
	
	
	
	/***
	 * validateLogout()
	 * 
	 * Validates that the logout was successful
	 * 
	 * @param html - String containing the response HTML
	 * @return whether successful or not
	 */
	private boolean validateLogout(String html) {
		return (html.indexOf("Logout successful") >= 0);
	}
}
