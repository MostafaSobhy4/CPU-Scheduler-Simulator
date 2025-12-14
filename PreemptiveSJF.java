import java.util.*;

class Process {
    String name;
    int arrival, burst, remaining, finish, waiting, turnaround;

    Process(String n, int a, int b) {
        name = n;
        arrival = a;
        burst = b;
        remaining = b;
    }
}

public class PreemptiveSJF {
    public PreemptiveSJF() {  // Constructor to execute the code
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        List<Process> processes = new ArrayList<>();

        // Taking input for each process
        for (int i = 0; i < n; i++) {
            System.out.println("\n" + String.format("Please Enter The Name of Process #%d, It's Arrival Time And Burst Time ... Like: P1 0 5", i + 1));
            String Name = sc.next();
            int Arrival = sc.nextInt();
            int Burst = sc.nextInt();
            processes.add(new Process(Name, Arrival, Burst));
        }

        // PriorityQueue based on shortest remaining time
        PriorityQueue<Process> pq = new PriorityQueue<>(Comparator.comparingInt(p -> p.remaining));

        int time = 0; // Current time
        int finished = 0; // Count of finished processes

        List<String> executionOrder = new ArrayList<>(); // To store execution order

        // Main loop for scheduling processes
        while (finished < n) {
            // Add processes to queue that have arrived
            for (Process p : processes)
                if (p.arrival == time)
                    pq.add(p); // Add newly arrived process to the queue

            if (!pq.isEmpty()) {
                Process current = pq.poll(); // Get process with shortest remaining time

                // Store the execution order without consecutive duplicates
                if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals(current.name)) {
                    executionOrder.add(current.name);
                }

                // Decrement remaining time of current process
                current.remaining--;

                // Check if process has finished
                if (current.remaining == 0) {
                    current.finish = time + 1;
                    finished++;
                } else {
                    pq.add(current); // Re-add to queue if not finished
                }
            }

            time++;
        }

        // Print execution order with arrows
        System.out.println("\nExecution Order:");
        for (int i = 0; i < executionOrder.size(); i++) {
            System.out.print(executionOrder.get(i));
            if (i != executionOrder.size() - 1) System.out.print(" -> ");
        }

        // Calculate and print waiting and turnaround times
        System.out.println("\n\nProcess Results:");
        for (Process p : processes) {
            p.turnaround = p.finish - p.arrival; // The total time taken from arrival to completion
            p.waiting = p.turnaround - p.burst; // The total time spent waiting in the ready queue

            System.out.println(
                    p.name + " | Waiting = " + p.waiting + " | Turnaround = " + p.turnaround
            );
        }

        // Calculate average waiting and turnaround times
            // stream => is to make stream and apply functions on it
            // mapToDouble => to map each process to its waiting time to be double
            // average() => built-in func to calculate average of those waiting times
            // orElse(0) => in case there are no processes, return 0
        double avgW = processes.stream().mapToDouble(p -> p.waiting).average().orElse(0); 
        double avgT = processes.stream().mapToDouble(p -> p.turnaround).average().orElse(0);

        // Output average times
        System.out.println("\nAverage Waiting Time = " + avgW);
        System.out.println("Average Turnaround Time = " + avgT);
    }

    public static void main(String[] args) {
        new PreemptiveSJF();  // Create and run the PreemptiveSJF class
    }
}
