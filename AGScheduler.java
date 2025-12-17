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
    }
    
   static int ceil25(int q) {
       return (int) Math.ceil(q / 4.0);
   }

    static int ceil50(int q) {
        return (int) Math.ceil(q / 2.0);
    }
    static Process pickHighestPriority(List<Process> ready) {
        int bestIndex = 0;
        for (int i = 1; i < ready.size(); i++) {
            if (ready.get(i).priority < ready.get(bestIndex).priority) {
                bestIndex = i;
            }
        }
        return ready.remove(bestIndex);
    }

    static Process pickShortestJob(List<Process> ready) {
        int bestIndex = 0;
        for (int i = 1; i < ready.size(); i++) {
            if (ready.get(i).remaining < ready.get(bestIndex).remaining) {
                bestIndex = i;
            }
        }
        return ready.remove(bestIndex);
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Number of processes");
        int n = sc.nextInt();
        System.out.println("RR quantum");
        int defaultQuantum = sc.nextInt();
        System.out.println("Context switch");
        int contextSwitch = sc.nextInt();

        List<Process> all = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String name = sc.next();
            int arrival = sc.nextInt();
            int burst = sc.nextInt();
            int priority = sc.nextInt();
            int quantum = sc.nextInt();

            if (quantum == 0) quantum = defaultQuantum;
            all.add(new Process(name, arrival, burst, priority, quantum));
        }

        all.sort(Comparator.comparingInt((Process p) -> p.arrival)
                .thenComparing(p -> p.name));

        Map<String, List<Integer>> quantumHistory = new LinkedHashMap<>();
        for (Process p : all) {
            quantumHistory.put(p.name, new ArrayList<>());
            quantumHistory.get(p.name).add(p.quantum);
        }

        List<Process> ready = new ArrayList<>();

        List<String> executionOrder = new ArrayList<>();

        int time = 0;
        int arrivalIndex = 0;
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
            while (arrivalIndex < n && all.get(arrivalIndex).arrival <= time) {
                Process p = all.get(arrivalIndex);
                if (p.remaining > 0) ready.add(p);
                arrivalIndex++;
            }

            if (running == null) {
                if (ready.isEmpty()) {
                    time++;
                    continue;
                } else {
                    running = ready.remove(0);
                    running.usedAgQuantum = 0;
                    running.originalAgQuantum = running.quantum;
                    executionOrder.add(running.name);
                }
            }

            running.remaining--;
            running.usedAgQuantum++;
            time++;

            while (arrivalIndex < n && all.get(arrivalIndex).arrival <= time) {
                Process p = all.get(arrivalIndex);
                if (p.remaining > 0) ready.add(p);
                arrivalIndex++;
            }

            if (running.remaining == 0) {
                running.completionTime = time;
                quantumHistory.get(running.name).add(0);
                running = null;
                time += contextSwitch;
                continue;
            }

            int q = running.originalAgQuantum;
            int used = running.usedAgQuantum;

            if (used >= q) {
                running.quantum += 2;
                quantumHistory.get(running.name).add(running.quantum);
                ready.add(running);
                running = null;
                time += contextSwitch;
                continue;
            }

            int boundary25 = ceil25(q);
            int boundary50 = ceil50(q);

            if (used == boundary25 && !ready.isEmpty()) {
                int bestPriority = Integer.MAX_VALUE;
                for (Process p : ready) {
                    bestPriority = Math.min(bestPriority, p.priority);
                }

                if (bestPriority < running.priority) {
                    int remainingQuantum = q - used;
                    int increment = (int) Math.ceil(remainingQuantum / 2.0);
                    running.quantum += increment;
                    quantumHistory.get(running.name).add(running.quantum);
                    ready.add(running);
                    running = pickHighestPriority(ready);
                    running.usedAgQuantum = 0;
                    running.originalAgQuantum = running.quantum;
                    executionOrder.add(running.name);
                    time += contextSwitch;
                }
            }

            if (used >= boundary50 && !ready.isEmpty()) {
                int bestRemaining = Integer.MAX_VALUE;
                for (Process p : ready) {
                    bestRemaining = Math.min(bestRemaining, p.remaining);
                }

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
                }
            }
        }

        System.out.println("Execution Order (AG Scheduling):");
        System.out.println(String.join(" -> ", executionOrder));
        System.out.println();

        double totalWT = 0, totalTAT = 0;
        for (Process p : all) {
            int turnaround = p.completionTime - p.arrival;
            int waiting = turnaround - p.burst;
            totalWT += waiting;
            totalTAT += turnaround;

            List<Integer> history = quantumHistory.get(p.name);
            StringBuilder qh = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                qh.append(history.get(i));
                if (i < history.size() - 1) qh.append(", ");
            }
            qh.append("]");

            System.out.println("name:"+ p.name + ","+ "waitingTime:" + waiting +","+ "turnaroundTime:"+ turnaround +","+ "quantumHistory:" + qh);
        }

        System.out.printf("Average Waiting Time = %.2f ", totalWT / n);
        System.out.printf("Average Turnaround Time = %.2f ", totalTAT / n);

        sc.close();
    }
}
