package edu.vt.ece4564.vtclasscatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

/***
 * NetworkTask
 * 
 * This class handles the network lookup of a specific course
 * 
 * @author Brian Hession
 *
 */

public class NetworkTask extends AsyncTask<String, Void, String> {
	
	private static final String TIMETABLE_URL = "https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_ProcComments";

	private UIManager ui_;
	private CASManager user_;
	
	public NetworkTask(UIManager ui, CASManager user) {
		ui_ = ui;
		user_ = user;
	}
	
	@Override
	protected String doInBackground(String... param) {
		try {			
			// Construct URL
			String url = TIMETABLE_URL + 
						 "?CRN=" + ui_.getCRNInputText() +
						 "&TERM=" + ui_.getTermSelection() +
						 "&YEAR=" + ui_.getYearInputText() +
						 "&SUBJ=&CRSE=&history=N";
			HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Cookie", user_.getCookies().get("IDMSESSID")); 
			conn.setRequestProperty("Cookie", user_.getCookies().get("SESSID")); 
			conn.connect();
			String html = getHtml(conn); // Build HTML String (It's not very big)
			conn.disconnect();
			
			return html;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("VTClassCatcher", "Class Lookup error");
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(result != null) new ProcessTask(ui_).execute(result);
		else {
			ui_.setClassTextColor(Color.RED);
			ui_.setClassText("Error connecting");
		}
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
}
