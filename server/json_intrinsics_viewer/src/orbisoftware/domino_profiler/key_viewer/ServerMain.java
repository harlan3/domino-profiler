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

import java.net.DatagramPacket;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServerMain {

	private String fileName = "settings.xml";
	private boolean shutdown = false;
	private Table table;

	private final int MAX_RECORD_SETS = 10;

	private int[] recordSetNumberKeys = new int[MAX_RECORD_SETS];

	public ServerMain() {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(fileName);
			Element rootElem = doc.getDocumentElement();

			if (rootElem != null) {
				parseElements(rootElem);
			}
		} catch (Exception e) {

			System.out.println("Exception in loadXML(): " + e.toString());
		}
	}

	private void parseElements(Element root) {

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

	public void setRecordSetIndex(int recordSetIndex, int numberKeys) {

		recordSetNumberKeys[recordSetIndex] = numberKeys;
	}

	public int calculateRowNumber(int recordSetIndex, int keyIndex) {

		int rowNumber = 0;

		for (int i = 1; i < recordSetIndex; i++) {
			rowNumber += recordSetNumberKeys[i];
		}

		return rowNumber + keyIndex;
	}

	public void createTable(Display display, Shell shell) {

		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumn(table, "Row", 130);
		createColumn(table, "Update Count", 130);
		createColumn(table, "Class.Method Identifier", 350);
		createColumn(table, "Thread Identifier", 250);
		createColumn(table, "CPU Time (μs)", 130);
		createColumn(table, "Elapsed Time (μs)", 130);
		createColumn(table, "CPU Usage (%)", 130);
		createColumn(table, "Memory Usage (bytes)", 150);
	}

	private static void createColumn(Table table, String title, int width) {
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(title);
		column.setWidth(width);
	}

	public void serverMainMethod(Display display, Shell shell) {

		shell.setText("Domino Profiler - JSON Intrinsics Viewer");
		ServerMain serverMain = new ServerMain();

		ProcessDatagramThread processDatagramThread = new ProcessDatagramThread();
		processDatagramThread.start();

		createTable(display, shell);

		shell.open();
		
		shell.addShellListener(new ShellAdapter() {
		    @Override
		    public void shellClosed(ShellEvent e) {
				try {
					Thread.sleep(200);
					serverMain.shutdown = true;
					processDatagramThread.shutdownReq();
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					e1.printStackTrace();
				}
		    }
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
					serverMain.shutdown = true;
					processDatagramThread.shutdownReq();
				} catch (InterruptedException e2) {
					Thread.currentThread().interrupt();
					e2.printStackTrace();
				}
			}
		});

		while (!serverMain.shutdown) {

			List<DatagramPacket> datagramPacketList = processDatagramThread.getQueuedData();

			// System.out.println("received: " + datagramPacketList.size());

			for (DatagramPacket packet : datagramPacketList) {

				String json = new String(packet.getData(), packet.getOffset(), packet.getLength());
				// System.out.println("contents: " + json);

				JSONObject jsonObject = new JSONObject(json);
				int recordSet = 0;
				int numberKeys = 0;

				try {
					recordSet = jsonObject.getShort("json_intrinsics.recordSet");
					numberKeys = jsonObject.getShort("json_intrinsics.numberKeys");

				} catch (org.json.JSONException e) {
					 e.printStackTrace();
				}
				setRecordSetIndex(recordSet, numberKeys);

				for (int keyIndex = 1; keyIndex <= numberKeys; keyIndex++) {

					int rowToUpdate = calculateRowNumber(recordSet, keyIndex);
					int keyFinal = keyIndex;

					display.asyncExec(() -> {
						TableItem item;

						if (table.isDisposed()) {
							return;
						}

						if ((rowToUpdate - 1) >= table.getItemCount())
							item = new TableItem(table, SWT.NONE);
						else
							item = table.getItem(rowToUpdate - 1);

						if (item.isDisposed()) {
							return;
						}

						try {
							String keyPrefix = "key" + Integer.toString(keyFinal) + ".";

							long updateCount = jsonObject.getLong(keyPrefix + "updateCount");
							String id = jsonObject.getString(keyPrefix + "id");
							String thread = jsonObject.getString(keyPrefix + "thread");
							long cpuTime = jsonObject.getLong(keyPrefix + "cpuTime");
							long elapsedTime = jsonObject.getLong(keyPrefix + "elapsedTime");
							float cpuUsage = jsonObject.getFloat(keyPrefix + "cpuUsage");
							long memoryUsage = jsonObject.getLong(keyPrefix + "memoryUsage");

							item.setText(0, Integer.toString(rowToUpdate));
							item.setText(1, Long.toString(updateCount));
							item.setText(2, id);
							item.setText(3, thread);
							item.setText(4, Long.toString(cpuTime));
							item.setText(5, Long.toString(elapsedTime));
							item.setText(6, Float.toString(cpuUsage));
							item.setText(7, Long.toString(memoryUsage));

						} catch (org.json.JSONException e) {
							// e.printStackTrace();
						}
					});
				}
			}
			
			// SWT thread process events
			if (!shell.isDisposed()) {
				while (display.readAndDispatch()) {
				}
			}

			// Background thread sleep
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
