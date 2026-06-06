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

#pragma once

#include <chrono>
#include <cstdint>
#include <sstream>
#include <stdexcept>
#include <string>
#include <thread>

#if defined(_WIN32)
    #ifndef NOMINMAX
        #define NOMINMAX
    #endif
    #include <windows.h>
#else
    #include <sys/resource.h>
    #include <sys/time.h>
    #include <unistd.h>
#endif

class PerformanceTimer {
public:
    class TimerResult {
    public:
        TimerResult(
            std::int64_t elapsedTimeMicroSec,
            std::int64_t cpuTimeMicroSec,
            float cpuUsagePercent
        )
            : elapsedTimeMicroSec_(elapsedTimeMicroSec),
              cpuTimeMicroSec_(cpuTimeMicroSec),
              cpuUsagePercent_(cpuUsagePercent) {
        }

        std::int64_t getElapsedTimeMicroSec() const {
            return elapsedTimeMicroSec_;
        }

        std::int64_t getCpuTimeMicroSec() const {
            return cpuTimeMicroSec_;
        }

        float getCpuUsagePercent() const {
            return cpuUsagePercent_;
        }

        bool logValidResults() const {
            return elapsedTimeMicroSec_ > 0 && cpuTimeMicroSec_ > 0;
        }

        std::string toString() const {
            std::ostringstream out;
            out << "TimerResult{"
                << "elapsedTimeMicroSec=" << elapsedTimeMicroSec_
                << ", cpuTimeMicroSec=" << cpuTimeMicroSec_
                << ", cpuUsagePercent=" << cpuUsagePercent_
                << '}';
            return out.str();
        }

    private:
        const std::int64_t elapsedTimeMicroSec_;
        const std::int64_t cpuTimeMicroSec_;
        const float cpuUsagePercent_;
    };

    PerformanceTimer()
        : running_(false),
          initialCpuTimeMicroSec_(0),
          initialWallTime_(Clock::now()) {
    }

    void startTimer() {
        initialCpuTimeMicroSec_ = getProcessCpuTimeMicroSec();
        initialWallTime_ = Clock::now();
        running_ = true;
    }

    TimerResult stopTimer() {
        if (!running_) {
            throw std::logic_error("Timer has not been started.");
        }

        const std::int64_t finalCpuTimeMicroSec = getProcessCpuTimeMicroSec();
        const Clock::time_point finalWallTime = Clock::now();

        const std::int64_t elapsedCpuMicroSec =
            finalCpuTimeMicroSec - initialCpuTimeMicroSec_;

        const std::int64_t elapsedWallTimeMicroSec =
            std::chrono::duration_cast<std::chrono::microseconds>(
                finalWallTime - initialWallTime_).count();

        const unsigned int availableProcessors = getAvailableProcessors();

        float processCpuLoad = 0.0f;
        if (elapsedWallTimeMicroSec > 0 && availableProcessors > 0) {
            processCpuLoad =
                (static_cast<float>(elapsedCpuMicroSec) /
                 static_cast<float>(elapsedWallTimeMicroSec)) /
                static_cast<float>(availableProcessors);
        }

        running_ = false;

        return TimerResult(
            elapsedWallTimeMicroSec,
            elapsedCpuMicroSec,
            processCpuLoad
        );
    }

private:
    using Clock = std::chrono::steady_clock;

    static unsigned int getAvailableProcessors() {
        const unsigned int count = std::thread::hardware_concurrency();
        return count == 0 ? 1U : count;
    }

    static std::int64_t getProcessCpuTimeMicroSec() {
#if defined(_WIN32)
        FILETIME creationTime;
        FILETIME exitTime;
        FILETIME kernelTime;
        FILETIME userTime;

        if (!GetProcessTimes(
                GetCurrentProcess(),
                &creationTime,
                &exitTime,
                &kernelTime,
                &userTime)) {
            throw std::runtime_error("Unable to get process CPU time.");
        }

        return fileTimeToMicroSec(kernelTime) + fileTimeToMicroSec(userTime);
#else
        struct rusage usage;
        if (getrusage(RUSAGE_SELF, &usage) != 0) {
            throw std::runtime_error("Unable to get process CPU time.");
        }

        const std::int64_t userMicroSec =
            static_cast<std::int64_t>(usage.ru_utime.tv_sec) * 1000000LL +
            static_cast<std::int64_t>(usage.ru_utime.tv_usec);

        const std::int64_t systemMicroSec =
            static_cast<std::int64_t>(usage.ru_stime.tv_sec) * 1000000LL +
            static_cast<std::int64_t>(usage.ru_stime.tv_usec);

        return userMicroSec + systemMicroSec;
#endif
    }

#if defined(_WIN32)
    static std::int64_t fileTimeToMicroSec(const FILETIME& fileTime) {
        ULARGE_INTEGER value;
        value.LowPart = fileTime.dwLowDateTime;
        value.HighPart = fileTime.dwHighDateTime;

        // FILETIME is stored in 100-nanosecond units.
        return static_cast<std::int64_t>(value.QuadPart / 10ULL);
    }
#endif

    bool running_;
    std::int64_t initialCpuTimeMicroSec_;
    Clock::time_point initialWallTime_;
};
