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
	public String getIt(@QueryParam("a") String wat) {
		try {
			Session session = BloombergApiManager.getSession();
			synchronized(session) {
				Service refService = BloombergApiManager.getService("//blp/refdata");
				Request req = refService.createRequest("ReferenceDataRequest");
				req.append("securities", wat);
				req.append("fields", "News Sentiment");
				CorrelationID d_cid = session.sendRequest(req, null);
				while (true) {
					Event event = session.nextEvent();
					MessageIterator msgIter = event.messageIterator();
					while (msgIter.hasNext()) {
						Message msg = msgIter.next();
						if (msg.correlationID() == d_cid) {
							System.out.println(msg);
						}
					}
					if (event.eventType() == Event.EventType.RESPONSE) {
						break;
					}
				}
			}
			return "yo";
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String[] getSecuritiesByCategory(String name) {
		if (name.equals("finance")) {
			return new String[] {
				"CM CN Equity"
			};
		}
		return null;
	}
}
