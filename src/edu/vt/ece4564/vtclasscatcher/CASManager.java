package edu.vt.ece4564.vtclasscatcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.protocol.HTTP;

import android.util.Log;

public class CASManager {
    
	// Static strings
    private static final String USER_AGENTS				= "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) Gecko/20100101 Firefox/11.0";
    private static final String CAS_LOGIN_URL			= "https://auth.vt.edu/login";
    private static final String CAS_LOGOUT_URL			= "https://auth.vt.edu/logout";
    private static final String RECOVERY_OPTIONS_STRING = "You have not updated account recovery options in the past";
    private static final String CERTIFICATE_PATH		= "Resources/auth.vt.edu.jks";

    // Member variables
    private char[] username_;
    private char[] password_;
    private boolean validCredentials_;
    private File certificate_;
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
	
	private void clearCredentials() {
		for(int i = 0; i < username_.length; ++i) username_[i] = 0;
		for(int i = 0; i < password_.length; ++i) password_[i] = 0;
	}
	
	private boolean getCertificate() {
		try {
			HttpsURLConnection conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
			conn.connect();
			Certificate[] certs = conn.getServerCertificates();
			if (certs.length > 0) {
				certificate_ = new File(CERTIFICATE_PATH);
				OutputStream out = new FileOutputStream(certificate_); // Throws exception here. No permission?
				out.write(certs[0].getEncoded());
				out.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private String getCookie(HttpsURLConnection conn) {
		String ret = conn.getHeaderField("Set-Cookie");
		ret = ret.substring(0,ret.indexOf(';')).trim();
		return ret;
	}
	
	private String getHtml(HttpsURLConnection conn) throws IOException {
		// Build the html string
		StringBuilder html = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String buffer;
		while((buffer = in.readLine()) != null)
			html.append(buffer);
		return html.toString();
	}
	
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
		divs.put("username", new String(username_));
		divs.put("password", new String(password_));
		Log.d("ClassCatcher", "Divs: " + divs.toString());

		return divs;
	}
	
	private boolean login() throws LoginException {
		try {
			if(true) {
				int rescode = 302;
				String url = CAS_LOGIN_URL;
				Map<String, String> divs;

				// Connect to get cookie and hidden fields
				while(rescode == 302 || rescode == 301) {

	//				Log.d("ClassCatcher", "Got certificate");
	//				System.setProperty("javax.net.ssl.trustStore", certificate_.getAbsolutePath());
					
					// Connect to url
					HttpsURLConnection.setFollowRedirects(false);
					HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
					if(cookie_ != null) conn.setRequestProperty("Cookie", cookie_);
					conn.connect();
					Log.d("ClassCatcher", "Header: " + conn.getHeaderFields().toString());
					
					// Get response code
					rescode = conn.getResponseCode();
					Log.d("ClassCatcher", "Response Code: " + rescode);
					
					// Get cookie and new location
					if(rescode == 301 || rescode == 302) {
						url = conn.getHeaderField("Location");
						cookie_ = getCookie(conn);
						Log.d("ClassCatcher", "Cookie: " + cookie_);
					} 

					// Get fields
					else {
						Log.d("ClassCatcher", "Response: " + getHtml(conn));
						divs = getDivs(getHtml(conn));
					}
					
					conn.disconnect();
				}
				
				/*
				// Follow redirects
				HttpsURLConnection.setFollowRedirects(true);
				conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
				conn.setRequestMethod("GET");
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setRequestProperty("User-Agent", USER_AGENTS);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Cookie", cookie_);
				conn.connect();
				
				// Get POST method name fields
		        String output = "_eventId=submit" +
		        				"&submit=_submit" +
		        				"username=" + URLEncoder.encode("Test", HTTP.UTF_8) +
		        				"&password=" + URLEncoder.encode("Test", HTTP.UTF_8);
		        DataOutputStream dataout = new DataOutputStream(conn.getOutputStream());
		        dataout.writeBytes(output);
		        dataout.flush();
		        Log.d("ClassCatcher", "Response Code: " + conn.getResponseCode());
		        */
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
}
