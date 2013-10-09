package edu.vt.ece4564.vtclasscatcher;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/***
 * UIManager
 * 
 * Useful class that controls the UI for me. This allows me to pass only
 * this class as a reference rather than passing each individual Widget
 * in the XMLs.
 * 
 * Must call every set* function to avoid null errors.
 * 
 * @author Brian Hession
 *
 */

public class UIManager {

	private Button loginButton_, runButton_, cancelButton_, logoutButton_;
	private EditText pidInput_, passwordInput_, crnInput_, yearInput_;
	private Spinner termSpinner_;
	private TextView loginUpdate_, classUpdate_;
	private View loginView_, classView_;
	
	public void setButtons(Button loginButton, Button runButton, Button cancelButton, Button logoutButton) {
		loginButton_ = loginButton;
		runButton_ = runButton;
		cancelButton_ = cancelButton;
		logoutButton_ = logoutButton;
	}
	
	public void setLoginButtonListener(OnClickListener l) {
		loginButton_.setOnClickListener(l);
	}
	
	public void setRunButtonListener(OnClickListener l) {
		runButton_.setOnClickListener(l);
	}
	
	public void setCancelButtonListener(OnClickListener l) {
		cancelButton_.setOnClickListener(l);
	}
	
	public void setLogoutButtonListener(OnClickListener l) {
		logoutButton_.setOnClickListener(l);
	}
	
	public void setEditTexts(EditText pidInput, EditText passwordInput, EditText crnInput, EditText yearInput) {
		pidInput_ = pidInput;
		passwordInput_ = passwordInput;
		crnInput_ = crnInput;
		yearInput_ = yearInput;
	}
	
	public void setSpinners(Spinner termSpinner) {
		termSpinner_ = termSpinner;
	}
	
	public void setTextViews(TextView loginUpdate, TextView classUpdate) {
		loginUpdate_ = loginUpdate;
		classUpdate_ = classUpdate;
	}
	
	public void setViews(View loginView, View classView) {
		loginView_ = loginView;
		classView_ = classView;
	}
	
	public void switchViews() {
		if(loginView_.getVisibility() == View.VISIBLE) {
			classView_.setVisibility(View.VISIBLE);
			loginView_.setVisibility(View.GONE);
		}
		else {
			loginView_.setVisibility(View.VISIBLE);
			classView_.setVisibility(View.GONE);
		}
	}
	
	public char[] getLoginInputText() {
		char[] user = new char[pidInput_.length()];
		pidInput_.getText().getChars(0, user.length, user, 0);
		return user;
	}
	
	public char[] getPasswordInputText() {
		char[] pass = new char[passwordInput_.length()];
		passwordInput_.getText().getChars(0, pass.length, pass, 0);
		return pass;
	}
	
	public void clearPassword() {
		passwordInput_.setText("");
	}
	
	public void setLoginTextColor(int color) {
		loginUpdate_.setTextColor(color);
	}
	
	public void setLoginText(String msg) {
		loginUpdate_.setText(msg);
	}
	
	public void setClassTextColor(int color) {
		classUpdate_.setTextColor(color);
	}
	
	public void setClassText(String msg) {
		classUpdate_.setText(msg);
	}
	
	public String getCRNInputText() {
		return crnInput_.getText().toString();
	}
	
	public String getYearInputText() {
		return yearInput_.getText().toString();
	}
	
	public String getTermSelection() {
		if(termSpinner_.getSelectedItem().toString().equals("Spring"))
			return "01";
		else
			return "09";
	}
	
	public void toggleRunButton() {
		if(runButton_.isEnabled())
			runButton_.setEnabled(false);
		else
			runButton_.setEnabled(true);
	}
	
	public void toggleCancelButton() {
		if(cancelButton_.isEnabled())
			cancelButton_.setEnabled(false);
		else
			cancelButton_.setEnabled(true);
	}
}
