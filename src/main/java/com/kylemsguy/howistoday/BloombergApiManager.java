package com.kylemsguy.howistoday;

import java.io.IOException;

import com.bloomberglp.blpapi.*;

public class BloombergApiManager {

	private static Session theSession;

	public static Session getSession() {
		if (theSession == null) {
			try {
				openSession();
			} catch (IOException ie) {
				throw new RuntimeException(ie);
			} catch (InterruptedException ie) {
				throw new RuntimeException(ie);
			}
		}
		return theSession;
	}

	private static void openSession() throws IOException, InterruptedException {
		SessionOptions opt = new SessionOptions();
		opt.setServerHost(System.getProperty("howistoday.host", "10.8.8.1"));
		opt.setServerPort(Integer.parseInt(System.getProperty("howistoday.port", "8194")));
		Session session = new Session(opt);
		if (!session.start()) {
			Event event;
			while ((event = session.tryNextEvent()) != null) {
				for (Message m: event) {
					System.out.println(m);
				}
			}
			throw new RuntimeException("Session failed to start");
		}
		theSession = session;
	}

	public static Service getService(String name) throws IOException, InterruptedException {
		Service retval = getSession().getService(name);
		if (retval != null) return retval;
		if (!getSession().openService(name)) {
			throw new RuntimeException("Cannot open service " + name);
		}
		return getSession().getService(name);
	}
}
