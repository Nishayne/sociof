#!/bin/bash

# Ensure MySQL is running for local tests...
echo "Ensure MySQL is running for local tests..."
sudo docker ps -a

# Replace with your actual API URL
API_URL="http://localhost:8080/api"

# Login and get the token
echo "--- Executing Signup Test ---"
signup_response=$(curl -k "$API_URL/auth/signup" -X POST -H "Content-Type: application/json" -d '{"email": "test2@test.com", "password": "pass1", "dateOfBirth": "2023-04-22T15:00:00Z"}')

login_response=$(curl -k "$API_URL/auth/login" -X POST -H "Content-Type: application/json" -d '{"email": "test2@test.com", "password": "pass1"}')

# Extract token
TOKEN=$(echo "$login_response" | sed 's/{.*\"accessToken\":\"\([^\"]*\).*}/\1/g')

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

    echo "--- $description ---"
    response=$(eval "$curl_command")
    status_code=$(echo "$response" | grep -oP 'HTTP/\d\.\d \K\d+')

    echo "Response Status: $status_code"
    echo "Response Body: $(echo "$response" | sed 's/HTTP\/.*//')"
    echo ""
}

# Group Controller
GROUP_CREATE_JSON='{"name": "Test Group", "isPrivate": false}'
create_group_response=$(curl -X POST "$API_URL/groups" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "GROUP_CREATE_JSON")
GROUP_ID=$(echo "$create_group_response" | sed 's/{.*\"id\":\"\([^\"]*\).*}/\1/g')

if [ -z "$GROUP_ID" ]; then
    echo "Error: Failed to retrieve GROUP_ID."
    echo "Create Group Response: $create_group_response"
    exit 1
fi
echo "Group ID: $GROUP_ID"

execute_curl "Create Group" "curl -X POST '$API_URL/groups' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$GROUP_CREATE_JSON'"
execute_curl "Get Group By ID" "curl -X GET '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN'"

# Create Post
CREATE_POST_JSON='{"content": "Test Post Content", "fileUrl": "http://example.com/file.jpg", "fileType": "image/jpeg", "groupId": 1}'
create_post_response=$(curl -X POST "$API_URL/posts" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$CREATE_POST_JSON")

# Extract Post ID
POST_ID=$(echo "$create_post_response" | sed 's/{.*\"id\":\"\([^\"]*\).*}/\1/g') # Extract post ID from previous response

if [ -z "$POST_ID" ]; then
    echo "Error: Failed to retrieve POST_ID."
    echo "Create Post Response: $create_post_response"
    exit 1
fi
echo "Post ID: $POST_ID"

#Create Report
CREATE_REPORT_JSON='{"justification": "Test Report Justification", "postId": 1}'
create_report_response=$(curl -X POST "$API_URL/reports" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$CREATE_REPORT_JSON")

# Extract Report ID
REPORT_ID=$(echo "$create_report_response" | sed 's/{.*\"id\":\"\([^\"]*\).*}/\1/g') # Extract report ID from previous response

if [ -z "$REPORT_ID" ]; then
    echo "Error: Failed to retrieve REPORT_ID."
    echo "Create Post Response: $create_report_response"
    exit 1
fi
echo "Report ID: $REPORT_ID"

# Share Post
execute_curl "Share Post" "curl -X POST '$API_URL/posts/$POST_ID/share' -H 'Authorization: Bearer $TOKEN'"

# Share Post Advanced
SHARE_ADVANCED_JSON='{"someOption": "value"}' # Replace with your share request
execute_curl "Share Post Advanced" "curl -X POST '$API_URL/posts/$POST_ID/share/advanced' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$SHARE_ADVANCED_JSON'"

# Update Post
UPDATE_POST_JSON='{"content": "Updated Post Content", "fileUrl": "http://example.com/updated.jpg", "fileType": "image/png", "groupId": 2}'
execute_curl "Update Post" "curl -X PUT '$API_URL/posts/$POST_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$UPDATE_POST_JSON'"

# Follow User
USER_ID_TO_FOLLOW=2 # Replace with the user ID to follow
execute_curl "Follow User" "curl -X POST '$API_URL/follows/$USER_ID_TO_FOLLOW' -H 'Authorization: Bearer $TOKEN'"

# Unfollow User
execute_curl "Unfollow User" "curl -X DELETE '$API_URL/follows/$USER_ID_TO_FOLLOW' -H 'Authorization: Bearer $TOKEN'"

# Get Follower Count
execute_curl "Get Follower Count" "curl -X GET '$API_URL/follows/$USER_ID_TO_FOLLOW/followers/count' -H 'Authorization: Bearer $TOKEN'"

# Get Following Count
execute_curl "Get Following Count" "curl -X GET '$API_URL/follows/$USER_ID_TO_FOLLOW/following/count' -H 'Authorization: Bearer $TOKEN'"

# Like Post
execute_curl "Like Post" "curl -X POST '$API_URL/interactions/like/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# Unlike Post
execute_curl "Unlike Post" "curl -X DELETE '$API_URL/interactions/like/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# Add Comment
ADD_COMMENT_JSON='"Test Comment Content"'
execute_curl "Add Comment" "curl -X POST '$API_URL/interactions/comment/$POST_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d $ADD_COMMENT_JSON"

# Get Comments
execute_curl "Get Comments" "curl -X GET '$API_URL/interactions/comments/$POST_ID' -H 'Authorization: Bearer $TOKEN'"

# API Info Controller
execute_curl "Health Check" "curl -X GET '$API_URL/health'"
execute_curl "API Info" "curl -X GET 'API\_URL/info'"

GROUP_UPDATE_JSON='{"name": "Updated Group", "description": "Updated group description"}'
execute_curl "Update Group" "curl -X PUT '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '$GROUP_UPDATE_JSON'"
execute_curl "Delete Group" "curl -X DELETE '$API_URL/groups/$GROUP_ID' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Add User To Group" "curl -X POST '$API_URL/groups/$GROUP_ID/members/2' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Remove User From Group" "curl -X DELETE '$API_URL/groups/$GROUP_ID/members/2' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get All Groups" "curl -X GET '$API_URL/groups' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get Visible Groups" "curl -X GET '$API_URL/groups/visible' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get My Groups" "curl -X GET '$API_URL/groups/my-groups' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get Created Groups" "curl -X GET '$API_URL/groups/created' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get Groups Ordered By Member Count" "curl -X GET '$API_URL/groups/stats/members' -H 'Authorization: Bearer $TOKEN'"
execute_curl "Get Groups Ordered By Post Count" "curl -X GET '$API_URL/groups/stats/posts' -H 'Authorization: Bearer $TOKEN'"

