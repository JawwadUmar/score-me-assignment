# Resource-Constrained Task Allocation & Constraint Optimization

![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)
![License](https://img.shields.io/badge/license-Academic-orange.svg)

**Abstract:**  
This repository presents a comprehensive solution to the ScoreMe Engineering Capstone assignment. The core problem is an NP-hard multidimensional resource-constrained scheduling task (combining Graph Coloring, Temporal Packing, and SLA window constraints). The solution features a fully original, custom-designed polynomial-time heuristic algorithm—Conflict-First Resource-Aware DSATUR (CRAD)—implemented purely in Java 17 without external solver dependencies. 

---

## Table of Contents
1. [Assignment Task Matrix](#assignment-task-matrix)
2. [Repository Structure](#repository-structure)
3. [Build, Installation & Usage Instructions](#build-installation--usage-instructions)
4. [Key Benchmark Highlights & Theoretical Guarantees](#key-benchmark-highlights--theoretical-guarantees)
5. [Academic Integrity & Usage Disclaimer](#academic-integrity--usage-disclaimer)

---

## Assignment Task Matrix

The repository maps directly to the 7 core assignment tasks. All theoretical proofs, architectural rationales, empirical benchmarks, and source code deliverables are provided below.

| Task | Deliverable Description | Repository Location |
| :--- | :--- | :--- |
| **Task 1: Complexity Analysis** | Formal NP-hardness proof via Graph $k$-Coloring polynomial-time reduction. | [`TASK1-NP-Hardness-Proof.md`](./TASK1-NP-Hardness-Proof.md) |
| **Task 2: Penalty Function** | Mathematical formulation $P(\sigma)$ for Load Imbalance and $O(n K d)$ complexity proof. | [`TASK-2-Penalty_Function-Justification.md`](./TASK-2-Penalty_Function-Justification.md) |
| **Task 3: Heuristic Design** | Named algorithm specification (CRAD), structured pseudocode, and line-level rationale. | [`TASK-3-Design-Heuristic-Algorithm.md`](./TASK-3-Design-Heuristic-Algorithm.md) |
| **Task 4: Theoretical Bounds** | Feasibility guarantees, analytic approximation ratio $\alpha$, and tight adversarial example. | [`TASK-4-Prove-Approximation-Ratio.md`](./TASK-4-Prove-Approximation-Ratio.md) |
| **Task 5: Java Core Engine** | Java 17 implementation (Zero external solvers), JUnit 5 tests, and CLI runner. | [`src/`](./src/), [`pom.xml`](./pom.xml) |
| **Task 6: Empirical Benchmarks** | Automated Python benchmark runner, execution table, and matplotlib plots. | [`TASK-6-Empirical-Analysis.md`](./TASK-6-Empirical-Analysis.md), [`benchmark.py`](./benchmark.py) |
| **Task 7: Design Journal** | Engineering reflections, production system mapping (GPU Cluster Scheduling), and failure analysis. | [`TASK-7-Design-Journal.md`](./TASK-7-Design-Journal.md) |

---

## Repository Structure

```text
├── TASK1-NP-Hardness-Proof.md                   # Formal mathematical proofs
├── TASK-2-Penalty_Function-Justification.md     # Penalty formulation & complexity
├── TASK-3-Design-Heuristic-Algorithm.md         # Algorithm pseudocode and design
├── TASK-4-Prove-Approximation-Ratio.md          # Bounds & adversarial analysis
├── TASK-6-Empirical-Analysis.md                 # Benchmark results & charts
├── TASK-7-Design-Journal.md                     # Engineering reflections & mappings
├── src/                                         # Core Algorithm implementation
│   ├── Main.java                                # Standard CLI Runner
│   ├── BenchmarkMain.java                       # Benchmark JSON bridging orchestrator
│   ├── algorithm/                               # CRAD heuristic logic & BruteForce baseline
│   ├── io/                                      # Jackson JSON serialization/deserialization
│   ├── model/                                   # Domain models (Task, SchedulingInstance)
│   └── test/                                    # JUnit 5 boundary test cases
├── benchmark.py                                 # Python script to generate & plot benchmarks
├── chart1_penalty.png / chart2_runtime.png      # Rendered performance charts
├── benchmark_results.csv                        # Raw CSV benchmark outputs
├── pom.xml                                      # Maven build configuration
└── README.md
```

---

## Build, Installation & Usage Instructions

### Prerequisites
* **Java:** JDK 17+ (Required to run the CRAD Core Engine)
* **Maven:** Apache Maven 3.8+ (For dependency resolution and building)
* **Python:** 3.9+ (For generating the benchmark JSON instances and plotting)
  * Requires `pandas`, `matplotlib`, `numpy` (`pip install pandas matplotlib numpy`)

### Compilation

Clone the repository and package the Java project using Maven. This pulls in the required JSON libraries (Jackson) and Testing suites (JUnit 5).

```bash
mvn clean package -DskipTests
```

### Running the Solver

You can run the core engine directly via Maven execution against any JSON instance generated according to the Task 5 schema requirements. 

```bash
# Execute the Main solver against an instance JSON and output to result JSON
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="instance.json output.json"
```

### Running Unit Tests

To run the mandated edge-case testing suite (Complete Graph Infeasibility, Tight SLA Windows, Zero Capacity Constraints, Single-Task validation), execute:

```bash
mvn test
```

### Running Benchmarks

To systematically run all 9 requested instances (Small, Medium, Stress) through the solver, dynamically compute empirical approximation ratios against a Brute-Force limit (for small cases), and render the visualizations, simply run:

```bash
python benchmark.py
```
*This command will output the final `benchmark_results.csv`, `chart1_penalty.png`, and `chart2_runtime.png`.*

---

## Key Benchmark Highlights & Theoretical Guarantees

| Metric / Dimension | CRAD Heuristic Evaluation |
| :--- | :--- |
| **Runtime Scaling** | Empirical execution times adhered strictly to theoretical polynomial bounds, scaling gracefully in $\approx O(n^2)$ relative to cluster size in real time ($< 100\text{ms}$ up to $n=200$). |
| **Empirical Gap ($\alpha_{\text{emp}}$)** | Achieved $\alpha_{\text{emp}} = 1.0$ (perfect absolute optimal bounds matching brute-force limits) on non-trivial small matrices where feasible spaces remained loose. |
| **Stress Resilience** | Displayed predictable, mathematically sound color exhaustion properties. Under aggressive temporal limits (SLA windows intersection) and Dense conflict topology ($K=5, \text{Density}=0.60$), the architecture fails fast (early infeasibility reporting) rather than triggering exponential execution hangs. |
| **Zero Dependencies** | True algorithmic adherence. Zero deployment reliance on heavyweight SAT solvers (Z3, CPLEX, Gurobi) enforcing clean combinatorial decision structures native to Java 17. |

---

## Academic Integrity & Usage Disclaimer

**Confidential & Academic:** This repository constitutes an original, independent solution produced specifically for the ScoreMe Solutions Pvt. Ltd. Advanced Systems Design Engineering Capstone. All algorithmic logic, formal proofs, testing frameworks, and custom heuristic architectures are authored originally in alignment with the specified constraints and generative boundaries.

