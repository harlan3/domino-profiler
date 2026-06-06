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

import java.net.*;
import java.beans.*;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.beans.PropertyChangeListener;

public class ProcessDatagramThread extends Thread implements PropertyChangeListener {

	private BlockingQueue<DatagramPacket> queue = new LinkedBlockingQueue<DatagramPacket>();
	private boolean shutdown = false;
	
	public void propertyChange(PropertyChangeEvent evt) {

		byte[] buffer = new byte[((DatagramPacket) evt.getNewValue()).getLength()];

		if (evt.getPropertyName().toString().equals("datagramReceived")) {

			synchronized (queue) {

				// Make a copy of the datagram packet, as the referenced object
				// is owned by the other thread.
				System.arraycopy(((DatagramPacket) evt.getNewValue()).getData(), 0, buffer, 0, buffer.length);

				DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

				queue.add(receivedPacket);
				queue.notify();
			}
		}
	}

	public List<DatagramPacket> getQueuedData() {

		List<DatagramPacket> datagramList = new LinkedList<DatagramPacket>();

		synchronized (queue) {
			queue.drainTo(datagramList);
		}

		return datagramList;
	}
	
	public void shutdownReq() {
		shutdown = true;
	}
	
	public void run() {

		ReceiveDatagramThread receiveDatagramThread = new ReceiveDatagramThread();

		receiveDatagramThread.getPropertyChangeSupport().addPropertyChangeListener(this);
		receiveDatagramThread.start();

		while (!shutdown) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		receiveDatagramThread.shutdownReq();
	}
}
