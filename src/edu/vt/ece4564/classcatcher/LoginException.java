package edu.vt.ece4564.classcatcher;

public class LoginException extends Exception {

	private static final long serialVersionUID = 6096924589356219907L;

	private static final String LOGIN_ERROR_MESSAGE = "Wrong username or password";
	
	public LoginException() {
		super(LOGIN_ERROR_MESSAGE);
	}
}
