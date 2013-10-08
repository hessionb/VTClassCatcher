package edu.vt.ece4564.vtclasscatcher;

import android.os.AsyncTask;

public class NetworkTask extends AsyncTask<User, Void, String> {

	User user_;
	
	@Override
	protected String doInBackground(User... param) {
		user_ = param[0];
		return null;
	}
	
	protected void onPostExecute(String result) {
		
	}
}
