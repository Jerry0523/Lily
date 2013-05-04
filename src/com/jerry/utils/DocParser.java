package com.jerry.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.jerry.model.Article;
import com.jerry.model.LoginInfo;
import com.jerry.model.Mail;
import com.jerry.model.SingleArticle;

public class DocParser {
	
	public static final String parseTime(long time) {
		Calendar now = Calendar.getInstance();
		if(time == 0 || time > now.getTimeInMillis()) {
			return "Î´ÖªÊ±¼ä";
		}
		long derta = now.getTimeInMillis() - time;
		if(derta < 60000) {
			return String.valueOf((int)(derta / 1000)) + "ÃëÇ°";
		} else if(derta < 3600000) {
			return String.valueOf((int)(derta / 60000)) + "·ÖÇ°";
		} else if(derta < 86400000){
			return String.valueOf((int)(derta / 3600000)) + "Ê±Ç°";
		} else if(derta < 2592000000L) {
			return String.valueOf((int)(derta / 86400000)) + "ÌìÇ°";
		} else if(derta < 31104000000L) {
			return String.valueOf((int)(derta / 2592000000L)) + "ÔÂÇ°";
		} else {
			return String.valueOf((int)(derta / 31104000000L)) + "ÄêÇ°";
		}
	}

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
				article.setGroup(contentUrl.substring(contentUrl.indexOf("=") + 1, contentUrl.indexOf("&")));
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
			Mail mail = new Mail(post2, content, System.currentTimeMillis(), true, Constants.MAIL_TYPE_SENT);
			List<Mail> mailList = new ArrayList<Mail>();
			mailList.add(mail);
			DatabaseDealer.insertMails(context, mailList);
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
		//content = formatContent(content);
		Elements links = doc.select("a");
		String replyUrl = links.get(links.size() - 3).attr("href");
		Bundle bundle = new Bundle();
		content = content.replaceAll("À´ Ô´: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "");
		bundle.putString("content", content);
		bundle.putString("postUrl", replyUrl);
		return bundle;
	}

	private static String compressBitmapByPath(String pathName) throws IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();  
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		Bitmap bitmap = null;
		int i = 0;
		while (true) {  
			if ((options.outWidth >> i <= 600)) {  
				options.inSampleSize = (int) Math.pow(2.0D, i);  
				options.inJustDecodeBounds = false;  
				bitmap = BitmapFactory.decodeFile(pathName, options);  
				break;  
			}  
			i++;  
		}

		if(bitmap == null) {
			return pathName;
		}

		String newPath = FileDealer.getCacheDir() + pathName.substring(pathName.lastIndexOf("/"));
		File file = new File(newPath);
		FileOutputStream fOut = new FileOutputStream(file);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		return newPath;
	}

	public static final String upLoadPic2Server(String picPath, String board, Context context) throws IOException {
		if (picPath == null || picPath.length() == 0) {
			return "";
		}
		LoginInfo loginInfo = LoginInfo.getInstance(context);
		File file = new File(compressBitmapByPath(picPath));
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
				return result.substring(result.indexOf("'\\n") + 3, result.indexOf("\\n'"));
			}
		}
		return null;
	}

	public static final Bundle getAuthorInfo(String authorName) throws IOException {
		Bundle bundle = new Bundle();
		String doc = Jsoup.connect("http://bbs.nju.edu.cn/bbsqry?userid=" + authorName).get().toString();
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
	
	public static final String formatSingleLine(String replyContent) {
		if(replyContent.length() > 40) {
			StringBuffer buffer = new StringBuffer();
			double count = 0;
			for(int i = 0; i <replyContent.length(); i++) {
				char tmp = replyContent.charAt(i);
				if(tmp == '\n') {
					count = 0;
				} else if(tmp > 255) {
					count ++;
				} else {
					count += 0.5;
				}
				buffer.append(tmp);
				if (count > 0 && Math.floor(count) % 40 == 0 && count != 0.5) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		} else {
			return replyContent;
		}
	}

	public static final String formatString(String replyContent,String authorName, Context context) {
		String finalString = formatSingleLine(replyContent);
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
}
