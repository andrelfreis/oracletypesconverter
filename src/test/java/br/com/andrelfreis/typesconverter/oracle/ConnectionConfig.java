package br.com.andrelfreis.typesconverter.oracle;

public class ConnectionConfig {
	
	private String url = System.getProperty("url");
	private String user = System.getProperty("user");
	private String pass = System.getProperty("pass");
	
	
	public String getUrl() {
		return url;
	}
	public String getUser() {
		return user;
	}
	public String getPass() {
		return pass;
	}
	
	
	
	
	
}