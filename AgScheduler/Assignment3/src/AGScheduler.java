import java.util.*;

public class AGScheduler {

    static class Process {
        String name;
        int arrival;
        int burst;
        int remaining;
        int priority;
        int quantum;

        int usedAgQuantum;
        int originalAgQuantum;
        int completionTime;

        Process(String name, int arrival, int burst, int priority, int quantum) {
            this.name = name;
            this.arrival = arrival;
            this.burst = burst;
            this.remaining = burst;
            this.priority = priority;
            this.quantum = quantum;
            this.usedAgQuantum = 0;
            this.originalAgQuantum = quantum;
            this.completionTime = -1;
        }

        Process(Process other) {
            this.name = other.name;
            this.arrival = other.arrival;
            this.burst = other.burst;
            this.remaining = other.burst;
            this.priority = other.priority;
            this.quantum = other.quantum;
            this.usedAgQuantum = 0;
            this.originalAgQuantum = other.quantum;
            this.completionTime = -1;
        }
    }

    public static class ProcessResult {
        public String name;
        public int waitingTime;
        public int turnaroundTime;
        public List<Integer> quantumHistory;

        public ProcessResult(String name, int waitingTime, int turnaroundTime, List<Integer> quantumHistory) {
            this.name = name;
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
            this.quantumHistory = quantumHistory;
        }
    }

    public static class SchedulingResult {
        public List<String> executionOrder;
        public List<ProcessResult> processResults;
        public double averageWaitingTime;
        public double averageTurnaroundTime;

        public SchedulingResult(List<String> executionOrder, List<ProcessResult> processResults,
                                double averageWaitingTime, double averageTurnaroundTime) {
            this.executionOrder = executionOrder;
            this.processResults = processResults;
            this.averageWaitingTime = averageWaitingTime;
            this.averageTurnaroundTime = averageTurnaroundTime;
        }
    }

    static int ceil25(int q) {
        return (int) Math.ceil(q / 4.0);
    }

    
    static int ceil50(int q) {
        int b25 = ceil25(q);
        return b25 + b25;
    }

    static Process pickHighestPriority(List<Process> ready) {
        int best = 0;
        for (int i = 1; i < ready.size(); i++) {
            Process a = ready.get(i), b = ready.get(best);
            if (a.priority < b.priority) {
                best = i;
            }
        }
        return ready.remove(best);
    }

    static Process pickShortestJob(List<Process> ready) {
        int best = 0;
        for (int i = 1; i < ready.size(); i++) {
            Process a = ready.get(i), b = ready.get(best);
            if (a.remaining < b.remaining) {
                best = i;
            }
        }
        return ready.remove(best);
    }

    public static SchedulingResult schedule(List<Process> processes) {
        int contextSwitch = 0;

        List<Process> all = new ArrayList<>();
        for (Process p : processes) all.add(new Process(p));

        int n = all.size();
        all.sort(Comparator.comparingInt((Process p) -> p.arrival).thenComparing(p -> p.name));

        Map<String, List<Integer>> quantumHistory = new LinkedHashMap<>();
        for (Process p : all) quantumHistory.put(p.name, new ArrayList<>(List.of(p.quantum)));

        List<Process> ready = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        int time = 0;
        int idx = 0;
        Process running = null;

        while (true) {
            boolean finished = true;
            for (Process p : all) {
                if (p.remaining > 0) {
                    finished = false;
                    break;
                }
            }
            if (finished) break;

            while (idx < n && all.get(idx).arrival <= time) {
                Process p = all.get(idx++);
                if (p.remaining > 0) ready.add(p);
            }

            if (running == null) {
                if (ready.isEmpty()) {
                    time++;
                    continue;
                }
                running = ready.remove(0);
                running.usedAgQuantum = 0;
                running.originalAgQuantum = running.quantum;
                executionOrder.add(running.name);
            }

            running.remaining--;
            running.usedAgQuantum++;
            time++;

            while (idx < n && all.get(idx).arrival <= time) {
                Process p = all.get(idx++);
                if (p.remaining > 0) ready.add(p);
            }
            // Fourth case
            if (running.remaining == 0) {
                running.completionTime = time;
                running.quantum = 0;
                quantumHistory.get(running.name).add(0);
                running = null;
                time += contextSwitch;
                continue;
            }

            int q = running.originalAgQuantum;
            int used = running.usedAgQuantum;

            // First case
            if (used >= q) {
                running.quantum += 2;
                quantumHistory.get(running.name).add(running.quantum);
                ready.add(running);
                running = null;
                time += contextSwitch;
                continue;
            }

            int PriorityStart = ceil25(q);
            int sjfStart = ceil50(q);

            // Second case
            if (used == PriorityStart && !ready.isEmpty()) {
                int bestPriority = Integer.MAX_VALUE;
                for (Process p : ready) bestPriority = Math.min(bestPriority, p.priority);

                if (bestPriority < running.priority) {
                    int remainingQuantum = q - used;
                    int inc = (int) Math.ceil(remainingQuantum / 2.0);

                    running.quantum += inc;
                    quantumHistory.get(running.name).add(running.quantum);

                    ready.add(running);
                    running = pickHighestPriority(ready);
                    running.usedAgQuantum = 0;
                    running.originalAgQuantum = running.quantum;
                    executionOrder.add(running.name);

                    time += contextSwitch;
                    continue;
                }
            }

            // Third case
            if (used >= sjfStart && !ready.isEmpty()) {
                int bestRemaining = Integer.MAX_VALUE;
                for (Process p : ready) bestRemaining = Math.min(bestRemaining, p.remaining);

                if (bestRemaining < running.remaining) {
                    int remainingQuantum = q - used;

                    running.quantum += remainingQuantum;
                    quantumHistory.get(running.name).add(running.quantum);

                    ready.add(running);
                    running = pickShortestJob(ready);
                    running.usedAgQuantum = 0;
                    running.originalAgQuantum = running.quantum;
                    executionOrder.add(running.name);

                    time += contextSwitch;
                    continue;
                }
            }
        }

        double totalWT = 0, totalTAT = 0;
        List<ProcessResult> results = new ArrayList<>();

        for (Process p : all) {
            int tat = p.completionTime - p.arrival;
            int wt = tat - p.burst;
            totalWT += wt;
            totalTAT += tat;

            results.add(new ProcessResult(p.name, wt, tat, new ArrayList<>(quantumHistory.get(p.name))));
        }

        return new SchedulingResult(
                executionOrder,
                results,
                totalWT / n,
                totalTAT / n
        );
    }
}