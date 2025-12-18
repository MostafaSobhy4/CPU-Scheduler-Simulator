// import java.util.*;

// class Process {
//     String name;
//     int arrivalTime;
//     int burstTime;
//     int originalPriority; // Stores the initial priority
//     int currentPriority;  // Changes dynamically to solve starvation or aging problem 
//     int remainingTime;
//     int waitingTime;
//     int turnaroundTime;
//     int completionTime;
//     int lastExecutionTime; // Helps track waiting for aging

//     public Process(String name, int arrivalTime, int burstTime, int priority) {
//         this.name = name;
//         this.arrivalTime = arrivalTime;
//         this.burstTime = burstTime;
//         this.originalPriority = priority;
//         this.currentPriority = priority;
//         this.remainingTime = burstTime;
//         this.lastExecutionTime = arrivalTime; // Initially arrived
//     }
// }

// public class PriorityScheduling {

//     // Aging Configuration: Decrease priority number (increase importance) every X units of waiting
//     static final int AGING_THRESHOLD = 5; 

//     public static void solve(List<Process> inputProcesses) {
//         int currentTime = 0;
//         int completedCount = 0;
//         int n = inputProcesses.size();
//         Process previousProcess = null;

//         // Deep copy or use the list directly depending on integration needs
//         List<Process> processes = new ArrayList<>(inputProcesses);
//         List<String> executionOrder = new ArrayList<>();

//         while (completedCount < n) {
//             // 1. Filter processes that have arrived and are not complete
//             List<Process> readyQueue = new ArrayList<>();
//             for (Process p : processes) {
//                 if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
//                     readyQueue.add(p);
//                 }
//             }

//             // 2. Solve Starvation: Apply Aging Logic
//             // If a process is waiting, improve its priority
//             for (Process p : readyQueue) {
//                 // Calculate wait time relative to the current moment
//                 // Note: Simple aging adds 1 priority level for every AGING_THRESHOLD wait units
//                 if (p != previousProcess) {
//                    int waitDuration = currentTime - p.arrivalTime - (p.burstTime - p.remainingTime);
//                    if (waitDuration > 0 && waitDuration % AGING_THRESHOLD == 0) {
//                        // Decrementing value means higher priority (0 is highest)
//                        if (p.currentPriority > 0) {
//                            p.currentPriority--;
//                        }
//                    }
//                 }
//             }

//             if (!readyQueue.isEmpty()) {
//                 // 3. Sort Ready Queue: 
//                 // Primary: Priority (Ascending), Secondary: Arrival Time (FCFS)
//                 readyQueue.sort((p1, p2) -> {
//                     if (p1.currentPriority != p2.currentPriority) {
//                         return p1.currentPriority - p2.currentPriority;
//                     }
//                     return p1.arrivalTime - p2.arrivalTime;
//                 });

//                 Process currentProcess = readyQueue.get(0);

//                 // Track Execution Order
//                 if (previousProcess != currentProcess) {
//                     executionOrder.add(currentProcess.name);
//                 }

//                 // Execute Process for 1 unit
//                 currentProcess.remainingTime--;
//                 currentTime++;
//                 previousProcess = currentProcess;

//                 // Check Completion
//                 if (currentProcess.remainingTime == 0) {
//                     completedCount++;
//                     currentProcess.completionTime = currentTime;
//                     currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
//                     currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
//                 }
//             } else {
//                 // CPU is idle
//                 currentTime++;
//             }
//         }

//         printResults(processes, executionOrder);
//     }

//     private static void printResults(List<Process> processes, List<String> executionOrder) {
//         System.out.println("Execution Order: " + executionOrder);
//         System.out.println("\nProcess\tAT\tBT\tPrio\tWT\tTAT");

//         double totalWT = 0;
//         double totalTAT = 0;

//         // Sort by ID for clean tabular output
//         processes.sort(Comparator.comparing(p -> p.name));

//         for (Process p : processes) {
//             System.out.println(p.name + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" + 
//                              p.originalPriority + "\t" + p.waitingTime + "\t" + p.turnaroundTime);
//             totalWT += p.waitingTime;
//             totalTAT += p.turnaroundTime;
//         }

//         System.out.printf("\nAverage Waiting Time: %.2f", totalWT / processes.size());
//         System.out.printf("\nAverage Turnaround Time: %.2f\n", totalTAT / processes.size());
//     }

//     public static void main(String[] args) {
//         // Sample Input
//         Scanner sc = new Scanner(System.in);
//         List<Process> processes = new ArrayList<>();

//         // Hardcoded example for testing (You can replace this with Scanner loop)
//         processes.add(new Process("P1", 0, 14, 4));
//         processes.add(new Process("P2", 1, 1, 1));
//         processes.add(new Process("P3", 2, 2, 4));
//         processes.add(new Process("P4", 3, 1, 5));
//         processes.add(new Process("P5", 4, 5, 2));

//         System.out.println("--- Preemptive Priority Scheduling with Starvation Solution ---");
//         solve(processes);
//         sc.close();
//     }
// }

import java.util.*;

/**
 * Process class updated to match the table structure in the image.
 * It now includes the 'quantum' field.
 */
class Process {
    String name;
    int burstTime; // Changed order to match image headers
    int arrivalTime;
    int originalPriority;
    int quantum; // Added field based on image_0.png

    // Execution metrics
    int currentPriority; // Changes dynamically to solve starvation
    int remainingTime;
    int waitingTime;
    int turnaroundTime;
    int completionTime;

    public Process(String name, int burstTime, int arrivalTime, int priority, int quantum) {
        this.name = name;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.originalPriority = priority;
        this.quantum = quantum;

        // Initial state for execution
        this.currentPriority = priority;
        this.remainingTime = burstTime;
    }
}

public class PriorityScheduling {

    // Aging Configuration: Decrease priority number (increase importance) every
    // AGING_THRESHOLD units of waiting
    // A lower number means higher priority.
    static final int AGING_THRESHOLD = 5;

    /**
     * Simulates Preemptive Priority Scheduling with aging to prevent starvation.
     * Note: The 'quantum' field in the Process class is ignored by this specific
     * scheduler
     * but is kept for compatibility with the overall assignment structure (AG
     * Scheduling).
     */
    public static void solve(List<Process> inputProcesses) {
        int currentTime = 0;
        int completedCount = 0;
        int n = inputProcesses.size();
        Process previousProcess = null;

        // Create a working copy of the processes
        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(new Process(p.name, p.burstTime, p.arrivalTime, p.originalPriority, p.quantum));
        }

        List<String> executionOrder = new ArrayList<>();

        while (completedCount < n) {
            // 1. Filter processes that have arrived and are not complete
            List<Process> readyQueue = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    readyQueue.add(p);
                }
            }

            // 2. Solve Starvation: Apply Aging Logic
            // If a process waiting in the ready queue is not the one just executed, check
            // its wait time.
            for (Process p : readyQueue) {
                if (p != previousProcess) {
                    // Calculate total time spent waiting so far
                    int timeSpentWaiting = currentTime - p.arrivalTime - (p.burstTime - p.remainingTime);

                    // If wait time is a multiple of threshold, increase priority (decrement value)
                    if (timeSpentWaiting > 0 && timeSpentWaiting % AGING_THRESHOLD == 0) {
                        if (p.currentPriority > 0) {
                            p.currentPriority--;
                            // Optional: Print aging event for debugging
                            // System.out.println("Time " + currentTime + ": Aged process " + p.name + " to
                            // priority " + p.currentPriority);
                        }
                    }
                }
            }

            if (!readyQueue.isEmpty()) {
                // 3. Sort Ready Queue based on current (potentially aged) priority
                // Primary sorting: Priority (Ascending - lower value is higher priority)
                // Secondary sorting: Arrival Time (FCFS) for tie-breaking
                
                readyQueue.sort((p1, p2) -> {
                    // Compare priorities first (lower value = higher priority)
                    if (p1.currentPriority < p2.currentPriority) {
                        return -1; // p1 comes before p2
                    } else if (p1.currentPriority > p2.currentPriority) {
                        return 1; // p2 comes before p1
                    } else {
                        // Same priority → FCFS (arrival time)
                        if (p1.arrivalTime < p2.arrivalTime) {
                            return -1;
                        } else if (p1.arrivalTime > p2.arrivalTime) {
                            return 1;
                        } else {
                            return 0; // completely equal
                        }
                    }
                });

                Process currentProcess = readyQueue.get(0);

                // Track Execution Order (only add if context switches occur)
                if (previousProcess != currentProcess) {
                    executionOrder.add(currentProcess.name);
                }

                // Execute Process for 1 time unit (Preemptive approach)
                currentProcess.remainingTime--;
                currentTime++;
                previousProcess = currentProcess;

                // Check Completion
                if (currentProcess.remainingTime == 0) {
                    completedCount++;
                    currentProcess.completionTime = currentTime;
                    // Turnaround Time = Completion Time - Arrival Time
                    currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                    // Waiting Time = Turnaround Time - Burst Time
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                }
            } else {
                // CPU is idle if no processes have arrived yet
                currentTime++;
            }
        }

        printResults(processes, executionOrder);
    }

    private static void printResults(List<Process> processes, List<String> executionOrder) {
        System.out.println("Execution Order: " + executionOrder);
        // Matching table headers from image + results
        System.out.println("\nProcess\tBT\tAT\tPrio\tQuant\t|\tWT\tTAT");
        System.out.println("----------------------------------------------------------------");

        double totalWT = 0;
        double totalTAT = 0;

        // Sort by name just for clean output presentation
        processes.sort(Comparator.comparing(p -> p.name));

        for (Process p : processes) {
            System.out.println(p.name + "\t" + p.burstTime + "\t" + p.arrivalTime + "\t" +
                    p.originalPriority + "\t" + p.quantum + "\t|\t" +
                    p.waitingTime + "\t" + p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.printf("\nAverage Waiting Time: %.2f", totalWT / processes.size());
        System.out.printf("\nAverage Turnaround Time: %.2f\n", totalTAT / processes.size());
    }

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();

        // Using the example data provided in the image
        // Constructor order: Name, Burst Time, Arrival Time, Priority, Quantum
        processes.add(new Process("P1", 17, 0, 4, 7));
        processes.add(new Process("P2", 6, 2, 7, 9));
        processes.add(new Process("P3", 11, 5, 3, 4));
        processes.add(new Process("P4", 4, 15, 6, 6));

        System.out.println("--- Preemptive Priority Scheduling (with Starvation Solution) ---");
        System.out.println("Processing example data from image...");
        solve(processes);
    }
}