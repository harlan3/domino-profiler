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

#include "ClientMain.hpp"

#include <iostream>
#include <random>
#include <boost/thread.hpp>
#include <iostream>
#include <string>

#include "util/Base64.hpp"
#include "JSONPacket.hpp"
#include "PublishDatagramThread.hpp"
#include "PerformanceTimer.hpp"
#include "SharedData.hpp"

using namespace std;

static bool shutdownRequested = false;

static long updateCount = 0;
static long lastLatchTime1;
static long lastLatchTime2;
static boost::mutex g_mutex;
static PublishDatagramThread* publishDatagramThread;

#ifdef WIN32
#include <windows.h> 

BOOL WINAPI consoleHandlerClient(DWORD signal) 
{

    if (signal == CTRL_C_EVENT)
        shutdownRequested = true;

    return TRUE;
}
#elif UNIX

void sigint_handler(int sig) {

    shutdownRequested = true;
}
#endif

ClientMain::ClientMain() 
{
    SharedData::getInstance()->appConfig.loadXml("config.xml");
}

void ClientMain::insertKey(json* jsonObject, std::string keyPrefix, long long updateCount, std::string keyID,
    std::string thread, long long cpuTime, long long elapsedTime, float cpuUsage, long long memoryUsage) {

    boost::mutex::scoped_lock lock(g_mutex);

    (*jsonObject)[keyPrefix + ".updateCount"] = updateCount;
    (*jsonObject)[keyPrefix + ".id"] = keyID;
    (*jsonObject)[keyPrefix + ".thread"] = thread;
    (*jsonObject)[keyPrefix + ".cpuTime"] = cpuTime;
    (*jsonObject)[keyPrefix + ".elapsedTime"] = elapsedTime;
    (*jsonObject)[keyPrefix + ".cpuUsage"] = std::round(cpuUsage * 100.0 * 10.0f) / 10.0f;
    (*jsonObject)[keyPrefix + ".memoryUsage"] = memoryUsage;
}

long long ClientMain::getMilliseconds()
{
    return std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::steady_clock::now().time_since_epoch()
    ).count();
}

int main(int argc, char* argv[])
{

#ifdef WIN32
    SetConsoleCtrlHandler(consoleHandlerClient, TRUE);
#elif UNIX
    signal(SIGINT, sigint_handler);
#endif

    ClientMain ClientMain;
    publishDatagramThread = new PublishDatagramThread();

    publishDatagramThread->start();

    int count = 0;

    while (true) {

        updateCount++;

        ClientMain.profileMethod1();
        ClientMain.profileMethod2();

        publishDatagramThread->sendJSONString(SharedData::getInstance()->jsonObject->dump());

        boost::this_thread::sleep(boost::posix_time::milliseconds(30));
    }

    publishDatagramThread->shutdownReq();

    return 0;
}

void ClientMain::profileMethod1() {

    PerformanceTimer* perfTimer = new PerformanceTimer();
    perfTimer->startTimer();

    for (int x = 0; x < 100000; x++) {
        std::random_device rd;
        std::mt19937 gen(rd());

       //std::uniform_int_distribution<int> dist(1, 10);
    }

    PerformanceTimer::TimerResult timerResults = perfTimer->stopTimer();

    if (timerResults.logValidResults()) {

        insertKey(SharedData::getInstance()->jsonObject,
            "key1", updateCount, "ClientMain.profileMethod1", "workerthread",
            timerResults.getCpuTimeMicroSec(),
            timerResults.getElapsedTimeMicroSec(),
            timerResults.getCpuUsagePercent(),
            sizeof(*SharedData::getInstance()));
        lastLatchTime1 = getMilliseconds();
    }

    // Clear out the stale data after 5 seconds
    if ((getMilliseconds() - lastLatchTime1 > 5000))
    {
        insertKey(SharedData::getInstance()->jsonObject,
            "key1", updateCount, "ClientMain.profileMethod1", "workerthread",
            0, 0, 0, 0);
    }
}

void ClientMain::profileMethod2() {

    PerformanceTimer* perfTimer = new PerformanceTimer();
    perfTimer->startTimer();

    for (int x = 0; x < 100000; x++) {
        std::random_device rd;
        std::mt19937 gen(rd());

      //  std::uniform_int_distribution<int> dist(1, 10);
    }

    PerformanceTimer::TimerResult timerResults = perfTimer->stopTimer();

    if (timerResults.logValidResults()) {

        insertKey(SharedData::getInstance()->jsonObject,
            "key2", updateCount, "ClientMain.profileMethod2", "workerthread",
            timerResults.getCpuTimeMicroSec(),
            timerResults.getElapsedTimeMicroSec(),
            timerResults.getCpuUsagePercent(),
            sizeof(*SharedData::getInstance()));
        lastLatchTime1 = getMilliseconds();
    }

    // Clear out the stale data after 5 seconds
    if ((getMilliseconds() - lastLatchTime1 > 5000))
    {
        insertKey(SharedData::getInstance()->jsonObject,
            "key2", updateCount, "ClientMain.profileMethod2", "workerthread",
            0, 0, 0, 0);
    }
}
