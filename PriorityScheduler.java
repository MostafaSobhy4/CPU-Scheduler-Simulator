import java.util.*;

class Process {
    String name;
    int arrival, burst, priority, remainingTime;
    int waitingTime, turnaroundTime, completionTime;

    public Process(String name, int arrival, int burst, int priority) {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remainingTime = burst;
    }
}

public class PriorityScheduler {
    public static void solve(List<Process> processes, int cs, int agingInterval) {
        int currentTime = 0;
        int completed = 0;
        int TotalSize = processes.size();
        Process currentProcess = null;
        List<String> executionOrder = new ArrayList<>();
        
        // Track aging: maps process name to last time it was "aged"
        Map<String, Integer> lastAgeTime = new HashMap<>();
        for (Process p : processes) lastAgeTime.put(p.name, p.arrival);

        while (completed < TotalSize) {
            // 1. Handle Aging for processes in the ready queue 
            for (Process p : processes) {
                if (p.arrival <= currentTime && p.remainingTime > 0 && p != currentProcess) { // this means process is arrived and waiting (not finished) and not currently executing
                    if (currentTime - lastAgeTime.get(p.name) >= agingInterval) {
                        p.priority = Math.max(0, p.priority - 1); 
                        // p.priority = p.priority > 0 ? p.priority - 1 : 0 ; // Increase priority (lower number)
                        lastAgeTime.put(p.name, currentTime);
                    }
                }
            }

            // 2. Find process with highest priority (lowest value) 
            Process bestProcess = null;
            for (Process p : processes) {
                if (p.arrival <= currentTime && p.remainingTime > 0) {
                    if (bestProcess == null || p.priority < bestProcess.priority) {
                        bestProcess = p;
                    }
                }
            }

            if (bestProcess == null) {
                currentTime++;
                continue;
            }

            // 3. Handle Context Switch 
            if (currentProcess != null && currentProcess != bestProcess && currentProcess.remainingTime > 0) {
                currentTime += cs;
                // Update execution order
                executionOrder.add(bestProcess.name);
            } else if (currentProcess == null || (currentProcess != bestProcess)) { // for first time execution or switch
                executionOrder.add(bestProcess.name);
            }

            currentProcess = bestProcess;
            currentProcess.remainingTime--;
            currentTime++;

            // 4. If process finishes
            if (currentProcess.remainingTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrival;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burst;
                completed++;
                currentTime += cs; // Context switch after finishing
                currentProcess = null; 
            }
        }

        // Print Output to match your requirements
        System.out.println("executionOrder: " + executionOrder);
        double totalWait = 0, totalTAT = 0;
        for (Process p : processes) {
            System.out.println(p.name + " waitingTime: " + p.waitingTime + " turnaroundTime: " + p.turnaroundTime);
            totalWait += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.println("Average WT: " + (totalWait / TotalSize));
        System.out.println("Average TAT: " + (totalTAT / TotalSize));
    }

    public static void main(String[] args) {
        int contextSwitch = 1;
        int agingInterval = 5; 

        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 8, 3));
        processes.add(new Process("P2", 1, 4, 1));
        processes.add(new Process("P3", 2, 2, 4));
        processes.add(new Process("P4", 3, 1, 2));
        processes.add(new Process("P5", 4, 3, 5));
        solve(processes, contextSwitch, agingInterval);

        //--- Secondary Test Case ---
        List<Process> processes1 = new ArrayList<>();
        processes1.add(new Process("P1", 0, 6, 3));
        processes1.add(new Process("P2", 0, 3, 1));
        processes1.add(new Process("P3", 0, 8, 2));
        processes1.add(new Process("P4", 0, 4, 4));
        processes1.add(new Process("P5", 0, 2, 5));
        solve(processes1, contextSwitch, agingInterval);

    }

}