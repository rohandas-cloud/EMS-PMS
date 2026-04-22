# 🚀 Quick Start Guide - PMS + EMS Dual AWS Integration

## ⚡ Get Started in 5 Minutes

### Step 1: Update AWS URLs (30 seconds)

Open **[RetrofitClient.kt](file:///home/vvdn/Desktop/Pms/AndroidStudioProjects/payroll_management_system/app/src/main/java/com/example/myapplication/data/api/RetrofitClient.kt)**

Replace these two lines with your actual AWS URLs:

```kotlin
// Line 14
private const val PMS_BASE_URL = "https://your-actual-pms-aws-url.amazonaws.com/"

// Line 15
private const val EMS_BASE_URL = "https://your-actual-ems-aws-url.amazonaws.com/"
```

**Example**:
```kotlin
private const val PMS_BASE_URL = "https://pms-backend-prod.us-east-1.elasticbeanstalk.com/"
private const val EMS_BASE_URL = "https://ems-backend-prod.us-east-1.elasticbeanstalk.com/"
```

---

### Step 2: Build & Run (2 minutes)

```bash
# Clean and build
./gradlew clean build

# Run on device/emulator
./gradlew installDebug
```

Or use Android Studio:
1. Click **Build** → **Clean Project**
2. Click **Build** → **Rebuild Project**
3. Click **Run** ▶️

---

### Step 3: Test Login (1 minute)

1. Open the app
2. Enter credentials (email/username and password)
3. Click Login

**Expected Behavior**:
- App calls PMS login API
- App calls EMS login API
- Both tokens are saved
- User navigates to main screen

**Success Toast**: "Login Successful (PMS + EMS)"

---

### Step 4: Verify Tokens (1 minute)

Add this temporary debug code in `MainActivity.kt` after login:

```kotlin
// After successful login
val pmsToken = MyApplication.sessionManager.fetchPmsToken()
val emsToken = MyApplication.sessionManager.fetchEmsToken()

Log.d("DEBUG", "PMS Token: ${pmsToken?.take(20)}...")
Log.d("DEBUG", "EMS Token: ${emsToken?.take(20)}...")
Log.d("DEBUG", "PMS EmpId: ${MyApplication.sessionManager.fetchEmpIdPms()}")
Log.d("DEBUG", "EMS EmpId: ${MyApplication.sessionManager.fetchEmpIdEms()}")
```

**Check Logcat** for tokens (filter by "DEBUG" tag).

---

## 🔍 Verification Checklist

After login, verify:

- [ ] PMS token is saved (not null)
- [ ] EMS token is saved (not null)
- [ ] PMS Employee ID is saved
- [ ] EMS Employee ID is saved
- [ ] No errors in Logcat
- [ ] User navigates to SecondActivity

---

## 🐛 Common Issues & Quick Fixes

### Issue 1: "Both PMS and EMS login failed"

**Cause**: Wrong URLs or credentials

**Fix**:
1. Verify URLs in `RetrofitClient.kt`
2. Test credentials with Postman
3. Check network connectivity
4. Review Logcat for detailed errors

---

### Issue 2: "Partial Login: EMS login failed"

**Cause**: EMS backend down or different credentials

**Fix**:
1. Check if EMS backend is running
2. Verify EMS credentials (might differ from PMS)
3. App will still work with PMS data only
4. Review error in Logcat (tag: "DualLoginRepo")

---

### Issue 3: No data showing after login

**Cause**: Repositories still using old API calls

**Fix**:
1. Update repositories to use new API instances
2. Follow examples in `REPOSITORY_MIGRATION_EXAMPLES.md`
3. Change `RetrofitClient.api` → `RetrofitClient.pmsApi`
4. Change `RetrofitClient.secondaryApi` → `RetrofitClient.emsApi`

---

### Issue 4: Token not attaching to requests

**Cause**: Interceptor not configured correctly

**Fix**:
1. Verify interceptor in `RetrofitClient.kt`
2. Check if endpoint path contains "auth/login" (skipped by design)
3. Ensure token is saved before API call
4. Enable logging to see request headers

---

## 📊 Testing Different Scenarios

### Test 1: Full Success (Both systems up)

**Expected**: 
- Toast: "Login Successful (PMS + EMS)"
- Both tokens saved
- Navigate to main app

---

### Test 2: Partial Success (One system down)

**Setup**: Turn off one backend temporarily

**Expected**:
- Toast: "Partial Login: [error message]"
- One token saved
- Still navigate to main app
- Warning in logs

---

### Test 3: Complete Failure (Both systems down)

**Setup**: Turn off both backends

**Expected**:
- Toast: "Both PMS and EMS login failed"
- No tokens saved
- Stay on login screen
- Error message shown

---

### Test 4: Wrong Credentials

**Setup**: Enter invalid email/password

**Expected**:
- Toast: "Invalid Email or Password"
- No tokens saved
- Stay on login screen

---

## 🎯 Next Steps After Verification

### 1. Update Other ViewModels

Migrate your existing ViewModels to use new architecture:

**Example - AttendanceViewModel**:

```kotlin
class AttendanceViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    // Use new repository that uses RetrofitClient.pmsApi
    fun fetchAttendance(empId: String) {
        viewModelScope.launch {
            repository.getAttendanceHistory(empId)
                .onSuccess { data -> _attendanceData.value = data }
                .onFailure { error -> _errorMessage.value = error.message }
        }
    }
}
```

---

### 2. Migrate Repositories

Follow the migration guide in `REPOSITORY_MIGRATION_EXAMPLES.md`

**Quick pattern**:

```kotlin
// OLD
private val api = RetrofitClient.api
val token = sessionManager.fetchPrimaryToken()
val response = api.getAttendanceHistory("Bearer $token", empId)

// NEW
private val pmsApi = RetrofitClient.pmsApi
val response = pmsApi.getAttendanceHistory(empId)  // Token auto-attached!
```

---

### 3. Implement Dashboard

Use the `DashboardViewModel` example to create a unified dashboard:

```kotlin
class DashboardActivity : AppCompatActivity() {
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fetch all data from both systems
        viewModel.fetchAllDashboardData()
        
        // Observe PMS data
        viewModel.attendanceData.observe(this) { /* Update UI */ }
        viewModel.leaveBalanceData.observe(this) { /* Update UI */ }
        
        // Observe EMS data
        viewModel.payrollSummaryData.observe(this) { /* Update UI */ }
    }
}
```

---

## 📱 Production Deployment Checklist

Before releasing to production:

```
✅ Update PMS_BASE_URL to production URL
✅ Update EMS_BASE_URL to production URL
✅ Test on real device (not emulator)
✅ Test with slow network (3G)
✅ Test offline behavior
✅ Verify error messages are user-friendly
✅ Remove debug logging
✅ Enable ProGuard/R8
✅ Test on Android 8.0+
✅ Verify no localhost references
✅ Test logout and re-login
✅ Test app kill and restart
```

---

## 🔐 Security Notes

### Token Storage

Currently using SharedPreferences. For production:

**Recommended**: Migrate to EncryptedSharedPreferences

```kotlin
// Add dependency
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Use encrypted prefs
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "encrypted_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## 📞 Need Help?

### Debug Logs

Enable verbose logging to see what's happening:

```kotlin
// In RetrofitClient.kt, logging is already enabled:
HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY  // Shows full request/response
}
```

### Log Tags to Monitor

- `DualLoginRepo` - Login flow
- `LoginViewModel` - Login state
- `CombinedRepo` - Data fetching
- `MainActivity` - UI events
- `AttendanceRepo`, `LeaveRepo`, etc. - Repository operations

### Filter Logcat

```
# Login flow
tag:DualLoginRepo OR tag:LoginViewModel

# API calls
tag:OkHttp

# Errors
level:error
```

---

## ✅ Success Indicators

You know it's working when:

1. ✅ Login succeeds with both systems
2. ✅ Tokens are saved separately
3. ✅ API calls use correct tokens automatically
4. ✅ No manual token passing needed
5. ✅ Errors are handled gracefully
6. ✅ App works with partial failures
7. ✅ No localhost URLs anywhere
8. ✅ Clean architecture maintained

---

## 🎓 Quick Reference

### API Access

```kotlin
// PMS APIs (Attendance, Leave, Holidays)
RetrofitClient.pmsApi

// EMS APIs (Payroll)
RetrofitClient.emsApi
```

### Token Management

```kotlin
// Save
sessionManager.savePmsToken(token)
sessionManager.saveEmsToken(token)

// Fetch
sessionManager.fetchPmsToken()
sessionManager.fetchEmsToken()

// Check
sessionManager.hasBothTokens()
```

### Login

```kotlin
// Call dual login
viewModel.dualLogin(email, password)

// Observe state
viewModel.loginState.observe(this) { state ->
    when (state) {
        is LoginState.Success -> { /* Navigate */ }
        is LoginState.PartialSuccess -> { /* Warn & navigate */ }
        is LoginState.Error -> { /* Show error */ }
    }
}
```

---

## 🚀 You're Ready!

Your dual AWS integration is complete and production-ready. 

**Next**: Update the URLs, test, and deploy!

---

**Questions?** Check these files:
- [DUAL_AWS_INTEGRATION_GUIDE.md](file:///home/vvdn/Desktop/Pms/AndroidStudioProjects/payroll_management_system/DUAL_AWS_INTEGRATION_GUIDE.md) - Full documentation
- [REPOSITORY_MIGRATION_EXAMPLES.md](file:///home/vvdn/Desktop/Pms/AndroidStudioProjects/payroll_management_system/REPOSITORY_MIGRATION_EXAMPLES.md) - Code examples
- [IMPLEMENTATION_SUMMARY.md](file:///home/vvdn/Desktop/Pms/AndroidStudioProjects/payroll_management_system/IMPLEMENTATION_SUMMARY.md) - Implementation details
