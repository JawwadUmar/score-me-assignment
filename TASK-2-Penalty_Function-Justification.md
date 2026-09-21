For Step 2, I am using a **resource-load imbalance penalty**. It fits the scheduling problem naturally and gives us a clean mathematical and complexity analysis.

# Step 2 — Design and Justify the Penalty Function

## 1. Formal definition

Recall the base penalty specified in the assignment:

$$
P_{\text{base}}(\sigma)
=
\sum_{i=1}^{n}w(t_i)\sigma(t_i),
$$

which penalizes assigning higher-priority tasks to later slots. 

We extend it with a **resource-load imbalance penalty**.

Let

$$
R_j(s;A)
=
\frac{
\displaystyle\sum_{t_i:A(t_i)=s}r_j(t_i)
}{
C_j(s)
}
$$

denote the utilization of resource dimension \(j\) in slot \(s\), where

$$
j\in\{1,2,3,4\}
$$

corresponds to CPU, RAM, GPU, and Network.

Thus,

$$
R_j(s;A)\in[0,1]
$$

for a feasible assignment.

Define the average utilization of resource \(j\) across all \(K\) slots as

$$
\overline R_j(A)
=
\frac{1}{K}\sum_{s=1}^{K}R_j(s;A).
$$

We measure imbalance using squared deviations from this average:

$$
I(A)
=
\sum_{j=1}^{4}
\sum_{s=1}^{K}
\left(
R_j(s;A)-\overline R_j(A)
\right)^2.
$$

Our complete penalty is therefore

$$
\boxed{
P(A)
=
\sum_{i=1}^{n}w(t_i)A(t_i)
+
\lambda
\sum_{j=1}^{4}
\sum_{s=1}^{K}
\left(
R_j(s;A)-\overline R_j(A)
\right)^2
}
$$

where

$$
\lambda>0
$$

is a tunable parameter controlling how strongly load balancing influences the objective.

Equivalently, substituting the definition of \(R_j\),

$$
\boxed{
P(A)
=
\sum_{i=1}^{n}w(t_i)A(t_i)
+
\lambda
\sum_{j=1}^{4}
\sum_{s=1}^{K}
\left[
\frac{\sum_{A(t_i)=s}r_j(t_i)}{C_j(s)}
-
\frac1K
\sum_{q=1}^{K}
\frac{\sum_{A(t_i)=q}r_j(t_i)}{C_j(q)}
\right]^2.
}
$$

The first term preserves the assignment's original delay/priority objective, while the second term discourages highly uneven utilization across processing slots.

The assignment explicitly permits load imbalance as a meaningful additional term and requires such an extension to be non-trivial and polynomial-time computable. 

---

# 2. Polynomial-time computability

We now show that \(P(A)\) can be evaluated in polynomial time.

Suppose we are given:

* \(n\) tasks,
* \(K\) slots,
* \(4\) resource dimensions,
* a candidate assignment \(A\).

We assume the numerical values \(r_j(t_i)\), \(C_j(s)\), and \(w(t_i)\) are represented with polynomially bounded precision.

## Step 1: Compute the base penalty

Calculate

$$
P_{\text{base}}(A)
=
\sum_{i=1}^{n}w(t_i)A(t_i).
$$

There are \(n\) terms, so:

$$
T_1(n,K)=O(n).
$$

---

## Step 2: Compute resource utilization

For every slot \(s\) and resource dimension \(j\), calculate

$$
L_j(s)
=
\sum_{A(t_i)=s}r_j(t_i).
$$

There are \(n\) tasks and \(4\) resource dimensions.

We can process every task once and add its resource vector to its assigned slot:

$$
T_2(n,K)=O(4n)=O(n).
$$

Then calculate

$$
R_j(s)=\frac{L_j(s)}{C_j(s)}
$$

for all \(4K\) slot-resource pairs.

Therefore,

$$
T_3(K)=O(4K)=O(K).
$$

---

## Step 3: Compute average utilization

For each resource dimension \(j\),

$$
\overline R_j
=
\frac1K\sum_{s=1}^{K}R_j(s).
$$

There are \(4\) dimensions and \(K\) slots:

$$
T_4(K)=O(4K)=O(K).
$$

---

## Step 4: Compute the imbalance penalty

Evaluate

$$
I(A)
=
\sum_{j=1}^{4}\sum_{s=1}^{K}
(R_j(s)-\overline R_j)^2.
$$

Again, there are exactly

$$
4K
$$

terms, giving

$$
T_5(K)=O(4K)=O(K).
$$

---

## Step 5: Combine the terms

Finally,

$$
P(A)=P_{\text{base}}(A)+\lambda I(A)
$$

requires constant-time arithmetic once the two components have been calculated:

$$
T_6=O(1).
$$

Hence the total complexity is

$$
\begin{aligned}
T(n,K)
&=
O(n)+O(n)+O(K)+O(K)+O(K)\\
&=
O(n+K).
\end{aligned}
$$

Since the assignment has \(K\leq20\) in its stated graded-instance range, this is effectively

$$
\boxed{O(n)}.
$$

More generally, if \(K\) is considered part of the input,

$$
\boxed{
T(n,K)=O(n+K)
}
$$

which is polynomial.

Thus \(P(A)\) is polynomial-time computable.

---

# 3. Monotonicity and domain justification

There are two different notions we should distinguish:

1. **constraint validity**, determined by \(F1,F2,F3\);
2. **quality among feasible assignments**, determined by \(P(A)\).

The assignment itself defines feasibility through conflict avoidance, resource capacity, and SLA compliance. 

Therefore, the cleanest formulation is to define \(P(A)\) over feasible assignments and use \(+\infty\) for infeasible assignments:

$$
\boxed{
\widetilde P(A)=
\begin{cases}
P(A), & A\text{ satisfies }F1,F2,F3,\\[4pt]
+\infty, & \text{otherwise}.
\end{cases}
}
$$

This ensures that an infeasible assignment can never be preferred to a feasible one.

### Interpretation of a lower penalty

For feasible assignments,

$$
P(A)
=
P_{\text{base}}(A)+\lambda I(A).
$$

The first component,

$$
P_{\text{base}}(A)
=
\sum_iw(t_i)A(t_i),
$$

increases when high-weight tasks are placed in later slots.

Therefore, holding everything else fixed, moving an important task to an earlier slot decreases its contribution to the base penalty.

The second component,

$$
I(A)
=
\sum_{j,s}
(R_j(s)-\overline R_j)^2,
$$

measures how unevenly resource utilization is distributed.

For a fixed resource \(j\),

$$
\sum_{s=1}^{K}
(R_j(s)-\overline R_j)^2
\geq0.
$$

It equals zero precisely when

$$
R_j(1)=R_j(2)=\cdots=R_j(K).
$$

Therefore,

$$
I(A)\geq0,
$$

and smaller values correspond to more balanced resource utilization.

For example, consider two feasible schedules with identical base penalty:

$$
P_{\text{base}}(A_1)
=
P_{\text{base}}(A_2).
$$

If

$$
I(A_1)<I(A_2),
$$

then

$$
P(A_1)<P(A_2).
$$

Thus the proposed extension prefers the schedule that distributes computational load more evenly.

This is consistent with the assignment's stated motivation that a slot operating at, for example, \(95\%\) CPU utilization while another is at \(10\%\) can be operationally undesirable. 

---

# 4. Does \(P(A)=0\) mean a perfectly valid assignment?

There is an important mathematical subtlety here.

Under the original base penalty,

$$
P_{\text{base}}(A)
=
\sum_iw(t_i)A(t_i),
$$

and the assignment slots are positive integers. Assuming

$$
w(t_i)>0,
$$

we have

$$
P_{\text{base}}(A)>0
$$

for every nonempty assignment.

Therefore, **it would be incorrect to claim that the original optimization objective has \(P(A)=0\) for a valid assignment.**

Instead, the correct interpretation is:

$$
\boxed{
P(A)=P_{\min}
}
$$

represents an optimal feasible assignment, where

$$
P_{\min}
=
\min_{A\text{ feasible}}P(A).
$$

Meanwhile,

$$
\boxed{
\widetilde P(A)=+\infty
}
$$

identifies an infeasible assignment.

So validity and optimality should not be conflated:

$$
\text{valid}
\not\equiv
P(A)=0.
$$

Rather,

$$
\text{valid}
\iff
A\text{ satisfies }F1,F2,F3,
$$

and

$$
\text{optimal}
\iff
A\text{ is feasible and }
P(A)=P_{\min}.
$$

This distinction follows directly from the assignment's formulation, which defines feasibility separately from the objective. 

If the assignment specifically insists on a **zero-based violation penalty**, we could define a separate feasibility-loss function later, but that would be different from the required extension of \(P_{\text{base}}\).

---

# 5. Non-triviality guarantee

The additional term

$$
\lambda I(A)
$$

is non-trivial because it depends directly on the assignment \(A\).

In particular,

$$
I(A)
=
\sum_{j=1}^{4}
\sum_{s=1}^{K}
(R_j(s)-\overline R_j)^2
$$

can take different values for different assignments.

For example, suppose, for one resource dimension, two feasible schedules produce utilization vectors

$$
R(A_1)=(0.25,0.25,0.25,0.25)
$$

and

$$
R(A_2)=(0.10,0.10,0.10,0.70).
$$

For \(A_1\),

$$
\overline R=0.25
$$

and therefore

$$
I(A_1)=0.
$$

For \(A_2\),

$$
\overline R=0.25,
$$

so

$$
\begin{aligned}
I(A_2)
&=
(0.10-0.25)^2
+(0.10-0.25)^2\\
&\quad +(0.10-0.25)^2
+(0.70-0.25)^2\\
&>0.
\end{aligned}
$$

Thus

$$
I(A_1)\ne I(A_2).
$$

Consequently,

$$
P(A_1)\ne P(A_2)
$$

in general.

The added term is therefore:

* **not constant**;
* **not identically zero**;
* **not independent of the assignment**;
* sensitive to how tasks are distributed across slots.

It genuinely changes the optimization landscape.

This satisfies the assignment's requirement that the extension be non-trivial rather than merely adding a constant or zero term. 

---

# 6. Final proposed penalty function

The complete function I would put in the report is:

$$
\boxed{
P(A)
=
\sum_{i=1}^{n}w(t_i)A(t_i)
+
\lambda
\sum_{j=1}^{4}
\sum_{s=1}^{K}
\left(
\frac{\sum_{A(t_i)=s}r_j(t_i)}
{C_j(s)}
-
\frac{1}{K}
\sum_{q=1}^{K}
\frac{\sum_{A(t_i)=q}r_j(t_i)}
{C_j(q)}
\right)^2
}
$$

for feasible assignments \(A\), with

$$
\boxed{
\widetilde P(A)=+\infty
}
$$

for assignments violating \(F1\), \(F2\), or \(F3\).

Its properties are:

$$
\boxed{
\begin{array}{ll}
\text{Polynomial evaluation:} & O(n+K),\\[2mm]
\text{Resource dimensions:} & 4,\\[2mm]
\text{Additional concern modeled:} & \text{load imbalance},\\[2mm]
\text{Non-negative imbalance term:} & I(A)\ge0,\\[2mm]
\text{Feasibility:} & F1,F2,F3,\\[2mm]
\text{Optimization:} & \min_A P(A),\\[2mm]
\text{Infeasible assignments:} & \widetilde P(A)=+\infty.
\end{array}
}
$$

The assignment specifically asks for an extension that is mathematically defined, polynomial-time computable, meaningful under minimization, and non-trivial; this construction satisfies those requirements while staying close to the stated ScoreMe scheduling concerns. 