# YouTube Interest Analyzer based on Linear Space and Cosine Similarity with Exponential Temporal Decay

- [Introduction](#introduction)
- [Mathematical Model](#mathematical-model)
    - [What is Linear Space?](#about-linear-space)
    - [Category Space](#category-space)
    - [Exponential Temporal Decay](#exponential-temporal-decay)
- [Project Structure](#project-structure)
    - [DataFlow](#data-flow)
    - [YouTubeAuth](#youtubeauth)
    - [YouTubeDataLoader](#youtubedataloader)
    - [JSONReader](#jsonreader)



# Introduction

YouTube Interest Analyzer represents users and content as vectors in a 17-dimensional category space.
The system uses **Exponential Temporal Decay** to account for temporal dynamics — older views lose relevance over time.
## How does it work? (Short description)

## 1. Data Collection (Java)

When a user logs into their YouTube account, the program:
- Loads their liked videos
- Extracts for each video:
  - Category
  - Title
  - Video duration
- Stores these actions

## 2. User Vector Calculation (Java)

The program iterates through a Map sorted by ascending category numbers, calculating using the formula:

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

(More about it in the Mathematical model)
  
## 3. Video Title Embedding and Clustering (Python)

The saved videos are passed to a Python script that:

**Step 1: Generate Embeddings**
- Uses the **LaBSE model** (Language-agnostic BERT Sentence Embedding)
- Converts each video title into an embedding (a set of numbers representing the text's semantic content)
- This allows the AI to understand the content of titles

**Step 2: Cluster Embeddings**
- Applies **DBSCAN algorithm** with:
  - `eps = 0.51`
  - `min_samples = 2`
  - `metric = 'cosine'`

**Step 3: Data Structures**
```python
titles = ["Title 1", "Title 2", "Title 3", ...]     # List of video titles
embeddings = model.encode(titles)                    # NumPy array (n_samples, embedding_dim)
labels = clustering.fit_predict(embeddings)          # NumPy array (n_samples,)
# Example: labels = [0, 0, 1, -1, 0] where -1 = noise
```

**Step 4: Map Titles to Clusters**

```python
clusters = defaultdict(list)
for title, label in zip(titles, labels):  # Order preserved via same indices!
    clusters[label].append(title)
```
Why this works:

- embeddings[0] corresponds to titles[0]
- labels[0] corresponds to embeddings[0]
- Therefore, the same index connects titles, embeddings, and labels

**Step 5: Save to JSON**

Preserves the cluster structure with grouped video titles

## 4. Final Recommendation Generation (Java)

Java opens the JSON file and:

Calculates cosine similarity between:

- User vector from Step 2
- Category vectors
- Queries videos from the clustered titles
- Outputs the required number of recommendations

Count of recommendations
```java
int totalToShow = Math.max(3, (int) (cosine * 20));                 // Total for the category
    Map<String, List<String>> catClusters = clusters.get(category);

    if (catClusters == null || catClusters.isEmpty()) continue;

    for (var cluster : catClusters.entrySet()) {
        String query = String.join(" ", cluster.getValue());
        int toShow = totalToShow / catClusters.size();               // For the cluster is Total / number of clusters
        if (toShow < 1) toShow = 3;
        searchOnYouTube(query, toShow, category);
    }
```



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

## Category Space & Vectors

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

---

### 2.  Category as Basis Vector

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

---

### 3. Linear Independence

The set of all category basis vectors $\{e_1, e_2, \dots, e_{17}\}$ is **linearly independent**:

$$
\alpha_1 \mathbf{e}_1 + \alpha_2 \mathbf{e}_2 + \dots + \alpha_{17} \mathbf{e}_{17} = \mathbf{0} \\Rightarrow\ \alpha_1 = \alpha_2 = \dots = \alpha_{17} = 0
$$

---

## Exponential Temporal Decay

When I created it, I was thinking about the Riemann integral sum. But my representation is discrete (and the Riemann sum uses continuous functions).

Each day, every category weight is multiplied by a decay factor λ:

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

A new day has begun.
We're reducing yesterday's Dynamics by 5%.
We're reducing yesterday's total by 5% (Why? For the case if user will never watch this category again and we need to 'forget' this category from the user's vector)

Is there a view today?

1) No -> We do nothing (the total has already decreased by 5% today), meaning the area under the graph for this day is zero.
2) Yes -> We add time to yesterday's Dynamics and multiply by lambda (how old the trend is).

This formula enables sliding decay:
- The more videos a user watches from a category, the higher the recommendation priority
- If they stop watching, interest doesn't immediately reset (in case it's temporary)
- Interest gradually decreases (5% each day)
- Recently liked videos have greater influence than older ones

Consider day 8 as today. Here're graphics for previous 7 days:

**How many minutes did the user watch per day:**

Data: minutes per day for category

<div align="center">
  <img width="600" alt="Daily views" src="https://github.com/user-attachments/assets/6db1efb5-4880-404f-a88a-6c4da30aa3b4" />
  <br/>
  <em>Figure 1: Daily watch time (minutes)</em>
  <br/>
</div>

**Contribution (area under the step function, Δx = 1 day):**

Data: V(t) for each day with the decay for 8th day

<div align="center">
  <img width="1250" height="788" alt="тотал_page-0001" src="https://github.com/user-attachments/assets/60bf9203-a089-4e45-b5f4-f208f1cfd79e" />
  <br/>
    <em>Figure 2: Category weight accumulation with exponential decay</em>
</div>

#### Viewing Dynamics

The dynamics $D(t)$ represent the **session effect** — consecutive views amplify each other:

$$
D(t) = D(t-1) \cdot \lambda + W(t)
$$

where $W(t)$ is the total watch time (in minutes) for the category on day $t$.

**Viewing dynamics D(t) — session effect:**

<div align="center">
  <img width="600" alt="Dynamics" src="https://github.com/user-attachments/assets/eabe4162-0f4b-4e9b-9433-6da42875b15d" />
  <br/>
  <em>Figure 3: Dynamics D(t) — accumulates during viewing sessions, decays otherwise</em>
</div>

---

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
```

---

# Project Structure

### Data Flow

| Step | Component |
|------|-----------|
| 1 | YouTube API |
| 2 | PlaylistItem | 
| 3 | Video Details |
| 4 | Event |
| 5 | Recommendation Engine |
---

## YoutubeAuth

### Overview

The `YouTubeAuth` class handles OAuth 2.0 authorization for YouTube API access. It implements the **Desktop Application flow** (Installed App), which opens a browser window for the user to log in and grant permissions.

---

### Class Structure

#### Constants

| Constant | Purpose |
|----------|---------|
| `CLIENT_SECRETS_PATH` | Path to the OAuth client secrets JSON file. Stores application credentials from Google Cloud Console |
| `JSON_FACTORY` | Factory for JSON parsing (Jackson) |
| `SCOPES` | List of permissions required from the user during authorization |

### Scopes Used

```java
private static final List<String> SCOPES = List.of(
    "https://www.googleapis.com/auth/youtube.readonly",
    "https://www.googleapis.com/auth/youtube.force-ssl"
);
```

---

### Authentication Flow

The `authenticate()` method executes the following steps:

1. **HTTP Transport Setup** - Creates a trusted `NetHttpTransport` instance for API communication
2. **Credentials Loading** - Reads OAuth client secrets from `client-secret.json` in classpath resources
3. **Authorization Flow Creation** - Builds a `GoogleAuthorizationCodeFlow` with required permissions
4. **Local Server Setup** - Spins up a `LocalServerReceiver` on port 8888 to catch the OAuth callback
5. **User Authorization** - Launches browser, waits for user login/permission, exchanges code for tokens
6. **YouTube Service** - Constructs and returns an authenticated `YouTube` API client

---

### Dependencies

- Google OAuth Client Libraries
- Jetty HTTP Server (for local callback receiver)
- Jackson JSON Processor

---

## YouTubeDataLoader

### Overview

The YouTubeDataLoader class is responsible for fetching user interaction data from the YouTube API and converting it into standardized Event objects for the recommendation system. It serves as the bridge between YouTube's raw API responses and the system's internal data model.

| Field | Type                                                            | Purpose                                                  |
|----------|-----------------------------------------------------------------|----------------------------------------------------------|
| `youtube` | YouTube                                                         | Authenticated YouTube API client instance |
| `categoryRegistry` |         CategoryRegistry                 | Mapping utility for YouTube category IDs to human-readable names

---

### Fetch Liked Videos

```java
public List<Event> fetchLikedVideos(int maxEvents) throws IOException { }
```

1. **Playlist access** - Accessible special playlist "LL" (liked videos)
2. **MaxResult** - Iterates through API response pages (50 items per page)
3. **Video Metadata Extraction** - For each liked video: Video ID, title, Like timestamp (when the user liked the video)
4. **Detailed Video Info** - Calls getVideoDetails() for additional metadata: Category ID, Category name, Video duration (converted from ISO format to minutes)
5. **Event Creation** - Creates an Event object with: Like date (as LocalDate), Category ID, Watch time (video duration in minutes)

--- 

### Duration Parsing

Converts YouTube's ISO 8601 duration format to minutes.

#### Format Examples:

1. "PT5M30S" → 5.5 minutes
2. "PT1H2M10S" → 62.167 minutes
3. "PT45S" → 0.75 minutes
Algorithm:

Strips the "PT" prefix
Parses hours (H), minutes (M), and seconds (S) components
Converts to decimal minutes

---

### Recommend Video

Purpose: Searches and displays recommended videos based on top user interests.

#### Process:

Iterates through top category recommendations (sorted by weight)
For each category, calculates the number of videos to fetch:
- count = weight × 10
- Performs a YouTube search for videos in that category
- Prints video titles and URLs to console

## JSONReader

The `JSON_Reader` class handles the export of video data from Java to Python via JSON format.

#### Class Structure

| Field | Type | Purpose |
|-------|------|---------|
| `data` | `List<Map<String, String>>` | Stores video titles and categories |
| `mapper` | `ObjectMapper` | Jackson JSON processor |

#### Methods

| Method | Description |
|--------|-------------|
| `addVideo(title, category)` | Adds a video entry (title + category) to the export list |
| `saveToJson(filePath)` | Writes all collected data to a JSON file |

#### Data Flow (Java → Python)
Java (YouTubeDataLoader) → JSON_Reader → user_videos.json → Python (cluster.py)


#### JSON Output Example

```json
[
  {
    "title": "Rammstein - Deutschland",
    "category": "Music"
  },
  {
    "title": "Как испечь хлеб дома",
    "category": "Howto & Style"
  }
]
```

## Python Clustering Script (cluster.py)

### Python Clustering Script (`cluster.py`)

The Python script performs semantic clustering of video titles using embeddings (by model LaBSE) and DBSCAN.


#### Key Components

**1. Text Cleaning (`clean_text`)**

Removes noise from video titles:
- Special characters → spaces
- Hashtags (`#`) → removed
- URLs and mentions → removed
- Numbers → removed
- Extra whitespace → normalized

**2. Stop Words Filtering**

A comprehensive set of stop words in both Russian and English:
- Common prepositions: `и`, `в`, `на`, `of`, `the`, `and`
- Temporal words: months, days of week, `today`, `yesterday`
- Numerals: `one`, `two`, `three`, `один`, `два`
- Gaming jargon: `walkthrough`, `playthrough`, `episode`
- Emojis and garbage tokens

**3. DBSCAN Parameters**

| Parameter | Value | Explanation |
|-----------|-------|-------------|
| `eps` | 0.51 | Maximum distance between samples in a cluster |
| `min_samples` | 2 | Minimum videos to form a cluster |
| `metric` | `'cosine'` | Cosine distance for semantic similarity |

**4. Cluster Processing**

For each cluster (label ≠ -1):
- Count word frequencies across all titles in the cluster
- For `Gaming` and `News & Politics`: limit to top 2 words (more precise)
- Skip empty clusters (no valid keywords)

#### Data Flow (Python → Java)

```python
clusters_result.json = {
    "Music": {
        "0": ["rammstein", "deutschland", "sonne"],
        "1": ["король", "шут", "лесник"]
    },
    "Gaming": {
        "0": ["minecraft", "horror"]
    }
}
```
