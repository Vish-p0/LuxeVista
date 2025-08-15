# User ID Mapping Fix for BookingsFragment

## Problem Identified

The BookingsFragment was not loading bookings because of a **user ID mismatch** between:

1. **Firebase Auth UID**: Auto-generated unique ID when user authenticates (e.g., `kGxR8F2mN3PQs7vW9zA2bCdE1fH2`)
2. **Custom User ID**: Your custom ID stored in Firestore documents (e.g., `"user01"`)

## Data Structure Analysis

### Your Firestore Structure:
```javascript
// users collection
{
  "uid": "user01",                    // ← Custom ID
  "email": "user01@example.com",
  "name": "John Doe",
  // ...
}

// bookings collection
{
  "bookingId": "booking001",
  "userId": "user01",                 // ← References custom ID
  "type": "room",
  // ...
}
```

### The Problem:
```java
// Before fix - This gets Firebase Auth UID, not custom "user01"
FirebaseUser currentUser = auth.getCurrentUser();
String currentUserId = currentUser.getUid();  // ❌ Gets "kGxR8..." not "user01"

// Query fails because no bookings have userId = "kGxR8..."
db.collection("bookings")
    .whereEqualTo("userId", currentUserId)  // ❌ No matches found
```

## Solution Implemented

### Two-Step User ID Resolution:

1. **Step 1: Get Firebase Auth User Email**
   ```java
   FirebaseUser currentUser = auth.getCurrentUser();
   String userEmail = currentUser.getEmail();  // "user01@example.com"
   ```

2. **Step 2: Look up Custom User ID by Email**
   ```java
   db.collection("users")
       .whereEqualTo("email", userEmail)
       .limit(1)
       .get()
       .addOnSuccessListener(querySnapshot -> {
           String customUserId = querySnapshot.getDocuments().get(0).getString("uid");
           // Now customUserId = "user01" ✅
           loadBookingsWithCustomUserId(customUserId);
       });
   ```

3. **Step 3: Load Bookings with Custom User ID**
   ```java
   db.collection("bookings")
       .whereEqualTo("userId", customUserId)  // ✅ Now finds "user01" bookings
       .get()
   ```

## Code Changes Made

### Added Fields:
```java
private String currentUserId;    // Will store custom "user01"
private String firebaseAuthUid;  // Stores Firebase Auth UID
```

### Updated Flow:
```java
// 1. Get Firebase Auth info
FirebaseUser currentUser = auth.getCurrentUser();
firebaseAuthUid = currentUser.getUid();

// 2. Look up custom user ID by email
private void loadBookings() {
    db.collection("users")
        .whereEqualTo("email", currentUser.getEmail())
        .limit(1)
        .get()
        .addOnSuccessListener(/* get custom uid */);
}

// 3. Load bookings with custom user ID
private void loadBookingsWithUserId() {
    db.collection("bookings")
        .whereEqualTo("userId", currentUserId)  // Now uses "user01"
        .get()
        .addOnSuccessListener(/* load bookings */);
}
```

## Debug Logging Added

Added debug logs to help troubleshoot:
- Email being used for lookup
- Custom user ID found
- Number of bookings found

## Testing

Now when user logs in with `user01@example.com`:
1. ✅ Email lookup finds user document with `"uid": "user01"`
2. ✅ Booking query uses `"user01"` to find matching bookings
3. ✅ Bookings display correctly in the app

## Alternative Solutions Considered

### Option 1: Use Firebase Auth UID in Firestore (Not Chosen)
- Would require updating all existing Firestore documents
- Risk of data loss
- More complex migration

### Option 2: Email-to-Custom-ID Mapping (Chosen ✅)
- No changes to existing data required
- Safe and reliable
- Works with current data structure

## Future Recommendations

For new projects, consider:
1. Using Firebase Auth UIDs directly in Firestore documents
2. Creating a mapping collection for custom user IDs
3. Using compound queries with both email and custom ID fields

The current solution maintains compatibility with your existing data structure while providing reliable user identification.
