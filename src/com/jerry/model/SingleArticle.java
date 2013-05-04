package com.jerry.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.jerry.utils.Constants;
import com.jerry.utils.DocParser;

public class SingleArticle {
	private String content;
	private String orginalContent;
	private String authorName;
	private String replyUrl;
	private long time;

	public String getContent() {
		return content;
	}
	
	public String getOriginalContent() {
		return orginalContent;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	public String getReplyUrl() {
		return replyUrl;
	}
	
	public void setReplyUrl(String replyUrl) {
		this.replyUrl = replyUrl;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public long getTimeValue() {
		return time;
	}

	public final void formatTime(String content){
		try {
			time = Constants.DATE_FORMAT.parse(content).getTime();
		} catch (ParseException e) {
			time = 0;
		}
	}

	public final String getOriginalTimeString() {
		return Constants.DATE_FORMAT.format(new Date(time));
	}
	
	public final String getAuthorUrl() {
		return "http://bbs.nju.edu.cn/bbsqry?userid=" + authorName;
	}
	
	public final void initContent4Blog(String source) {
		content = source;
	}

	public final void initContent(String toBeFormatedContent) {
		content = toBeFormatedContent.substring(toBeFormatedContent.indexOf("发信站: 南京大学小百合站 ("));
		formatTime(content.substring(content.indexOf("(") + 1, content.indexOf(")")));
		content = content.substring(content.indexOf(")") + 3);
		orginalContent = content.lastIndexOf("--") > 0 ? content.substring(0,content.indexOf("--")) : content;
		content = SingleArticle.parseTags(orginalContent);
	}

	private static boolean isBlank(char c) {
		return c == '\n' || c == '\r' || c == ' ';
	}

	public static final String parseTags(String sourceString) {
		Matcher matcher = Pattern.compile(Constants.REG_URL, Pattern.CASE_INSENSITIVE).matcher(sourceString);
		StringBuffer newBuffer = new StringBuffer();
		while (matcher.find()) {
			String value = matcher.group().trim();
			if(value.endsWith("[/wma]")){
				matcher.appendReplacement(newBuffer, value);
			} else if(Pattern.compile(Constants.REG_PIC, Pattern.CASE_INSENSITIVE).matcher(value).find()) {
				matcher.appendReplacement(newBuffer, "<pic>" +  value + "<pic>");
			} else {
				matcher.appendReplacement(newBuffer, "<url>" + value + "<url>");
			}
		}
		matcher.appendTail(newBuffer);
		String result = newBuffer.toString();
		if(result.contains("[")) {
			result = result.replace("[:s]", "<emotion>s<emotion>");
			result = result.replace("[:O]", "<emotion>o<emotion>");
			result = result.replace("[:|]", "<emotion>v<emotion>");
			result = result.replace("[:$]", "<emotion>d<emotion>");
			result = result.replace("[:X]", "<emotion>x<emotion>");
			result = result.replace("[:'(]", "<emotion>q<emotion>");
			result = result.replace("[:@]", "<emotion>a<emotion>");
			result = result.replace("[:-|]", "<emotion>h<emotion>");
			result = result.replace("[:P]", "<emotion>p<emotion>");
			result = result.replace("[:D]", "<emotion>e<emotion>");
			result = result.replace("[:)]", "<emotion>b<emotion>");
			result = result.replace("[:(]", "<emotion>c<emotion>");
			result = result.replace("[:Q]", "<emotion>f<emotion>");
			result = result.replace("[:T]", "<emotion>g<emotion>");
			result = result.replace("[;P]", "<emotion>i<emotion>");
			result = result.replace("[;-D]", "<emotion>j<emotion>");
			result = result.replace("[:!]", "<emotion>k<emotion>");
			result = result.replace("[:L]", "<emotion>l<emotion>");
			result = result.replace("[:?]", "<emotion>m<emotion>");
			result = result.replace("[:U]", "<emotion>n<emotion>");
			result = result.replace("[:K]", "<emotion>r<emotion>");
			result = result.replace("[:C-]", "<emotion>t<emotion>");
			result = result.replace("[;X]", "<emotion>u<emotion>");
			result = result.replace("[:H]", "<emotion>w<emotion>");
			result = result.replace("[;bye]", "<emotion>y<emotion>");
			result = result.replace("[;cool]", "<emotion>z<emotion>");
			result = result.replace("[:-b]", "<emotion>0<emotion>");
			result = result.replace("[:-8]", "<emotion>1<emotion>");
			result = result.replace("[;PT]", "<emotion>2<emotion>");
			result = result.replace("[:hx]", "<emotion>3<emotion>");
			result = result.replace("[;K]", "<emotion>4<emotion>");
			result = result.replace("[:E]", "<emotion>5<emotion>");
			result = result.replace("[:-(]", "<emotion>6<emotion>");
			result = result.replace("[;hx]", "<emotion>7<emotion>");
			result = result.replace("[:-v]", "<emotion>8<emotion>");
			result = result.replace("[;xx]", "<emotion>9<emotion>");
		}
		result = result.replace("[uid]", "<uid>");
		result = result.replace("[/uid]", "<uid>");
		result = result.replace("[brd]", "<brd>");
		result = result.replace("[/brd]", "<brd>");
		if(result.contains("[/wma]")) {
			result = result.replace("[wma]", "<wma>");
			result = result.replace("[/wma]", "<wma>");
		}
	
		result = SingleArticle.deleteExtraBlankAndSymbol(result);
		
		StringBuffer tmp = new StringBuffer();
		String[] split = result.split("[\t\n]+");
		for(int i = 0; i < split.length; i++) {
			tmp.append(split[i]);
			if((split[i].length() < 38 && i != split.length - 1)) {
				tmp.append("<br/>");
			}
		}
		result = tmp.toString();
		return result;
	}
	
	public static final String deleteExtraBlankAndSymbol(String result) {
		result = result.replaceAll("\\[((1;)*3[1234567](;1)*)*m", "");

		for(int i = 0; i < result.length() - 2; i++) {
			if(isBlank(result.charAt(i))) {
				result = result.substring(i + 1);
				i--;
			} else {
				break;
			}
		}

		for(int i = result.length() - 1; i > 0; i--) {
			if(isBlank(result.charAt(i))) {
				result = result.substring(0, i);
			} else {
				break;
			}
		}
		return result;
	}

	public final boolean deleteArticle(Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String tmp = replyUrl.replace("/bbspst", "/bbsdel");
		String deleteUrl = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() +tmp.substring(tmp.indexOf("/bbsdel"));
		HttpPost httpRequest = new HttpPost(deleteUrl);
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse;
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			return true;
		}
		return false;
	}
	
	public static final boolean modifyArticle(Context context, String board, String fileNode, String content) throws IOException{
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String action = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsedit";
		HttpPost httpRequest = new HttpPost(action);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("type", "1"));
		postData.add(new BasicNameValuePair("board", board));
		postData.add(new BasicNameValuePair("file", fileNode));
		postData.add(new BasicNameValuePair("text", content));
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			return true;
		}
		return false;
	}

	public static final boolean sendReply(String boardName,String title, String pidString,String reIdString,String replyContent, String authorName, Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String newurlString = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbssnd?board=" + boardName;
		HttpPost httpRequest = new HttpPost(newurlString);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("title", title));
		postData.add(new BasicNameValuePair("pid", pidString));
		postData.add(new BasicNameValuePair("reid", reIdString));
		postData.add(new BasicNameValuePair("signature", "1"));
		postData.add(new BasicNameValuePair("autocr", "on"));
		postData.add(new BasicNameValuePair("text", DocParser.formatString(replyContent, authorName, context)));
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			return true;
		}

		return false;
	}

	public static final String getPid(String replyUrl, Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String tempString = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + replyUrl.substring(replyUrl.indexOf("/bbspst"));
		URL mUrl;
		mUrl = new URL(tempString);
		HttpURLConnection conn;
		conn = (HttpURLConnection) mUrl.openConnection();
		conn.setRequestProperty("Cookie", loginInfo.getLoginCookie());
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		conn.connect();
		InputStream in = conn.getInputStream(); 
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(in,"gb2312")); 
		String inputLine = null;
		while ((inputLine = reader1.readLine()) != null) {  
			if ( inputLine.contains("name=pid") ) {
				String temp = inputLine.substring(inputLine.indexOf("name=pid"));
				if (temp.indexOf("value='")!=-1 && temp.indexOf("'>")!=-1) {
					reader1.close();
					in.close();
					return temp.substring(temp.indexOf("value='") + 7,temp.indexOf("'>"));
				}
			}
		}
		return null;
	}
}
