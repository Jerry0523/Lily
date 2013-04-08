package com.jerry.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import android.accounts.AccountsException;
import android.content.Context;
import android.os.Bundle;

import com.jerry.utils.DatabaseDealer;


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
			loginInfo = LoginInfo.login(userName, password);
			currentTime = System.currentTimeMillis();
			ip = loginInfo.getLocalIpAddress();
		} else if(System.currentTimeMillis() - currentTime > 600000 || !ip.equals(loginInfo.getLocalIpAddress())){
			loginInfo = LoginInfo.login(userName, password);
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
				loginInfo = LoginInfo.login(bundle);
				currentTime = System.currentTimeMillis();
				ip = loginInfo.getLocalIpAddress();
			} else if(System.currentTimeMillis() - currentTime > 700000 || !ip.equals(loginInfo.getLocalIpAddress())){
				Bundle bundle = DatabaseDealer.query(context);
				loginInfo = LoginInfo.login(bundle);
				currentTime = System.currentTimeMillis();
				ip = loginInfo.getLocalIpAddress();
			} else {
				currentTime = System.currentTimeMillis();
			}
		} catch(AccountsException e) {

		}

		return loginInfo;
	}

	public static final void keepConnected(Context context) throws IOException {
		loginInfo = getInstance(context);
		String url = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsnewmail";
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse;
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(httpResponse.getEntity());
			if(result.contains("´íÎó! ÄúÉÐÎ´µÇÂ¼, ÇëÏÈµÇÂ¼!")) {
				LoginInfo.resetLoginInfo(context);
				keepConnected(context);
			}
		}
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

	private static final LoginInfo login(Bundle userInfo) throws IOException, AccountsException {
		return login(userInfo.getString("username"),userInfo.getString("password"));
	}

	private static final LoginInfo login(String username, String password) throws IOException,AccountsException {
		int s = new Random().nextInt(99999)%(90000) + 10000;
		String urlString = "http://bbs.nju.edu.cn/vd" + String.valueOf(s) + "/bbslogin?type=2&id=" + username + "&pw=" + password;
		String doc = Jsoup.connect(urlString).get().toString();
		if (doc.indexOf("setCookie") < 0) {
			throw new AccountsException();
		} else {
			LoginInfo info = new LoginInfo();
			String loginString = doc.substring(doc.indexOf("setCookie"));
			loginString =  loginString.substring(11, loginString.indexOf(")") - 1) + "+vd" + String.valueOf(s);
			String[] tmpString =  loginString.split("\\+");
			String _U_KEY = String.valueOf(Integer.parseInt(tmpString[1])-2);
			String[] loginTmp = tmpString[0].split("N");
			String _U_UID = loginTmp[1];
			String _U_NUM = "" + String.valueOf(Integer.parseInt(loginTmp[0]) + 2);
			info.setLoginCookie("_U_KEY=" + _U_KEY + "; " + "_U_UID=" + _U_UID + "; " + "_U_NUM=" + _U_NUM + ";");
			info.setLoginCode(tmpString[2]);
			info.setUsername(username);
			info.setPassword(password);
			return info;
		}
	}
}
