package edu.vt.ece4564.vtclasscatcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.protocol.HTTP;


import android.text.TextUtils;
import android.util.Log;

/***
 * CASManager
 * 
 * @author Brian Hession
 *
 * Class that manages a session on the Virginia Tech CAS. 
 * 
 * This class is incomplete for the time being and it is not 100% secure.
 * Use at your own risk.
 * 
 */

public class CASManager {
    
	// LogCat info
    private static final String LOGCAT_TAG					= "VTClassCatcher";
    
	// Static strings
    private static final String CAS_LOGIN_URL			= "https://auth.vt.edu/login";
    private static final String CAS_LOGOUT_URL			= "https://auth.vt.edu/logout";
    private static final String USER_AGENTS				= "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) Gecko/20100101 Firefox/11.0";
    private static final String HOKIE_SPA_URL			= "https://banweb.banner.vt.edu/ssomanager_prod/c/SSB";
    
    // Member variables
    private char[] username_;
    private char[] password_;
    private boolean validCredentials_;
    private Map<String, String> cookies_;
    
	public CASManager() {
		validCredentials_ = false;
		cookies_ = new HashMap<String, String>();
	}
	
	
	
	/***
	 * setCredentials()
	 * 
	 * Sets the username and password
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredentials(char[] username, char[] password) {
		username_ = new char[username.length];
		password_ = new char[password.length];
		
		// Copy over credentials
		for(int i = 0; i < username.length; ++i) username_[i] = username[i];
		for(int i = 0; i < password.length; ++i) password_[i] = password[i];
		
		// Clear temporary credentials
		for(int i = 0; i < username.length; ++i) username[i] = 0;
		for(int i = 0; i < password.length; ++i) password[i] = 0;
	}
	
	

	/***
	 * getCookie()
	 * 
	 * @return the cookie for the session
	 */
	public Map<String, String> getCookies() {
		return cookies_;
	}
	

	
	/***
	 * updateCookes()
	 * 
	 * Updates the SESSID cookie which changes per request
	 * 
	 * @param SESSID
	 */
	public void updateCookies(String SESSID) {
		cookies_.put("SESSID",SESSID);
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
	 * startSession()
	 * 
	 * Starts a session on CAS
	 * 
	 * @return whether successful or not
	 * @throws LoginException
	 */
	public boolean startSession() throws LoginException {
		return (validCredentials_ = login());
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
		cookies_.clear();
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
	 * Starts a session with CAS.
	 * 
	 * ***NOTE*** I was running out of time, so I hard-coded each redirect to send the 
	 * proper cookie. I plan fixing this later in my spare time.
	 * 
	 * @return whether the login was successful
	 * @throws LoginException
	 */
	private boolean login() throws LoginException {
		try {
			String url;
			HttpsURLConnection conn;
			ArrayList<String> authCookies;
			ArrayList<String> hokiespaCookies;
			
			// Connect to URL and grab cookie
			conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			authCookies = getCookies(conn);
			url = conn.getHeaderField("Location");
			conn.disconnect();
			
			// Handle redirect to extract Divs
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestProperty("Cookie", TextUtils.join(",", authCookies));
			conn.connect();
			Map<String, String> divs = getDivs(getHtml(conn));
			conn.disconnect();
			
			/***
			 * ***NOTE*** username and password are being stored in a String. This is
			 * not secure. I plan on fixing this later.
			 */
			
			// Encode data and log in
			String data = "username=" + URLEncoder.encode(divs.get("username"),HTTP.UTF_8) +
						  "&password=" + URLEncoder.encode(divs.get("password"),HTTP.UTF_8) +
						  "&lt=" + divs.get("lt") +
						  "&execution=" + divs.get("execution") +
						  "&_eventId=" + divs.get("_eventId") +
						  "&submit=" + divs.get("submit");
			conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Cookie", TextUtils.join(",", authCookies));
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(data); // Send POST login data
			out.flush();
			out.close();
			if(!validateLogin(getHtml(conn))) throw new LoginException(); // Check login
			authCookies.addAll(getCookies(conn)); // Get the cookies
			conn.disconnect();

			// Go to HokieSpa
			conn = (HttpsURLConnection) new URL(HOKIE_SPA_URL).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.connect();
			hokiespaCookies = getCookies(conn);
			url = conn.getHeaderField("Location");
			conn.disconnect();

			// Send CASTGC cookie
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.setRequestProperty("Cookie", authCookies.get(0) + "," + authCookies.get(2));
			conn.connect();
			url = conn.getHeaderField("Location");		
			conn.disconnect();

			// Send j2eeroute cookie
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.setRequestProperty("Cookie", TextUtils.join(",", hokiespaCookies));
			conn.connect();
			hokiespaCookies = getCookies(conn);
			url = conn.getHeaderField("Location");
			conn.disconnect();

			// Send IDMSESSID cookie
			conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENTS);
			conn.setRequestProperty("Cookie", hokiespaCookies.get(1));
			conn.connect();
			hokiespaCookies.addAll(getCookies(conn));
			conn.disconnect();

			// Add important cookies to map
			cookies_.put("CASTGC",authCookies.get(0));
			cookies_.put("IDMSESSID",hokiespaCookies.get(1));
			cookies_.put("SESSID",hokiespaCookies.get(3));
			
			return true;
		} catch (IOException e) {
			Log.e(LOGCAT_TAG, "Login: IOException error: " + e.getMessage());
		} catch (Exception e) {
			Log.e(LOGCAT_TAG, "Login: Exception error: " + e.getMessage());
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
			// Send CASTGC and IDMSESSID cookies
			HttpsURLConnection conn = (HttpsURLConnection) new URL(CAS_LOGOUT_URL).openConnection();
			conn.setRequestProperty("Cookie", cookies_.get("CASTGC"));
			conn.setRequestProperty("Cookie", cookies_.get("IDMSESSID"));
			conn.connect();
			if(validateLogout(getHtml(conn))) return true;
			else return false;
		} catch (IOException e) {
			Log.e(LOGCAT_TAG, "Logout: IOException error");
		} catch (Exception e) {
			Log.e(LOGCAT_TAG, "Logout: Exception error");
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
	 * getCookies()
	 * 
	 * Extracts the cookie(s)
	 * 
	 * @param conn - The HTTP Connection object
	 * @return the cookies in an ArrayList
	 */
	private ArrayList<String> getCookies(HttpsURLConnection conn) {
		ArrayList<String> ret = new ArrayList<String>();
		for(String value : conn.getHeaderFields().get("Set-Cookie")) {
			value = value.substring(0,value.indexOf(';'));
			ret.add(value);
		}
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
	private Map<String, String> getDivs(String html) throws Exception {
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
		return (html.indexOf("Login Succeeded") >= 0);
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
		return (html.indexOf("Logout Succeeded") >= 0);
	}
}
