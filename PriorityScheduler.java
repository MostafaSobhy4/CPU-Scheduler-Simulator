import java.util.*;

enum Status {
    WAITING,
    RUNNING,
    COMPLETED
}

class Process {
    String name;
    int arrival, burst, priority, remainingTime;
    int waitingTime, turnaroundTime, completionTime;
    int originalArrival;
    Status status;

    public Process(String name, int arrival, int burst, int priority) {
        this.name = name;
        this.arrival = arrival;
        this.originalArrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remainingTime = burst;
        this.status = Status.WAITING;
    }

    @Override
    public String toString() {
        return name + "(P:" + priority + ", R:" + remainingTime + ", S:" + status + ")";
    }
}

public class PriorityScheduler {

    public static Process selectBestProcess(List<Process> processes, int currentTime) {
        Process bestProcess = null;

        for (Process p : processes) {
            if (p.arrival <= currentTime && p.status != Status.COMPLETED) {
                if (bestProcess == null || p.priority < bestProcess.priority) {
                    bestProcess = p;
                } else if (p.priority == bestProcess.priority) {
                    if (p.originalArrival < bestProcess.originalArrival) {
                        bestProcess = p;
                    }
                }
            }
        }
        return bestProcess;
    }

    public static void applyAgingAtTime(List<Process> processes, int time, int agingInterval,
                                        Map<String, Integer> nextAgeTime, Process excludeProcess) {
        for (Process p : processes) {
            if (p.arrival <= time && p.status == Status.WAITING && p != excludeProcess) {
                if (time >= nextAgeTime.get(p.name)) {
                    if (p.priority > 1) {
                        int oldPriority = p.priority;
                        p.priority--;
                        System.out.println("  [Aging] Time " + time + ": " + p.name +
                                         " priority " + oldPriority + " -> " + p.priority);
                    }
                    nextAgeTime.put(p.name, time + agingInterval);
                }
            }
        }
    }

    public static void solve(List<Process> processes, int cs, int agingInterval) {
        int currentTime = 0;
        int completed = 0;
        int totalSize = processes.size();
        Process currentProcess = null;
        List<String> executionOrder = new ArrayList<>();
        boolean hasExecuted = false;

        Map<String, Integer> nextAgeTime = new HashMap<>();
        for (Process p : processes) {
            nextAgeTime.put(p.name, p.arrival + agingInterval);
        }

        while (completed < totalSize) {
            // If no current process, select one
            if (currentProcess == null) {
                Process bestProcess = selectBestProcess(processes, currentTime);

                if (bestProcess == null) {
                    System.out.println("  [Idle] Time " + currentTime + ": CPU idle");
                    currentTime++;
                    continue;
                }

                System.out.println("  [Start] Time " + currentTime + ": Starting " + bestProcess.name);
                executionOrder.add(bestProcess.name);
                currentProcess = bestProcess;
                currentProcess.status = Status.RUNNING;
                hasExecuted = false;
            }

            // Apply aging to waiting processes
            applyAgingAtTime(processes, currentTime, agingInterval, nextAgeTime, currentProcess);

            // Check if we need to preempt
            Process bestProcess = selectBestProcess(processes, currentTime);

            if (currentProcess != bestProcess) {
                // Preemption needed
                System.out.println("  [Preempt] Time " + currentTime + ": " + currentProcess.name +
                                 " preempted by " + bestProcess.name);

                currentProcess.status = Status.WAITING;
                
                // Update arrival time if it has executed
                if (hasExecuted) {
                    currentProcess.arrival = currentTime;
                    nextAgeTime.put(currentProcess.name, currentTime + agingInterval);
                }

                // Context switch
                System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                
                // Age during context switch
                for (int t = currentTime; t < currentTime + cs; t++) {
                    applyAgingAtTime(processes, t, agingInterval, nextAgeTime, bestProcess);
                }
                
                currentTime += cs;

                System.out.println("  [Start] Time " + currentTime + ": Starting " + bestProcess.name);
                executionOrder.add(bestProcess.name);
                currentProcess = bestProcess;
                currentProcess.status = Status.RUNNING;
                hasExecuted = false;
                continue;
            }

            // Execute current process for 1 time unit
            System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
            currentProcess.remainingTime--;
            currentTime++;
            hasExecuted = true;

            // Check if process completed
            if (currentProcess.remainingTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.originalArrival;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burst;
                currentProcess.status = Status.COMPLETED;
                completed++;

                System.out.println("  [Complete] Time " + currentTime + ": " + currentProcess.name + 
                                 " finished -> Status: " + currentProcess.status);

                // Context switch after completion (if more processes remain)
                if (completed < totalSize) {
                    System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                    
                    // Age during context switch
                    for (int t = currentTime; t < currentTime + cs; t++) {
                        applyAgingAtTime(processes, t, agingInterval, nextAgeTime, null);
                    }
                    
                    currentTime += cs;
                }

                currentProcess = null;
            }
        }

        // Print results
        System.out.println("\n=== RESULTS ===");
        System.out.println("Execution Order: " + executionOrder);
        System.out.println("\nProcess Details:");
        double totalWait = 0, totalTAT = 0;
        for (Process p : processes) {
            System.out.println(p.name + " - Waiting Time: " + p.waitingTime +
                             ", Turnaround Time: " + p.turnaroundTime +
                             ", Status: " + p.status);
            totalWait += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.println("\nAverage Waiting Time: " + String.format("%.2f", totalWait / totalSize));
        System.out.println("Average Turnaround Time: " + String.format("%.2f", totalTAT / totalSize));
        System.out.println("Total Context Switches: " + Math.max(0, executionOrder.size() - 1));
    }

    public static void main(String[] args) {
        int contextSwitch = 2;
        int agingInterval = 6;
        //--- First Test Case ---
        System.out.println("=== FIRST TEST CASE ===");
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 8, 3));
        processes.add(new Process("P2", 1, 4, 1));
        processes.add(new Process("P3", 2, 2, 4));
        processes.add(new Process("P4", 3, 1, 2));
        processes.add(new Process("P5", 4, 3, 5));
        solve(processes, contextSwitch, agingInterval);

        //--- Second Test Case ---
        System.out.println("\n\n=== SECOND TEST CASE ===");
        List<Process> processes1 = new ArrayList<>();
        processes1.add(new Process("P1", 0, 6, 3));
        processes1.add(new Process("P2", 0, 3, 1));
        processes1.add(new Process("P3", 0, 8, 2));
        processes1.add(new Process("P4", 0, 4, 4));
        processes1.add(new Process("P4", 0, 2, 5));
        solve(processes1, contextSwitch, agingInterval);

        //--- Third Test Case ---
        System.out.println("\n\n=== THIRD TEST CASE ===");
        List<Process> processes2 = new ArrayList<>();
        processes2.add(new Process("P1", 0, 10, 5));
        processes2.add(new Process("P2", 2, 5, 1));
        processes2.add(new Process("P3", 5, 3, 2));
        processes2.add(new Process("P4", 8, 7, 1));
        processes2.add(new Process("P5", 10, 2, 3));
        solve(processes2, contextSwitch, agingInterval);


        //--- Fourth Test Case ---
        System.out.println("\n\n=== FOURTH TEST CASE ===");
        List<Process> processes3 = new ArrayList<>();
        processes3.add(new Process("P1", 0, 12, 2));
        processes3.add(new Process("P2", 4, 9, 3));
        processes3.add(new Process("P3", 8, 15, 1));
        processes3.add(new Process("P4", 12, 6, 4));
        processes3.add(new Process("P5", 16, 11, 2));
        processes3.add(new Process("P6", 20, 5, 5));
        solve(processes3, contextSwitch, agingInterval);

        //--- Fifth Test Case ---
        System.out.println("\n\n=== FIFTH TEST CASE ===");
        List<Process> processes4 = new ArrayList<>();
        processes4.add(new Process("P1", 0, 3, 3));
        processes4.add(new Process("P2", 1, 2, 1));
        processes4.add(new Process("P3", 2, 4, 2));
        processes4.add(new Process("P4", 3, 1, 4));
        processes4.add(new Process("P5", 4, 3, 5));
        solve(processes4, contextSwitch, agingInterval);


        //--- Sixth Test Case ---
        System.out.println("\n\n=== SIXTH TEST CASE ===");
        List<Process> processes5 = new ArrayList<>();
        processes5.add(new Process("P1", 0, 14, 4));
        processes5.add(new Process("P2", 3, 7, 2));
        processes5.add(new Process("P3", 6, 10, 5));
        processes5.add(new Process("P4", 9, 5, 1));
        processes5.add(new Process("P5", 12, 8, 3));
        processes5.add(new Process("P6", 15, 4, 6));
        solve(processes5, contextSwitch, agingInterval);
    }
}