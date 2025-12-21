
// import java.util.*;

// enum Status {
//     WAITING, // Process is in ready queue
//     RUNNING, // Process is currently executing
//     COMPLETED   // Process has finished execution
// }

// class Process {

//     String name;
//     int arrival, burst, priority, remainingTime;
//     int waitingTime, turnaroundTime, completionTime;
//     int originalArrival; // Store original arrival time
//     Status status;       // Current status of the process

//     public Process(String name, int arrival, int burst, int priority) {
//         this.name = name;
//         this.arrival = arrival;
//         this.originalArrival = arrival;
//         this.burst = burst;
//         this.priority = priority;
//         this.remainingTime = burst;
//         this.status = Status.WAITING; // Initially all processes are waiting
//     }

//     @Override
//     public String toString() {
//         return name + "(P:" + priority + ", R:" + remainingTime + ", S:" + status + ")";
//     }
// }

// public class PriorityScheduler {

//     /**
//      * Function 1: Select the best process based on priority Returns the process
//      * with the lowest priority number (highest urgency) If priorities are
//      * equal, returns the one with earlier arrival time
//      */
//     public static Process selectBestProcess(List<Process> processes, int currentTime, Process currentProcess) {
//         Process bestProcess = null;

//         for (Process p : processes) {
//             // Process must be ready: arrived, not completed
//             if (p.arrival <= currentTime && p.status != Status.COMPLETED) {
//                 // Select if it's the first candidate or has higher priority (lower number)
//                 if (bestProcess == null || p.priority < bestProcess.priority) {
//                     bestProcess = p;
//                 } else if (p.priority == bestProcess.priority) {
//                     // If priorities are equal, choose the one that arrived earlier
//                     if (p.arrival < bestProcess.arrival) {
//                         bestProcess = p;
//                     }
//                 }
//             }
//         }

//         return bestProcess;
//     }

//     /**
//      * Function 2: Apply aging to all waiting processes Decreases priority
//      * number (increases urgency) for processes that have been waiting Returns
//      * true if any aging occurred
//      */
//     public static boolean applyAging(List<Process> processes, int currentTime, int agingInterval,
//             Map<String, Integer> lastAgeTime, Process currentProcess) {
//         boolean agingOccurred = false;

//         for (Process p : processes) {
//             // Only age processes that are WAITING (not running, not completed)
//             if (p.arrival <= currentTime && p.status == Status.WAITING) {
//                 // Check if aging interval has passed since last age
//                 if (currentTime - lastAgeTime.get(p.name) > agingInterval) {
//                     int oldPriority = p.priority;
//                     p.priority = Math.max(1, p.priority - 1); // Minimum priority is 1
//                     lastAgeTime.put(p.name, currentTime);
//                     if (oldPriority != p.priority) {
//                         System.out.println("  [Aging] Time " + currentTime + ": " + p.name
//                                 + " priority " + oldPriority + " -> " + p.priority);
//                         agingOccurred = true;
//                     }
//                 }
//             }
//         }

//         return agingOccurred;
//     }

//     /**
//      * Main scheduling algorithm
//      */
//     public static void solve(List<Process> processes, int cs, int agingInterval) {
//         int currentTime = 0;
//         int completed = 0;
//         int TotalSize = processes.size();
//         Process currentProcess = null;
//         List<String> executionOrder = new ArrayList<>();

//         // Track aging: maps process name to last time priority was decreased
//         Map<String, Integer> lastAgeTime = new HashMap<>();
//         for (Process p : processes) {
//             lastAgeTime.put(p.name, p.originalArrival);
//         }

//         while (completed < TotalSize) {

//             // STEP 1: Select the highest priority process (lowest priority number)
//             Process bestProcess = selectBestProcess(processes, currentTime, currentProcess);

//             // STEP 2: Apply aging to all waiting processes
//             applyAging(processes, currentTime, agingInterval, lastAgeTime, currentProcess);
//             Process agedBestProcess = selectBestProcess(processes, currentTime, currentProcess);
//             if (agedBestProcess != bestProcess) {
//                 System.out.println("  [Priority Change] Time " + currentTime + ": " + bestProcess.name +
//                                  " replaced by " + agedBestProcess.name + " due to aging");
//                 System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
//                 bestProcess.arrival = currentTime; // Update arrival time before context switch
                
//                 currentTime += cs;
//                 bestProcess = agedBestProcess;
//             }


//             // STEP 3: If no process is ready, advance time (CPU idle)
//             if (bestProcess == null) {
//                 System.out.println("  [Idle] Time " + currentTime + ": CPU idle");
//                 currentTime++;
//                 continue;
//             }

//             // STEP 4: Check if we need to preempt current process
//             if (currentProcess != null && currentProcess != bestProcess && currentProcess.status == Status.RUNNING) {
//                 // Current process is preempted - change status to WAITING
//                 System.out.println("  [Preempt] Time " + currentTime + ": " + currentProcess.name
//                         + " preempted by " + bestProcess.name);

//                 currentProcess.status = Status.WAITING;
//                 // UPDATE ARRIVAL TIME of preempted process to current time
//                 currentProcess.arrival = currentTime;

//                 // Apply context switch time
//                 System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
//                 currentTime += cs;

//                 // After context switch, check if aging occurred during CS time
//                 boolean agingOccurredDuringCS = applyAging(processes, currentTime, agingInterval, lastAgeTime, null);

//                 // If aging occurred, re-select best process
//                 if (agingOccurredDuringCS) {
//                     Process newBestProcess = selectBestProcess(processes, currentTime, null);

//                     // If best process changed, do another context switch
//                     if (newBestProcess != null && newBestProcess != bestProcess) {
//                         System.out.println("  [Priority Change] Time " + currentTime + ": " + bestProcess.name
//                                 + " replaced by " + newBestProcess.name + " due to aging");
//                         System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
//                         currentTime += cs;
//                         bestProcess = newBestProcess;
//                     }
//                 }

//                 // Add new process to execution order
//                 executionOrder.add(bestProcess.name);
//                 currentProcess = bestProcess;
//                 currentProcess.status = Status.RUNNING; // Set new process to RUNNING

//             } else if (currentProcess != bestProcess) {
//                 // First time starting this process or switching from completed process
//                 System.out.println("  [Start] Time " + currentTime + ": Starting " + bestProcess.name);
//                 executionOrder.add(bestProcess.name);
//                 currentProcess = bestProcess;
//                 currentProcess.status = Status.RUNNING; // Set status to RUNNING
//             }

            // // STEP 5: Execute current process for 1 time unit
            // if (currentProcess != null) {
            //     System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
            //     currentProcess.remainingTime--;
            //     currentTime++;
            // }

//             // STEP 6: Check if current process finished
//             if (currentProcess.remainingTime == 0) {
//                 currentProcess.completionTime = currentTime;
//                 currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.originalArrival;
//                 currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burst;
//                 currentProcess.status = Status.COMPLETED; // Set status to COMPLETED
//                 completed++;

//                 System.out.println("  [Complete] Time " + currentTime + ": " + currentProcess.name + " finished -> Status: " + currentProcess.status);

//                 // Context switch after completion (if more processes remain)
//                 if (completed < TotalSize) {
//                     System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
//                     currentTime += cs;
//                 }

//                 currentProcess = null;
//             }
//         }

//         // Print Output
//         System.out.println("\n=== RESULTS ===");
//         System.out.println("Execution Order: " + executionOrder);
//         System.out.println("\nProcess Details:");
//         double totalWait = 0, totalTAT = 0;
//         for (Process p : processes) {
//             System.out.println(p.name + " - Waiting Time: " + p.waitingTime
//                     + ", Turnaround Time: " + p.turnaroundTime
//                     + ", Status: " + p.status);
//             totalWait += p.waitingTime;
//             totalTAT += p.turnaroundTime;
//         }
//         System.out.println("\nAverage Waiting Time: " + String.format("%.2f", totalWait / TotalSize));
//         System.out.println("Average Turnaround Time: " + String.format("%.2f", totalTAT / TotalSize));
//     }

//     public static void main(String[] args) {
//         int contextSwitch = 1;
//         int agingInterval = 4;

//         //--- Primary Test Case ---
//         System.out.println("=== PRIMARY TEST CASE ===");
//         List<Process> processes = new ArrayList<>();
//         processes.add(new Process("P1", 0, 8, 3));
//         processes.add(new Process("P2", 1, 4, 1));
//         processes.add(new Process("P3", 2, 2, 4));
//         processes.add(new Process("P4", 3, 1, 2));
//         processes.add(new Process("P5", 4, 3, 5));
//         solve(processes, contextSwitch, agingInterval);

//         //--- Secondary Test Case ---
//         System.out.println("\n\n=== SECONDARY TEST CASE ===");
//         List<Process> processes1 = new ArrayList<>();
//         processes1.add(new Process("P1", 0, 6, 3));
//         processes1.add(new Process("P2", 0, 3, 1));
//         processes1.add(new Process("P3", 0, 8, 2));
//         processes1.add(new Process("P4", 0, 4, 4));
//         processes1.add(new Process("P5", 0, 2, 5));
//         solve(processes1, contextSwitch, agingInterval);

//         //--- Third Test Case ---
//         System.out.println("\n\n=== THIRD TEST CASE ===");
//         List<Process> processes2 = new ArrayList<>();
//         processes2.add(new Process("P1", 0, 10, 5));
//         processes2.add(new Process("P2", 2, 5, 1));
//         processes2.add(new Process("P3", 5, 3, 2));
//         processes2.add(new Process("P4", 8, 7, 1));
//         processes2.add(new Process("P5", 10, 2, 3));
//         solve(processes2, contextSwitch, agingInterval);

//     }
// }




import java.util.*;

enum Status {
    WAITING,    // Process is in ready queue
    RUNNING,    // Process is currently executing
    COMPLETED   // Process has finished execution
}

class Process {
    String name;
    int arrival, burst, priority, remainingTime;
    int waitingTime, turnaroundTime, completionTime;
    int originalArrival; // Store original arrival time
    Status status;       // Current status of the process

    public Process(String name, int arrival, int burst, int priority) {
        this.name = name;
        this.arrival = arrival;
        this.originalArrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remainingTime = burst;
        this.status = Status.WAITING; // Initially all processes are waiting
    }

    @Override
    public String toString() {
        return name + "(P:" + priority + ", R:" + remainingTime + ", S:" + status + ")";
    }
}

public class PriorityScheduler {

    /**
     * Function 1: Select the best process based on priority
     * Returns the process with the lowest priority number (highest urgency)
     * If priorities are equal, returns the one with earlier arrival time
     */
    public static Process selectBestProcess(List<Process> processes, int currentTime, Process currentProcess) {
        Process bestProcess = null;

        for (Process p : processes) {
            // Process must be ready: arrived, not completed
            if (p.arrival <= currentTime && p.status != Status.COMPLETED) {
                // Select if it's the first candidate or has higher priority (lower number)
                if (bestProcess == null || p.priority < bestProcess.priority) {
                    bestProcess = p;
                } else if (p.priority == bestProcess.priority) {
                    // If priorities are equal, choose the one that arrived earlier
                    if (p.arrival < bestProcess.arrival) {
                        bestProcess = p;
                    }
                }
            }
        }

        return bestProcess;
    }

    /**
     * Function 2: Apply aging to all waiting processes
     * Decreases priority number (increases urgency) for processes that have been waiting
     * Returns true if any aging occurred
     */
    public static boolean applyAging(List<Process> processes, int currentTime, int agingInterval,
                                      Map<String, Integer> lastAgeTime, Process currentProcess) {
        boolean agingOccurred = false;
        
        for (Process p : processes) {
            // Only age processes that are WAITING (not running, not completed)
            if (p.arrival <= currentTime && p.status == Status.WAITING) {
                // Check if aging interval has passed since last age
                if (currentTime - lastAgeTime.get(p.name) >= agingInterval ) {
                    int oldPriority = p.priority;
                    p.priority = Math.max(1, p.priority - 1); // Minimum priority is 1
                    lastAgeTime.put(p.name, currentTime);

                    if (oldPriority != p.priority) {
                        System.out.println("  [Aging] Time " + currentTime + ": " + p.name +
                                         " priority " + oldPriority + " -> " + p.priority);
                        agingOccurred = true;
                    }
                }
            }
        }
        
        return agingOccurred;
    }

    /**
     * Main scheduling algorithm
     */
    public static void solve(List<Process> processes, int cs, int agingInterval) {
        int currentTime = 0;
        int completed = 0;
        int TotalSize = processes.size();
        Process currentProcess = null;
        List<String> executionOrder = new ArrayList<>();

        // Track aging: maps process name to last time priority was decreased
        Map<String, Integer> lastAgeTime = new HashMap<>();
        for (Process p : processes) {
            lastAgeTime.put(p.name, p.originalArrival);
        }

        while (completed < TotalSize) {
            System.out.println("\n[Time " + currentTime + "]" );

            // STEP 1: Select the highest priority process (lowest priority number)
            Process bestProcess = selectBestProcess(processes, currentTime, currentProcess);

            // STEP 2: Apply aging to all waiting processes
            applyAging(processes, currentTime, agingInterval, lastAgeTime, currentProcess);
            Process agedBestProcess = selectBestProcess(processes, currentTime, currentProcess);

            if (agedBestProcess != bestProcess) {
                System.out.println("  [Priority Change] Time " + currentTime + ": " + bestProcess.name +
                                 " replaced by " + agedBestProcess.name + " due to aging");
                System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                bestProcess.arrival = currentTime; // Update arrival time before context switch
                
                currentTime += cs;
                bestProcess = agedBestProcess;
            }


            // STEP 3: If no process is ready, advance time (CPU idle)
            if (bestProcess == null) {
                System.out.println("  [Idle] Time " + currentTime + ": CPU idle");
                currentTime++;
                continue;
            }

            // STEP 4: Check if we need to preempt current process
            if (currentProcess != null && currentProcess != bestProcess && currentProcess.status == Status.RUNNING) {
                // Current process is preempted - change status to WAITING
                System.out.println("  [Preempt] Time " + currentTime + ": " + currentProcess.name +
                                 " preempted by " + bestProcess.name);

                currentProcess.status = Status.WAITING;
                // UPDATE ARRIVAL TIME of preempted process to current time
                currentProcess.arrival = currentTime;

                // Apply context switch time
                System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                currentTime += cs;

                // After context switch, check if aging occurred during CS time
                boolean agingOccurredDuringCS = applyAging(processes, currentTime, agingInterval, lastAgeTime, null);

                // If aging occurred, re-select best process
                if (agingOccurredDuringCS) {
                    Process newBestProcess = selectBestProcess(processes, currentTime, null);
                    
                    // If best process changed, do another context switch
                    if (newBestProcess != null && newBestProcess != bestProcess) {
                        System.out.println("  [Priority Change] Time " + currentTime + ": " + bestProcess.name +
                                         " replaced by " + newBestProcess.name + " due to aging");
                        System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                        currentTime += cs;
                        bestProcess = newBestProcess;
                    }
                }

                // Add new process to execution order
                executionOrder.add(bestProcess.name);
                currentProcess = bestProcess;
                currentProcess.status = Status.RUNNING; // Set new process to RUNNING

            } else if (currentProcess != bestProcess) {
                // First time starting this process or switching from completed process
                System.out.println("  [Start] Time " + currentTime + ": Starting " + bestProcess.name);
                executionOrder.add(bestProcess.name);
                currentProcess = bestProcess;
                currentProcess.status = Status.RUNNING; // Set status to RUNNING
            }

            // // STEP 5: Execute current process for 1 time unit
            // System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
            // currentProcess.remainingTime--;
            // currentTime++;

            // STEP 5: Execute current process for 1 time unit
            if (currentProcess != null) {
                System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
                currentProcess.remainingTime--;
                currentTime++;
            }

            // STEP 6: Check if current process finished
            if (currentProcess.remainingTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.originalArrival;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burst;
                currentProcess.status = Status.COMPLETED; // Set status to COMPLETED
                completed++;

                System.out.println("  [Complete] Time " + currentTime + ": " + currentProcess.name + " finished");

                // Context switch after completion (if more processes remain)
                if (completed < TotalSize) {
                    System.out.println("  [Context Switch] Time " + currentTime + " -> " + (currentTime + cs));
                    currentTime += cs;
                }
                currentProcess = null;
            }
        }

        // Print Output
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
        System.out.println("\nAverage Waiting Time: " + String.format("%.2f", totalWait / TotalSize));
        System.out.println("Average Turnaround Time: " + String.format("%.2f", totalTAT / TotalSize));
    }

    public static void main(String[] args) {
        int contextSwitch = 1;
        int agingInterval = 4;

        // //--- Primary Test Case ---
        // System.out.println("=== PRIMARY TEST CASE ===");
        // List<Process> processes = new ArrayList<>();
        // processes.add(new Process("P1", 0, 8, 3));
        // processes.add(new Process("P2", 1, 4, 1));
        // processes.add(new Process("P3", 2, 2, 4));
        // processes.add(new Process("P4", 3, 1, 2));
        // processes.add(new Process("P5", 4, 3, 5));
        // solve(processes, contextSwitch, agingInterval);

        // //--- Secondary Test Case ---
        // System.out.println("\n\n=== SECONDARY TEST CASE ===");
        // List<Process> processes1 = new ArrayList<>();
        // processes1.add(new Process("P1", 0, 6, 3));
        // processes1.add(new Process("P2", 0, 3, 1));
        // processes1.add(new Process("P3", 0, 8, 2));
        // processes1.add(new Process("P4", 0, 4, 4));
        // processes1.add(new Process("P5", 0, 2, 5));
        // solve(processes1, contextSwitch, agingInterval);

        //--- Third Test Case ---
        System.out.println("\n\n=== THIRD TEST CASE ===");
        List<Process> processes2 = new ArrayList<>();
        processes2.add(new Process("P1", 0, 10, 5));
        processes2.add(new Process("P2", 2, 5, 1));
        processes2.add(new Process("P3", 5, 3, 2));
        processes2.add(new Process("P4", 8, 7, 1));
        processes2.add(new Process("P5", 10, 2, 3));
        solve(processes2, contextSwitch, agingInterval);
        
   
    }
}