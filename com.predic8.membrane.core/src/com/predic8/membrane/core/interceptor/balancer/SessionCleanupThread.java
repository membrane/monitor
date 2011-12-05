package com.predic8.membrane.core.interceptor.balancer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionCleanupThread extends Thread {
	private static Log log = LogFactory.getLog(SessionCleanupThread.class.getName());
	
	Map<String, Map<String, Cluster>> balancers;

	private long sessionTimeout;
	
	public SessionCleanupThread(Map<String, Map<String, Cluster>> balancers, long sessionTimeout) {
		super("SessionCleanupThread");
		this.balancers = balancers;
		this.sessionTimeout = sessionTimeout;
	}
	
	@Override
	public void run() {
		try {
			sleep(10000); //TODO without exceptions are thrown because log4j is not ready.
		} catch (InterruptedException e1) {
		}
		
		log.debug("SessionCleanupThread started");
		
		while (true) {
			synchronized (balancers) {
				
				log.debug("cleanup started");
				
				long time = System.currentTimeMillis();
				int size = 0;
				int cleaned = 0;
				for (Map<String, Cluster> m : balancers.values()) {
					for (Cluster c : m.values()) {
						synchronized (c.getSessions()) {
							size = c.getSessions().size();
							cleaned = cleanupSessions(c);									
						}
					}
				}
				log.debug(""+ cleaned +" sessions removed of "+ size +" in " +(System.currentTimeMillis()-time)+"ms");
			}
			
			try {
				sleep(15000);
			} catch (InterruptedException e) {
				log.warn(e.getMessage()); 
			}
		}					
	}

	private int cleanupSessions(Cluster c) {
		Collection<Session> ss = c.getSessions().values();
		Iterator<Session> sIt = ss.iterator();
		int cleaned = 0;
		
		while (sIt.hasNext()) {
			Session s = sIt.next();
			if ( System.currentTimeMillis()-s.getLastUsed() > sessionTimeout ) {
				cleaned++;
				sIt.remove();
			}
		}
		return cleaned;
	}	
}
