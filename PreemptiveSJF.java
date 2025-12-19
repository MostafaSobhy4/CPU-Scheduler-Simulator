import java.util.*;
import java.io.FileReader;
import java.io.File;
import com.google.gson.Gson;

class ProcessData {
    String name;
    int arrival;
    int burst;
    int priority;
}

class InputData {
    int contextSwitch;
    List<ProcessData> processes;
}

class ExpectedOutput {
    List<String> executionOrder;
    double averageWaitingTime;
    double averageTurnaroundTime;
}

class JsonTestCase {
    String name;
    InputData input;
    Map<String, ExpectedOutput> expectedOutput;
}

class Process {
    String name;
    int arrival;
    int burst;
    int remaining;
    int finish;
    int waiting;
    int turnaround;

    Process(String n, int a, int b) {
        this.name = n;
        this.arrival = a;
        this.burst = b;
        this.remaining = b;
    }
}

public class PreemptiveSJF {
    static class Result {
        List<String> executionOrder = new ArrayList<>();
        List<Process> processes;
        double avgWaiting;
        double avgTurnaround;
    }

    private static int addArrivals(List<Process> processes, int index, int currentTime, PriorityQueue<Process> pq) {
        while (index < processes.size() && processes.get(index).arrival <= currentTime) {
            pq.offer(processes.get(index));
            index++;
        }
        return index;
    }

    public static Result scheduleSRTF(List<Process> input, int contextSwitch) {
        List<Process> processes = new ArrayList<>();
        for (Process p : input) {
            processes.add(new Process(p.name, p.arrival, p.burst));
        }

        processes.sort(Comparator.comparingInt(p -> p.arrival));

        PriorityQueue<Process> pq = new PriorityQueue<>((a, b) -> {
            if (a.remaining != b.remaining) return Integer.compare(a.remaining, b.remaining);
            if (a.arrival != b.arrival) return Integer.compare(a.arrival, b.arrival);
            return a.name.compareTo(b.name);
        });

        int currentTime = 0;
        int index = 0;
        int finishedCount = 0;
        Process currentProcess = null;
        String lastProcessName = null;

        Result result = new Result();
        result.processes = processes;

        while (finishedCount < processes.size()) {
            index = addArrivals(processes, index, currentTime, pq);

            if (currentProcess == null) {
                if (pq.isEmpty()) {
                    if (index < processes.size()) {
                        currentTime = Math.max(currentTime, processes.get(index).arrival);
                        index = addArrivals(processes, index, currentTime, pq);
                        if (pq.isEmpty()) continue;
                    } else {
                        break;
                    }
                }

                Process next = pq.poll();

                if (lastProcessName != null && !lastProcessName.equals(next.name)) {
                    currentTime += contextSwitch;
                    index = addArrivals(processes, index, currentTime, pq);
                }

                currentProcess = next;

                if (result.executionOrder.isEmpty() || 
                    !result.executionOrder.get(result.executionOrder.size() - 1).equals(currentProcess.name)) {
                    result.executionOrder.add(currentProcess.name);
                }
            }

            currentProcess.remaining--;
            currentTime++;
            index = addArrivals(processes, index, currentTime, pq);

            if (currentProcess.remaining == 0) {
                currentProcess.finish = currentTime;
                finishedCount++;
                lastProcessName = currentProcess.name;
                currentProcess = null;
            } else {
                Process candidate = pq.peek();
                if (candidate != null) {
                    boolean shouldPreempt = (candidate.remaining < currentProcess.remaining) ||
                            (candidate.remaining == currentProcess.remaining && 
                             (candidate.arrival < currentProcess.arrival || 
                             (candidate.arrival == currentProcess.arrival && candidate.name.compareTo(currentProcess.name) < 0)));

                    if (shouldPreempt) {
                        pq.offer(currentProcess);
                        Process next = pq.poll();
                        
                        currentTime += contextSwitch;
                        index = addArrivals(processes, index, currentTime, pq);

                        if (!result.executionOrder.get(result.executionOrder.size() - 1).equals(next.name)) {
                            result.executionOrder.add(next.name);
                        }
                        currentProcess = next;
                    }
                }
            }
        }

        calculateStatistics(result);
        return result;
    }

    private static void calculateStatistics(Result res) {
        double totalWaiting = 0, totalTurnaround = 0;
        for (Process p : res.processes) {
            p.turnaround = p.finish - p.arrival;
            p.waiting = p.turnaround - p.burst;
            totalWaiting += p.waiting;
            totalTurnaround += p.turnaround;
        }
        res.avgWaiting = totalWaiting / res.processes.size();
        res.avgTurnaround = totalTurnaround / res.processes.size();
    }

    static class TestRunner {
        public void runTests(String filePath) {
            try {
                Gson gson = new Gson();
                FileReader reader = new FileReader(filePath);
                JsonTestCase testCase = gson.fromJson(reader, JsonTestCase.class);

                System.out.println("\n>>> Running Test Case: " + testCase.name);
                System.out.println("--------------------------------------------------");

                List<Process> input = new ArrayList<>();
                for (ProcessData pd : testCase.input.processes) {
                    input.add(new Process(pd.name, pd.arrival, pd.burst));
                }
                Result actual = scheduleSRTF(input, testCase.input.contextSwitch);
                ExpectedOutput expected = testCase.expectedOutput.get("SJF");

                if (expected == null) {
                    System.out.println("Warning: No expected output found for SJF in JSON.");
                    return;
                }
                compareResults(actual, expected);
            } catch (Exception e) {
                System.err.println("Error processing test file: " + e.getMessage());
            }
        }
        
        private void compareResults(Result actual, ExpectedOutput expected) {
            boolean passed = true;

            if (!actual.executionOrder.equals(expected.executionOrder)) {
                System.out.println("FAILED: Execution Order mismatch!");
                System.out.println("Expected: " + expected.executionOrder);
                System.out.println("Actual:   " + actual.executionOrder);
                passed = false;
            }

            if (Math.abs(actual.avgWaiting - expected.averageWaitingTime) > 0.1) {
                System.out.println("FAILED: Avg Waiting Time mismatch!");
                System.out.printf("Expected: %.2f, Actual: %.2f\n", expected.averageWaitingTime, actual.avgWaiting);
                passed = false;
            }

            if (passed) {
                System.out.println("TEST PASSED SUCCESSFULLY!");
                System.out.printf("Avg Waiting: %.2f | Avg Turnaround: %.2f\n", actual.avgWaiting, actual.avgTurnaround);
            }
        }
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();

        for (int i = 1; i <= 6; i++) {
            String fileName = "test_" + i + ".json";
            String path = "tests" + File.separator + fileName;

            File file = new File(path);
            if (file.exists()) {
                runner.runTests(path);
            } else {
                System.out.println("\nTest file not found: " + fileName + " at " + file.getAbsolutePath());
            }
        }
    }
}
