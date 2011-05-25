/* Copyright 2009 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.ws.relocator;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import static com.predic8.membrane.core.Constants.*;

public class Relocator {
	XMLEventWriter writer;
	
	public static final QName ADDRESS_SOAP11 = new QName(WSDL_SOAP11_NS, "address");
	public static final QName ADDRESS_SOAP12 = new QName(WSDL_SOAP12_NS, "address");
	public static final QName ADDRESS_HTTP = new QName(WSDL_HTTP_NS, "address");
	
	public static final QName IMPORT = new QName(XSD_NS, "import");
	public static final QName INCLUDE = new QName(XSD_NS, "include");
	
	private String host;
	private int port;
	private String protocol;
	
	
	private class ReplaceIterator implements Iterator<Attribute> {

		Iterator<Attribute> attrs;
		String replace;
		
		public ReplaceIterator(String replace, Iterator<Attribute> attrs) {
			this.replace = replace;
			this.attrs = attrs;
		}
		
		public boolean hasNext() {
			return attrs.hasNext();
		}

		public Attribute next() {
			Attribute atr = attrs.next();
			if ( atr.getName().equals(new QName(replace)) && atr.getValue().startsWith("http") ) {
				return XMLEventFactory.newInstance().createAttribute(replace,getNewLocation(atr));
			}
			return atr;
		}

		private String getNewLocation(Attribute atr) {
			try {
				URL oldURL = new URL(atr.getValue());
				if (port == -1) {
					return new URL(protocol, host, oldURL.getFile()).toString();
				} 
				return new URL(protocol, host, port, oldURL.getFile()).toString();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return "";
		}

		public void remove() {
			attrs.remove();
		}
		
	}
	
	public Relocator( OutputStream ostream, String protocol, String host, int port ) throws Exception {
		XMLOutputFactory output = XMLOutputFactory.newInstance();
		this.writer = output.createXMLEventWriter(ostream);
		this.host = host;
		this.port = port;
		this.protocol = protocol;
	}
	

	public void relocate( InputStream istream ) throws Exception {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader parser = factory.createXMLEventReader(istream);
		
		while (parser.hasNext()) {
			XMLEvent event = parser.nextEvent();
			if ( event.isStartElement() ) {
				if ( isInNamespace(event)) {
					event = replace(event, "location");
				} else if ( getElementName(event).equals(INCLUDE)) {
					event = replace(event, "schemaLocation");
				} else if ( getElementName(event).equals(IMPORT)) {
					event = replace(event, "schemaLocation");
				}
			}
			writer.add(event);
		}
	}


	private boolean isInNamespace(XMLEvent event) {
		return getElementName(event).equals(ADDRESS_SOAP11) || getElementName(event).equals(ADDRESS_SOAP12) || getElementName(event).equals(ADDRESS_HTTP);
	}


	private QName getElementName(XMLEvent event) {
		return event.asStartElement().getName();
	}

	@SuppressWarnings("unchecked")
	private XMLEvent replace(XMLEvent event, String attribute) {
		XMLEventFactory fac = XMLEventFactory.newInstance();
		StartElement startElement = event.asStartElement();
		return fac.createStartElement(startElement.getName(), 
				new ReplaceIterator(attribute, startElement.getAttributes()), 
				startElement.getNamespaces());
	}
}
