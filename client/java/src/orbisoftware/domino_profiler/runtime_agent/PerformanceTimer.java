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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import com.sun.management.OperatingSystemMXBean;

public class PerformanceTimer {

    OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    private boolean running;

    private long initialCpuTime;
    private long initialUpTime;
    
    public void startTimer() {

        initialCpuTime = osBean.getProcessCpuTime();
        initialUpTime = runtimeBean.getUptime();
        
        running = true;
    }

    public TimerResult stopTimer() {
    	
        if (!running) {
            throw new IllegalStateException("Timer has not been started.");
        }
        
        long finalCpuTime = osBean.getProcessCpuTime();
        long finalUpTime = runtimeBean.getUptime();

        long elapsedCpu = finalCpuTime - initialCpuTime;
        long elapsedWallTime = (finalUpTime - initialUpTime) * 1_000; // convert milliseconds to microseconds
        int availableProcessors = osBean.getAvailableProcessors();
        
        float processCpuLoad = ((float) elapsedCpu / elapsedWallTime) / availableProcessors;

        running = false;

        return new TimerResult(elapsedWallTime, elapsedCpu, processCpuLoad);
    }

    public static class TimerResult {
        private final long elapsedTimeMicroSec;
        private final long cpuTimeMicroSec;
        private final float cpuUsagePercent;

        public TimerResult(
                long elapsedTimeMicroSec,
                long cpuTimeMicroSec,
                float cpuUsagePercent
        ) {
            this.elapsedTimeMicroSec = elapsedTimeMicroSec;
            this.cpuTimeMicroSec = cpuTimeMicroSec;
            this.cpuUsagePercent = cpuUsagePercent;
        }

        public long getElapsedTimeMicroSec() {
            return elapsedTimeMicroSec;
        }

        public long getCpuTimeMicroSec() {
            return cpuTimeMicroSec;
        }

        public float getCpuUsagePercent() {
            return cpuUsagePercent;
        }

        public boolean logValidResults() {

            return (elapsedTimeMicroSec > 0 && cpuTimeMicroSec > 0);
        }
        
        @Override
        public String toString() {
            return "TimerResult{" +
                    "elapsedTimeMicroSec=" + elapsedTimeMicroSec +
                    ", cpuTimeMicroSec=" + cpuTimeMicroSec +
                    ", cpuUsagePercent=" + cpuUsagePercent +
                    '}';
        }
    }
}