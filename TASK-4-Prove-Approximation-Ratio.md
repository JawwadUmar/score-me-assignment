Task 4 exposes an important issue in the Step 3 algorithm: **the full feasibility guarantee and finite approximation ratio requested in the rubric cannot be proved for CRAD as currently designed.** In fact, CRAD has a concrete counterexample in which a feasible instance is reported `INFEASIBLE`. Therefore, presenting a proof of the requested guarantee would be mathematically incorrect.

The assignment itself asks Task 4 to prove that the algorithm always finds a feasible assignment whenever one exists, and then asks for an approximation ratio and tight example.  The rigorous analysis below identifies exactly where the Step 3 design falls short and gives the strongest valid theorem we can prove.

# Task 4 — Theoretical Analysis of CRAD

## 1. Feasibility Guarantee

### 1.1 What we can prove

For CRAD, we can prove the following **conditional feasibility theorem**:

$$
\boxed{
\text{If CRAD returns an assignment }A,
\text{ then }A\text{ satisfies }F1,F2,F3.
}
$$

However, the stronger statement requested by the rubric,

$$
\boxed{
\text{If a feasible assignment exists, CRAD always finds one},
}
$$

is **false**.

We first prove the valid conditional theorem and then give a counterexample to the stronger claim.

---

## 2. Conditional Feasibility Theorem

### Theorem 1

Let \(I=(T,E,r,C,w,\tau,K)\) be any scheduling instance. If CRAD terminates with an assignment

$$
A:T\rightarrow\{1,\ldots,K\},
$$

then \(A\) satisfies all three feasibility constraints \(F1,F2,F3\).

### Proof

We prove the result using the invariant maintained after every successful assignment.

Define the invariant:

$$
\mathcal I_q:
$$

> After \(q\) iterations of CRAD, every one of the \(q\) assigned tasks satisfies its SLA window, does not conflict with any already-assigned neighboring task, and the resource capacity of every slot is respected.

We prove this by induction.

### Base case

Initially,

$$
q=0.
$$

No tasks have been assigned, so

$$
L_j(s)=0
$$

for every resource \(j\) and slot \(s\).

Since resource requirements and capacities are nonnegative,

$$
0\le C_j(s).
$$

There are also no assigned edges that could violate \(F1\).

Therefore,

$$
\mathcal I_0
$$

holds.

---

### Inductive step

Assume

$$
\mathcal I_q
$$

holds.

CRAD selects an unassigned task \(t\) and considers a candidate slot \(s\).

A slot is admitted only if all three tests pass.

#### \(F3\): SLA constraint

CRAD considers only

$$
s\in[\ell_t,u_t].
$$

Therefore,

$$
\ell_t\le A(t)=s\le u_t.
$$

Hence \(F3\) remains satisfied.

#### \(F1\): conflict constraint

Let \(u\) be any already-assigned neighbor of \(t\).

CRAD constructs

$$
D(t)
=
\{A(u):(t,u)\in E,\ A(u)\neq\bot\}.
$$

The candidate slot is rejected whenever

$$
s\in D(t).
$$

Consequently,

$$
A(u)\ne s=A(t)
$$

for every already-assigned conflicting neighbor \(u\).

Previously assigned pairs remain valid by the induction hypothesis.

Thus \(F1\) remains satisfied.

#### \(F2\): resource capacity

CRAD accepts \(s\) only if, for every resource dimension \(j\),

$$
L_j(s)+r_j(t)\le C_j(s).
$$

After assignment,

$$
L'_j(s)
=
L_j(s)+r_j(t),
$$

so

$$
L'_j(s)\le C_j(s).
$$

Other slots are unchanged.

Thus \(F2\) remains satisfied.

Therefore,

$$
\mathcal I_q\Longrightarrow\mathcal I_{q+1}.
$$

By induction,

$$
\mathcal I_n
$$

holds when all \(n\) tasks have been assigned.

Therefore,

$$
\boxed{
A\text{ returned by CRAD satisfies }F1,F2,F3.
}
$$

\(\square\)

---

# 3. Why the Required Stronger Guarantee Is False

The previous theorem does **not** establish

$$
\text{feasible instance}\Longrightarrow\text{CRAD finds a solution}.
$$

The greedy decision made early in the algorithm can destroy a feasible placement for a later task.

Here is a concrete counterexample.

---

## 3.1 Counterexample instance

Use

$$
K=3
$$

slots and four tasks

$$
T=\{A,B,D_1,D_2\}.
$$

Use four resource dimensions, with every task requiring

$$
r(t)=(1,1,1,1).
$$

Every slot has capacity

$$
C(s)=(2,2,2,2),
\qquad s\in\{1,2,3\}.
$$

### Conflict edges

Let

$$
E=\{(A,D_1),(A,D_2)\}.
$$

Thus \(A\) conflicts with both \(D_1\) and \(D_2\), while \(B\) has no conflicts.

### SLA windows

$$
\tau(A)=[1,3],
$$

$$
\tau(B)=[1,1],
$$

$$
\tau(D_1)=\tau(D_2)=[2,3].
$$

### Weights

Take

$$
w(A)=10,
$$

$$
w(B)=1,
$$

$$
w(D_1)=w(D_2)=1.
$$

---

# 4. A Feasible Assignment Exists

Consider

$$
A^*(A)=2,
$$

$$
A^*(B)=1,
$$

$$
A^*(D_1)=A^*(D_2)=3.
$$

### SLA

$$
2\in[1,3],
$$

$$
1\in[1,1],
$$

$$
3\in[2,3].
$$

Thus \(F3\) holds.

### Conflicts

Since

$$
A^*(A)=2
$$

and

$$
A^*(D_1)=A^*(D_2)=3,
$$

both conflict edges have differently assigned slots.

Thus \(F1\) holds.

### Resources

Slot 1 contains \(B\):

$$
L(1)=(1,1,1,1)\le(2,2,2,2).
$$

Slot 2 contains \(A\):

$$
L(2)=(1,1,1,1)\le(2,2,2,2).
$$

Slot 3 contains \(D_1,D_2\):

$$
L(3)=(2,2,2,2)\le(2,2,2,2).
$$

Therefore \(F2\) holds.

Hence

$$
\boxed{A^*\text{ is feasible}.}
$$

---

# 5. CRAD's Execution

Initially every task has saturation

$$
\operatorname{sat}(t)=0.
$$

The degrees are

$$
\deg(A)=2,
$$

$$
\deg(B)=0,
$$

$$
\deg(D_1)=\deg(D_2)=1.
$$

Therefore CRAD selects

$$
\boxed{A}
$$

first because it has the largest degree.

For \(A\), the feasible slots initially are

$$
\{1,2,3\}.
$$

Because \(A\) has weight \(10\), the base component of the incremental penalty is

$$
10s.
$$

Thus:

$$
P_{\text{base}}(A\mapsto1)=10,
$$

$$
P_{\text{base}}(A\mapsto2)=20,
$$

$$
P_{\text{base}}(A\mapsto3)=30.
$$

The load-imbalance term does not distinguish these three choices at this point: placing one identical task into any one of three identical-capacity slots produces the same multiset of utilization levels.

Therefore CRAD selects

$$
\boxed{A(A)=1}.
$$

Now consider \(B\).

Its SLA window is

$$
[1,1].
$$

Therefore its only possible slot is

$$
s=1.
$$

But slot 1 currently contains \(A\), whose resource usage is

$$
(1,1,1,1).
$$

Adding \(B\) would produce

$$
(2,2,2,2),
$$

which actually still fits capacity \(2\).

So this particular numerical choice **does not yet cause failure**.

We therefore need the capacity to be \(1\), but that creates an issue for the feasible solution involving \(D_1,D_2\). We can fix this cleanly by making the resource requirements asymmetric.

---

# 6. Corrected Counterexample

Use the same tasks, conflicts, windows, and weights, but let the resource requirements be

$$
r(A)=(1,1,1,1),
$$

$$
r(B)=(1,1,1,1),
$$

$$
r(D_1)=r(D_2)=(0,0,0,0).
$$

Let all capacities be

$$
C(s)=(1,1,1,1).
$$

Zero resource requirements are permitted by the formal model because it specifies resource requirements as nonnegative vectors.

The feasible assignment remains

$$
A^*(A)=2,\qquad
A^*(B)=1,\qquad
A^*(D_1)=A^*(D_2)=3.
$$

But CRAD chooses \(A\to1\), after which

$$
L(1)=(1,1,1,1).
$$

Task \(B\) can only use slot \(1\), and

$$
L(1)+r(B)
=
(2,2,2,2)
>
(1,1,1,1).
$$

Therefore

$$
\mathcal F_B(A)=\varnothing.
$$

CRAD returns

$$
\boxed{\texttt{INFEASIBLE}}.
$$

Yet \(A^*\) is feasible.

Therefore:

$$
\boxed{
\exists I:
I\text{ is feasible but CRAD returns INFEASIBLE}.
}
$$

Hence the requested theorem

$$
\boxed{
I\text{ feasible}\Rightarrow\text{CRAD returns feasible}
}
$$

is false.

---

# 7. Consequence for the 10-point Feasibility Requirement

The strongest mathematically valid result for the current algorithm is therefore:

$$
\boxed{
\text{CRAD-output}\Rightarrow F1\land F2\land F3
}
$$

but **not**

$$
\boxed{
\text{feasible instance}\Rightarrow\text{CRAD-output}.
}
$$

This is not merely a gap in the proof; the counterexample demonstrates that the stronger statement is false.

To obtain the rubric's requested guarantee, Step 3 would need to be modified to include **backtracking or another complete search mechanism**. But complete search is exponential in the worst case for this NP-hard problem, so it would no longer satisfy Step 3's polynomial-time requirement.

---

# 8. Approximation Ratio

The requested approximation guarantee is

$$
P(A_{\text{CRAD}})
\le
\alpha P(A^*)
$$

for some finite

$$
\alpha>1.
$$

For the current CRAD algorithm, **no such universal finite \(\alpha\) can be established**.

The reason follows immediately from the counterexample.

For that instance,

$$
A^*
$$

exists, so

$$
P(A^*)<\infty.
$$

But CRAD returns

$$
\texttt{INFEASIBLE}.
$$

Thus there is no assignment

$$
A_{\text{CRAD}}
$$

whose penalty can be compared with \(P(A^*)\).

If we represent failure mathematically by

$$
P(\texttt{INFEASIBLE})=+\infty,
$$

then

$$
P(A_{\text{CRAD}})
=
+\infty
$$

while

$$
P(A^*)<\infty.
$$

Consequently, for every finite \(\alpha\),

$$
+\infty
>
\alpha P(A^*).
$$

Therefore,

$$
\boxed{
\not\exists\alpha<\infty
\quad
\text{such that}
\quad
P(A_{\text{CRAD}})
\le
\alpha P(A^*)
$$

for all valid instances.

In other words, the approximation ratio of the current algorithm is **unbounded** if failure is included.

---

# 9. Why the Step 2 penalty makes a universal ratio even harder

Recall our objective:

$$
P(A)
=
P_{\text{base}}(A)+\lambda I(A),
$$

where

$$
P_{\text{base}}(A)
=
\sum_iw_iA(t_i).
$$

The weights \(w_i\) are arbitrary positive inputs.

Thus the scale of the objective itself is not bounded by a fixed constant independent of the instance.

Even if we restricted attention to instances on which CRAD succeeds, a finite approximation theorem would require an additional structural argument bounding the damage caused by its greedy choices. The Step 3 algorithm provides no such exchange argument.

In particular, DSATUR-style selection establishes a useful **ordering heuristic**, but it does not imply

$$
P(A_{\text{CRAD}})
\le
f(\Delta,K,C,\ldots)P(A^*)
$$

for this compound scheduling objective.

Therefore we should not invent an \(\alpha\) merely to satisfy the requested format.

---

# 10. Tight Adversarial Example

The counterexample above is already a tight adversarial example for the **feasibility limitation** of CRAD.

Summarize it as

$$
I_{\mathrm{adv}}
=
(T,E,r,C,w,\tau,K)
$$

with

$$
T=\{A,B,D_1,D_2\},
$$

$$
K=3,
$$

$$
E=\{(A,D_1),(A,D_2)\},
$$

$$
r(A)=r(B)=(1,1,1,1),
$$

$$
r(D_1)=r(D_2)=(0,0,0,0),
$$

$$
C(1)=C(2)=C(3)=(1,1,1,1),
$$

$$
\tau(A)=[1,3],
$$

$$
\tau(B)=[1,1],
$$

$$
\tau(D_1)=\tau(D_2)=[2,3],
$$

and

$$
w(A)=10,\qquad
w(B)=w(D_1)=w(D_2)=1.
$$

The optimal/feasible schedule is

$$
\boxed{
A^*=
\{A\mapsto2,\;
B\mapsto1,\;
D_1\mapsto3,\;
D_2\mapsto3\}.
}
$$

CRAD instead makes

$$
A\mapsto1
$$

because \(A\) has the largest initial degree and slot \(1\) minimizes its weighted delay.

It then reaches

$$
\mathcal F_B=\varnothing
$$

because \(B\) has the singleton SLA window

$$
[1,1]
$$

and slot \(1\) is already saturated.

Thus

$$
\boxed{
I_{\mathrm{adv}}\text{ is feasible but CRAD fails}.
}
$$

This demonstrates that the failure is not caused by an invalid output: CRAD never violates \(F1,F2,F3\). Instead, it makes a **locally valid but globally harmful greedy choice**.

---

# 11. What would be needed to satisfy the original Task 4 rubric?

There are two possible paths.

### Option A — Preserve polynomial time

Keep CRAD and honestly report the strongest theorem:

$$
\boxed{
\text{CRAD returns}
\Rightarrow
\text{feasible}.
}
$$

Then explicitly document:

$$
\boxed{
\text{No completeness guarantee and no finite worst-case approximation ratio}.
}
$$

This is mathematically defensible, although it would not satisfy the assignment's stated full-credit requirement for the feasibility and approximation sections.

### Option B — Modify Step 3

Add bounded backtracking:

$$
\text{CRAD}
+
\text{limited backtracking}.
$$

For example, when a task has no available slot, undo the last \(b\) decisions and try the next-best alternatives.

This can substantially improve empirical feasibility, but with unrestricted backtracking the worst-case complexity becomes exponential:

$$
O(K^n).
$$

Therefore, it cannot simultaneously provide a complete feasibility guarantee for this NP-hard problem and remain a polynomial-time algorithm unless \(P=NP\).

---

# 12. Final Theoretical Conclusions

For the **CRAD algorithm exactly as designed in Step 3**, the rigorous Task 4 conclusions are:

$$
\boxed{\textbf{Feasibility}}
$$

$$
\text{CRAD returns an assignment}
\Longrightarrow
F1\land F2\land F3.
$$

But

$$
\boxed{
\text{Feasible instance}
\not\Longrightarrow
\text{CRAD returns an assignment}.
}
$$

Therefore:

$$
\boxed{\textbf{Approximation}}
$$

$$
\boxed{
\alpha_{\mathrm{worst}}=\infty
}
$$

if failure is treated as infinite penalty.

Finally,

$$
\boxed{\textbf{Tightness}}
$$

The four-task construction above establishes that the failure is real: a feasible schedule exists, but the greedy algorithm can commit to a locally attractive slot that eliminates the only SLA-compatible slot of another task.

So the correct mathematical conclusion is **not** an invented finite approximation ratio. The current Step 3 heuristic has a valid **feasibility-preservation guarantee**, but no **completeness guarantee** and no finite worst-case approximation ratio.

This also gives us a useful design lesson: the weakness comes specifically from making an irreversible greedy placement. A modified algorithm with controlled backtracking would be the natural route if the goal is to improve Task 4's score, at the cost of losing the unconditional polynomial-time guarantee. The assignment explicitly asks for the feasibility guarantee, approximation ratio, and tight adversarial example as separate levels of credit. 
