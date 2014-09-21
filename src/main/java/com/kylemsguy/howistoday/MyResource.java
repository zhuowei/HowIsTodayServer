package com.kylemsguy.howistoday;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.bloomberglp.blpapi.*;
import com.kylemsguy.howistoday.headline.*;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	public String getIt(@QueryParam("subject") String subject) {
		try {
			String[] stockNames = getSecuritiesByCategory(subject);
			if (stockNames == null) {
				if (Main.isDebug()) {
					stockNames = new String[] {subject};
				} else {
					return "{\"0\": 0, \"1\": \"Canucks win Stanley Cup; riots start anyways\"}";
				}
			}
			Session session = BloombergApiManager.getSession();
			double sum = 0;
			synchronized(session) {
				Service refService = BloombergApiManager.getService("//blp/refdata");
				Request req = refService.createRequest("ReferenceDataRequest");
				for (String s: stockNames) {
					req.append("securities", s);
				}
				req.append("fields", "News Sentiment");
				CorrelationID d_cid = session.sendRequest(req, null);
				while (true) {
					Event event = session.nextEvent();
					MessageIterator msgIter = event.messageIterator();
					while (msgIter.hasNext()) {
						Message msg = msgIter.next();
						if (msg.correlationID() == d_cid) {
							if (Main.isDebug()) System.out.println(msg);
							Element allVals = msg.getElement("securityData");
							for (int i = 0; i < allVals.numValues(); i++) {
								double val = allVals.getValueAsElement(i).
									getElement("fieldData").
									getElementAsFloat64("News Sentiment");
								if (Main.isDebug()) System.out.println("Yo, it's " + val);
								sum += val;
							}
						}
					}
					if (event.eventType() == Event.EventType.RESPONSE) {
						break;
					}
				}
			}
			double avg = sum / stockNames.length;
			String headline = HeadlineGrabber.getSuitableHeadline(avg);
			if (headline.length() > 100) {
				headline = headline.substring(0, 100) + "...";
			}
			return "{\"0\":" + avg + ", \"1\":\"" + headline + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String[] getSecuritiesByCategory(String name) {
		if (name == null) return null;
		if (name.equals("Finance")) {
			return new String[] {
				"JPM US Equity",
				"BAC US Equity",
				"C US Equity",
				"WFC US Equity"
			};
		} else if (name.equals("SciTech")) {
			return new String[] {
				"AAPL US Equity",
				"GOOG US Equity",
				"AMZN US Equity",
				"MSFT US Equity"
			};
		}

		return null;
	}
}
