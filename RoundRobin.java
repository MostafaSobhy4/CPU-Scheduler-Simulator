import java.util.*;

class Process {
    String name;
    int arrivalTime;
    int burstTime; //Total CPU time required by the process.
    int remainingBurstTime; //Tracks how much CPU time the process still needs.
    int completionTime; //Records the exact time the process finishes execution.
    int waitingTime; // Total time the process spends waiting in the ready queue.
    int turnaroundTime; //Total time from arrival to completion.
    int lastScheduledTime; // Track when process was last scheduled

    Process(String name, int a, int b) {
        this.name = name;
        this.arrivalTime = a;
        this.burstTime = b;
        this.remainingBurstTime = burstTime;
        this.lastScheduledTime = arrivalTime; // Initialize
    }
}

public class RoundRobin {
    int timeQuantum;
    List<Process> processes;
    List<Process> completedProcesses;

    RoundRobin(int timeQuantum, List<Process> processes) {
        this.timeQuantum = timeQuantum;
        this.processes = new ArrayList<>(processes);
        this.completedProcesses = new ArrayList<>();
    }

    public void execute() {
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int processesCompleted = 0;
        int totalProcesses = processes.size();
        
        // Sort processes by arrival time initially
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        
        int nextProcessIndex = 0;
        boolean[] inQueue = new boolean[totalProcesses];

        while (processesCompleted < totalProcesses) {
            // Add newly arrived processes to the ready queue
            while (nextProcessIndex < totalProcesses && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                Process p = processes.get(nextProcessIndex);
                if (!inQueue[nextProcessIndex]) {
                    readyQueue.add(p);
                    inQueue[nextProcessIndex] = true;
                }
                nextProcessIndex++;
            }

            if (readyQueue.isEmpty()) {
                // If no process is ready, jump to next arrival
                if (nextProcessIndex < totalProcesses) {
                    currentTime = processes.get(nextProcessIndex).arrivalTime;
                }
                continue;
            }

            // Get next process from ready queue
            Process currentProcess = readyQueue.poll();
            int processIndex = processes.indexOf(currentProcess);
            inQueue[processIndex] = false;

            // Calculate waiting time for this scheduling
            currentProcess.waitingTime += (currentTime - currentProcess.lastScheduledTime);

            // Execute for time quantum or remaining burst time
            int executionTime = Math.min(timeQuantum, currentProcess.remainingBurstTime);
            currentTime += executionTime;
            currentProcess.remainingBurstTime -= executionTime;
            currentProcess.lastScheduledTime = currentTime;

            // Add newly arrived processes during this execution
            while (nextProcessIndex < totalProcesses && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                Process p = processes.get(nextProcessIndex);
                if (!inQueue[nextProcessIndex]) {
                    readyQueue.add(p);
                    inQueue[nextProcessIndex] = true;
                }
                nextProcessIndex++;
            }

            if (currentProcess.remainingBurstTime == 0) {
                // Process completed
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                completedProcesses.add(currentProcess);
                processesCompleted++;
            } else {
                // Process not finished, add back to ready queue
                readyQueue.add(currentProcess);
                inQueue[processIndex] = true;
            }
        }
    }

    public void printMetrics() {
        System.out.printf("%-10s %-15s %-10s %-15s %-15s %-15s%n", 
            "Process", "Arrival Time", "Burst Time", "Completion Time", "Waiting Time", "Turnaround Time");
        
        float totalWaitingTime = 0;
        float totalTurnaroundTime = 0;
        
        for (Process p : completedProcesses) {
            System.out.printf("%-10s %-15d %-10d %-15d %-15d %-15d%n", 
                p.name, p.arrivalTime, p.burstTime, p.completionTime, p.waitingTime, p.turnaroundTime);
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }
        
        System.out.println("\nAverage Waiting Time: " + (totalWaitingTime / completedProcesses.size()));
        System.out.println("Average Turnaround Time: " + (totalTurnaroundTime / completedProcesses.size()));
    }

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 5));
        processes.add(new Process("P2", 1, 3));
        processes.add(new Process("P3", 2, 8));
        processes.add(new Process("P4", 3, 6));

        int timeQuantum = 4;

        RoundRobin rr = new RoundRobin(timeQuantum, processes);
        rr.execute();
        rr.printMetrics();
    }
}