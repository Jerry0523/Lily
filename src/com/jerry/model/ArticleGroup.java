package com.jerry.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;

import com.jerry.utils.DatabaseDealer;

public class ArticleGroup {
	private List<Article> articleList;
	private String nextTargetUrl;
	
	public List<Article> getArticleList() {
		return articleList;
	}
	public void setArticleList(List<Article> articleList) {
		this.articleList = articleList;
	}
	public String getNextTargetUrl() {
		return nextTargetUrl;
	}
	public void setNextTargetUrl(String nextTargetUrl) {
		this.nextTargetUrl = nextTargetUrl;
	}
	
	public ArticleGroup(List<Article> articleList, String nextTargetUrl) {
		this.articleList = articleList;
		this.nextTargetUrl = nextTargetUrl;
	}
	
	public static final ArticleGroup getHotArticleTitleList() throws IOException {
		List<Article> hotList = new ArrayList<Article>();
		Document doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstopall").get();
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
		return new ArticleGroup(hotList, null);
	}
	
	public static final ArticleGroup getTopArticleTitleList(Context context) throws IOException {
		List<String> blockList = DatabaseDealer.getBlockList(context);
		List<Article> list = new ArrayList<Article>(); 
		Document doc = Jsoup.connect("http://bbs.nju.edu.cn/bbstop10").timeout(5000).get();
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
		return new ArticleGroup(list, null);
	}
	
	public static final ArticleGroup getBoardArticleTitleList(String url, String boardName, Context context) throws IOException, ParseException {
		List<String> blockList = DatabaseDealer.getBlockList(context);
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
			String title = links.get(1).select("a").text();
			if(title.startsWith("¡ð ")) {
				title = title.substring(1);
			}
			article.setTitle(title);
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
		Collections.reverse(list);
		String nextTargetURL = "http://bbs.nju.edu.cn/" + links.get(0).select("a").attr("href");
		return new ArticleGroup(list, nextTargetURL);
	}
}
