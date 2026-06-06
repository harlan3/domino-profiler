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

public class NetworkSetup {

	NetworkSetup() {

		boolean useMulticast = Boolean.parseBoolean(SharedData.getInstance().xmlMap.get("UseMulticast"));
		int portNumber = Integer.parseInt(SharedData.getInstance().xmlMap.get("PortValue"));
		String multicastAddress = SharedData.getInstance().xmlMap.get("MulticastAddress");
		String multicastDeviceAddress = SharedData.getInstance().xmlMap.get("MulticastDeviceAddress");
		String broadcastAddress = SharedData.getInstance().xmlMap.get("BroadcastAddress");

		// Initalize application socket
		SharedSocketInterface.getInstance().initSocket();

		System.out.println("       Listening on port: " + portNumber);

		if (useMulticast) {

			System.out.println("       Multicast Address: " + multicastAddress);
			System.out.println("        Multicast Device: " + multicastDeviceAddress);
		} else {

			System.out.println("       Broadcast Address: " + broadcastAddress);
		}
	}
}
