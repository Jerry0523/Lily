package com.jerry.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import com.jerry.utils.Constants;

public class SingleArticle {
	private String content;
	private String authorName;
	private String replyUrl;
	private long time;

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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

	public final void formatTime(String content){
		try {
			time = Constants.DATE_FORMAT.parse(content).getTime();
		} catch (ParseException e) {
			time = 0;
		}
	}

	public final String getAuthorUrl() {
		return "http://bbs.nju.edu.cn/bbsqry?userid=" + authorName;
	}

	public final void initContent(String toBeFormatedContent) {
		content = toBeFormatedContent.substring(toBeFormatedContent.indexOf("发信站: 南京大学小百合站 ("));
		formatTime(content.substring(content.indexOf("(") + 1, content.indexOf(")")));
		content = content.substring(content.indexOf(")") + 3);
		content = content.lastIndexOf("--") > 0 ? content.substring(0,content.indexOf("--")) : content;
		content = SingleArticle.parseTags(content);
	}

	private static boolean isBlank(char c) {
		return c == '\n' || c == '\r' || c == ' ';
	}
	
	public static final String parseTags(String sourceString) {
		Matcher matcher = Pattern.compile(Constants.reg, Pattern.CASE_INSENSITIVE).matcher(sourceString);
		StringBuffer newBuffer = new StringBuffer();
		while (matcher.find()) {
			String originalValue = matcher.group().trim();
			String value = originalValue.toLowerCase();
			if(value.endsWith("jpg") || value.endsWith("gif") || value.endsWith("png") || value.endsWith("jpeg")) {
				matcher.appendReplacement(newBuffer, "<pic>" +  originalValue + "<pic>");
			} else if(value.endsWith("[/wma]")){
				matcher.appendReplacement(newBuffer, originalValue);
			} else {
				matcher.appendReplacement(newBuffer, "<url>" + originalValue + "<url>");
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
		if(result.contains("[/wma]")) {
			result = result.replace("[wma]", "<wma>");
			result = result.replace("[/wma]", "<wma>");
		}
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

	public String getDetailTime() {
		Calendar now = Calendar.getInstance();
		if(time == 0 || time > now.getTimeInMillis()) {
			return "未知时间";
		}
		long derta = now.getTimeInMillis() - time;
		if(derta < 60000) {
			return String.valueOf((int)(derta / 1000)) + "秒前";
		} else if(derta < 3600000) {
			return String.valueOf((int)(derta / 60000)) + "分前";
		} else if(derta < 86400000){
			return String.valueOf((int)(derta / 3600000)) + "时前";
		} else if(derta < 2592000000L) {
			return String.valueOf((int)(derta / 86400000)) + "天前";
		} else if(derta < 31104000000L) {
			return String.valueOf((int)(derta / 2592000000L)) + "月前";
		} else {
			return String.valueOf((int)(derta / 31104000000L)) + "年前";
		}
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
}
