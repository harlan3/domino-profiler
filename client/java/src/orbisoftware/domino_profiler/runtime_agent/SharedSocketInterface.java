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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class SharedSocketInterface {

    private static SharedSocketInterface instance = null;

    private static DatagramSocket datagramSocket = null;
    private static MulticastSocket multicastSocket = null;

    public static SharedSocketInterface getInstance() {

        if (instance == null) {
            instance = new SharedSocketInterface();
        }
        return instance;
    }

    public MulticastSocket getMulticastSocket() {

        return multicastSocket;
    }

    public DatagramSocket getDatagramSocket() {

        return datagramSocket;
    }

    public void initSocket() {

        boolean useMulticast = Boolean.parseBoolean(SharedData.getInstance().xmlMap.get("UseMulticast"));
        int portNumber = Integer.parseInt(SharedData.getInstance().xmlMap.get("PortValue"));

        if (useMulticast) {

            try {

                InetAddress multicastAddress = InetAddress.getByName(SharedData.getInstance().xmlMap.get("MulticastAddress"));
                InetAddress multicastDeviceAddress = InetAddress.getByName(SharedData.getInstance().xmlMap.get("MulticastDeviceAddress"));
                multicastSocket = new MulticastSocket(portNumber);
                
                // Explicitly join the group on the specified interface
                NetworkInterface netIf = NetworkInterface.getByInetAddress(multicastDeviceAddress);
                multicastSocket.setNetworkInterface(netIf);
                multicastSocket.joinGroup(new InetSocketAddress(multicastAddress, portNumber), netIf);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            try {
                datagramSocket = new DatagramSocket(portNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
