/*
	MIT License
	
	Copyright (c) Harlan Murphy
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
*/

package orbisoftware.domino_profiler.key_viewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SWTApplication {
    
	private static void parseElements(Element root) {

		String name = "";

		if (root != null) {

			NodeList nl = root.getChildNodes();

			if (nl != null) {

				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);

					if (node.getNodeName().equalsIgnoreCase("setting")) {

						NodeList childNodes = node.getChildNodes();

						for (int j = 0; j < childNodes.getLength(); j++) {

							Node child = childNodes.item(j);

							if (child.getNodeName().equalsIgnoreCase("name"))
								name = child.getTextContent();
							else if (child.getNodeName().equalsIgnoreCase("value"))
								SharedData.getInstance().xmlMap.put(name, child.getTextContent());
						}
					}
				}
			}
		}
	}
	
    public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		ServerMain serverMain = new ServerMain();

		try {
			// Process XML
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse("settings.xml");
			Element rootElem = doc.getDocumentElement();

			if (rootElem != null) {
				SWTApplication.parseElements(rootElem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		serverMain.serverMainMethod(display, shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();

	}
}