package com.jerry.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.AccountsException;
import android.content.Context;
import android.os.Bundle;

import com.jerry.utils.DatabaseDealer;
import com.jerry.utils.DocParser;


public class LoginInfo {

	private static LoginInfo loginInfo;
	private static long currentTime;
	private static String ip;

	private String username;
	private String password;
	private String loginCode;
	private String loginCookie;

	public void logOut(Context context) {
		if(loginInfo == null) {
			return;
		}
		try {
			String outUrl = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbslogout";
			HttpPost httpRequest = new HttpPost(outUrl);
			ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
			postData.add(new BasicNameValuePair("action", "bbslogout"));
			httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {

			}
		}  catch (IOException e) {

		}
	}

	public static final LoginInfo getInstance(String userName, String password) throws IOException, AccountsException {
		if(loginInfo == null) {
			loginInfo = DocParser.login(userName, password);
			currentTime = System.currentTimeMillis();
			ip = loginInfo.getLocalIpAddress();
		} else if(System.currentTimeMillis() - currentTime > 600000 || !ip.equals(loginInfo.getLocalIpAddress())){
			loginInfo = DocParser.login(userName, password);
			currentTime = System.currentTimeMillis();
			ip = loginInfo.getLocalIpAddress();
		} else {
			currentTime = System.currentTimeMillis();
		}
		return loginInfo;
	}
	

public static final LoginInfo resetLoginInfo(Context context) throws IOException {
	loginInfo = null;
	return getInstance(context);
}

public static final LoginInfo getLoginInfo() {
	return loginInfo;
}

public static final LoginInfo getInstance(Context context) throws IOException {
	try {
		if(loginInfo == null) {
			Bundle bundle = DatabaseDealer.query(context);
			loginInfo = DocParser.login(bundle);
			currentTime = System.currentTimeMillis();
			ip = loginInfo.getLocalIpAddress();
		} else if(System.currentTimeMillis() - currentTime > 700000 || !ip.equals(loginInfo.getLocalIpAddress())){
			Bundle bundle = DatabaseDealer.query(context);
			loginInfo = DocParser.login(bundle);
			currentTime = System.currentTimeMillis();
			ip = loginInfo.getLocalIpAddress();
		} else {
			currentTime = System.currentTimeMillis();
		}
	} catch(AccountsException e) {

	}

	return loginInfo;
}

public String getUsername() {
	return username;
}
public void setUsername(String username) {
	this.username = username;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getLoginCode() {
	return loginCode;
}
public void setLoginCode(String loginCode) {
	this.loginCode = loginCode;
}
public String getLoginCookie() {
	return loginCookie;
}
public void setLoginCookie(String loginCookie) {
	this.loginCookie = loginCookie;
}

private String getLocalIpAddress() {
	try { 
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
			NetworkInterface intf = en.nextElement(); 
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
				InetAddress inetAddress = enumIpAddr.nextElement(); 
				if (!inetAddress.isLoopbackAddress()) { 
					return inetAddress.getHostAddress().toString(); 
				} 
			} 
		} 
	} catch (SocketException ex) { 
	} 
	return null; 
} 
}
