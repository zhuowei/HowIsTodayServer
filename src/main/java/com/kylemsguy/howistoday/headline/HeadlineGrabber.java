package com.kylemsguy.howistoday.headline;

import java.util.*;

/* you can never use too much XML */
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class HeadlineGrabber {

	public static List<Headline> headlines = new ArrayList<Headline>();

	public static List<String> getLatestRedditHeadlines() throws Exception {
		List<String> retval = new ArrayList<String>();
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().
			parse("http://www.reddit.com/r/worldnews.xml");
		NodeList titles = document.getElementsByTagName("title");
		for (int i = 0; i < titles.getLength(); i++) {
			Node n = titles.item(i);
			String textContent = n.getTextContent();
			if (textContent.equals("World News")) continue;
			retval.add(textContent);
		}
		return retval;
	}

	public static void updateOnce() {
		try {
			headlines.clear();
			List<String> headlinesStr = getLatestRedditHeadlines();
			for (String s: headlinesStr) {
				headlines.add(new Headline(s, FlappyClassifier.checkSentiment(s)));
			}
			Collections.sort(headlines);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getSuitableHeadline(double val) {
		if (headlines.size() == 0) return "Toronto Leafs win Grey Cup";
		if (val >= 0) {
			return headlines.get(0).text;
		}
		return headlines.get(headlines.size() - 1).text;
	}

	public static void startUpdateThread() {
		Thread leThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try {
						updateOnce();
						Thread.sleep(30 * 60 * 1000); // 30 min
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		leThread.setDaemon(true);
		leThread.start();
	}

	public static void main(String[] args) throws Exception {
		startUpdateThread();
		Thread.sleep(1000000);
	}

	public static class Headline implements Comparable {
		public final String text;
		public final int score;
		public Headline(String text, int score) {
			this.text = text;
			this.score = score;
		}
		@Override
		public int compareTo(Object obj) {
			if (obj instanceof Headline) {
				return ((Headline) obj).score > this.score? 1: (((Headline) obj).score == this.score? 0: -1);
			}
			return 0;
		}

		@Override
		public String toString() {
			return text + ":"  + score;
		}
	}
}
