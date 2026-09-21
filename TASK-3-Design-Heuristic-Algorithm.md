The assignment asks for a **polynomial-time heuristic** that constructs a feasible schedule when its greedy choices succeed, while also requiring explicit handling of infeasibility. It also emphasizes that the algorithm should be adapted to the problem's combination of graph conflicts, four-dimensional resources, and SLA windows rather than being a generic textbook algorithm. 

One important theoretical point should be made explicit: **a polynomial-time heuristic cannot, in general, both always find a feasible solution whenever one exists and correctly certify global infeasibility unless \(P=NP\)**. Therefore, the algorithm below reports `INFEASIBLE` when **no feasible slot exists for the task under the algorithm's current partial assignment**. That is a valid heuristic failure/infeasibility signal, but it is not a mathematical certificate that the original instance has no solution. A genuine global infeasibility certificate would require backtracking/search in the worst case.

# Step 3 — Conflict-First Resource-Aware DSATUR

## 1. Algorithm identity and core concept

I propose the following algorithm:

$$
\boxed{\textbf{CRAD: Conflict-first Resource-Aware DSATUR}}
$$

The name reflects its four main design decisions:

1. **Conflict-first:** tasks with many difficult-to-place neighbors are scheduled first.
2. **DSATUR-inspired:** the priority of a task depends on the number of distinct slots already occupied by its conflicting neighbors.
3. **Resource-aware:** a slot is considered only if adding the task remains within all four resource capacities.
4. **Penalty-aware:** among feasible candidate slots, choose the one giving the smallest incremental penalty from Step 2.

The assignment's problem has three simultaneous feasibility constraints:

$$
F1=\text{conflict avoidance},
$$

$$
F2=\text{four-dimensional resource capacity},
$$

$$
F3=\text{SLA windows}.
$$

A naive algorithm such as "assign tasks in input order to the earliest available slot" can make poor early choices. In contrast, CRAD attempts to schedule the most constrained tasks first.

The assignment explicitly encourages hybrids such as DSATUR variants, resource-aware greedy methods, and local-search approaches. 

---

# 2. Data maintained by the algorithm

Let

$$
T=\{t_1,\ldots,t_n\}
$$

be the tasks and

$$
S=\{1,\ldots,K\}
$$

the processing slots.

For each task \(t_i\), maintain

$$
A(t_i)\in S\cup\{\bot\},
$$

where

$$
A(t_i)=\bot
$$

means that the task has not yet been assigned.

For every slot \(s\), maintain its current resource load:

$$
L_j(s)
=
\sum_{A(t_i)=s}r_j(t_i),
\qquad j\in\{1,2,3,4\}.
$$

We also maintain the set of already occupied slots among the neighbors of task \(t_i\):

$$
D_i
=
\{A(t_j):(t_i,t_j)\in E,\ A(t_j)\neq\bot\}.
$$

The corresponding DSATUR score is

$$
\operatorname{sat}(t_i)=|D_i|.
$$

A task with a larger \(\operatorname{sat}\) value has fewer choices remaining and therefore receives higher priority.

---

# 3. Task-selection priority

We use the following lexicographic priority key:

$$
\boxed{
Q(t_i)=
\left(
\operatorname{sat}(t_i),
\deg(t_i),
\frac{w(t_i)}{u_i-\ell_i+1},
w(t_i)
\right)
}
$$

and select the lexicographically largest \(Q(t_i)\).

The components have the following meaning.

### First: saturation

$$
\operatorname{sat}(t_i)
$$

is the number of distinct slots already used by its assigned conflicting neighbors.

This is the primary criterion because a task whose neighbors already occupy many different slots has fewer legal choices.

### Second: conflict degree

$$
\deg(t_i)
$$

breaks ties in favor of highly connected tasks.

A high-degree task has many opportunities to conflict with future assignments, so placing it earlier exposes those constraints before the remaining tasks are committed.

### Third: SLA tightness

Define the window width

$$
W_i=u_i-\ell_i+1.
$$

We use

$$
\frac{w(t_i)}{W_i}
$$

as the third criterion.

A high-priority task with a narrow SLA window receives greater priority than a low-priority task with a large scheduling window.

### Fourth: lender priority

Finally,

$$
w(t_i)
$$

breaks any remaining tie.

This incorporates the original weighted delay objective rather than treating every task identically.

---

# 4. Candidate-slot selection

Once a task \(t_i\) has been selected, define its currently feasible slot set:

$$
\mathcal F_i(A)
=
\left\{
s\in[\ell_i,u_i]:
\begin{array}{l}
\text{no assigned neighbor of }t_i\text{ is in }s,\\
L_j(s)+r_j(t_i)\le C_j(s),\\
\forall j\in\{1,2,3,4\}
\end{array}
\right\}.
$$

Thus a candidate slot must satisfy **all three types of restrictions**:

$$
\boxed{
\text{SLA}+\text{conflict}+\text{resource capacity}.
}
$$

For each feasible candidate slot \(s\), we temporarily place \(t_i\) there and calculate its incremental objective value

$$
\Delta P_i(s).
$$

We choose

$$
\boxed{
s^*
=
\arg\min_{s\in\mathcal F_i(A)}
\Delta P_i(s).
}
$$

Ties are broken by:

1. smaller slot index;
2. larger remaining resource slack.

The first tie-breaker favors earlier execution, consistent with

$$
P_{\text{base}}(A)
=
\sum_iw(t_i)A(t_i).
$$

The second avoids unnecessarily consuming a highly constrained slot.

---

# 5. Structured pseudocode

### **Algorithm: CRAD — Conflict-first Resource-Aware DSATUR**

```text
CRAD(T, E, r, C, w, τ, K, λ):

1.  A(t) ← ⊥ for every task t
2.  L[j][s] ← 0 for every resource j ∈ {1,2,3,4}
       and slot s ∈ {1,...,K}

3.  while there exists an unassigned task do

4.      for every unassigned task t do
5.          D(t) ← { A(u) :
                    (t,u) ∈ E and A(u) ≠ ⊥ }
6.          sat(t) ← |D(t)|
7.          degree(t) ← number of neighbors of t
8.          width(t) ← u_t - l_t + 1
9.          priority(t) ←
                (sat(t), degree(t),
                 w(t)/width(t), w(t))

10.     t* ← lexicographically largest priority(t)

11.     Candidates ← ∅

12.     for s = l_t*,...,u_t* do

13.         if s ∈ D(t*) then
14.             continue

15.         if for some resource j,
               L[j][s] + r_j(t*) > C_j(s) then
16.             continue

17.         Compute ΔP(t*,s)
18.         Insert (s, ΔP(t*,s)) into Candidates

19.     if Candidates = ∅ then
20.         return INFEASIBLE

21.     s* ← candidate with minimum ΔP(t*,s)
22.           breaking ties by smaller s,
23.           then by larger total remaining slack

24.     A(t*) ← s*

25.     for j = 1,...,4 do
26.         L[j][s*] ← L[j][s*] + r_j(t*)

27.     Update saturation information of
        every unassigned neighbor of t*

28. return A
```

---

# 6. Line-level justification

## Lines 1–2: Initialization

We initially have

$$
A(t)=\bot
$$

for every task, and every slot has zero resource utilization:

$$
L_j(s)=0.
$$

This gives the algorithm a consistent representation of the empty schedule.

---

## Lines 4–9: Calculate task priorities

For every unassigned task we calculate its current saturation set

$$
D(t).
$$

The number

$$
|D(t)|
$$

is the DSATUR-style measure of how constrained the task has become.

The algorithm then computes

$$
Q(t)=
\left(
\operatorname{sat}(t),
\deg(t),
\frac{w(t)}{W_t},
w(t)
\right).
$$

The lexicographic ordering is intentional: **constraint pressure dominates optimization preference**.

For example, a task with

$$
\operatorname{sat}(t)=2
$$

takes priority over one with

$$
\operatorname{sat}(u)=1
$$

even if \(u\) has a larger lender weight. This avoids sacrificing a scarce scheduling opportunity merely to improve the objective slightly.

---

## Line 10: Select the most constrained task

$$
t^*=\arg\max Q(t).
$$

This is the central DSATUR idea.

Rather than allowing an easy task to consume a slot that a difficult task needs, the algorithm handles constrained tasks first.

---

## Lines 12–16: Generate feasible slots

We examine only slots inside the SLA window:

$$
s\in[\ell_{t^*},u_{t^*}].
$$

Therefore \(F3\) is enforced directly.

Line 13 checks

$$
s\notin D(t^*).
$$

Therefore, if a neighboring task already occupies \(s\), the candidate is rejected, enforcing \(F1\).

Line 15 checks

$$
L_j(s)+r_j(t^*)\le C_j(s)
$$

for all four resource dimensions.

Therefore, \(F2\) is enforced before a task is committed.

This is important: the algorithm never intentionally creates a capacity violation.

---

# 7. Candidate scoring

For each feasible slot, we evaluate the change in the Step 2 objective.

Conceptually,

$$
\Delta P(t,s)
=
P(A\cup\{t\mapsto s\})-P(A).
$$

The algorithm chooses

$$
s^*
=
\arg\min_{s\in\mathcal F_t(A)}
\Delta P(t,s).
$$

This means the heuristic does not simply choose the earliest legal slot.

Instead, it asks:

> Among the slots that preserve feasibility, which placement produces the smallest increase in the objective?

This incorporates both:

$$
P_{\text{base}}
$$

and the resource-balance term introduced in Step 2.

The base component favors earlier placement, particularly for high-weight tasks, while the imbalance component discourages creating heavily overloaded slots relative to the other slots.

---

# 8. Why the tie-breaking rules matter

Suppose two candidate slots satisfy

$$
\Delta P(t,s_1)
=
\Delta P(t,s_2).
$$

We choose

$$
\min\{s_1,s_2\}.
$$

This is consistent with the delay term

$$
w(t)A(t).
$$

If the objective is genuinely identical, the earlier slot is a deterministic and operationally sensible choice.

If both slot indices are still tied, we choose the slot with larger remaining resource slack:

$$
\operatorname{Slack}(s)
=
\sum_{j=1}^{4}
\left(
C_j(s)-L_j(s)
\right).
$$

The intuition is to preserve scarce capacity in tight slots for future tasks.

---

# 9. Lines 19–20: Infeasibility detection

The algorithm reaches

$$
\texttt{Candidates}=\varnothing
$$

when the selected task has **no slot satisfying all currently enforced constraints**.

Formally,

$$
\mathcal F_{t^*}(A)=\varnothing.
$$

It then returns

$$
\boxed{\texttt{INFEASIBLE}}.
$$

However, there is a crucial distinction for the report:

$$
\boxed{
\texttt{INFEASIBLE}
\neq
\text{mathematical proof that the original instance is globally infeasible}.
}
$$

It means:

$$
\boxed{
\text{No feasible slot remains for }t^*
\text{ under the assignments already committed by CRAD.}
}
$$

A different sequence of earlier choices might conceivably have produced a feasible schedule.

This distinction is necessary because the underlying problem contains graph coloring plus resource and temporal constraints. A polynomial-time greedy algorithm cannot generally provide a complete infeasibility certificate unless \(P=NP\).

For an implementation, I would therefore make the output field explicit, e.g.

```text
feasible = false
violation_reason =
    "No feasible slot remained for task T_i under CRAD's
     current partial assignment."
```

rather than claiming that the entire instance has been mathematically proven infeasible.

---

# 10. Why the algorithm preserves feasibility

Whenever CRAD actually returns an assignment, every committed task satisfies:

### Conflict constraint

A slot \(s\) is rejected whenever

$$
s\in D(t).
$$

Therefore no assigned edge has equal endpoints.

### Capacity constraint

A slot \(s\) is rejected whenever

$$
L_j(s)+r_j(t)>C_j(s)
$$

for any \(j\).

Therefore no resource dimension exceeds capacity.

### SLA constraint

The algorithm only examines

$$
s\in[\ell_t,u_t].
$$

Therefore every assigned task satisfies its SLA window.

Consequently:

$$
\boxed{
\text{If CRAD returns an assignment, that assignment satisfies }F1,F2,F3.
}
$$

This is a **feasibility-preservation property of successful output**, not a guarantee that CRAD will find a feasible solution whenever one exists.

That distinction will be particularly important for the approximation/feasibility analysis in Task 4.

---

# 11. Polynomial-time complexity

Let

$$
n=|T|,
\qquad
m=|E|.
$$

There are \(K\) slots and \(d=4\) resource dimensions.

We analyze the worst case.

## Task selection

A straightforward implementation scans all unassigned tasks.

There are at most \(n\) tasks per iteration and \(n\) iterations:

$$
O(n^2).
$$

Calculating the saturation set by scanning a task's neighbors costs

$$
O(\deg(t)).
$$

Across all tasks, this is

$$
O(m)
$$

per complete priority recomputation.

Thus a straightforward implementation remains polynomial.

---

## Candidate-slot search

For every selected task, we inspect at most \(K\) slots.

For each slot we check:

* conflicts;
* \(4\) resource dimensions;
* candidate score.

A direct conflict check can cost \(O(n)\), while the resource check is

$$
O(4)=O(1).
$$

Therefore the conservative cost per task is

$$
O(Kn).
$$

Over \(n\) tasks:

$$
O(n^2K).
$$

---

## Priority queue implementation

The priority ordering can instead be maintained using a heap.

For each task we maintain its key

$$
Q(t).
$$

Heap insertion/extraction costs

$$
O(\log n).
$$

There are \(n\) extractions and at most \(O(m)\) neighbor-key updates, giving approximately

$$
O((n+m)\log n).
$$

Because the conflict graph can contain

$$
m=O(n^2),
$$

this is bounded by

$$
O(n^2\log n).
$$

---

## Candidate penalty computation

There are at most \(K\) candidate slots for each task.

If the Step 2 penalty is recomputed directly from scratch for each candidate, this could cost

$$
O(n+K)
$$

per candidate.

That would give the conservative bound

$$
O(nK(n+K)).
$$

With the assignment's \(K\le20\), this is polynomial and effectively quadratic in \(n\).

However, a production implementation should maintain slot loads incrementally and calculate only the affected terms. Then the candidate evaluation can be reduced substantially.

---

# 12. Overall worst-case bound

Using the straightforward implementation,

$$
T(n,K,m)
=
O(n^2K+n^2\log n+m\log n).
$$

Since

$$
m\le\binom n2=O(n^2),
$$

we obtain

$$
T(n,K)
=
O(n^2K+n^2\log n).
$$

If \(K\le n\), then

$$
\boxed{
T(n)=O(n^3)
}
$$

is a conservative polynomial upper bound.

For the assignment's stated graded range, where

$$
K\le20,
$$

we can state the tighter practical bound

$$
\boxed{
T(n)=O(n^2\log n)
}
$$

for the heap-based implementation, excluding expensive from-scratch objective recomputation.

The assignment's specified instance sizes have \(20\le n\le200\) and \(3\le K\le20\), so this polynomial behavior is appropriate for the intended scale. 

genui{"learning_viz":{"type_id":"BIG_O_TIME_COMPLEXITY"}}

---

# 13. Why CRAD is suited to this problem

The central architectural idea is:

$$
\boxed{
\text{constrain first, optimize second}.
}
$$

The problem is not merely a weighted scheduling problem. Every task simultaneously interacts with:

$$
\text{conflicts}
+
\text{resources}
+
\text{SLA windows}.
$$

CRAD therefore prioritizes the constraint that is most likely to become impossible first:

$$
\operatorname{sat}(t)
\rightarrow
\deg(t)
\rightarrow
\text{SLA tightness}
\rightarrow
\text{priority weight}.
$$

Then, **only among currently feasible slots**, it uses the penalty from Step 2 to select the placement.

This separation is important:

$$
\boxed{
\text{priority determines which task to place;}
}
$$

$$
\boxed{
\text{feasibility tests determine where it may go;}
}
$$

$$
\boxed{
\text{penalty determines which legal location is preferred.}
}
$$

Thus the algorithm does not trade away feasibility merely to improve the objective.

---

# 14. Final algorithm summary

The complete strategy can be summarized as:

$$
\boxed{
\begin{array}{c}
\textbf{CRAD}\\[2mm]
\downarrow\\
\text{Select most constrained task}\\
\downarrow\\
\text{Filter by SLA window}\\
\downarrow\\
\text{Filter by conflicts}\\
\downarrow\\
\text{Filter by 4-D resource capacity}\\
\downarrow\\
\text{Score remaining slots using }\Delta P\\
\downarrow\\
\text{Choose best feasible slot}\\
\downarrow\\
\text{Update loads and DSATUR scores}\\
\downarrow\\
\text{Repeat}\\
\downarrow\\
\text{FEASIBLE assignment or INFEASIBLE-under-heuristic}
\end{array}
}
$$

The key guarantee is:

$$
\boxed{
\text{CRAD returns FEASIBLE}
\Longrightarrow
F1\land F2\land F3.
}
$$

It does **not** claim:

$$
\text{CRAD returns INFEASIBLE}
\Longrightarrow
\text{no feasible assignment exists},
$$

because that would turn the heuristic into a complete polynomial-time infeasibility solver for an NP-hard problem.

This gives us a clean foundation for **Step 4**, where we can formally analyze exactly what feasibility guarantee CRAD can prove, derive an approximation/bound if possible, and construct a tight adversarial example—the assignment's three requested levels of analysis. 
