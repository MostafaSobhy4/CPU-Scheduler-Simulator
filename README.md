# CPU-Scheduler-Simulator

# Project Description

This Java project simulates multiple CPU scheduling algorithms with context switching, including Preemptive Shortest-Job-First (SJF), Round Robin (RR), Priority Scheduling, and AG Scheduling (Adaptive General Scheduling). The program allows the user to enter processes with their respective arrival times, burst times, priorities, and initial quantum times, and then it calculates the execution order, waiting time, turnaround time, and prints quantum time updates during scheduling.

The simulation is designed to illustrate the dynamic behavior of CPU scheduling algorithms, including preemption, quantum adjustments, and starvation avoidance.

# Features

# Preemptive Shortest-Job-First (SJF) Scheduling

- Executes the process with the shortest remaining burst time.

- Supports preemption and context switching.

- Handles multiple processes arriving at different times.

# Round Robin (RR) Scheduling

- Executes processes in a cyclic order based on a fixed time quantum.

- Supports context switching.

- Properly handles processes that don’t finish in their quantum.

# Priority Scheduling

- Supports non-preemptive and preemptive priority-based scheduling.

- Solves the starvation problem by adjusting priorities dynamically.

# AG Scheduling (Adaptive General Scheduling)

- Combines FCFS, Priority, and Preemptive SJF scheduling.

- Adjusts process quantum dynamically based on remaining time and scheduling behavior.

- Handles the following scenarios:

1- Process uses all its quantum → added to end of queue and quantum increased.

2- Process preempted due to priority → quantum adjusted.

3- Process preempted due to SJF → quantum adjusted.

4- Process completes → quantum set to zero.

# User Input

Accepts process name, arrival time, burst time, priority, and quantum.

# Outputs

- Execution order of processes.

- Waiting time for each process.

- Turnaround time for each process.

- Average waiting time and turnaround time.

- Quantum history updates for each process.

Input Example
Number of processes: 4
Round Robin Time Quantum: 4
Context Switching Time: 1

Process details:
Name: P1, Arrival Time: 0, Burst Time: 6, Priority: 2
Name: P2, Arrival Time: 1, Burst Time: 17, Priority: 5
Name: P3, Arrival Time: 2, Burst Time: 11, Priority: 4
Name: P4, Arrival Time: 3, Burst Time: 6, Priority: 7

Output Example
Execution Order: P1 -> P2 -> P3 -> P4 -> ...
Waiting Time: P1: 4, P2: 7, P3: 9, P4: 6
Turnaround Time: P1: 10, P2: 12, P3: 15, P4: 19
Average Waiting Time: 6.5
Average Turnaround Time: 14
Quantum History Updates:
P1: 7 -> 10 -> 14
P2: 9 -> 12
