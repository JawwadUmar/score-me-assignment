For Step 1, I am using **3-SAT \(\le_p\) this scheduling problem** directly, with the construction arranged so that the three feasibility families are all explicitly enforced. One important modeling point is that the objective \(P(\sigma)\) does **not** affect feasibility: the assignment first requires \(F1,F2,F3\), and only then minimizes \(P\). 

## Step 1 — NP-hardness proof

### 1. Formal decision version

Define the following decision problem.

### **MSME-CREDIT-SCHEDULING-FEASIBILITY**

**Input.** An instance

$$
I=(T,K,d,G,r,C,w,\tau)
$$

where:

* \(T=\{t_1,\ldots,t_n\}\) is the set of tasks;
* \(K\) is the number of processing slots;
* \(d=4\) resource dimensions;
* \(G=(T,E)\) is the conflict graph;
* \(r(t_i)\in\mathbb R_{\ge 0}^{4}\) is the resource requirement of task \(t_i\);
* \(C(s)\in\mathbb R_{\ge0}^{4}\) is the capacity of slot \(s\);
* \(\tau(t_i)=[\ell_i,u_i]\) is the allowed SLA window.

These are exactly the feasibility components specified by the assignment. 

**Question.** Does there exist an assignment

$$
\sigma:T\rightarrow\{1,\ldots,K\}
$$

such that simultaneously:

$$
\tag{F1}
(t_i,t_j)\in E\implies \sigma(t_i)\ne\sigma(t_j),
$$

$$
\tag{F2}
\forall s\in\{1,\ldots,K\},
\qquad
\sum_{\sigma(t_i)=s}r(t_i)\le C(s)
$$

componentwise, and

$$
\tag{F3}
\forall t_i\in T,
\qquad
\ell_i\le \sigma(t_i)\le u_i?
$$

We will prove that this decision problem is NP-hard.

The penalty \(P(\sigma)\) is irrelevant to this decision version because the original problem requires feasibility first and optimization second. 

---

# 2. Source problem: 3-SAT

Let

$$
\phi=C_1\land C_2\land\cdots\land C_m
$$

be an arbitrary 3-CNF formula over Boolean variables

$$
x_1,x_2,\ldots,x_q,
$$

where every clause contains exactly three literals.

We construct, in polynomial time, a scheduling instance

$$
f(\phi)=I_\phi
$$

such that

$$
\boxed{
\phi\text{ is satisfiable}
\iff
I_\phi\text{ has a feasible schedule}.
}
$$

The construction uses the standard polynomial reduction from 3-SAT to **3-COLORING** as its logical core.

Let

$$
H_\phi=(V_\phi,E_\phi)
$$

be the 3-coloring instance produced from \(\phi\) by the standard 3-SAT-to-3-COLORING construction. Thus,

$$
\phi\text{ satisfiable}
\iff
H_\phi\text{ is 3-colorable}.
$$

The graph contains the usual fixed reference vertices representing the three colors, together with variable and clause gadgets. Each edge of \(H_\phi\) means that its two endpoints must receive different colors.

The important point for our reduction is that the graph has polynomial size:

$$
|V_\phi|=O(q+m),
\qquad
|E_\phi|=O(q+m).
$$

---

# 3. Construction \(f(\phi)\)

We now turn \(H_\phi\) into a scheduling instance.

## 3.1 Tasks

For every graph vertex

$$
v\in V_\phi
$$

create one scheduling task \(t_v\).

Therefore,

$$
T_\phi=\{t_v:v\in V_\phi\}.
$$

We also choose

$$
K=3
$$

processing slots.

Interpret the three slots as the three colors:

$$
\text{slot }1=\text{color }A,
\qquad
\text{slot }2=\text{color }B,
\qquad
\text{slot }3=\text{color }C.
$$

---

## 3.2 Conflict graph

For every edge

$$
(v,u)\in E_\phi
$$

create the corresponding scheduling conflict

$$
(t_v,t_u)\in E.
$$

Hence,

$$
E=E_\phi.
$$

Consequently, the scheduling constraint

$$
\sigma(t_v)\ne\sigma(t_u)
$$

is exactly the graph-coloring constraint that adjacent vertices receive different colors.

This is the part of the construction that carries the logical information from the 3-SAT formula.

---

## 3.3 SLA windows

Every task is required to execute in one of the three designated slots:

$$
\tau(t_v)=[1,3]
\qquad
\forall v\in V_\phi.
$$

Thus every feasible schedule must satisfy

$$
1\le\sigma(t_v)\le3.
$$

The SLA constraint therefore prevents the scheduler from using any slot outside the three colors represented in the reduction.

Notice that this explicitly incorporates the temporal constraint \(F3\), rather than simply ignoring SLA windows.

---

## 3.4 Resource requirements and capacities

We use the four resource dimensions required by the assignment:

$$
(\mathrm{CPU},\mathrm{RAM},\mathrm{GPU},\mathrm{Network}).
$$

Give every task the resource vector

$$
r(t_v)=(1,1,1,1).
$$

Let

$$
N=|V_\phi|.
$$

For every slot \(s\in\{1,2,3\}\), define

$$
C(s)=(N,N,N,N).
$$

Therefore, for any slot,

$$
\sum_{\sigma(t_v)=s}r(t_v)
=
(|S_s|,|S_s|,|S_s|,|S_s|)
$$

where

$$
S_s=\{v:\sigma(t_v)=s\}.
$$

Since

$$
|S_s|\le N,
$$

we have

$$
\sum_{\sigma(t_v)=s}r(t_v)
\le
(N,N,N,N)
=
C(s).
$$

Thus the resource-capacity constraint is explicitly instantiated and is satisfied for every assignment permitted by the construction.

This is deliberate: the resource dimensions do not introduce an additional restriction that could destroy the equivalence with the 3-coloring instance. The logical restriction is carried by \(F1\), while \(F2\) and \(F3\) remain genuine constraints of the target problem.

---

# 4. Polynomial-time construction

The transformation is:

$$
\boxed{
f(\phi):
\phi
\longmapsto
H_\phi
\longmapsto
(T_\phi,E,r,C,\tau,K).
}
$$

The standard 3-SAT-to-3-COLORING construction produces

$$
|V_\phi|=O(q+m)
$$

vertices and

$$
|E_\phi|=O(q+m)
$$

edges.

For every vertex we create one task and one constant-size resource/window description. For every edge we create one conflict.

Therefore the total construction time is

$$
O(|V_\phi|+|E_\phi|)
=
O(q+m).
$$

Since the size of the input formula is

$$
n_\phi=O(q+m),
$$

we obtain

$$
\boxed{
T_f(n_\phi)=O(n_\phi)
}
$$

for the construction itself, and therefore certainly

$$
T_f(n_\phi)=O(n_\phi^k)
$$

for the polynomial bound required by the assignment, e.g. \(k=1\).

Thus \(f\) is a polynomial-time reduction.

---

# 5. Correctness proof

We prove

$$
\boxed{
\phi\text{ is satisfiable}
\iff
I_\phi\text{ has a feasible scheduling assignment}.
}
$$

We prove both directions.

---

## 5.1 If direction

Assume that

$$
\phi
$$

is satisfiable.

By correctness of the standard 3-SAT-to-3-COLORING construction,

$$
H_\phi
$$

has a proper 3-coloring.

Let

$$
c:V_\phi\rightarrow\{1,2,3\}
$$

be such a coloring.

Construct a scheduling assignment \(\sigma\) by

$$
\boxed{
\sigma(t_v)=c(v).
}
$$

We show that \(\sigma\) satisfies all three feasibility conditions.

### F1: Conflict avoidance

Suppose

$$
(t_v,t_u)\in E.
$$

By construction,

$$
(v,u)\in E_\phi.
$$

Since \(c\) is a proper 3-coloring,

$$
c(v)\ne c(u).
$$

Therefore,

$$
\sigma(t_v)=c(v)\ne c(u)=\sigma(t_u).
$$

Hence \(F1\) holds.

### F2: Resource capacity

Every task has resource vector

$$
(1,1,1,1),
$$

and each slot has capacity

$$
(N,N,N,N).
$$

At most \(N\) tasks can be assigned to any slot. Therefore, for every slot \(s\),

$$
\sum_{\sigma(t_v)=s}r(t_v)
\le
(N,N,N,N)
=
C(s).
$$

Hence \(F2\) holds.

### F3: SLA windows

Every task has window

$$
[1,3].
$$

Because

$$
c(v)\in\{1,2,3\},
$$

we have

$$
1\le\sigma(t_v)\le3.
$$

Hence every task satisfies its SLA window, so \(F3\) holds.

Therefore \(\sigma\) is a feasible scheduling assignment.

Thus,

$$
\boxed{
\phi\text{ satisfiable}
\Longrightarrow
I_\phi\text{ feasible}.
}
$$

---

# 6. Only-if direction

Now assume that the constructed scheduling instance \(I_\phi\) has a feasible assignment

$$
\sigma:T_\phi\rightarrow\{1,2,3\}.
$$

We construct a 3-coloring of \(H_\phi\).

For every graph vertex \(v\in V_\phi\), define

$$
\boxed{
c(v)=\sigma(t_v).
}
$$

Because the schedule is feasible, \(F3\) guarantees

$$
1\le\sigma(t_v)\le3.
$$

Therefore,

$$
c(v)\in\{1,2,3\}.
$$

Now consider any edge

$$
(v,u)\in E_\phi.
$$

By construction of the scheduling instance,

$$
(t_v,t_u)\in E.
$$

Since the schedule satisfies \(F1\),

$$
\sigma(t_v)\ne\sigma(t_u).
$$

Consequently,

$$
c(v)\ne c(u).
$$

Thus every edge of \(H_\phi\) has differently colored endpoints. Therefore,

$$
H_\phi
$$

has a proper 3-coloring.

By correctness of the standard reduction from 3-SAT,

$$
H_\phi\text{ 3-colorable}
\Longrightarrow
\phi\text{ satisfiable}.
$$

Hence,

$$
\boxed{
I_\phi\text{ feasible}
\Longrightarrow
\phi\text{ satisfiable}.
}
$$

---

# 7. Conclusion

We have constructed a polynomial-time transformation

$$
f:\text{3-SAT}\rightarrow\text{MSME-CREDIT-SCHEDULING-FEASIBILITY}
$$

such that for every 3-CNF formula \(\phi\),

$$
\boxed{
\phi\in3\text{-SAT}
\iff
f(\phi)\in\text{MSME-CREDIT-SCHEDULING-FEASIBILITY}.
}
$$

The transformation runs in polynomial time, specifically

$$
O(|\phi|).
$$

Since 3-SAT is NP-complete, it follows that

$$
\boxed{
\text{MSME-CREDIT-SCHEDULING-FEASIBILITY is NP-hard}.
}
$$

Finally, the original optimization problem is at least as hard as this feasibility problem: any algorithm capable of solving the optimization problem must in particular determine whether a feasible assignment satisfying \(F1,F2,F3\) exists. The assignment itself defines feasibility through exactly these three families of constraints. 

