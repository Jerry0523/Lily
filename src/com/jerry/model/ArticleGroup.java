package com.jerry.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;

import com.jerry.utils.Constants;
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

	public static final ArticleGroup getHotArticleGroup() throws IOException {
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
			article.setGroup(links.get(1).select("a").text());
			hotList.add(article);
		}
		return new ArticleGroup(hotList, null);
	}

	public static final ArticleGroup getBlogArticleList(Context context, String url) throws IOException, ParseException{
		List<Article> blogList = new ArrayList<Article>();
		Document doc = Jsoup.connect(url).get();
		Elements blocks = doc.select("tr");
		for (Element block : blocks) {
			Elements trs = block.getElementsByTag("td");
			if (trs.size() != 4) {
				continue;
			}
			Article article = new Article();
			article.setContentUrl(trs.get(2).select("a").attr("abs:href"));

			Element viewCount = trs.get(3);
			if(viewCount.getElementsByTag("a").size() > 0) {
				article.setExtraUrl(viewCount.getElementsByTag("a").select("a").attr("abs:href"));
				article.setReplyCount(Integer.valueOf(viewCount.getElementsByTag("a").select("a").text()));
			}
			String viewString = viewCount.text();
			viewString = viewString.substring(viewString.indexOf("/") + 1);
			article.setViewCount(Integer.valueOf(viewString));

			Calendar now = Calendar.getInstance();
			Date value = Constants.DATE_FORMAT2.parse(trs.get(1).text() + " " + String.valueOf(now.get(Calendar.YEAR)));
			article.setTime(value.getTime());
			article.setTitle(trs.get(2).select("a").text());

			blogList.add(article);
		}
		Collections.reverse(blogList);
		
		String allString = doc.toString();
		int i = allString.indexOf("×îÇ°Ò³</a> <a href=");
		String nextTargetURL = null;
		if(i >= 0) {
			Document subDoc = Jsoup.parse(allString.substring(i + 8));
			Elements links = subDoc.getElementsByTag("a");
			nextTargetURL = "http://bbs.nju.edu.cn/" + links.get(0).select("a").attr("href");
		}
		
		return new ArticleGroup(blogList, nextTargetURL);
	}

	public static final ArticleGroup getTopBlogArticleList(Context context) throws IOException, ParseException {
		List<Article> blogList = new ArrayList<Article>();
		Document doc = Jsoup.connect("http://bbs.nju.edu.cn/blogall").get();
		Elements blocks = doc.select("tr");
		for (Element block : blocks) {
			Elements links = block.getElementsByTag("a");
			if (links.size() == 0 || links.get(2).select("a").text().length() == 0) {
				continue;
			}
			Elements trs = block.getElementsByTag("td");
			Article article = new Article();
			article.setAuthorName(links.get(0).select("a").text());
			article.setGroup(links.get(1).select("a").text());
			article.setContentUrl(links.get(2).select("a").attr("abs:href"));
			article.setViewCount(Integer.valueOf(trs.get(4).text()));

			Calendar now = Calendar.getInstance();
			Date value = Constants.DATE_FORMAT2.parse(trs.get(3).text() + " " + String.valueOf(now.get(Calendar.YEAR)));
			article.setTime(value.getTime());
			article.setTitle(links.get(2).select("a").text());
			blogList.add(article);
		}
		return new ArticleGroup(blogList, null);
	}

	public static final ArticleGroup getTopArticleGroup(Context context) throws IOException {
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
			article.setGroup(links.get(1).select("a").text());
			article.setContentUrl(links.get(2).select("a").attr("abs:href"));
			article.setViewCount(Integer.valueOf(links.get(4).text()));
			article.setTitle(links.get(2).select("a").text());
			list.add(article);
		}
		return new ArticleGroup(list, null);
	}

	public static final ArticleGroup getBoardArticleGroup(String url, String boardName, Context context) throws IOException, ParseException {
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
				title = title.substring(2);
			}
			article.setTitle(title);
			article.setContentUrl(links.get(1).select("a").attr("abs:href"));

			Element author = links.get(0).parent();
			if(author != null) {
				Element time = author.nextElementSibling();
				Calendar now = Calendar.getInstance();
				Date value = Constants.DATE_FORMAT2.parse(time.text() + " " + String.valueOf(now.get(Calendar.YEAR)));
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
