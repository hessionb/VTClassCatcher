package edu.vt.ece4564.vtclasscatcher;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

	private UIManager ui_;
	private CASManager user_;
	private Timer timer_;
	private boolean running_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);

		// Create managers
		ui_ = new UIManager();
		user_ = new CASManager();
		
		// Get resources
		ui_.setViews((View) findViewById(R.id.loginview), 
					 (View) findViewById(R.id.classview),
					 (View) findViewById(R.id.enterclassview));
		ui_.setTextViews((TextView) findViewById(R.id.loginupdate), 
						 (TextView) findViewById(R.id.classupdate));
		ui_.setEditTexts((EditText) findViewById(R.id.pidinput),
						 (EditText) findViewById(R.id.passwordinput),
						 (EditText) findViewById(R.id.crninput),
						 (EditText) findViewById(R.id.yearinput));
		initializeSpinners();
		initializeButtons();
		
		running_ = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Gets Spinners from R and populates them
	private void initializeSpinners() {
		Spinner termSpinner = (Spinner) findViewById(R.id.termspinner);

		// Add items to term spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.term_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		termSpinner.setAdapter(adapter);
		
		// Set Spinners
		ui_.setSpinners(termSpinner);
	}

	// Gets Buttons from R and sets their click listeners
	private void initializeButtons() {
		ui_.setButtons((Button) findViewById(R.id.submit),
					   (Button) findViewById(R.id.runbutton),
					   (Button) findViewById(R.id.cancelbutton),
					   (Button) findViewById(R.id.logoutbutton));
		
		// Logs in to HokieSpa
		ui_.setLoginButtonListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ui_.setLoginTextColor(Color.BLACK);
				ui_.setLoginText("Logging in...");
				ui_.setClassTextColor(Color.BLACK);
				ui_.setClassText("");
				new LoginTask(ui_,user_).execute("Login");
			}
		});

		// Runs class check
		ui_.setRunButtonListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ui_.setClassInfoView();
				ui_.toggleCancelButton();
				ui_.toggleRunButton();
				timer_ = new Timer();
				// Switch for demo
				timer_.schedule(new MyTimer(), 0, 5*60*1000); // 5 minutes
				//timer_.schedule(new MyTimer(), 0, 30*1000); // 30 seconds
				running_ = true;
			}
		});

		// Cancels class check
		ui_.setCancelButtonListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ui_.setEnterClassView();
				timer_.cancel();
				timer_.purge();
				ui_.toggleCancelButton();
				ui_.toggleRunButton();
				running_ = false;
			}
		});

		// Logs out
		ui_.setLogoutButtonListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ui_.setLoginTextColor(Color.BLACK);
				ui_.setLoginText("");
				if(running_) {
					timer_.cancel();
					timer_.purge();
				}
				new LoginTask(ui_,user_).execute("Logout");
			}
		});
	}
	
	class MyTimer extends TimerTask {

		@Override
		public void run() {
			MainActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					ui_.setClassTextColor(Color.BLACK);
					ui_.setClassText("Getting data...");
					new NetworkTask(ui_, user_).execute("");
				}
			});
		}
	}
}
