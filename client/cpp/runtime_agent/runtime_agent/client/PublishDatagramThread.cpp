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

#include "PublishDatagramThread.hpp"
#include "SharedData.hpp"

#include <iostream>
#include <string>
#include <boost/asio.hpp>

#include <iostream>

void PublishDatagramThread::start() 
{

    shutdown = false;

    publishThread = new boost::thread(
        boost::bind(&PublishDatagramThread::runPublishThread, this));
}

void PublishDatagramThread::sendJSONString(string jsonString) 
{

    JSONPacket packet;

    strcpy_s(packet.data, sizeof(packet.data), jsonString.c_str());
    packet.length = strlen(jsonString.c_str());

    queue.push(packet);
}

void PublishDatagramThread::runPublishThread()
{
	try {
		// Setup the I/O context required by all Asio programs
		boost::asio::io_context io_ctx;

		// Define destination port
		const unsigned short port = SharedData::getInstance()->appConfig.getMulticastPort();

		// Create the endpoints
		boost::asio::ip::address_v4 interface_address = boost::asio::ip::address_v4::from_string(SharedData::getInstance()->appConfig.getMulticastDeviceAddress());
		boost::asio::ip::address_v4 multicast_address = boost::asio::ip::address_v4::from_string(SharedData::getInstance()->appConfig.getMulticastAddress());
		boost::asio::ip::udp::endpoint destination_endpoint(multicast_address, port);

		// Open the IPv4 UDP socket
		boost::asio::ip::udp::socket socket(io_ctx);
		socket.open(boost::asio::ip::udp::v4());

		// Specify the physical interface outbound multicast data must use
		boost::asio::ip::multicast::outbound_interface option(interface_address);
		socket.set_option(option);

		while (!shutdown) {

			int queueSize = queue.size();

			for (int i = 0; i < queueSize; i++)
			{
				JSONPacket packet;

				queue.front(packet);
				socket.send_to(boost::asio::buffer(packet.data, packet.length), destination_endpoint);

				boost::this_thread::sleep(boost::posix_time::milliseconds(30));
			}
		}
	}
	catch (const std::exception& e) {
		std::cerr << "Exception: " << e.what() << std::endl;
	}
}

void PublishDatagramThread::shutdownReq() 
{

    shutdown = true;
}
