package edu.vt.ece4564.vtclasscatcher;

import android.os.AsyncTask;

public class LoginTask extends AsyncTask<User, Void, Boolean> {
	
	@Override
	protected Boolean doInBackground(User... params) {
		User user = params[0];
		
		
		
		return false;
	}
	
	protected void onPostExecute(String result) {
		
	}
}
