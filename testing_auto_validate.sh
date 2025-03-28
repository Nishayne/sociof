#!/bin/bash

# Ensure MySQL is running for local tests...
echo "Ensure MySQL is running for local tests..."
sudo docker ps -a

# Replace with your actual API URL
API_URL="http://localhost:8080/api"

# Login and get the token
echo "--- Executing Signup Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
signup_response=$(curl -k "$API_URL/auth/signup" -X POST -H "Content-Type: application/json" -d '{"email": "test1@test.com", "password": "pass1", "dateOfBirth": "2023-04-22T15:00:00Z"}')

signup_response=$(curl -k "$API_URL/auth/signup" -X POST -H "Content-Type: application/json" -d '{"email": "test2@test.com", "password": "pass1", "dateOfBirth": "2023-04-22T15:00:00Z"}')

login_response=$(curl -k "$API_URL/auth/login" -X POST -H "Content-Type: application/json" -d '{"email": "test1@test.com", "password": "pass1"}')

# Extract token
TOKEN=$(echo "$login_response" | sed 's/{.*\"accessToken\":\"\([^\"]*\).*}/\1/g') # Extract token from previous response

# Check if token retrieval was successful
if [ -z "$TOKEN" ]; then
  echo "Error: Failed to retrieve token."
  echo "Login Response: $login_response"
  exit 1
fi

echo "token = $TOKEN"

# Function to execute curl and print response
execute_curl() {
    local description="$1"
    local curl_command="$2"
    local response
    local status_code

    echo "--- $description ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
    response=$(eval "$curl_command")
    status_code=$(echo "$response" | grep -oP 'HTTP/\d\.\d \K\d+')

    echo "Response Status: $status_code"
    echo "Response Body: $(echo "$response" | sed 's/HTTP\/.*//')"
    echo ""
}

# Group Controller
echo "--- Executing Create Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
GROUP_CREATE_JSON='{"name": "Test Group", "isPrivate": false}'
create_group_response=$(curl -X POST "$API_URL/groups" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$GROUP_CREATE_JSON")
echo "create_group_response=$create_group_response"
GROUP_ID=$(echo "$create_group_response" | sed 's/{.*"id":\([^,]*\).*}/\1/g') # Extract group ID from previous response

if [ -z "$GROUP_ID" ]; then
    echo "Error: Failed to retrieve GROUP_ID."
    exit 1
fi
echo "Group ID: $GROUP_ID"

execute_curl "Get Group By ID" "curl -X GET '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN'"

# Create Post
echo "--- Executing Create Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
CREATE_POST_JSON='{"content": "Test Post Content", "fileUrl": "http://example.com/file.jpg", "fileType": "image/jpeg", "groupId": '"$GROUP_ID"' }'
echo "CREATE_POST_JSON=$CREATE_POST_JSON"
create_post_response=$(curl -X POST "$API_URL/posts" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$CREATE_POST_JSON")

# Extract Post ID
POST_ID=$(echo "$create_post_response" | sed 's/{.*"id":\([^,]*\).*}/\1/g') # Extract post ID from previous response

if [ -z "$POST_ID" ]; then
    echo "Error: Failed to retrieve POST_ID."
    echo "Create Post Response: $create_post_response"
    exit 1
fi
echo "Post ID: $POST_ID"

#Create Report
echo "--- Executing Create Report Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
CREATE_REPORT_JSON='{"justification": "Test Report Justification", "postId": '"$POST_ID"' }'
create_report_response=$(curl -X POST "$API_URL/reports" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$CREATE_REPORT_JSON")

# Extract Report ID
REPORT_ID=$(echo "$create_report_response" | sed 's/{.*"id":\([^,]*\).*}/\1/g') # Extract report ID from previous response

if [ -z "$REPORT_ID" ]; then
    echo "Error: Failed to retrieve REPORT_ID."
    echo "Create Post Response: $create_report_response"
    exit 1
fi
echo "Report ID: $REPORT_ID"

# Share Post
echo "--- Executing Share Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Share Post" "curl -X POST '$API_URL/posts/$POST_ID/share' -H 'Authorization: Bearer $TOKEN'"

# Share Post Advanced
echo "--- Executing Share Post Advanced Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
SHARE_ADVANCED_JSON='{"someOption": "value"}' # Replace with your share request
execute_curl "Share Post Advanced" "curl -X POST '$API_URL/posts/$POST_ID/share/advanced' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$SHARE_ADVANCED_JSON'"

# Update Post
echo "--- Executing Update Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
UPDATE_POST_JSON='{"content": "Updated Post Content", "fileUrl": "http://example.com/updated.jpg", "fileType": "image/png", "groupId":'"$GROUP_ID"' }'
execute_curl "Update Post" "curl -X PUT '$API_URL/posts/$POST_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$UPDATE_POST_JSON'"

# Follow User
echo "--- Executing Follow User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
USER_ID_TO_FOLLOW=2 # Replace with the user ID to follow
execute_curl "Follow User" "curl -X POST '$API_URL/follows/1' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Follow User" "curl -X POST '$API_URL/follows/$USER_ID_TO_FOLLOW' -H 'Authorization: Bearer $TOKEN'"

# Unfollow User
echo "--- Executing Unfollow User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Unfollow User" "curl -X DELETE '$API_URL/follows/$USER_ID_TO_FOLLOW' -H 'Authorization: Bearer $TOKEN'"

# Get Follower Count
echo "--- Executing Get Follower count Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Follower Count" "curl -X GET '$API_URL/follows/$USER_ID_TO_FOLLOW/followers/count' -H 'Authorization: Bearer $TOKEN'"

# Get Following Count
echo "--- Executing Get Following count Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Following Count" "curl -X GET '$API_URL/follows/$USER_ID_TO_FOLLOW/following/count' -H 'Authorization: Bearer $TOKEN'"

# Like Post
echo "--- Executing Like Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Like Post" "curl -X POST '$API_URL/interactions/like/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# Unlike Post
echo "--- Executing Unlike Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Unlike Post" "curl -X DELETE '$API_URL/interactions/like/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# Add Comment
echo "--- Executing Add comment Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
ADD_COMMENT_JSON='"Test Comment Content"'
execute_curl "Add Comment" "curl -X POST '$API_URL/interactions/comment/$POST_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d $ADD_COMMENT_JSON"

# Get Comments
echo "--- Executing Get comment Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Comments" "curl -X GET '$API_URL/interactions/comments/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# API Info Controller
echo "--- Executing Get Api Info Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Health Check" "curl -X GET '$API_URL/health'"
execute_curl "API Info" "curl -X GET '$API_URL/info'"

echo "--- Executing Update Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
GROUP_UPDATE_JSON='{"name": "Updated Group", "description": "Updated group description"}'
execute_curl "Update Group" "curl -X PUT '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$GROUP_UPDATE_JSON'"
echo "--- Executing Delete Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Delete Group" "curl -X DELETE '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Add User to Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Add User To Group" "curl -X POST '$API_URL/groups/$GROUP_ID/members/2' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Remove User to Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Remove User From Group" "curl -X DELETE '$API_URL/groups/$GROUP_ID/members/2' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Get All Groups Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get All Groups" "curl -X GET '$API_URL/groups' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Get Visible Groups Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Visible Groups" "curl -X GET '$API_URL/groups/visible' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Get My Groups Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get My Groups" "curl -X GET '$API_URL/groups/my-groups' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Created Groups Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Created Groups" "curl -X GET '$API_URL/groups/created' -H 'Authorization: Bearer $TOKEN'"

echo "--- Admin functionality tests below ---"
echo "--- Executing Signup Admin Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
signup_response=$(curl -k "$API_URL/auth/signup" -X POST -H "Content-Type: application/json" -d '{"email": "admin1@socio.com", "password": "A@pass1", "dateOfBirth": "1872-04-22T15:00:00Z"}')

login_response=$(curl -k "$API_URL/auth/login" -X POST -H "Content-Type: application/json" -d '{"email": "admin1@socio.com", "password": "A@pass1"}')

# Extract token
TOKEN=$(echo "$login_response" | sed 's/{.*\"accessToken\":\"\([^\"]*\).*}/\1/g') # Extract token from previous response

# Check if token retrieval was successful
if [ -z "$TOKEN" ]; then
  echo "Error: Failed to retrieve token."
  echo "Login Response: $login_response"
  exit 1
fi

echo "token = $TOKEN"

echo "--- Executing Groups Ordered by Member Count Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Groups Ordered By Member Count" "curl -X GET '$API_URL/groups/stats/members' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Groups Ordered by Post Count Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Groups Ordered By Post Count" "curl -X GET '$API_URL/groups/stats/posts' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Posts By User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Posts By User" "curl -X GET '$API_URL/posts/user/2' -H 'Authorization: Bearer $TOKEN'"
echo "--- Executing Get Posts By Group Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Posts By Group" "curl -X GET '$API_URL/posts/group/$GROUP_ID' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Feed Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Feed" "curl -X GET '$API_URL/posts/feed' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get All Posts Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get All Posts (Admin)" "curl -X GET '$API_URL/posts' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Posts Ordered By Engagement Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Posts Ordered By Engagement (Admin)" "curl -X GET '$API_URL/posts/stats/engagement' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Post Stats By Date Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Post Stats By Date (Admin)" "curl -X GET '$API_URL/posts/stats/by-date' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Post Stats By User Tes (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Post Stats By User (Admin)" "curl -X GET '$API_URL/posts/stats/by-user' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Post Stats By File Type Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Post Stats By File Type (Admin)" "curl -X GET '$API_URL/posts/stats/by-file-type' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Delete Post Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Delete Post" "curl -X DELETE '$API_URL/posts/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Report By ID Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Report By ID (Admin)" "curl -X GET '$API_URL/reports/$REPORT_ID' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Moderate Report Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Moderate Report (Admin)" "curl -X PUT '$API_URL/reports/$REPORT_ID?approved=true' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get All Reports Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get All Reports (Admin)" "curl -X GET '$API_URL/reports' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Reports By Post Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Reports By Post (Admin)" "curl -X GET '$API_URL/reports/post/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Report Stats By Date Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Report Stats By Date (Admin)" "curl -X GET '$API_URL/reports/stats/by-date' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Report Stats By User Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Report Stats By User (Admin)" "curl -X GET '$API_URL/reports/stats/by-user' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Report Stats By File Type Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Report Stats By File Type (Admin)" "curl -X GET '$API_URL/reports/stats/by-file-type' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Current User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Current User" "curl -X GET '$API_URL/users/me' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get User By ID Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get User By ID" "curl -X GET '$API_URL/users/2' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Update User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
UPDATE_USER_JSON='{"firstName": "UpdatedFirstName", "lastName": "UpdatedLastName"}'
execute_curl "Update User" "curl -X PUT '$API_URL/users/2' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$UPDATE_USER_JSON'"

echo "--- Executing Delete User Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Delete User" "curl -X DELETE '$API_URL/users/2' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get All Users Test ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get All Users" "curl -X GET '$API_URL/users' -H 'Authorization: Bearer $TOKEN'"

echo "--- Executing Get Users Ordered By Follower Count Test (Admin) ---" && echo "Wait for sleep 3 seconds to continue ..." && sleep 3
execute_curl "Get Users Ordered By Follower Count (Admin)" "curl -X GET '$API_URL/users/stats/followers' -H 'Authorization: Bearer $TOKEN'"
