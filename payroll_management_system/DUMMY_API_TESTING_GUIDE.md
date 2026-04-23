# Dummy API Setup for Testing

## What I've Done

I've added a **dummy API from the internet** (JSONPlaceholder) to test if the issue is with:
1. **Your EMS API** (server-side issue), OR
2. **Your UI code** (client-side issue)

## Changes Made

### 1. **Created Dummy API Client**
- **File**: `DummyApiClient.kt`
- **Base URL**: `https://jsonplaceholder.typicode.com/`
- This is a free, reliable API for testing

### 2. **Created Dummy Data Model**
- **File**: `DummyAttendanceResponse.kt`
- Converts JSONPlaceholder posts to AttendanceResponse format
- Generates realistic dummy attendance data

### 3. **Modified EmsApiService**
- **File**: `EmsApiService.kt` (Lines 29-39)
- **Commented out**: Original attendance history API
- **Added**: Dummy API endpoint (`@GET("posts")`)
- Same function name: `getAttendanceHistory()`

### 4. **Modified AttendanceViewModel**
- **File**: `AttendanceViewModel.kt` (Lines 14-19)
- **Commented out**: `private val emsApi = RetrofitClient.emsApi`
- **Added**: `private val emsApi = DummyApiClient.dummyApi`
- Function `fetchAttendanceHistory()` now uses dummy API
- Converts dummy response to AttendanceResponse automatically

### 5. **Updated Test Utility**
- **File**: `ApiTestUtil.kt`
- Test button now uses dummy API
- Shows converted attendance data in logs

## How to Test

### Step 1: Build and Run
```bash
cd /home/vvdn/Desktop/Pms/AndroidStudioProjects/payroll_management_system
./gradlew clean assembleDebug
```

### Step 2: Login
- Login with any credentials (even fake ones work now)
- The dummy API doesn't require authentication

### Step 3: Check Attendance
- Navigate to **Calendar** view
- OR check the **Attendance Summary** on dashboard
- You should see dummy attendance data

### Step 4: Check Logs
Filter Logcat by `AttendanceVM` and look for:
```
AttendanceVM: Using DUMMY API for testing
AttendanceVM: Success! Received 100 dummy records
AttendanceVM: Converted to 100 attendance records
AttendanceVM:   Record 1:
AttendanceVM:     date: 2026-04-02
AttendanceVM:     inTime: 09:07:00
AttendanceVM:     outTime: 18:13:00
AttendanceVM:     workingHour: 8.3
AttendanceVM:     status: Present
```

### Step 5: Use Test Button
- Click the orange **"Test APIs"** button on dashboard
- Check logs for `ApiTestUtil` tag
- Should show successful dummy API response

## Expected Results

### ✅ If Dummy API Works (UI is fine):
- Calendar shows attendance days (green/red)
- Dashboard shows in-time, out-time, working hours
- Leave history shows dummy data
- **Conclusion**: Your EMS API is the problem, not the UI

### ❌ If Dummy API Doesn't Work (UI has issues):
- Calendar remains empty
- Dashboard shows "--:--" for all fields
- Error messages appear
- **Conclusion**: Your UI code has bugs that need fixing

## Dummy Data Format

The dummy API generates:
- **100 records** (from JSONPlaceholder)
- **Dates**: April 1-28, 2026
- **In Time**: 09:00-09:59
- **Out Time**: 18:00-18:59
- **Working Hours**: 8.0-8.59
- **Status**: Present (80%), Absent (20%)

## How to Switch Back to Real EMS API

When you're ready to test with your actual EMS API:

### 1. In `AttendanceViewModel.kt`:
```kotlin
// Comment out dummy API
// private val emsApi = com.example.myapplication.data.api.DummyApiClient.dummyApi

// Uncomment real API
private val emsApi = RetrofitClient.emsApi
```

### 2. In `EmsApiService.kt`:
```kotlin
// Comment out dummy endpoint
// @GET("posts")
// suspend fun getAttendanceHistory(
//     @Query("userId") userId: String? = null
// ): Response<List<DummyAttendanceResponse>>

// Uncomment real endpoint
@GET("api/attendance/employee/{empId}")
suspend fun getAttendanceHistory(
    @Path("empId") empId: String
): Response<List<AttendanceResponse>>
```

## Files Modified/Created

### Created:
1. `app/src/main/java/com/example/myapplication/data/api/DummyApiClient.kt`
2. `app/src/main/java/com/example/myapplication/data/model/DummyAttendanceResponse.kt`

### Modified:
1. `app/src/main/java/com/example/myapplication/data/api/EmsApiService.kt`
2. `app/src/main/java/com/example/myapplication/viewmodel/AttendanceViewModel.kt`
3. `app/src/main/java/com/example/myapplication/util/ApiTestUtil.kt`
4. `app/src/main/java/com/example/myapplication/view/SecondActivity.kt` (added test button)
5. `app/src/main/res/layout/activity_second.xml` (added test button UI)

## Next Steps

1. **Run the app** with the dummy API
2. **Check if data appears** in the UI
3. **Share the results**:
   - If data shows → Your EMS API is the issue
   - If data doesn't show → Your UI code has bugs
4. **I'll help you fix** whichever issue is identified

## Important Notes

- ✅ All existing code is **commented out, NOT deleted**
- ✅ Function names remain the **same**
- ✅ Easy to switch back to real API
- ✅ Dummy API is **free and reliable**
- ✅ No authentication required
- ✅ Works offline (if cached)

---

**Ready to test! Run the app and let me know what you see.** 🚀
