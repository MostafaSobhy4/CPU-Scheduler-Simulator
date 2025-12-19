import com.google.gson.Gson;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AGSchedulerTest {

    static class TestCase {
        Input input;
        ExpectedOutput expectedOutput;
    }

    static class Input {
        ProcessInput[] processes;
    }

    static class ProcessInput {
        String name;
        int arrival;
        int burst;
        int priority;
        int quantum;
    }

    static class ExpectedOutput {
        String[] executionOrder;
        ProcessResultExpected[] processResults;
        double averageWaitingTime;
        double averageTurnaroundTime;
    }

    static class ProcessResultExpected {
        String name;
        int waitingTime;
        int turnaroundTime;
        int[] quantumHistory;
    }

    static Stream<Path> testCaseRead() throws IOException {
        Path testCasesDir = Paths.get("src/test/resources/AGtestcases");
        return Files.walk(testCasesDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"));
    }

    @ParameterizedTest
    @MethodSource("testCaseRead")
    void testAGScheduler(Path testCaseFile) throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(testCaseFile.toFile());
        TestCase testCase = gson.fromJson(reader, TestCase.class);
        reader.close();

        List<AGScheduler.Process> processes = new ArrayList<>();
        for (ProcessInput pi : testCase.input.processes) {
            processes.add(new AGScheduler.Process(pi.name, pi.arrival, pi.burst, pi.priority, pi.quantum));
        }

        AGScheduler.SchedulingResult result = AGScheduler.schedule(processes);

        assertArrayEquals(
                testCase.expectedOutput.executionOrder,
                result.executionOrder.toArray(new String[0]),
                "Execution order mismatch in test: " + testCaseFile.getFileName()
        );

        assertEquals(
                testCase.expectedOutput.processResults.length,
                result.processResults.size(),
                "Number of process results mismatch in test: " + testCaseFile.getFileName()
        );

        for (int i = 0; i < testCase.expectedOutput.processResults.length; i++) {
            ProcessResultExpected expected = testCase.expectedOutput.processResults[i];
            AGScheduler.ProcessResult actual = result.processResults.stream()
                    .filter(pr -> pr.name.equals(expected.name))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Process " + expected.name + " not found in results"));

            assertEquals(expected.waitingTime, actual.waitingTime,
                    "Waiting time mismatch for " + expected.name + " in test: " + testCaseFile.getFileName());

            assertEquals(expected.turnaroundTime, actual.turnaroundTime,
                    "Turnaround time mismatch for " + expected.name + " in test: " + testCaseFile.getFileName());

            assertArrayEquals(
                    expected.quantumHistory,
                    actual.quantumHistory.stream().mapToInt(Integer::intValue).toArray(),
                    "Quantum history mismatch for " + expected.name + " in test: " + testCaseFile.getFileName()
            );
        }

        assertEquals(
                testCase.expectedOutput.averageWaitingTime,
                result.averageWaitingTime,
                0.01,
                "Average waiting time mismatch in test: " + testCaseFile.getFileName()
        );

        assertEquals(
                testCase.expectedOutput.averageTurnaroundTime,
                result.averageTurnaroundTime,
                0.01,
                "Average turnaround time mismatch in test: " + testCaseFile.getFileName()
        );
    }
}