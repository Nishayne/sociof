Here’s a variety of SQL queries (from basic to advanced) based on the social media app. Each query reflects real-world scenarios, analytics, or application needs.

---

### 📌 Basic Queries

#### 1. Get all public posts from non-private users
```sql
SELECT p.id, p.content, u.email
FROM POSTS p
JOIN USERS u ON p.user_id = u.id
WHERE u.is_profile_private = 0;
```

#### 2. List comments on a specific post
```sql
SELECT c.content, u.email, c.created_at
FROM COMMENTS c
JOIN USERS u ON c.user_id = u.id
WHERE c.post_id = 101
ORDER BY c.created_at DESC;
```

#### 3. Get users who liked a specific post
```sql
SELECT u.email, l.created_at
FROM LIKES l
JOIN USERS u ON l.user_id = u.id
WHERE l.post_id = 202;
```

---

### 🧩 Intermediate Queries

#### 4. Count number of posts each user made
```sql
SELECT u.email, COUNT(p.id) AS post_count
FROM USERS u
LEFT JOIN POSTS p ON u.id = p.user_id
GROUP BY u.email
ORDER BY post_count DESC;
```

#### 5. Get users with more than 100 followers
```sql
SELECT u.email, COUNT(f.follower_id) AS total_followers
FROM USERS u
JOIN FOLLOWS f ON u.id = f.following_id
GROUP BY u.email
HAVING COUNT(f.follower_id) > 100;
```

#### 6. Find most liked posts
```sql
SELECT p.id, p.content, COUNT(l.id) AS like_count
FROM POSTS p
LEFT JOIN LIKES l ON p.id = l.post_id
GROUP BY p.id, p.content
ORDER BY like_count DESC
LIMIT 10;
```

---

### 🧠 Analytical / Window Queries

#### 7. Rank users by number of posts using `ROW_NUMBER()`
```sql
SELECT email, post_count, 
       ROW_NUMBER() OVER (ORDER BY post_count DESC) AS rank
FROM (
  SELECT u.email, COUNT(p.id) AS post_count
  FROM USERS u
  LEFT JOIN POSTS p ON u.id = p.user_id
  GROUP BY u.email
) AS user_posts;
```

#### 8. Daily post count trend for the last 7 days
```sql
SELECT DATE(created_at) AS post_date, COUNT(*) AS posts
FROM POSTS
WHERE created_at >= CURDATE() - INTERVAL 7 DAY
GROUP BY post_date
ORDER BY post_date;
```

---

### 🔍 Correlated Subqueries

#### 9. Get users whose posts have the highest average likes
```sql
SELECT u.email
FROM USERS u
WHERE (
  SELECT AVG(p.likes)
  FROM POSTS p
  WHERE p.user_id = u.id
) > 100;
```

#### 10. Users who reported more than 5 posts
```sql
SELECT u.email
FROM USERS u
WHERE (
  SELECT COUNT(*)
  FROM REPORTS r
  WHERE r.reporter_id = u.id
) > 5;
```

---

### 🧮 Aggregation & Summation

#### 11. Group post statistics: number of posts per group
```sql
SELECT g.name, COUNT(p.id) AS post_count
FROM GROUPS_ g
LEFT JOIN POSTS p ON g.id = p.group_id
GROUP BY g.name;
```

#### 12. Total likes on posts per user
```sql
SELECT u.email, SUM(like_counts.total_likes) AS total_likes
FROM USERS u
JOIN (
  SELECT p.user_id, COUNT(l.id) AS total_likes
  FROM POSTS p
  JOIN LIKES l ON p.id = l.post_id
  GROUP BY p.user_id
) AS like_counts ON u.id = like_counts.user_id
GROUP BY u.email
ORDER BY total_likes DESC;
```

---

### 🛠️ Moderation & Reporting Insights

#### 13. Posts with highest number of reports
```sql
SELECT p.id, COUNT(r.id) AS report_count
FROM POSTS p
JOIN REPORTS r ON p.id = r.post_id
GROUP BY p.id
ORDER BY report_count DESC
LIMIT 5;
```

#### 14. Average moderation time (in hours)
```sql
SELECT 
  AVG(TIMESTAMPDIFF(HOUR, created_at, moderated_at)) AS avg_moderation_time
FROM REPORTS
WHERE moderated_at IS NOT NULL;
```

---

