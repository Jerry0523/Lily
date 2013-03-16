package com.jerry.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.accounts.AccountsException;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.jerry.model.Article;
import com.jerry.model.LoginInfo;
import com.jerry.model.Mail;
import com.jerry.model.SingleArticle;

public class DocParser {

	public static final String getAllTopicArtileUrl(String url) throws IOException {
		String topic = null;
		Document doc = Jsoup.connect(url).get();
		Elements blocks = doc.select("a");
		topic = "http://bbs.nju.edu.cn/" + blocks.get(blocks.size() - 1).attr("href");
		String nextContent = Jsoup.connect(topic).get().toString();
		topic = nextContent.substring(nextContent.indexOf("url=") + 4, nextContent.indexOf(".A\" />") + 2);
		topic = "http://bbs.nju.edu.cn/" +  topic.replace("amp;", "");
		return topic;
	}

	public static final List<Article> search(String author, String title, String title2,int time) throws IOException {
		List<Article> list = new ArrayList<Article>();
		String url = "http://bbs.nju.edu.cn/bbsfind";
		HttpPost httpRequest = new HttpPost(url);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("action", "bbsfind"));
		postData.add(new BasicNameValuePair("flag", "1"));
		postData.add(new BasicNameValuePair("user", author));
		postData.add(new BasicNameValuePair("title", title));
		postData.add(new BasicNameValuePair("title2", ""));
		postData.add(new BasicNameValuePair("title3", title2));
		postData.add(new BasicNameValuePair("day", "0"));
		postData.add(new BasicNameValuePair("day2", String.valueOf(time)));
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		HttpResponse httpResponse;
		httpResponse = new DefaultHttpClient().execute(httpRequest);

		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			String result = EntityUtils.toString(httpResponse.getEntity());
			Document doc = Jsoup.parse(result);
			Elements rows = doc.select("tr");
			for(Element line : rows) {
				Elements links = line.select("a");
				if(links.size() == 0) {
					return list;
				}
				Article article = new Article();
				Element content = links.get(1);
				String contentUrl = "http://bbs.nju.edu.cn/" + content.attr("href");
				article.setTitle(content.select("a").text());
				article.setContentUrl(contentUrl);
				article.setAuthorName(links.get(0).select("a").text());
				article.setBoard(contentUrl.substring(contentUrl.indexOf("=") + 1, contentUrl.indexOf("&")));
				list.add(article);
			}
		} 
		return list;
	}

	public static final String getLastUpdateTime() {
		long time=System.currentTimeMillis();
		final Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
		int mMinuts = mCalendar.get(Calendar.MINUTE);
		return (mHour < 10 ? ("0" + mHour) : mHour) + ":" + (mMinuts < 10 ? ("0" + mMinuts) : mMinuts);

	}

	public static void sendMail(String post2, String content, Context context) throws IOException{
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		HttpResponse httpResponse;
		String newUrl = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbssndmail?pid=0&userid=" + post2;
		HttpPost httpRequest = new HttpPost(newUrl);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("action", "bbssndmail?pid=0&userid=" + post2));
		postData.add(new BasicNameValuePair("signature", "1"));
		postData.add(new BasicNameValuePair("text", content));
		postData.add(new BasicNameValuePair("title", "Õ¾ÄÚÐÅ"));
		postData.add(new BasicNameValuePair("userid", post2));
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {

		}
	}

	public static boolean isNewVersionAvailable(Context context) throws IOException, NameNotFoundException{
		String doc = Jsoup.connect("http://bbs.nju.edu.cn/blogcon?userid=WStaotao&file=1346326084").get().toString();
		doc = doc.substring(doc.indexOf("version:["));
		doc = doc.substring(0,doc.indexOf("]"));
		doc = doc.substring(doc.indexOf("[") + 1);
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo;
		packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
		String version = packInfo.versionName;
		double d1 = Double.valueOf(doc);
		double d2 = Double.valueOf(version);
		if( d1 > d2 ) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean replyMail(String url, String content, Context context) throws IOException{
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String mailto = url.substring(url.indexOf("userid=")+7);
		mailto = mailto.substring(0, mailto.indexOf("&"));
		String pidString = url.substring(url.indexOf("pid=")+4);
		pidString = pidString.substring(0, pidString.indexOf("&"));
		String mailId = url.substring(url.indexOf("file=") + 5);
		mailId = mailId.substring(0, mailId.indexOf("&"));
		String titleString = url.substring(url.indexOf("title=")+6);
		String newUrl = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbssndmail?pid=" + pidString + "&userid=" + mailto;
		HttpResponse httpResponse;
		HttpPost httpRequest = new HttpPost(newUrl);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("action", mailId));
		postData.add(new BasicNameValuePair("signature", "1"));
		postData.add(new BasicNameValuePair("text", content));
		postData.add(new BasicNameValuePair("title", titleString));
		postData.add(new BasicNameValuePair("userid", mailto));
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			return true;
		}
		return false;
	}

	public static Bundle getMailContent(String url, Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String contentUrl = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/" + url;
		HttpPost httpRequest = new HttpPost(contentUrl);
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse;
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		String result = "";
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			result = EntityUtils.toString(httpResponse.getEntity());
		}
		Document doc = Jsoup.parse(result);
		String content = doc.select("textarea").get(0).text();
		content = formatContent(content);
		Elements links = doc.select("a");
		String replyUrl = links.get(links.size() - 3).attr("href");
		Bundle bundle = new Bundle();
		content = content.replaceAll("À´ Ô´: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "");
		bundle.putString("content", content);
		bundle.putString("postUrl", replyUrl);
		return bundle;
	}

	public static final void keepConnected(Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
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

	public static final List<Mail> getMailList(List<String> blockList, Context context) throws IOException{
		List<Mail> list = new ArrayList<Mail>();
		LoginInfo loginInfo;
		loginInfo = LoginInfo.getInstance(context);
		String url = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsmail";
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse;
		httpResponse = new DefaultHttpClient().execute(httpRequest);
		String result = "";
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			result = EntityUtils.toString(httpResponse.getEntity());
		}
		Elements rows = Jsoup.parse(result).select("tr");
		for(Element line : rows) {
			Elements links = line.select("a");
			if (links.size() == 0) {
				continue;
			}
			if(links.size() == 2) {
				String authorName = links.get(0).select("a").text();
				if(blockList.contains(authorName)) {
					continue;
				}
				Mail mail = new Mail();
				mail.setPoster(authorName);
				mail.setPosterUrl(links.get(0).select("a").attr("href"));
				mail.setContentUrl(links.get(1).select("a").attr("href"));
				mail.setTitle(links.get(1).select("a").text());
				mail.setTime(line.select("td").get(4).text());
				mail.setRead(line.select("img").size() != 0);
				list.add(mail);
			}

		}
		return list;
	}
	/*
	 * ·µ»ØÒ»¸öÒ³ÃæËùÓÐÌû×ÓµÄ×ÜÌåList£¬±ÈÈçÊ®´óÌû×ÓµÄList
	 */
	public static final List<Article> getArticleTitleList(String url, List<String> blockList) throws IOException {
		List<Article> list = new ArrayList<Article>(); 
		Document doc = Jsoup.connect(url).timeout(5000).get();
		Elements blocks = doc.select("tr");
		for (Element block : blocks) {
			Elements links = block.select("a[href]");
			if (links.size()==0) {
				continue;
			}
			links = block.select("td");
			String authorName = links.get(3).select("a").text();
			if(blockList.contains(authorName)) {
				continue;
			}
			Article article = new Article();
			article.setAuthorName(authorName);
			article.setBoard(links.get(1).select("a").text());
			article.setContentUrl(links.get(2).select("a").attr("abs:href"));
			article.setViewCount(Integer.valueOf(links.get(4).text()));
			article.setTitle(links.get(2).select("a").text());
			list.add(article);
		}
		return list;
	}

	public static final List<Article> getBoardArticleTitleList(String url, String boardName, List<String>blockList) throws IOException, ParseException {
		List<Article> list = new ArrayList<Article>(); 
		Document doc = Jsoup.connect(url).get();
		Elements blocks = doc.select("tr");
		for (Element block : blocks) {
			Elements links = block.getElementsByTag("a");
			if (links.size() == 0) {
				continue;
			}
			String authorName = links.get(0).select("a").text();
			if(blockList.contains(authorName)) {
				continue;
			}
			Article article = new Article();
			article.setAuthorName(authorName);
			article.setTitle(links.get(1).select("a").text());
			article.setContentUrl(links.get(1).select("a").attr("abs:href"));

			Element author = links.get(0).parent();
			if(author != null) {
				Element time = author.nextElementSibling();
				SimpleDateFormat dateFormat =  new SimpleDateFormat("MMM dd HH:mm yyyy",Locale.ENGLISH);
					Calendar now = Calendar.getInstance();
					Date value = dateFormat.parse(time.text() + " " + String.valueOf(now.get(Calendar.YEAR)));
					article.setTime(value.getTime());
			}

			Elements fonts = block.getElementsByTag("font");
			article.setViewCount(Integer.valueOf(fonts.get(fonts.size() - 1).select("font").text()));
			if (fonts.get(0).select("nobr").size() == 0) {
				article.setReplyCount(Integer.valueOf(fonts.get(fonts.size() - 2).select("font").text()));
			}
			list.add(article);
		}
		String allString = doc.toString();
		int i = allString.indexOf("<a href=\"bbstdoc?board=" + boardName + "&amp;start=");
		Document subDoc = Jsoup.parse(allString.substring(i));
		Elements links = subDoc.getElementsByTag("a");
		Article article = new Article();
		article.setBoard(links.get(0).select("a").attr("href"));
		list.add(article);
		return list;
	}

	public static boolean sendReply(String boardName,String title, String pidString,String reIdString,String replyContent, String authorName,String picPath, Context context) throws IOException {
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		String newurlString = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbssnd?board=" + boardName;
		HttpPost httpRequest = new HttpPost(newurlString);
		ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("title", title));
		postData.add(new BasicNameValuePair("pid", pidString));
		postData.add(new BasicNameValuePair("reid", reIdString));
		postData.add(new BasicNameValuePair("signature", "1"));
		postData.add(new BasicNameValuePair("autocr", "on"));
		postData.add(new BasicNameValuePair("text", DocParser.formatString(replyContent, authorName, picPath, context)));
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		httpRequest.setEntity(new UrlEncodedFormEntity(postData, "GB2312"));
		HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			return true;
		} else {
			LoginInfo.resetLoginInfo(context);
			return sendReply(boardName, title, pidString, reIdString, replyContent, authorName, picPath, context);
		}
	}

	public static final String upLoadPic2Server(List<String> picPath, String board, Context context) throws IOException {
		if (picPath == null || picPath.size() == 0) {
			return "";
		}
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		File file = new File(picPath.get(0));
		HttpClient client;
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		client = new DefaultHttpClient(httpParameters);
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		String url = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsdoupload?ptext=text&board=" + board;
		HttpPost imgPost = new HttpPost(url);
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart("up", new FileBody(file));
		reqEntity.addPart("exp", new StringBody("", Charset.forName("GB2312")));
		reqEntity.addPart("ptext", new StringBody("text", Charset.forName("GB2312")));
		reqEntity.addPart("board", new StringBody(board, Charset.forName("GB2312")));
		imgPost.setEntity(reqEntity);
		imgPost.addHeader("Cookie", loginInfo.getLoginCookie());
		imgPost.addHeader(reqEntity.getContentType());

		String result;
		HttpResponse httpResponse;
		httpResponse = client.execute(imgPost);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			result = EntityUtils.toString(httpResponse.getEntity());
			result = result.substring(result.indexOf("url=")+2, result.indexOf(">")-1);
			String fileNum = result.substring(result.indexOf("file=")+5, result.indexOf("&name"));
			String fileName = file.getName();
			if (fileName.length() > 10) {
				fileName = fileName.substring(fileName.length() - 10);
			}
			url = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsupload2?board=" + board + "&file=" + fileNum + "&name=" + fileName + "&exp=&ptext=text";
			HttpGet uploadGet = new HttpGet(url);
			uploadGet.addHeader("Cookie", loginInfo.getLoginCookie());
			httpResponse = client.execute(uploadGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResponse.getEntity());
				String urlString = result.substring(result.indexOf("'\\n") + 3, result.indexOf("\\n'"));
				System.gc();
				return '\n' + urlString + '\n';
			}
		}
		return null;
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

	public static final List<Article> getHotArticleTitleList(String url, List<Article> hotList) throws IOException {
		if(hotList == null) {
			hotList = new ArrayList<Article>();
		} else {
			hotList.clear();
		}
		Document doc = Jsoup.connect(url).get();
		Elements blocks = doc.select("tr");
		for (Element block : blocks) {
			Elements links = block.getElementsByTag("a");
			if (links.size() == 0) {
				continue;
			}
			Article article = new Article();
			article.setTitle(links.get(0).select("a").text());
			article.setContentUrl(links.get(0).select("a").attr("abs:href"));
			article.setBoard(links.get(1).select("a").text());
			hotList.add(article);
		}
		return hotList;
	}

	public static final Bundle getAuthorInfo(String authorUrl,String authorName) throws IOException {
		Bundle bundle = new Bundle();
		String doc = Jsoup.connect(authorUrl).get().toString();
		if(doc.indexOf("[[1;36m") > 0) {
			bundle.putBoolean("isMale", true);
		} else if(doc.indexOf("[[1;35m") > 0) {
			bundle.putBoolean("isMale", false);
		}
		if(doc.indexOf("Ä¿Ç°ÔÚÕ¾ÉÏ") > 0) {
			bundle.putBoolean("isOnline", true);
		} else {
			bundle.putBoolean("isOnline", false);
		}
		String totalLogin = doc.substring(doc.indexOf("¹²ÉÏÕ¾ [32m") + 9, doc.indexOf("[m ´Î£¬·¢±íÎÄÕÂ [32m"));
		String totalPost = doc.substring(doc.indexOf("[m ´Î£¬·¢±íÎÄÕÂ [32m") + 15, doc.indexOf("[m Æª"));
		String experienceValue = doc.substring(doc.indexOf("¾­ÑéÖµ£º[[32m") + 10, doc.indexOf("±íÏÖÖµ£º[[32m"));
		if(doc.indexOf("¾­ÑéÖµ£º[[32m") > 0) {
			experienceValue = experienceValue.substring(0, experienceValue.indexOf("["));
		} else {
			experienceValue = "Î´Öª";
		}

		String actValue = doc.substring(doc.indexOf("±íÏÖÖµ£º[[32m") + 10, doc.indexOf(" ÉúÃüÁ¦£º[[32m"));
		actValue = actValue.substring(0, actValue.indexOf("["));
		String lifeValue = doc.substring(doc.indexOf("ÉúÃüÁ¦£º[[32m") + 10, doc.indexOf("[37m]¡£"));
		String name = doc.substring(doc.indexOf("([33m") + 6, doc.indexOf("[37m)"));
		bundle.putString("name", name);

		bundle.putString("totalLogin", totalLogin);
		bundle.putString("totalPost", totalPost);
		bundle.putString("actValue", actValue);
		bundle.putString("lifeValue", lifeValue);
		bundle.putString("experienceValue", experienceValue);

		if(doc.indexOf("¸öÈËËµÃ÷µµÈçÏÂ") > 0) {
			bundle.putString("personal", SingleArticle.parseTags(doc.substring(doc.indexOf("[36m¸öÈËËµÃ÷µµÈçÏÂ[37m") + 16, doc.indexOf("</textarea><script>"))));
		} else {
			bundle.putString("personal","Ã»ÓÐ¸öÈËËµÃ÷µµ!");
		}
		return bundle;
	}

	public static final LoginInfo login(Bundle userInfo) throws IOException, AccountsException {
		return login(userInfo.getString("username"),userInfo.getString("password"));
	}

	public static final LoginInfo login(String username, String password) throws IOException,AccountsException {
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

	public static final String formatString(String replyContent,String authorName,String picPath, Context context) {
		String finalString = replyContent;
		if(replyContent.length() > 40) {
			StringBuffer buffer = new StringBuffer();
			double count = 0;
			for(int i = 0; i <replyContent.length(); i++) {
				char tmp = replyContent.charAt(i);
				if(tmp > 255) {
					count ++;
				} else {
					count += 0.5;
				}
				buffer.append(tmp);
				if (count > 0 && Math.floor(count) % 40 == 0) {
					buffer.append('\n');
				}
			}
			finalString = buffer.toString();
		}
		finalString += picPath;
		if (authorName != null) {
			finalString += '\n' + "¡¾ÔÚ[uid]" + authorName + "[/uid]µÄ´ó×÷ÖÐÌáµ½¡¿";
		}
		String sign = DatabaseDealer.getSettings(context).getSign();
		if (sign != null && sign.length() > 0) {
			finalString += ('\n' + "-" + '\n' + sign);
		}
		return finalString;
	}

	public static final List <ContentValues>  synchronousFav(LoginInfo loginInfo) throws IOException{
		List <ContentValues> favList = new ArrayList<ContentValues>();
		String urlString = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsmybrd";
		HttpClient client;
		BasicHttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000 );
		HttpConnectionParams.setSoTimeout(httpParameters, 10000 );
		client = new DefaultHttpClient(httpParameters);
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpGet uploadGet = new HttpGet(urlString);
		uploadGet.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse = client.execute(uploadGet);
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			Document doc = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
			Elements boards = doc.select("input[checked]");
			if (boards.size() == 0) {
				return new ArrayList<ContentValues>();
			}
			for (Element board : boards) {
				ContentValues values = new ContentValues();
				String boardName = board.nextSibling().toString();
				boardName = boardName.substring(boardName.indexOf(">") + 1);
				boardName = boardName.substring(0, boardName.indexOf("<"));
				values.put("english", boardName.substring(0, boardName.indexOf("(")));
				values.put("chinese", boardName.substring(boardName.indexOf("(") + 1, boardName.length() - 1));
				values.put("islocal", 0);
				favList.add(values);
			}
		} else {
			throw new IOException();
		}

		return favList;
	}

	private static final String formatContent(String content) {
		String result = content.substring(content.indexOf("·¢ÐÅÕ¾: ÄÏ¾©´óÑ§Ð¡°ÙºÏÕ¾ ("));
		result = result.substring(result.indexOf(")") + 3);
		result = result.lastIndexOf("--") > 0 ? result.substring(0,result.indexOf("--")) : result;
		Matcher matcher = Pattern.compile(Constants.reg, Pattern.CASE_INSENSITIVE).matcher(result.toString());
		StringBuffer newBuffer = new StringBuffer();
		while (matcher.find()) {
			String originalValue = matcher.group().trim();
			String value = originalValue.toLowerCase();
			if(value.endsWith("jpg") || value.endsWith("gif") || value.endsWith("png") || value.endsWith("jpeg")) {
				matcher.appendReplacement(newBuffer, "<pic>" +  originalValue + "<pic>");
			} else {
				matcher.appendReplacement(newBuffer, "<url>" + originalValue + "<url>");
			}
		}
		matcher.appendTail(newBuffer);
		result = newBuffer.toString();
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
			result = result.replace("[:C-]", "<emotion>t/emotion>");
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
		result = result.replaceAll("\\[(1;.*?|37;1|32|33)m", "");
		result = result.replaceAll("\n", "<br/>");
		return result;
	}
}
