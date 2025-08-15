# Debug Guide for "UserId not found in profile" Error

## Issue Analysis

The error "UserId not found in profile" occurs during the user lookup process. The enhanced logging will help identify the exact problem.

## What to Check

### 1. **User Document Structure in Firestore**

Make sure your user document in Firestore looks like this:

**Collection**: `users`  
**Document ID**: Could be either:
- `user01` (the custom ID itself)
- Or some auto-generated ID containing the data

**Document Content**:
```json
{
  "uid": "user01",
  "name": "John Doe", 
  "email": "user01@example.com",
  "password": "User01Pass!",
  "phone": "+94123456789",
  "preferences": {"roomType":"room001","noSmoking":true},
  "isAdmin": false,
  "createdAt": "2025-08-01T10:00:00Z"
}
```

### 2. **Debug Logs to Monitor**

Run the app and check Android Studio Logcat for these messages:

```
BookingsFragment: Looking up user with email: user01@example.com
BookingsFragment: User lookup query returned X documents
BookingsFragment: User document ID: [document_id]
BookingsFragment: User document data: {uid=user01, name=John Doe, email=user01@example.com, ...}
```

## Two Lookup Strategies Implemented

### Strategy 1: Email-Based Query
```java
db.collection("users").whereEqualTo("email", "user01@example.com")
```

### Strategy 2: Direct Document ID Lookup (Fallback)
```java
db.collection("users").document("user01")
```

## Common Issues & Solutions

### Issue 1: No documents found with email query
**Symptoms**: Log shows "User lookup query returned 0 documents"
**Solutions**: 
- Check if email in Firestore exactly matches login email
- Verify case sensitivity
- Check for extra spaces

### Issue 2: Document found but no `uid` field
**Symptoms**: Log shows document data but "No uid, userId field found"
**Solutions**:
- Check if field is named `uid` or `userId`
- App will try both field names
- App will fallback to document ID

### Issue 3: Email mismatch in direct lookup
**Symptoms**: "Email mismatch" error
**Solutions**:
- Ensure document ID matches email prefix
- Check email consistency

## Testing Steps

### 1. **Login with user01@example.com**

### 2. **Check Logcat Messages**
Look for the sequence:
```
BookingsFragment: Looking up user with email: user01@example.com
BookingsFragment: User lookup query returned X documents
```

### 3. **Verify Firestore Data**
In Firebase Console:
- Go to Firestore Database
- Check `users` collection
- Find document with email `user01@example.com`
- Verify it has `uid: "user01"` field

## Quick Firestore Verification

### Option A: Document stored as auto-ID with uid field
```
Collection: users
├── [auto-generated-id-123]
    ├── uid: "user01"
    ├── email: "user01@example.com"
    └── name: "John Doe"
```

### Option B: Document stored with custom ID
```
Collection: users  
├── user01
    ├── uid: "user01"
    ├── email: "user01@example.com"
    └── name: "John Doe"
```

Both structures will work with the updated code.

## Expected Flow

✅ **Success Path**:
1. Login with `user01@example.com`
2. Email lookup finds user document
3. Extract `uid: "user01"` from document
4. Query bookings with `userId: "user01"`
5. Display bookings

❌ **Current Issue Path**:
1. Login with `user01@example.com`
2. Email lookup fails OR finds document without `uid` field
3. Shows "UserId not found in profile"

## Next Steps

1. **Run the app and check the debug logs**
2. **Verify your Firestore user document structure**
3. **Share the log output** to pinpoint the exact issue

The enhanced logging will show exactly what's happening during the lookup process!
