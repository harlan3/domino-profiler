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

package orbisoftware.domino_profiler.runtime_agent;

import java.net.DatagramPacket;
import java.net.InetAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ClientMain extends Thread {

    private int portNumber = 0;
    private boolean useMulticast = false;
    private String fileName = "settings.xml";
    private final Object lock = new Object();
    
    private static ClientMain instance = null;
    private static NetworkSetup networkSetup = null;
    
    private ClientMain() {

        try {
            // Process XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileName);
            Element rootElem = doc.getDocumentElement();

            if (rootElem != null) {
                parseElements(rootElem);
            }

            useMulticast = Boolean.parseBoolean(SharedData.getInstance().xmlMap.get("UseMulticast"));
            portNumber = Integer.parseInt(SharedData.getInstance().xmlMap.get("PortValue"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendUDPMessage(String message) {

        InetAddress ipAddress = null;

        try {

            byte[] msg = message.getBytes();

            if (useMulticast) {
                ipAddress = InetAddress.getByName(SharedData.getInstance().xmlMap.get("MulticastAddress"));
            } else {
                ipAddress = InetAddress.getByName(SharedData.getInstance().xmlMap.get("BroadcastAddress"));
            }

            DatagramPacket datagram = new DatagramPacket(msg, msg.length, ipAddress, portNumber);

            if (useMulticast) {
                SharedSocketInterface.getInstance().getMulticastSocket().send(datagram);
            } else {
                SharedSocketInterface.getInstance().getDatagramSocket().send(datagram);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

                            if (child.getNodeName().equalsIgnoreCase("name")) {
                                name = child.getTextContent();
                            } else if (child.getNodeName().equalsIgnoreCase("value")) {
                                SharedData.getInstance().xmlMap.put(name, child.getTextContent());
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void insertKey(JSONObject jsonObject, String keyPrefix, long updateCount, String keyID, 
    		String thread, long cpuTime, long elapsedTime, float cpuUsage, long memoryUsage) {
		
    	synchronized (lock) {
    		
			jsonObject.put(keyPrefix + ".updateCount", updateCount);
			jsonObject.put(keyPrefix + ".id", keyID);
			jsonObject.put(keyPrefix + ".thread", thread);
			jsonObject.put(keyPrefix + ".cpuTime", cpuTime);
			jsonObject.put(keyPrefix + ".elapsedTime", elapsedTime);
			jsonObject.put(keyPrefix + ".cpuUsage", String.format("%.1f", cpuUsage));
			jsonObject.put(keyPrefix + ".memoryUsage", memoryUsage);
    	}
    }

	@Override
	public void run() {
		
		while (true) {

			synchronized (lock) {
				
				if (!SharedData.getInstance().jsonObject.toString().equals(""))
					sendUDPMessage(SharedData.getInstance().jsonObject.toString());
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) { }
		}
	}
	
	public static ClientMain getInstance() {
		
		if (instance == null) {
			instance = new ClientMain();
			networkSetup = new NetworkSetup();
            instance.start();
		}
		return instance;
	}
}
