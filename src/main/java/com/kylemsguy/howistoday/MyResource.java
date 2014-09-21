package com.kylemsguy.howistoday;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.bloomberglp.blpapi.*;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
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
				// DISABLE THIS IN PROD!!!1111
				stockNames = new String[] {subject};
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
							System.out.println(msg);
							//Element allVals = msg.getElementByName("securityData[]");
							//for (int i = 0; i < allVals.numValues(); i++) {
							//	//double val = allVals.get
							//}	
						}
					}
					if (event.eventType() == Event.EventType.RESPONSE) {
						break;
					}
				}
			}
			double avg = 0;
			String headline = "Have a nice day";
			return "{0:" + avg + ", 1:\"" + headline + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String[] getSecuritiesByCategory(String name) {
		if (name.equals("Finance")) {
			return new String[] {
				"JPM US Equity",
				"BAC US Equity",
				"C US Equity",
				"WFC US Equity"
			};
		} else if (name.equals("Science and Technology")) {
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
