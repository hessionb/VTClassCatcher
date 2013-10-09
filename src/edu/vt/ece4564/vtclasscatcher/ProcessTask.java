package edu.vt.ece4564.vtclasscatcher;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

/***
 * ProcessTask
 * 
 * This is a task that processes the html returned from the network task.
 * 
 * @author Brian
 *
 */

public class ProcessTask extends AsyncTask<String, Void, Map<String, String>> {

	private UIManager ui_;
	
	public ProcessTask(UIManager ui) {
		ui_ = ui;
	}
	
	@Override
	protected Map<String, String> doInBackground(String... params) {
		String html = params[0];
		return parseHtml(html);
	}
	
	protected void onPostExecute(Map<String, String> result) {
		if(result != null) {
			ui_.setClassTextColor(Color.BLACK);
			ui_.setClassText("Seats available: " + result.get("Seats"));
		}
		else {
			ui_.setClassTextColor(Color.RED);
			ui_.setClassText("Error: Exception was thrown");
		}
	}
	
	/***
	 * parseHtml()
	 * 
	 * Parses the HTML
	 * 
	 * @param html of the course info
	 * @return a hashmap of all the useful fields for each course on the time table on
	 * HokieSpa
	 */
	private Map<String, String> parseHtml(String html) {
		Map<String, String> info = new HashMap<String, String>();
		String description, days, beginTime, endTime, location, exam, instructor, type, status, seats, capacity;
		
		try {
			// Parse description
			description = html.substring(html.indexOf("<TD COLSPAN=\"1\" CLASS=\"pldefault\">") + "<TD COLSPAN=\"1\" CLASS=\"pldefault\">".length());
			description = description.substring(0,description.indexOf("</TD")).trim();
	
			// Parse days, beginTime, endTime, location, and exam
			String temp = html.substring(html.indexOf("<TD CLASS=\"mpdefault\""));
			days = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			beginTime = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			endTime = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			location = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			exam = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			
			// Parse instructor, type, status, seats, capacity
			temp = html.substring(html.indexOf("<TD CLASS=\"mpdefault\"style=text-align:center;>"));
			instructor = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			type = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			status = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			seats = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
			if(seats.contains("&nbsp;"))
				seats = seats.substring(seats.lastIndexOf("&nbsp;") + "&nbsp;".length(), seats.lastIndexOf('<')).trim();
			temp = temp.substring(temp.indexOf("</TD>") + "</TD>".length());
			capacity = temp.substring(temp.indexOf('>') + 1,temp.indexOf("</TD>")).trim();
	
			// Populate info
			info.put("Description", description);
			info.put("Days", days);
			info.put("BeginTime", beginTime);
			info.put("EndTime", endTime);
			info.put("Location", location);
			info.put("Exam", exam);
			info.put("Instructor", instructor);
			info.put("Type", type);
			info.put("Status", status);
			info.put("Seats", seats);
			info.put("Capacity", capacity);
			
			return info;
		}
		catch (Exception e) {
			Log.e("VTClassCatcher", "Parse class exception: " + e.getMessage());
		}
		return null;
	}
}
