// import java.util.*;

// enum Status {
//     WAITING,    // Process is in ready queue
//     RUNNING,    // Process is currently executing
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
//      * Function 1: Select the best process based on priority
//      * Returns the process with the lowest priority number (highest urgency)
//      * If priorities are equal, returns the one with earlier arrival time
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
//      * Function 2: Apply aging to all waiting processes
//      * Decreases priority number (increases urgency) for processes that have been waiting
//      * Returns true if any aging occurred
//      */
//     public static boolean applyAging(List<Process> processes, int currentTime, int agingInterval,
//                                       Map<String, Integer> lastAgeTime, Process currentProcess) {
//         boolean agingOccurred = false;
        
//         for (Process p : processes) {
//             // Only age processes that are WAITING (not running, not completed)
//             if (p.arrival <= currentTime && p.status == Status.WAITING) {
//                 // Check if aging interval has passed since last age
//                 if (currentTime - lastAgeTime.get(p.name) >= agingInterval ) {
//                     int oldPriority = p.priority;
//                     p.priority = Math.max(1, p.priority - 1); // Minimum priority is 1
//                     lastAgeTime.put(p.name, currentTime);

//                     if (oldPriority != p.priority) {
//                         System.out.println("  [Aging] Time " + currentTime + ": " + p.name +
//                                          " priority " + oldPriority + " -> " + p.priority);
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
//             System.out.println("\n[Time " + currentTime + "]" );

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
//                 System.out.println("  [Preempt] Time " + currentTime + ": " + currentProcess.name +
//                                  " preempted by " + bestProcess.name);

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
//                         System.out.println("  [Priority Change] Time " + currentTime + ": " + bestProcess.name +
//                                          " replaced by " + newBestProcess.name + " due to aging");
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

//             // // STEP 5: Execute current process for 1 time unit
//             // System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
//             // currentProcess.remainingTime--;
//             // currentTime++;

//             // STEP 5: Execute current process for 1 time unit
//             if (currentProcess != null) {
//                 System.out.println("  [Execute] Time " + currentTime + ": " + currentProcess);
//                 currentProcess.remainingTime--;
//                 currentTime++;
//             }

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
//             System.out.println(p.name + " - Waiting Time: " + p.waitingTime +
//                              ", Turnaround Time: " + p.turnaroundTime +
//                              ", Status: " + p.status);
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

//         // //--- Secondary Test Case ---
//         // System.out.println("\n\n=== SECONDARY TEST CASE ===");
//         // List<Process> processes1 = new ArrayList<>();
//         // processes1.add(new Process("P1", 0, 6, 3));
//         // processes1.add(new Process("P2", 0, 3, 1));
//         // processes1.add(new Process("P3", 0, 8, 2));
//         // processes1.add(new Process("P4", 0, 4, 4));
//         // processes1.add(new Process("P5", 0, 2, 5));
//         // solve(processes1, contextSwitch, agingInterval);

        // //--- Third Test Case ---
        // System.out.println("\n\n=== THIRD TEST CASE ===");
        // List<Process> processes2 = new ArrayList<>();
        // processes2.add(new Process("P1", 0, 10, 5));
        // processes2.add(new Process("P2", 2, 5, 1));
        // processes2.add(new Process("P3", 5, 3, 2));
        // processes2.add(new Process("P4", 8, 7, 1));
        // processes2.add(new Process("P5", 10, 2, 3));
        // solve(processes2, contextSwitch, agingInterval);
        
   
//     }
// }


// import java.util.*;

// enum Status {
//     WAITING,    // Process is in ready queue
//     RUNNING,    // Process is currently executing
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
//         return name + "(P:" + priority + ", R:" + remainingTime + ")";
//     }
// }

// public class PriorityScheduler {

//     /**
//      * Function 1: Select the best process based on priority
//      */
//     public static Process selectBestProcess(List<Process> processes, int currentTime) {
//         Process bestProcess = null;

//         for (Process p : processes) {
//             // Process must be ready: arrived, not completed
//             if (p.arrival <= currentTime && p.status != Status.COMPLETED) {
//                 // Select if it's the first candidate or has higher priority (lower number)
//                 if (bestProcess == null || p.priority < bestProcess.priority) {
//                     bestProcess = p;
//                 } else if (p.priority == bestProcess.priority) {
//                     // Tie-breaker: choose the one that arrived earlier
//                     if (p.arrival < bestProcess.arrival) {
//                         bestProcess = p;
//                     }
//                 }
//             }
//         }
//         return bestProcess;
//     }

//     /**
//      * Function 2: Apply aging to all waiting processes
//      * Runs continuously to check if any process deserves a priority boost
//      */
//     public static void applyAging(List<Process> processes, int currentTime, int agingInterval,
//                                          Map<String, Integer> lastAgeTime) {
        
//         for (Process p : processes) {
//             // Only age processes that are waiting and have arrived
//             if (p.arrival <= currentTime && p.status != Status.COMPLETED) {
                
//                 // Calculate how long since the last aging occurred for this specific process
//                 int lastTime = lastAgeTime.get(p.name);
                
//                 // Check if the interval has passed
//                 if (currentTime >= lastTime + agingInterval) {
//                     if (p.priority > 1) { // Cap priority at 1
//                         int oldPriority = p.priority;
//                         p.priority--;
                        
//                         // Update the last age time to the current time
//                         lastAgeTime.put(p.name, lastAgeTime.get(p.name) + agingInterval);

//                         System.out.println("  [Aging] Time " + currentTime + ": " + p.name +
//                                 " priority " + oldPriority + " -> " + p.priority);
//                     } else {
//                         // Even if priority is 1, we bump the time so we don't check uselessly
//                         lastAgeTime.put(p.name, lastAgeTime.get(p.name) + agingInterval);
//                     }
//                 }
//             }
//         }
//     }

//     /**
//      * Main scheduling algorithm
//      */
//     public static void solve(List<Process> processes, int cs, int agingInterval) {
//         int currentTime = 0;
//         int completed = 0;
//         int n = processes.size();
//         Process currentProcess = null;
//         List<String> executionOrder = new ArrayList<>();

//         // Initialize aging map
//         Map<String, Integer> lastAgeTime = new HashMap<>();
//         for (Process p : processes) {
//             lastAgeTime.put(p.name, p.originalArrival);
//         }

//         while (completed < n) {
            
//             // 1. Check for Aging at the start of the tick
//             // (Note: In the array code, aging usually happens before selection check)
//             applyAging(processes, currentTime, agingInterval, lastAgeTime);

//             // 2. Select the best candidate
//             Process bestProcess = selectBestProcess(processes, currentTime);

//             // 3. Handle Idle CPU
//             if (bestProcess == null) {
//                 System.out.println("  [Idle] Time " + currentTime);
//                 currentTime++;
//                 continue;
//             }

//             // 4. Check for Context Switch
//             // Switch happens if:
//             // a) We were running something (currentProcess != null) AND
//             // b) The best choice is different from what we were running
//             if (currentProcess != bestProcess) {
                
//                 // If we were effectively running someone else, preempt them
//                 if (currentProcess != null && currentProcess.status == Status.RUNNING) {
//                      currentProcess.status = Status.WAITING;
//                      currentProcess.arrival = currentTime; // Logically return to queue
//                 }

//                 // PERFORM CONTEXT SWITCH (Simulate time passing)
//                 // We loop 'cs' times. During this time, aging must still continue!
//                 int switchEnd = currentTime + cs;
//                 System.out.println("  [Context Switch] Time " + currentTime + " -> " + switchEnd + 
//                                    " (Switching to " + bestProcess.name + ")");
                
//                 while(currentTime < switchEnd) {
//                     currentTime++;
//                     applyAging(processes, currentTime, agingInterval, lastAgeTime);
//                 }

//                 // Update state after switch
//                 currentProcess = bestProcess;
//                 currentProcess.status = Status.RUNNING;
//                 executionOrder.add(currentProcess.name);
                
//                 // We do NOT execute immediately. We go back to start of loop 
//                 // because the switch might have caused another aging event or arrival.
//                 continue; 
//             }

//             // 5. Execution (1 Tick)
//             // If we are here, currentProcess == bestProcess and the switch is done/not needed.
//             currentProcess.remainingTime--;
//             currentTime++;
            
//             // 6. Completion Check
//             if (currentProcess.remainingTime == 0) {
//                 currentProcess.status = Status.COMPLETED;
//                 completed++;
//                 currentProcess.completionTime = currentTime;
//                 currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.originalArrival;
//                 currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burst;

//                 System.out.println("  [Complete] Time " + currentTime + ": " + currentProcess.name + " finished.");

//                 // If there are still processes remaining, we simulate context switch overhead
//                 // immediately after completion (preparing for the next one)
//                 if (completed < n) {
//                     int switchEnd = currentTime + cs;
//                     System.out.println("  [Context Switch] Time " + currentTime + " -> " + switchEnd + " (After Completion)");
//                     while(currentTime < switchEnd) {
//                         currentTime++;
//                         applyAging(processes, currentTime, agingInterval, lastAgeTime);
//                     }
//                 }
                
//                 // Reset current process so selection logic runs fresh next loop
//                 currentProcess = null;
//             }
//         }

//         // Print Output
//         System.out.println("\n=== RESULTS ===");
//         System.out.println("Execution Order: " + executionOrder);
//         System.out.println("\nProcess Details:");
//         System.out.println("Name\tWait\tTurnaround\tStatus");
//         double totalWait = 0, totalTAT = 0;
        
//         // Sort by name for clean output
//         processes.sort(Comparator.comparing(p -> p.name));
        
//         for (Process p : processes) {
//             System.out.printf("%s\t%d\t%d\t\t%s\n", p.name, p.waitingTime, p.turnaroundTime, p.status);
//             totalWait += p.waitingTime;
//             totalTAT += p.turnaroundTime;
//         }
//         System.out.println("\nAverage Waiting Time: " + String.format("%.2f", totalWait / n));
//         System.out.println("Average Turnaround Time: " + String.format("%.2f", totalTAT / n));
//     }

//     public static void main(String[] args) {
//         int contextSwitch = 1;
//         int agingInterval = 5; // Updated to 5 to match your previous example logic if needed


//     }
// }


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
        int contextSwitch = 1;
        int agingInterval = 3;
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
        processes1.add(new Process("P2", 0, 8, 1));
        processes1.add(new Process("P3", 0, 7, 2));
        processes1.add(new Process("P4", 0, 7, 4));
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