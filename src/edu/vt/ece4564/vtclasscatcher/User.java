package edu.vt.ece4564.vtclasscatcher;

public class User {

	private char[] username_;
	private char[] password_;
	
	public User() {};
	public User(char[] username, char[] password) {
		// Set credentials
		setCredentials(username,password);

		// Clear temporary credentials (probably overkill, but eh why not?)
		for(int i = 0; i < username.length; ++i) username[i] = 0;
		for(int i = 0; i < password.length; ++i) password[i] = 0;
	}

	
	
	/***
	 * setCredentials()
	 * 
	 * sets username and password of the user
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredentials(char[] username, char[] password) {
		for(int i = 0; i < username.length; ++i) username_[i] = username[i];
		for(int i = 0; i < password.length; ++i) password_[i] = password[i];

		// Clear temporary credentials
		for(int i = 0; i < username.length; ++i) username[i] = 0;
		for(int i = 0; i < password.length; ++i) password[i] = 0;
	}
	
	

	/***
	 * getUsername()
	 * 
	 * @return username
	 */
	public char[] getUsername() {
		return username_;
	}
	
	
	
	/***
	 * getPassword()
	 * 
	 * @return password
	 */
	public char[] getPassword() {
		return password_;
	}
	
	
	
	/***
	 * clearCredentials()
	 * 
	 * Clears the credentials in memory
	 */
	public void clearCredentials() {
		for(int i = 0; i < username_.length; ++i) username_[i] = 0;
		for(int i = 0; i < password_.length; ++i) password_[i] = 0;
	}
}
