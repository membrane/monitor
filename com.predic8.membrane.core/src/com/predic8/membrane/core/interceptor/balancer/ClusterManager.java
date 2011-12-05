package com.predic8.membrane.core.interceptor.balancer;

import java.util.*;

import javax.xml.stream.*;

import org.apache.commons.logging.*;

import com.predic8.membrane.core.config.*;

/**
 * Maps pairs of the form (balancer name, cluster name) to Cluster objects.
 */
public class ClusterManager {
	private static Log log = LogFactory.getLog(ClusterManager.class.getName());

	Map<String, Map<String, Cluster>> balancers = new Hashtable<String, Map<String, Cluster>>();
	long timeout = 0;
	long sessionTimeout = 2 * 60 * 60000;

	public ClusterManager() {
		new SessionCleanupThread(balancers, sessionTimeout).start();
	}
	
	public List<String> getBalancers() {
		return new ArrayList<String>(balancers.keySet());
	}

	public void up(String balancerName, String cName, String host, int port) {
		getCluster(balancerName, cName).nodeUp(new Node(host, port));
	}

	public void down(String balancerName, String cName, String host, int port) {
		getCluster(balancerName, cName).nodeDown(new Node(host, port));
	}

	public void takeout(String balancerName, String cName, String host, int port) {
		getCluster(balancerName, cName).nodeTakeOut(new Node(host, port));
	}

	public List<Node> getAllNodesByCluster(String balancerName, String cName) {
		return getCluster(balancerName, cName).getAllNodes(timeout);
	}

	public List<Node> getAvailableNodesByCluster(String balancerName, String cName) {
		return getCluster(balancerName, cName).getAvailableNodes(timeout);
	}

	public void addSession2Cluster(String balancerName, String sessionId, String cName, Node n) {
		getCluster(balancerName, cName).addSession(sessionId, n);
	}

	private Map<String, Cluster> getBalancer(String name) {
		return balancers.get(name);
	}
	
	private Cluster getCluster(String balancerName, String name) {
		Map<String, Cluster> clusters = balancers.get(balancerName);
		if (!clusters.containsKey(name)) // backward-compatibility: auto create clusters as they are accessed
			addCluster(balancerName, name);
		return clusters.get(name);
	}
	
	public List<Cluster> getClusters(String balancerName) {
		return new LinkedList<Cluster>(balancers.get(balancerName).values());
	}

	public boolean addBalancer(String balancerName) {
		if (balancers.containsKey(balancerName))
			return false;
		log.debug("adding balancer with name [" + balancerName + "]");
		HashMap<String, Cluster> clusters = new HashMap<String, Cluster>();
		balancers.put(balancerName, clusters);
		addCluster(balancerName, "Default");
		return true;
	}
	
	public boolean addCluster(String balancerName, String name) {
		Map<String, Cluster> clusters = balancers.get(balancerName);
		if (clusters.containsKey(name))
			return false;
		log.debug("adding cluster with name [" + name + "] to balancer [" + balancerName + "]");
		clusters.put(name, new Cluster(name));
		return true;
	}

	public void removeNode(String balancerName, String cluster, String host, int port) {
		getCluster(balancerName, cluster).removeNode(new Node(host, port));
	}
	
	public AbstractXmlElement getBalancerXMLElement(final String balancerName) {
		return new AbstractXmlElement() {
			@Override
			protected void parseChildren(XMLStreamReader token, String child)
					throws Exception {
				if (token.getLocalName().equals("cluster")) {
					final GenericComplexElement c = new GenericComplexElement();
					c.setChildParser(new AbstractXmlElement() {
						@Override
						protected void parseChildren(XMLStreamReader token, String child)
								throws Exception {
							if (token.getLocalName().equals("node")) {
								GenericComplexElement n = new GenericComplexElement();
								n.parse(token);
								up(balancerName, c.getAttribute("name"), n.getAttribute("host"),
										Integer.parseInt(n.getAttribute("port")));
							} else {
								super.parseChildren(token, child);
							}
						}
					});
					c.parse(token);
				} else {
					super.parseChildren(token, child);
				}
			}

			@Override
			public void write(XMLStreamWriter out) throws XMLStreamException {
				out.writeStartElement("clusters");
				for (Cluster c : getBalancer(balancerName).values()) {
					out.writeStartElement("cluster");
					out.writeAttribute("name", c.getName());

					for (Node n : c.getAllNodes(0)) {
						out.writeStartElement("node");
						out.writeAttribute("host", n.getHost());
						out.writeAttribute("port", "" + n.getPort());
						out.writeEndElement();
					}
					out.writeEndElement();
				}
				out.writeEndElement();
			}
		};
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Node getNode(String balancerName, String cluster, String host, int port) {
		return getCluster(balancerName, cluster).getNode(new Node(host, port));
	}

	public Map<String, Session> getSessions(String balancerName, String cluster) {
		return getCluster(balancerName, cluster).getSessions();
	}

	public List<Session> getSessionsByNode(String balancerName, String cName, Node node) {
		return getCluster(balancerName, cName).getSessionsByNode(node);
	}

	public long getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(long sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

}
