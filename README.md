# YouTube Interest Analyzer based on linear space, cosine and Riemann Integral Sums Decay

- [Introduction](#introduction)
- [Mathematical Model](#mathematical-model)
    - [What is Linear Space?](#about-linear-space)
    - [What are Riemann Integral Sums?](#about-riemann-integral)
    - [Category Space](#category-space)
    - [User Vector](#user-vector)
    - [Recommendations](#recommendations)
- [Project Structure](#️-project-structure)


# Introduction
YouTube Interest Analyzer based on a geometric and integral approach 
where user and content are represented as vectors in a multi-dimensional category space. The system uses **integral decay (Riemann sum)** to account for temporal dynamics — older views lose relevance over time.

# Mathematical model
## About linear space
Linear space is a mathematical structure that represents 
a set of vectors for which the operation of addition to each other 
and multiplication by a scalar is defined.

(L, ℝ, +, ·)

### Why linear space?

1. **Categories as basis vectors** — each YouTube category is a basis vector
2. **User as linear combination** — user interests = sum of weighted categories
3. **Similarity as angle** — cosine between vectors shows interest closeness

## Riemann Integral Sums: Complete Introduction

### 1. Partition of an Interval

**Definition (Partition):** A **partition** of an interval $[a; b]$ is an arbitrary finite set $\tau$ of distinct points:

$$
a = x_0 < x_1 < x_2 < \dots < x_{n-1} < x_n = b
$$

The numbers $x_0, x_1, \dots, x_n$ are called **partition points**, and the intervals $[x_0; x_1], [x_1; x_2], \dots, [x_{n-1}; x_n]$ are called **partition subintervals**.

This set of points is $\tau $

The length of the $i$-th subinterval is denoted by:

$$
\Delta x_i = x_i - x_{i-1}, \quad i = 1, 2, \dots, n
$$

**Definition (Mesh/Rank):** The largest of the numbers $\Delta x_i$ is called the **mesh** or **rank** of the partition $\tau$ and is denoted by $\lambda(\tau)$:

$$
\lambda(\tau) = \max\{\Delta x_1, \Delta x_2, \dots, \Delta x_n\}
$$

**Example:**

For the interval $[0; 10]$ and partition $\tau: 0 < 2 < 5 < 7 < 10$:

$$
\Delta x_1 = 2,\ \Delta x_2 = 3,\ \Delta x_3 = 2,\ \Delta x_4 = 3 \quad \Rightarrow \quad \lambda(\tau) = 3
$$

---

### 2. Tagged Partition 

Choosing a point $\xi_i$ in **each** subinterval $[x_{i-1}; x_i]$ of the partition of $[a; b]$ gives us a **partition with marked points** or a **tagged partition**.

If we denote by $\xi$ the set of chosen points:

$$
\xi = \{\xi_1, \xi_2, \dots, \xi_n\}
$$

where $\xi_i \in [x_{i-1}; x_i]$ for each $i = 1, 2, \dots, n$, then the tagged partition is denoted by $(\tau, \xi)$.

The **rank** of a tagged partition $(\tau, \xi)$ is understood as the rank of the partition $\tau$:

$$
\lambda(\tau, \xi) = \lambda(\tau)
$$

### 3. Integral Sums

**Definition:** Let a function $f$ be defined on the interval $[a; b]$. For an arbitrary tagged partition $(\tau, \xi)$ of the interval $[a; b]$, where:

$$
\tau: a = x_0 < x_1 < x_2 < \dots < x_{n-1} < x_n = b
$$

$$
\xi: \xi_1, \xi_2, \dots, \xi_n, \quad \xi_i \in [x_{i-1}, x_i]
$$

we define the **integral sum** $S_f(\tau, \xi)$ by the formula:

$$
S_f(\tau, \xi) = \sum_{i=1}^{n} f(\xi_i) \, \Delta x_i
$$

where $\Delta x_i = x_i - x_{i-1}$.

**Geometric meaning:** Sum of areas of rectangles with:
- Height = $f(\xi_i)$
- Width = $\Delta x_i$

# Category Space

### YouTube Category Mapping

Each YouTube video belongs to one of 17 content categories. The system uses the following mapping:

| ID | Category Name | Index |
|----|--------------|-------|
| 1 | Film & Animation | 0 |
| 2 | Autos & Vehicles | 1 |
| 10 | Music | 2 |
| 15 | Pets & Animals | 3 |
| 17 | Sports | 4 |
| 18 | Short Movies | 5 |
| 19 | Travel & Events | 6 |
| 20 | Gaming | 7 |
| 21 | Videoblogging | 8 |
| 22 | People & Blogs | 9 |
| 23 | Comedy | 10 |
| 24 | Entertainment | 11 |
| 25 | News & Politics | 12 |
| 26 | Howto & Style | 13 |
| 27 | Education | 14 |
| 28 | Science & Technology | 15 |
| 29 | Nonprofits & Activism | 16 |

## Category as Basis Vector

Each category is represented as a **standard basis vector** in $\mathbb{R}^{17}$:

$$
\mathbf{e}_i = (0, 0, \dots, 1, \dots, 0)
$$

where the $1$ is at position $i$ (the index of the category), and all other coordinates are $0$.

**Examples:**

$$
\mathbf{e}_{\text{Music}} = (0, 0, 1, 0, \dots, 0)
$$

$$
\mathbf{e}_{\text{Gaming}} = (0, 0, 0, 0, 0, 0, 0, 1, 0, \dots, 0)
$$

$$
\mathbf{e}_{\text{Education}} = (0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0)
$$

### Linear Independence

The set of all category basis vectors $\{\mathbf{e}_1, \mathbf{e}_2, \dots, \mathbf{e}_{17}\}$ is **linearly independent**:

$$
\alpha_1 \mathbf{e}_1 + \alpha_2 \mathbf{e}_2 + \dots + \alpha_{17} \mathbf{e}_{17} = \mathbf{0} \;\Rightarrow\; \alpha_1 = \alpha_2 = \dots = \alpha_{17} = 0
$$

### User Vector

#### The Decay Formula

For each category, we compute its weight $V(t)$ using a **two-layer exponential decay** model:

$$
V(t) = V(t-1) \cdot \lambda + \big( D(t) \cdot \lambda^{\text{age}} \big)
$$

where:
- $V(t)$ — category weight at day $t$
- $\lambda = 0.95$ — decay coefficient (-5% every day)
- $D(t)$ — viewing dynamics at day $t$
- $\text{age} = T - t_k$ — age of the view in days
- $T$ — today's date
- $t_k$ — date of the view

#### Viewing Dynamics

The dynamics $D(t)$ represent the **session effect** — consecutive views amplify each other:

$$
D(t) = D(t-1) \cdot \lambda + W(t)
$$

where $W(t)$ is the total watch time (in minutes) for the category on day $t$.

#### Complete Algorithm in Code

```java
public void calculateWithDecayAndDynamics(double lambda) {
    LocalDate today = LocalDate.now();
    LocalDate cutoffDate = today.minusDays(MAX_DAYS);
    MyVector result = MyVector.zero(dimension);
    
    Map<Integer, TreeMap<LocalDate, Integer>> categoryByDay = groupEventsByDay();
    
    for (var entry : categoryByDay.entrySet()) {
        int categoryId = entry.getKey();
        TreeMap<LocalDate, Integer> days = entry.getValue();
        
        double dynamic = 0.0;  // D(t)
        double total = 0.0;    // V(t)
        
        for (LocalDate date = firstDay; !date.isAfter(today); date = date.plusDays(1)) {
            // Apply decay
            total = total * lambda;      // V(t) = V(t-1) * lambda
            dynamic = dynamic * lambda;  // D(t) = D(t-1) * lambda
            
            Integer watchTime = days.get(date);
            if (watchTime != null) {
                dynamic = dynamic + watchTime;  // D(t) += W(t)
                
                long age = ChronoUnit.DAYS.between(date, today);
                double decay = Math.pow(lambda, age);
                double contribution = decay * dynamic;  // D(t) * lambda^age
                
                total += contribution;  // V(t) += contribution
            }
        }
        
        result.set(categoryIndex, total);
    }
    
    this.userVector = result;
}

