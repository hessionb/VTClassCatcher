package edu.vt.ece4564.vtclasscatcher;

import android.graphics.Color;
import android.os.AsyncTask;

/***
 * LoginTask
 * 
 * This task decides whether to start or end a session on CAS
 * 
 * @author Brian Hession
 *
 */

public class LoginTask extends AsyncTask<String, Void, String> {
	
	private UIManager ui_;
	private CASManager user_;
	
	public LoginTask(UIManager ui, CASManager user) {
		ui_ = ui;
		user_ = user;
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			if(params[0].equals("Login")) {
				// Retreive credentials and login
				user_.setCredentials(ui_.getLoginInputText(), ui_.getPasswordInputText());
				if(user_.startSession()) return "Login Succeeded";
				else return "Login Failed";
			}
			else {
				// Logout
				if(user_.endSession()) return "Logout Succeeded";
				else return "Logout Failed";
			}
		} catch (LoginException e) {
			return "Login Failed";
		}
	}
	
	protected void onPostExecute(String result) {
		if(result.equals("Login Succeeded")) ui_.switchViews();
		else if(result.equals("Login Failed")) {
			ui_.setLoginTextColor(Color.RED);
			ui_.setLoginText("Wrong username or password");
		}
		else {
			ui_.clearPassword();
			ui_.switchViews();
		}
	}
}
