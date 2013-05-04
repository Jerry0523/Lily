package com.jerry.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.jerry.utils.Constants;
import com.jerry.utils.DatabaseDealer;

public class Article implements Parcelable{
	private String title;
	private String contentUrl;
	private String authorName;
	private String groupName;
	private long time;
	private int replyCount;
	private int viewCount;
	private String extraUrl;
	
	private List<SingleArticle> articleList;
	private int totalArticleCount;

	public Article() {
		
	}
	
	public Article(String url, Context context, boolean isBlog) throws IOException, ParseException {
		if(isBlog) {
			articleList = getBlogArticleList(url, context);
		} else {
			articleList = getSingleArticleList(url, context, true);
		}
	}
	
	public void refresh(String url, Context context) throws IOException {
		List<SingleArticle> list = getSingleArticleList(url, context, true);
		articleList.clear();
		articleList.addAll(list);
	}
	
	public static final Parcelable.Creator<Article> CREATOR = new Creator<Article>() {  
		public Article createFromParcel(Parcel source) {  
			Article myArticle = new Article();  
			myArticle.title = source.readString();
			myArticle.contentUrl = source.readString();
			myArticle.authorName = source.readString();
			myArticle.groupName = source.readString();
			myArticle.replyCount = source.readInt();
			myArticle.viewCount = source.readInt();
			myArticle.time = source.readLong();
			return myArticle;  
		}  
		public Article[] newArray(int size) {  
			return new Article[size];  
		}  
	};  

	public int describeContents() {  
		return 0;  
	}  
	public void writeToParcel(Parcel parcel, int flags) {  
		parcel.writeString(title);  
		parcel.writeString(contentUrl);
		parcel.writeString(authorName);
		parcel.writeString(groupName);
		parcel.writeInt(replyCount);
		parcel.writeInt(viewCount);
		parcel.writeLong(time);
	}  

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContentUrl() {
		return contentUrl;
	}
	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}
	public String getAuthorName() {
		if(authorName == null) {
			return "";
		}
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getGroup() {
		return groupName;
	}
	public void setGroup(String board) {
		this.groupName = board;
	}
	public int getReplyCount() {
		return replyCount;
	}
	public void setReplyCount(int reply) {
		this.replyCount = reply;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int view) {
		this.viewCount = view;
	}
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public boolean isArticleListNotEmpty() {
		return articleList != null && articleList.size() > 0;
	}
	
	public int getSingleArticleIndex(SingleArticle singleArticle) {
		if(isArticleListNotEmpty()) {
			return articleList.indexOf(singleArticle);
		}
		return 0;
	}
	
	public List<SingleArticle> getSingleArticleList() {
		return articleList;
	}
	
	public String getArticleAuthorName() {
		if(isArticleListNotEmpty()) {
			return articleList.get(0).getAuthorName();
		}
		return null;
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
	
	private final List<SingleArticle> getBlogArticleList(String url, Context context) throws IOException, ParseException {
		List<SingleArticle> list = new ArrayList<SingleArticle>(); 
		Document doc = Jsoup.connect(url).get();
		String totalContent = doc.select("textarea").text();
		SingleArticle article = new SingleArticle();
		article.setAuthorName(totalContent.substring(totalContent.indexOf("作  者: [uid]") + 11, totalContent.indexOf("[/uid]")));
		article.setTime(Constants.DATE_FORMAT.parse(totalContent.substring(totalContent.indexOf("时  间: ") + 6, totalContent.indexOf("\n点  击:"))).getTime());
		article.initContent4Blog(totalContent);
		list.add(article);
		return list;
	}
	
	private final List<SingleArticle> getSingleArticleList(String url, Context context, boolean isConstruction) throws IOException {
		List<String> blockList = DatabaseDealer.getBlockList(context);
		List<SingleArticle> list = new ArrayList<SingleArticle>(); 
		Document doc = Jsoup.connect(url).get();
		Elements blocks = doc.select("tr");
		if(blocks.size() == 0) {
			return null;
		}
		for (int i = 0; i < blocks.size() - 1; i++) {
			Elements links = blocks.get(i).select("a[href]");
			if (links.size()==0) {
				continue;
			}
			Elements content =  blocks.get(i + 1).select("textarea");
			String authorName = links.get(links.size() == 2 ? 1 : 2).select("a").text();
			if(blockList.contains(authorName)) {
				continue;
			}
			SingleArticle article = new SingleArticle();
			article.setAuthorName(authorName);
			if(links.size() == 3) {
				article.setReplyUrl(links.get(1).select("a").attr("abs:href"));
			}
			article.initContent(content.get(0).text());
			list.add(article);
		}
		
		if(isConstruction) {
			String allString = doc.toString();
			String s = allString.substring(allString.indexOf("</script>本主题共有 ") + 15);
			totalArticleCount = Integer.valueOf(s.substring(0, s.indexOf(" ")));
		}
		
		return list;
	}
	
	public void addSingleArticles(String url, Context context) throws IOException {
		List<SingleArticle> more = getSingleArticleList(url, context, false);
		more.remove(0);
		articleList.addAll(more);
	}
	
	public int getCurrentArticleCount() {
		if(isArticleListNotEmpty()) {
			return articleList.size();
		}
		return 0;
	}
	
	public boolean isNeedPullLoadMore() {
		return totalArticleCount > getCurrentArticleCount() - 1;
	}

	public String getExtraUrl() {
		return extraUrl;
	}

	public void setExtraUrl(String extraUrl) {
		this.extraUrl = extraUrl;
	}
}
