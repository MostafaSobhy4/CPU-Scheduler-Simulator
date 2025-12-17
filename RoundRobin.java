import java.util.*;

class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingBurstTime;
    int completionTime;
    int waitingTime;
    int turnaroundTime;

    Process(String name, int arrivalTime, int burstTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingBurstTime = burstTime;
    }
}

public class RoundRobin {
    int timeQuantum;
    int contextSwitchTime;  
    List<Process> processes;
    List<Process> completedProcesses;
    List<String> executionOrder;

    RoundRobin(int timeQuantum, int contextSwitchTime, List<Process> processes) {
        this.timeQuantum = timeQuantum;
        this.contextSwitchTime = contextSwitchTime;
        this.processes = new ArrayList<>(processes);
        this.completedProcesses = new ArrayList<>();
        this.executionOrder = new ArrayList<>();
    }

    public void execute() {
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int totalProcesses = processes.size();
        int processesCompleted = 0;

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int nextProcessIndex = 0;
        boolean[] inQueue = new boolean[totalProcesses];
        
        Map<Process, Integer> processIndexMap = new HashMap<>();
        for (int i = 0; i < processes.size(); i++) {
            processIndexMap.put(processes.get(i), i);
        }

        Process previousProcess = null;
        
        while (processesCompleted < totalProcesses) {
            while (nextProcessIndex < totalProcesses && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                Process p = processes.get(nextProcessIndex);
                if (!inQueue[nextProcessIndex]) {
                    readyQueue.add(p);
                    inQueue[nextProcessIndex] = true;
                }
                nextProcessIndex++;
            }

            if (readyQueue.isEmpty() && nextProcessIndex < totalProcesses) {
                currentTime = processes.get(nextProcessIndex).arrivalTime;
                continue;
            }

            if (readyQueue.isEmpty()) {
                break;
            }

            Process currentProcess = readyQueue.poll();
            executionOrder.add(currentProcess.name);
            
            if (previousProcess != null && previousProcess != currentProcess && contextSwitchTime > 0) {
                currentTime += contextSwitchTime;
                while (nextProcessIndex < totalProcesses && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                    Process p = processes.get(nextProcessIndex);
                    if (!inQueue[nextProcessIndex]) {
                        readyQueue.add(p);
                        inQueue[nextProcessIndex] = true;
                    }
                    nextProcessIndex++;
                }
            }
            previousProcess = currentProcess;
            
            int executionTime = Math.min(timeQuantum, currentProcess.remainingBurstTime);
            currentProcess.remainingBurstTime -= executionTime;
            currentTime += executionTime;

            while (nextProcessIndex < totalProcesses && processes.get(nextProcessIndex).arrivalTime <= currentTime) {
                Process p = processes.get(nextProcessIndex);
                if (!inQueue[nextProcessIndex]) {
                    readyQueue.add(p);
                    inQueue[nextProcessIndex] = true;
                }
                nextProcessIndex++;
            }

            if (currentProcess.remainingBurstTime == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                completedProcesses.add(currentProcess);
                processesCompleted++;
                
                int processIndex = processIndexMap.get(currentProcess);
                inQueue[processIndex] = false;
            } else {
                readyQueue.add(currentProcess);
            }
        }
    }

    public void printMetrics() {
        System.out.println("Execution Order: " + String.join(" -> ", executionOrder));
        System.out.println();
        System.out.println("=".repeat(85));
        System.out.printf("%-10s %-15s %-10s %-15s %-15s %-15s%n", 
            "Process", "Arrival Time", "Burst Time", "Completion Time", "Waiting Time", "Turnaround Time");
        System.out.println("-".repeat(85));

        float totalWaitingTime = 0;
        float totalTurnaroundTime = 0;

        completedProcesses.sort(Comparator.comparing(p -> p.name));

        for (Process p : completedProcesses) {
            System.out.printf("%-10s %-15d %-10d %-15d %-15d %-15d%n", 
                p.name, p.arrivalTime, p.burstTime, p.completionTime, p.waitingTime, p.turnaroundTime);
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }

        System.out.println("=".repeat(85));
        System.out.println();
        
        if (!completedProcesses.isEmpty()) {
            System.out.printf("Average Waiting Time: %.2f%n", totalWaitingTime / completedProcesses.size());
            System.out.printf("Average Turnaround Time: %.2f%n", totalTurnaroundTime / completedProcesses.size());
        } else {
            System.out.println("No processes completed.");
        }
    }

    public static void main(String[] args) {
        List<Process> processes1 = new ArrayList<>();
        processes1.add(new Process("P1", 0, 8));
        processes1.add(new Process("P2", 1, 4));
        processes1.add(new Process("P3", 2, 2));
        processes1.add(new Process("P4", 3, 1));
        processes1.add(new Process("P5", 4, 3));

        RoundRobin rr1 = new RoundRobin(2, 1, processes1);
        rr1.execute();
        rr1.printMetrics();
        
        System.out.println("\n" + "=".repeat(85) + "\n");
    }
}
