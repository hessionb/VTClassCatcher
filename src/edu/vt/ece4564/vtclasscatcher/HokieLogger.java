package edu.vt.ece4564.vtclasscatcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Log;

public class HokieLogger extends AsyncTask<String, Void, String> {
	
	// Static webpages
	private static final String CENTRAL_LOGIN_URL = "https://banweb.banner.vt.edu/ssomanager_prod/c/SSB";
	private static final String TIMETABLE_CLASSES_URL = "https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_DispRequest";
	private static final String CENTRAL_LOGOUT_URL = "https://auth.vt.edu/logout";
	private static final String ROOT_URL = "https://auth.vt.edu";
	
	// Member variables
	private String username_ = "";
	private String password_ = "";
	private String cookie_ = "";
	
	
	
	/***
	 * setCredentials()
	 * 
	 * Sets the username and password.
	 */
	public void setCredentials(String username, String password) {
		username_ = username;
		password_ = password;
	}
	
	
	
	/***
	 * getHeader()
	 * 
	 * Used for debugging
	 */
	public String getHeader(HttpsURLConnection urlc) {
		return urlc.getHeaderFields().toString();
	}
	
	
	
	/***
	 * findLoginURL()
	 * 
	 * Finds and returns the target link of the login form.
	 */
	public String findLoginURL(HttpsURLConnection urlc) throws Exception {
		String res = null;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
	    while((res = reader.readLine()) != null) {
	    	if(res.indexOf("<form id=\"login-form\" action=\"") >= 0) {
	    		res = res.substring(res.indexOf("<form id=\"login-form\" action=\"") + "<form id=\"login-form\" action=\"".length());
	    		res = res.substring(0,res.indexOf("\" method=\"post\">"));
	    		break;
	    	}
	    	res = null;
	    }
	    return res;
	}
	
	
	
	/***
	 * checkLogin()
	 * 
	 * Checks to make sure the login was successful.
	 */
	public String checkLogin(HttpsURLConnection urlc) throws IOException {
		String res = null;
	    BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));        
	    char[] buffer = new char[5000];
	    reader.read(buffer);
	    res = new String(buffer);
	    return res;
	}

	
	
	/***
	 * login()
	 * 
	 * Logs in to the Virginia Tech central login.
	 */
	public String login() {

		boolean loggedIn = false;
		HttpsURLConnection urlc;
        String urlLocation = CENTRAL_LOGIN_URL;
        cookie_ = "";

		try {			
	        while(true) {

	        	// Go to central login
		        HttpsURLConnection.setFollowRedirects(false);
		        urlc = (HttpsURLConnection) new URL(urlLocation).openConnection();
		        urlc.setRequestMethod("GET");
		        urlc.setRequestProperty("Cookie", cookie_);
				urlc.addRequestProperty("User-Agent", "Mozilla");
		        urlc.connect();
		        Log.d("ClassCatcher", "Response Code: " + urlc.getResponseCode());
		        
		        // Break if no redirect
		        if(urlc.getResponseCode() != 301 && urlc.getResponseCode() != 302) break;

		        // Get cookie
		        if((cookie_ = urlc.getHeaderField("Set-Cookie")) == null) throw new IOException("no cookie");
		        cookie_ = cookie_.substring(0,cookie_.indexOf(";")).trim();
		        
		        // Get Redirect
		        urlLocation = urlc.getHeaderField("Location");
		        Log.d("ClassCatcher", "Redirect: " + urlLocation);		        
	        }
	        
	        // Find login link
	        String urlLogin = findLoginURL(urlc);
	        
	        // End connection
	        urlc.disconnect();

	        // Login
	        HttpsURLConnection.setFollowRedirects(false);
	        urlc = (HttpsURLConnection) new URL(ROOT_URL + urlLogin).openConnection();
	        urlc.setRequestMethod("POST");
	        urlc.setDoOutput(true);
	        urlc.setDoInput(true);
	        urlc.setUseCaches(false);
	        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        urlc.setRequestProperty("Cookie", cookie_);
	        urlc.setRequestProperty("User-Agent", "Mozilla");
	        urlc.connect();
	        
	        // Get POST method name fields
	        String output = "username=" + URLEncoder.encode(username_, HTTP.UTF_8) +
	        				"&password=" + URLEncoder.encode(password_, HTTP.UTF_8) +
	        				"&submit=true";
	        DataOutputStream dataout = new DataOutputStream(urlc.getOutputStream());
	        dataout.writeBytes(output);
	        dataout.flush();
	        Log.d("ClassCatcher", "Response Code: " + urlc.getResponseCode());

	        // Get cookie
	        Log.d("ClassCatcher", "Header: " + getHeader(urlc));
	        if((cookie_ = urlc.getHeaderField("Set-Cookie")) == null) throw new IOException("no cookie");
	        cookie_ = cookie_.substring(0,cookie_.indexOf(";")).trim();
	        
	        // Check if logged in successfully
	        Log.d("ClassCatcher", "Response: " + checkLogin(urlc));

	        // Disconnect
	        urlc.disconnect();
	    }
		catch (ProtocolException e) {
	        Log.d("ClassCatcher", "Protocol Error: " + e.getMessage());
	        e.printStackTrace();
	    }
		catch (IOException e) {
	        Log.d("ClassCatcher", "Input/Output Error: " + e.getMessage());
	        e.printStackTrace();
	    }
		catch (Exception e) {
	        Log.d("ClassCatcher", "Exception: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return "Successful";
	}

	
	
	@Override
	protected String doInBackground(String... param) {
		try {
			CASManager user = new CASManager("test".toCharArray(),"test".toCharArray());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
