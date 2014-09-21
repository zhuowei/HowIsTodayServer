package com.kylemsguy.howistoday.headline;

import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/* ba-ding! */

public class FlappyClassifier {

	private static StanfordCoreNLP pipeline;

	public static StanfordCoreNLP getPipeline() {
		if (pipeline != null) return pipeline;
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
		return pipeline;
	}

	public static int checkSentiment(String text) {
		int sum = 0;
		int count = 0;
		Annotation annotation = getPipeline().process(text);
		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
			int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
			//System.out.println("Sentiment: " + sentiment + " String: " + sentence.toString());
			sum += sentiment;
			count++;
		}
		return (int) (sum / (double)count);
	}
}
