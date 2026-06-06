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

import java.util.Random;

import org.openjdk.jol.info.GraphLayout;

import orbisoftware.domino_profiler.runtime_agent.PerformanceTimer.TimerResult;

public class MainApplication {
    
	static long updateCount = 0;
	static long lastLatchTime1;
	static long lastLatchTime2;
	
	public static void main(String[] args) {

		ClientMain.getInstance();
		
		while (true) {

			updateCount++;
			
			profileMethod1();
			profileMethod2();

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void profileMethod1() {
		
		PerformanceTimer perfTimer = new PerformanceTimer();
		perfTimer.startTimer();
		
		for (int x=0; x<1000000; x++) {
			Random random2 = new Random();
		}
			
		TimerResult timerResults = perfTimer.stopTimer();

		if (timerResults.logValidResults()) {
			
			ClientMain.getInstance().insertKey(SharedData.getInstance().jsonObject, 
					"key1", updateCount, "MainApplication.profileMethod1", "workerthread", 
					timerResults.getCpuTimeMicroSec(), 
					timerResults.getElapsedTimeMicroSec(), 
					timerResults.getCpuUsagePercent(),
					GraphLayout.parseInstance(SharedData.getInstance()).totalSize());
			lastLatchTime1 = System.currentTimeMillis();
		}
		
        // Clear out the stale data after 5 seconds
        if ((System.currentTimeMillis() - lastLatchTime1 > 5000))
        {
            ClientMain.getInstance().insertKey(SharedData.getInstance().jsonObject,
                    "key1", updateCount, "MainApplication.profileMethod1", "workerthread",
                    0, 0, 0, 0);
        }
	}
	
	private static void profileMethod2() {
		
		PerformanceTimer perfTimer = new PerformanceTimer();
		perfTimer.startTimer();
		
		for (int x=0; x<1000000; x++) {
			Random random2 = new Random();
		}
			
		TimerResult timerResults = perfTimer.stopTimer();

		if (timerResults.logValidResults()) {
			
			ClientMain.getInstance().insertKey(SharedData.getInstance().jsonObject, 
					"key2", updateCount, "MainApplication.profileMethod2", "workerthread", 
					timerResults.getCpuTimeMicroSec(), 
					timerResults.getElapsedTimeMicroSec(), 
					timerResults.getCpuUsagePercent(),
					GraphLayout.parseInstance(SharedData.getInstance()).totalSize());
			lastLatchTime2 = System.currentTimeMillis();
		}
		
        // Clear out the stale data after 5 seconds
        if ((System.currentTimeMillis() - lastLatchTime2 > 5000))
        {
            ClientMain.getInstance().insertKey(SharedData.getInstance().jsonObject,
                    "key2", updateCount, "MainApplication.profileMethod2", "workerthread",
                    0, 0, 0, 0);
        }
	}
}
