erDiagram
    USERS {
        bigint id PK
        bit is_admin
        bit is_profile_private
        datetime created_at
        datetime date_of_birth
        datetime password_updated_at
        datetime updated_at
        varchar(255) email 
        varchar(255) password
        enum role
    }

    POSTS {
        bigint id PK
        bit is_shared
        integer likes
        datetime created_at
        datetime updated_at
        varchar(255) content
        varchar(255) file_type
        varchar(255) file_url
        bigint user_id FK
        bigint group_id FK
        bigint original_post_id
        bigint original_user_id
    }

    COMMENTS {
        bigint id PK
        datetime created_at
        varchar(255) content
        bigint post_id FK
        bigint user_id FK
    }

    LIKES {
        bigint id PK
        datetime created_at
        bigint post_id FK
        bigint user_id FK
    }

    REPORTS {
        bigint id PK
        datetime created_at
        datetime moderated_at
        varchar(255) justification
        enum status
        bigint moderator_id FK
        bigint post_id FK
        bigint reporter_id FK
    }

    FOLLOWS {
        bigint id PK
        datetime created_at
        bigint follower_id FK
        bigint following_id FK
    }

    GROUPS_ {
        bigint id PK
        bit is_private
        datetime created_at
        datetime updated_at
        varchar(255) name
        bigint creator_id FK
    }

    GROUP_MEMBERS {
        bigint group_id PK 
        bigint user_id PK 
    }

    USERS ||--o{ POSTS : creates
    USERS ||--o{ COMMENTS : writes
    USERS ||--o{ LIKES : likes
    USERS ||--o{ REPORTS : reports
    USERS ||--o{ FOLLOWS : follows
    USERS ||--o{ GROUPS_ : creates
    USERS ||--o{ GROUP_MEMBERS : joins

    POSTS ||--o{ COMMENTS : has
    POSTS ||--o{ LIKES : receives
    POSTS ||--o{ REPORTS : flagged
    POSTS ||--|{ GROUPS_ : belongs_to
    GROUPS_ ||--o{ GROUP_MEMBERS : "has members"
