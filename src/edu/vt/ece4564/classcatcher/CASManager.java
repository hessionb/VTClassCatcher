package edu.vt.ece4564.classcatcher;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.protocol.HTTP;

import android.content.Context;
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
	
	private boolean login() throws LoginException {
		try {
			if(true) {

//				Log.d("ClassCatcher", "Got certificate");
//				System.setProperty("javax.net.ssl.trustStore", certificate_.getAbsolutePath());
				
				HttpsURLConnection.setFollowRedirects(false);
				HttpsURLConnection conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
				conn.connect();
				
				cookie_ = conn.getHeaderField("Set-Cookie");
				cookie_ = cookie_.substring(0,cookie_.indexOf(';')).trim();
				Log.d("ClassCatcher", "Cookie: " + cookie_);
				
				conn.disconnect();
				
				// Follow redirects
				HttpsURLConnection.setFollowRedirects(true);
				conn = (HttpsURLConnection) new URL(CAS_LOGIN_URL).openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setRequestProperty("User-Agent", USER_AGENTS);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Cookie", cookie_);
				conn.connect();
				
				Log.d("ClassCatcher", "Header: " + conn.getHeaderFields().toString());
				
				// Get POST method name fields
		        String output = "_eventId=submit" +
		        				"&submit=_submit" +
		        				"username=" + URLEncoder.encode("Test", HTTP.UTF_8) +
		        				"&password=" + URLEncoder.encode("Test", HTTP.UTF_8);
		        DataOutputStream dataout = new DataOutputStream(conn.getOutputStream());
		        dataout.writeBytes(output);
		        dataout.flush();
		        Log.d("ClassCatcher", "Response Code: " + conn.getResponseCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
}
