import json
import random
import subprocess
import os
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np

def generate_instance(n, K, d=4, conflict_density=0.3, seed=42):
    random.seed(seed)
    tasks = [f'T{i}' for i in range(n)]
    conflicts = [(i,j) for i in range(n) for j in range(i+1,n) 
                 if random.random() < conflict_density]
    cap = [32, 128, 8, 6.0] # CPU, RAM, GPU, Network
    resources = [[random.uniform(1, cap[d]//(n//K+1)) 
                  for d in range(4)] for _ in range(n)]
    capacities = [cap[:] for _ in range(K)]
    windows = [(lo := random.randint(0,K-2), 
                random.randint(lo+1, K-1)) for _ in range(n)]
    weights = [random.uniform(1, 10) for _ in range(n)]
    return dict(tasks=tasks, conflicts=conflicts, 
                resources=resources, capacities=capacities, 
                windows=windows, weights=weights, K=K)

benchmarks = [
    {"type": "Small", "n": 8, "K": 3, "density": 0.30, "seed": 1, "bf": "true"},
    {"type": "Small", "n": 10, "K": 4, "density": 0.40, "seed": 2, "bf": "true"},
    {"type": "Small", "n": 12, "K": 4, "density": 0.50, "seed": 3, "bf": "true"},
    {"type": "Medium", "n": 50, "K": 8, "density": 0.25, "seed": 10, "bf": "false"},
    {"type": "Medium", "n": 100, "K": 10, "density": 0.30, "seed": 11, "bf": "false"},
    {"type": "Medium", "n": 150, "K": 12, "density": 0.35, "seed": 12, "bf": "false"},
    {"type": "Stress (Base)", "n": 200, "K": 15, "density": 0.40, "seed": 20, "bf": "false"},
    {"type": "Stress (Tight)", "n": 200, "K": 5, "density": 0.60, "seed": 21, "bf": "false"},
    {"type": "Stress (Sparse)", "n": 200, "K": 20, "density": 0.10, "seed": 22, "bf": "false"},
]

results = []

for b in benchmarks:
    print(f"Running benchmark: n={b['n']}, K={b['K']}, seed={b['seed']}")
    inst = generate_instance(b["n"], b["K"], conflict_density=b["density"], seed=b["seed"])
    with open("instance.json", "w") as f:
        json.dump(inst, f)
        
    cmd = [
        "mvn.cmd" if os.name == "nt" else "mvn",
        "exec:java",
        "-Dexec.mainClass=BenchmarkMain",
        f"-Dexec.args=instance.json output.json {b['bf']}"
    ]
    
    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    
    with open("output.json", "r") as f:
        out = json.load(f)
        
    r = {
        "Group": b["type"],
        "n": b["n"],
        "K": b["K"],
        "Density": b["density"],
        "Seed": b["seed"],
        "Feasible": out.get("feasible"),
        "Runtime_ms": out.get("runtime_ms"),
        "Penalty": out.get("penalty"),
        "Optimal_Penalty": out.get("optimal_penalty"),
        "Violation_Reason": out.get("violation_reason")
    }
    
    if r["Penalty"] is not None and r["Optimal_Penalty"] is not None:
        r["Alpha_Emp"] = r["Penalty"] / r["Optimal_Penalty"]
    else:
        r["Alpha_Emp"] = None
        
    results.append(r)

df = pd.DataFrame(results)
print(df)
df.to_csv("benchmark_results.csv", index=False)

# Chart 1: Penalty vs n
plt.figure(figsize=(10, 6))
feasible_df = df[df["Feasible"] == True]
plt.plot(feasible_df["n"], feasible_df["Penalty"], marker='o', linestyle='-', color='b', label="CRAD Heuristic")
if not feasible_df[feasible_df["Optimal_Penalty"].notnull()].empty:
    opt_df = feasible_df[feasible_df["Optimal_Penalty"].notnull()]
    plt.plot(opt_df["n"], opt_df["Optimal_Penalty"], marker='x', linestyle='--', color='g', label="Brute-Force Optimal")

plt.xlabel("Number of Tasks (n)")
plt.ylabel("Penalty P(σ)")
plt.title("Solution Penalty vs Problem Size (n)")
for i, row in feasible_df.iterrows():
    if "Stress" in row["Group"]:
        plt.annotate(f"K={row['K']}", (row["n"], row["Penalty"]), textcoords="offset points", xytext=(0,10), ha='center')
plt.legend()
plt.grid(True)
plt.savefig("chart1_penalty.png", bbox_inches='tight')

# Chart 2: Runtime vs n (Log-Log)
plt.figure(figsize=(10, 6))
plt.plot(df["n"], df["Runtime_ms"], marker='o', linestyle='-', color='r', label="Execution Time")

# Add O(n^2) and O(n^3) theoretical curves for comparison, scaled
n_vals = np.linspace(min(df["n"]), max(df["n"]), 100)
# scale factor to make it overlap visually
c2 = df["Runtime_ms"].iloc[0] / (df["n"].iloc[0]**2) if df["Runtime_ms"].iloc[0] > 0 else 1e-4
c3 = df["Runtime_ms"].iloc[0] / (df["n"].iloc[0]**3) if df["Runtime_ms"].iloc[0] > 0 else 1e-5

plt.plot(n_vals, c2 * n_vals**2, 'k--', alpha=0.5, label="O(n^2) reference")
plt.plot(n_vals, c3 * n_vals**3, 'k:', alpha=0.5, label="O(n^3) reference")

plt.xscale("log")
plt.yscale("log")
plt.xlabel("Number of Tasks (n)")
plt.ylabel("Runtime (ms)")
plt.title("Execution Time Scaling vs Problem Size (n)")
plt.legend()
plt.grid(True, which="both", ls="--")
plt.savefig("chart2_runtime.png", bbox_inches='tight')
