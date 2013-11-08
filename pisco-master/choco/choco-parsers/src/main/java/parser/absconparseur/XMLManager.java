/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.absconparseur;

import choco.kernel.common.logging.ChocoLogging;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class XMLManager {

	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private static final boolean useStyleSheet = true; // true; // TODO //true; //false; // true // a style sheet can seriously degrade performances

	private static final class ErrorHandler extends DefaultHandler {
		
		private final MessageFormat message = new MessageFormat("({0}: {1}, {2}): {3}");

		private void print(SAXParseException x) {
			String msg = message.format(new Object[] { x.getSystemId(), x.getLineNumber(), x.getColumnNumber(), x.getMessage() });
			LOGGER.info(msg);
		}
		
		@Override
		public void warning(SAXParseException x) {
			print(x);
		}

		@Override
		public void error(SAXParseException x) {
			print(x);
		}

		@Override
		public void fatalError(SAXParseException x) throws SAXParseException {
			print(x);
			throw x;
		}
	}
		private static void dealWithException(Exception e) {
			if (e instanceof SAXParseException) {
				SAXParseException ee = (SAXParseException) e;
				LOGGER.log(Level.WARNING,"\n** Parsing error" + ", line " + ee.getLineNumber() + ", uri " + ee.getSystemId(),e);
			} else if (e instanceof SAXException) {
				SAXException ee = (SAXException) e;
				Exception x = (ee.getException() == null ? ee : ee.getException());
				x.printStackTrace();
			} else if (e instanceof TransformerConfigurationException) {
				LOGGER.log(Level.WARNING,"\n** Transformer Factory error\n",e);
			} else if (e instanceof TransformerException) {
				LOGGER.log(Level.WARNING,"\n** Transformation error", e);
			} else {
				LOGGER.log(Level.WARNING,"Unknown error", e);
			}
			System.exit(1);
		}
	
		public static Document createNewDocument() {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				return builder.newDocument();
			} catch (ParserConfigurationException e) {
				dealWithException(e);
				return null;
			}
		}

		/**
		 * Build a DOM object that corresponds to the given input stream.
		 * 
		 * @param is the input stream that denotes the XML document to be loaded.
		 * @param schemaUrl the schema to be used (<code> null </code> if not used) to validate the document
		 * @return a DOM object
		 */
		public static Document load(InputStream is, URL schemaUrl) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(true);
				if (schemaUrl != null) {
					SchemaFactory schemaFactory = SchemaFactory.newInstance(InstanceTokens.W3C_XML_SCHEMA);
					Schema schema = schemaFactory.newSchema(schemaUrl); // new File(schemaFileName));
					documentBuilderFactory.setSchema(schema);
				}
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				documentBuilder.setErrorHandler(new ErrorHandler());
				return documentBuilder.parse(is);
			} catch (Exception e) {
				dealWithException(e);
				return null;
			}
		}

		
		public static Document load(File file, URL schemaUrl) {
			try {
				return load(new FileInputStream(file), schemaUrl);
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.INFO, "File {0} does not exist", file);
				System.exit(1);
				return null;
			}
		}

		public static Document load(InputStream is) {
			return load(is, null);
		}

		public static Document load(File file) {
			return load(file, null);
		}

		public static Document load(String fileName) {
			if (fileName.endsWith("xml.bz2")) {
				try {
					Process p = Runtime.getRuntime().exec("bunzip2 -c " + fileName);
					Document document = load(p.getInputStream());
					p.waitFor();
					p.exitValue();
					p.destroy();
					return document;

				} catch (Exception e) {
					LOGGER.log(Level.INFO, "Problem with {0}", fileName);
					System.exit(1);
					return null;
				}
			}
			return load(new File(fileName), null);
		}

		private static Transformer buildTransformer(InputStream styleSheetInputStream) {
			try {
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				if (styleSheetInputStream == null || !useStyleSheet)
					return transformerFactory.newTransformer();
				Document document = load(styleSheetInputStream, null);
				DOMSource source = new DOMSource(document);
				return transformerFactory.newTransformer(source);
			} catch (TransformerConfigurationException e) {
				dealWithException(e);
				return null;
			}
		}

		public static void save(Document document, PrintWriter writer, InputStream styleSheetInputStream) {
			try {
				Transformer transformer = buildTransformer(styleSheetInputStream);
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);
			} catch (TransformerException e) {
				dealWithException(e);
			}
		}

		public static void save(Document document, String fileName, InputStream styleSheetInputStream) {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
				save(document, out, styleSheetInputStream);
				out.close();
			} catch (IOException e) {
				dealWithException(e);
			}
		}

		public static void save(Document document, String fileName) {
			save(document, fileName, (InputStream)null);
		}

		public static void save(Document document, String fileName, String styleSheetFileName) {
			save(document, fileName, styleSheetFileName == null ? null : XMLManager.class.getResourceAsStream(styleSheetFileName));
		}

		public static Element getElementByTagNameFrom(Element element, String tagName, int i) {
			NodeList nodeList = element.getElementsByTagName(tagName);
			if (nodeList == null || nodeList.getLength() <= i)
				return null;
			return (Element) (nodeList.item(i));
		}

		public static Element getFirstElementByTagNameFromRoot(Document document, String tagName) {
			return getElementByTagNameFrom(document.getDocumentElement(), tagName, 0);
		}

		public static void deleteElementByTagNameFrom(Element element, String tagName, int i) {
			NodeList list = element.getElementsByTagName(tagName);
			if (list.getLength() <= i)
				throw new IllegalArgumentException();
			element.removeChild(list.item(i));
		}

		public static Node getChildElement(Document document, String elementName, int index) {
			NodeList list = document.getElementsByTagName(elementName);
			return list.item(index);
		}

		public static void deleteChildElement(Document document, String elementName, int index) {
			Element element = (Element) document.getElementsByTagName(elementName).item(0);
			element.removeChild(getChildElement(document, elementName, index));
	}

		public static String[] displayAttributes(Element element) {
			NamedNodeMap map = element.getAttributes();
			String[] values = new String[map.getLength()];
			for (int i = 0; i < map.getLength(); i++) {
				Attr attribut = (Attr) map.item(i);
				values[i] = attribut.getValue();
				LOGGER.info(attribut.getName() + " = " + values[i]);
			}
			return values;
		}

	public static void displayElement(Document document, String elementName) {
		Node node = document.getElementsByTagName(elementName).item(0);
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			displayAttributes(element);
		}
		}

	}
