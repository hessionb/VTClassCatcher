package edu.vt.ece4564.classcatcher;

import android.app.Activity;
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

	private Button loginbutton_, runbutton_, cancelbutton_, logoutbutton_;
	private EditText pidinput_, passwordinput_, crninput_, yearinput_, crseinput_;
	private Spinner termspinner_, subjectspinner_;
	private TextView loginupdate_, classupdate_;
	private View loginview_, classview_;
	
	private HokieLogger hokieLogger_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		hokieLogger_ = new HokieLogger();
		
		// Get resources
		loginview_ = (View) findViewById(R.id.loginview);
		classview_ = (View) findViewById(R.id.classview);
		loginupdate_ = (TextView) findViewById(R.id.loginupdate);
		classupdate_ = (TextView) findViewById(R.id.classupdate);

		// Initialize Buttons
		initializeInputs();
		initializeSpinners();
		initializeButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Gets all of the EditTexts from R
	private void initializeInputs() {
		pidinput_ = (EditText) findViewById(R.id.pidinput);
		passwordinput_ = (EditText) findViewById(R.id.passwordinput);
		crninput_ = (EditText) findViewById(R.id.crninput);
		yearinput_ = (EditText) findViewById(R.id.yearinput);
		crseinput_ = (EditText) findViewById(R.id.crseinput);
	}

	// Gets Spinners from R and populates them
	private void initializeSpinners() {
		termspinner_ = (Spinner) findViewById(R.id.termspinner);
		subjectspinner_ = (Spinner) findViewById(R.id.subjectspinner);

		// Add items to term spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.term_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		termspinner_.setAdapter(adapter);

		// Add items to subject spinner
		adapter = ArrayAdapter.createFromResource(this, R.array.subj_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		subjectspinner_.setAdapter(adapter);
	}

	// Gets Buttons from R and sets their click listeners
	private void initializeButtons() {
		loginbutton_ = (Button) findViewById(R.id.submit);
		runbutton_ = (Button) findViewById(R.id.runbutton);
		cancelbutton_ = (Button) findViewById(R.id.cancelbutton);
		logoutbutton_ = (Button) findViewById(R.id.logoutbutton);

		// Logs in to HokieSpa
		loginbutton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				classview_.setVisibility(View.VISIBLE);
				loginview_.setVisibility(View.GONE);
			}
		});

		// Runs class check
		runbutton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				classupdate_.setText("Running...");
				hokieLogger_.setCredentials("test","test2");
				hokieLogger_.execute();
			}
		});

		// Cancels class check
		cancelbutton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				classupdate_.setText("Cancelled");
				hokieLogger_.cancel(true);
			}
		});

		// Logs out
		logoutbutton_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loginview_.setVisibility(View.VISIBLE);
				classview_.setVisibility(View.GONE);
			}
		});
	}
}
