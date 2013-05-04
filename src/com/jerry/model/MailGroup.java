package com.jerry.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;

import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;

public class MailGroup {
	private String poster;
	private Mail latestMail;
	
	public MailGroup(String poster) {
		this.poster = poster;
	}
	
	public MailGroup(String poster, Mail latestMail) {
		this(poster);
		String content = latestMail.getContent().trim();
		StringBuffer buffer = new StringBuffer();
		float count = 0;
		for(int i = 0; i < content.length(); i++) {
			if(count > 20) {
				buffer.append("...");
				break;
			}
			if(content.charAt(i) == '\t' || content.charAt(i) == '\n') {
				continue;
			}
			buffer.append(content.charAt(i));
			if(content.charAt(i) < 255) {
				count += 0.5;
			} else {
				count ++;
			}
		}
		latestMail.setContent(buffer.toString());
		this.latestMail = latestMail;
	}

	public String getPoster() {
		return poster;
	}
	
	public Mail getLatestMail() {
		return latestMail;
	}

	public List<Mail> getMailList() {
		return null;
	}

	
	private static final List<Mail> mail2Push = new ArrayList<Mail>();
	
	private static final void syncMailContent(Context context) throws IOException, ParseException {
		LoginInfo  loginInfor= LoginInfo.getInstance(context);
		for(Mail mail : mail2Push) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HttpPost httpRequest = new HttpPost("http://bbs.nju.edu.cn/" + loginInfor.getLoginCode() + "/" + mail.getContentUrl());
			httpRequest.addHeader("Cookie", loginInfor.getLoginCookie());
			HttpResponse httpResponse;
			httpResponse = new DefaultHttpClient().execute(httpRequest);
			String result = "";
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResponse.getEntity());
			}
			Document doc = Jsoup.parse(result);
			String content = doc.select("textarea").get(0).text();
			content = content.substring(content.indexOf("发信站: 南京大学小百合站 ("));
			long time = Constants.DATE_FORMAT.parse(content.substring(content.indexOf("(") + 1, content.indexOf(")"))).getTime();
			mail.setTime(time);
			content = content.substring(content.indexOf("\n\n") + 2);
			if(content.contains("--")) {
				content = content.substring(0, content.lastIndexOf("--"));
			}
			content = content.replaceAll("'", "''");
			content = SingleArticle.deleteExtraBlankAndSymbol(content);
			mail.setContent(content);
		}
		DatabaseDealer.insertMails(context, mail2Push);
		mail2Push.clear();
	}
	
	public static final void getMailList(Context context, boolean isLoadMore) throws IOException, ParseException{
		String nextTargetUrl = DatabaseDealer.getLastMailTargetUrl(context);
		if(isLoadMore && nextTargetUrl.length() == 0) {
			return;
		}
		int lastUpdateHashCode =  DatabaseDealer.getStoredLatestMailCode(context, !isLoadMore);
		List<String> blockList = DatabaseDealer.getBlockList(context);
		LoginInfo loginInfo;
		loginInfo = LoginInfo.getInstance(context);
		String url = "http://bbs.nju.edu.cn/" + loginInfo.getLoginCode() + "/bbsmail" + (isLoadMore ?  nextTargetUrl : "");
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.addHeader("Cookie", loginInfo.getLoginCookie());
		HttpResponse httpResponse = null;
		try{
			httpResponse = new DefaultHttpClient().execute(httpRequest);
		} catch(IOException e) {
			
		} finally {
			
		}
		String result = "";
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			result = EntityUtils.toString(httpResponse.getEntity());
		}
		Elements rows = Jsoup.parse(result).select("tr");
		List<Mail> tmpMailList = new ArrayList<Mail>();
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
				mail.setContentUrl(links.get(1).select("a").attr("href"));
				mail.setRead(line.select("img").size() == 0);
				mail.setType(Constants.MAIL_TYPE_NORMAL);
				if(lastUpdateHashCode == mail.hashCode()) {
					if(isLoadMore) {
						mail2Push.addAll(tmpMailList);
						syncMailContent(context);
						MailGroup.syncTargetUrl(context, result);
						return;
					} else {
						tmpMailList.clear();
					}
				} else {
					tmpMailList.add(mail);
				}
			}
		}
		
		mail2Push.addAll(tmpMailList);
		syncMailContent(context);
		MailGroup.syncTargetUrl(context, result);
	}
	
	private static final void syncTargetUrl(Context context, String result) {
		int index = result.indexOf(Constants.REG_MAIL_DEVIDE);
		String tmp = result.substring(index + Constants.REG_MAIL_DEVIDE.length());
		Elements controllLinks = Jsoup.parse(tmp).select("a");
		if(controllLinks.size() >= 2 && controllLinks.get(1).text().equals("上一页") && mail2Push.size() < 30) {
			String nextTarget = controllLinks.get(1).attr("href");
			nextTarget = nextTarget.substring(nextTarget.indexOf("bbsmail") + 7);
			DatabaseDealer.syncLastMailTargetUrl(context, nextTarget);
		} else {
			DatabaseDealer.syncLastMailTargetUrl(context, "");
		}
	}
}
