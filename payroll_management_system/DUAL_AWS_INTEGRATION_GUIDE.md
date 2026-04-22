# PMS + EMS Dual AWS Integration Guide

## 📋 Overview

This document provides a complete guide to the dual AWS integration system that coordinates both PMS (Payroll Management System) and EMS (Employee Management System) backends in a unified Android MVVM architecture.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                            │
│  (MainActivity, SecondActivity, DashboardActivity)      │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                   ViewModel Layer                        │
│  (LoginViewModel, DashboardViewModel, etc.)             │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│                  Repository Layer                        │
│  (DualLoginRepository, CombinedRepository)              │
└────────┬─────────────────────────────┬──────────────────┘
         │                             │
┌────────▼────────┐           ┌────────▼────────┐
│   PMS API       │           │   EMS API       │
│   Service       │           │   Service       │
└────────┬────────┘           └────────┬────────┘
         │                             │
┌────────▼────────┐           ┌────────▼────────┐
│  PMS Retrofit   │           │  EMS Retrofit   │
│  Client         │           │  Client         │
└────────┬────────┘           └────────┬────────┘
         │                             │
┌────────▼────────┐           ┌────────▼────────┐
│  PMS AWS        │           │  EMS AWS        │
│  Backend        │           │  Backend        │
└─────────────────┘           └─────────────────┘
```

---

## 📁 Project Structure

```
app/src/main/java/com/example/myapplication/
├── data/
│   ├── api/
│   │   ├── PmsApiService.kt          # PMS API endpoints
│   │   ├── EmsApiService.kt          # EMS API endpoints
│   │   └── RetrofitClient.kt         # Dual Retrofit clients
│   ├── local/
│   │   └── SessionManager.kt         # Dual token management
│   ├── model/
│   │   └── (All data models)
│   └── repository/
│       ├── DualLoginRepository.kt    # Dual login coordination
│       ├── CombinedRepository.kt     # Data fetching coordination
│       └── (Other repositories)
└── viewmodel/
    ├── LoginViewModel.kt             # Dual login state management
    ├── DashboardViewModel.kt         # Combined data example
    └── (Other ViewModels)
```

---

## 🔐 Authentication Flow

### 1. User Login Process

```
User enters credentials
        ↓
LoginViewModel.dualLogin()
        ↓
DualLoginRepository.performDualLogin()
        ↓
┌───────────────────────┬───────────────────────┐
│   PMS Login API       │   EMS Login API       │
│   (Sequential)        │   (Sequential)        │
└───────────┬───────────┴───────────┬───────────┘
            │                       │
    Returns pms_token       Returns ems_token
            │                       │
            └───────────┬───────────┘
                        ↓
            SessionManager saves both tokens
                        ↓
            Navigate to main app
```

### 2. Token Storage

Tokens are stored securely in SharedPreferences:

```kotlin
// PMS Token
SessionManager.savePmsToken(token)
SessionManager.fetchPmsToken()

// EMS Token
SessionManager.saveEmsToken(token)
SessionManager.fetchEmsToken()
```

### 3. Token Usage

Each Retrofit client has its own interceptor:

- **PMS Interceptor**: Attaches PMS token to PMS API calls
- **EMS Interceptor**: Attaches EMS token to EMS API calls
- **Never mixed**: PMS APIs NEVER use EMS token and vice versa

---

## 🛠️ Implementation Details

### 1. Retrofit Client Setup

**File**: `data/api/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    // Separate base URLs
    private const val PMS_BASE_URL = "https://your-pms-aws-url.amazonaws.com/"
    private const val EMS_BASE_URL = "https://your-ems-aws-url.amazonaws.com/"

    // Separate HTTP clients with independent interceptors
    private fun createPmsHttpClient(): OkHttpClient { ... }
    private fun createEmsHttpClient(): OkHttpClient { ... }

    // Separate API instances
    val pmsApi: PmsApiService by lazy { ... }
    val emsApi: EmsApiService by lazy { ... }
}
```

**Key Features**:
- ✅ Two independent OkHttp clients
- ✅ Separate auth interceptors
- ✅ Automatic token attachment
- ✅ Skip token for login endpoints
- ✅ Comprehensive logging

### 2. Dual Login Repository

**File**: `data/repository/DualLoginRepository.kt`

```kotlin
class DualLoginRepository(
    private val pmsApi: PmsApiService,
    private val emsApi: EmsApiService
) {
    suspend fun performDualLogin(
        email: String,
        password: String
    ): DualLoginResult {
        // 1. Login to PMS
        // 2. Login to EMS
        // 3. Return combined result
    }
}
```

**Result Types**:
- `isFullySuccessful`: Both PMS and EMS login succeeded
- `isPartiallySuccessful`: One succeeded, one failed
- `errors`: List of error messages

### 3. Session Manager

**File**: `data/local/SessionManager.kt`

Enhanced to manage dual tokens:

```kotlin
// New methods
fun savePmsToken(token: String)
fun fetchPmsToken(): String?
fun saveEmsToken(token: String)
fun fetchEmsToken(): String?
fun hasBothTokens(): Boolean
fun saveEmpIdPms(empId: String)
fun saveEmpIdEms(empId: String)

// Backward compatible (deprecated)
@Deprecated("Use savePmsToken instead")
fun savePrimaryToken(token: String)
```

### 4. Login ViewModel

**File**: `viewmodel/LoginViewModel.kt`

Uses sealed class for state management:

```kotlin
sealed class LoginState {
    object Loading : LoginState()
    data class Success(val result: DualLoginResult) : LoginState()
    data class PartialSuccess(val result: DualLoginResult) : LoginState()
    data class Error(val message: String) : LoginState()
}
```

**Usage in Activity**:

```kotlin
viewModel.loginState.observe(this) { state ->
    when (state) {
        is LoginState.Loading -> { /* Show loading */ }
        is LoginState.Success -> { /* Navigate to main */ }
        is LoginState.PartialSuccess -> { /* Warn & navigate */ }
        is LoginState.Error -> { /* Show error */ }
    }
}
```

### 5. Combined Repository

**File**: `data/repository/CombinedRepository.kt`

Coordinates data fetching from both systems:

```kotlin
class CombinedRepository(
    private val pmsApi: PmsApiService,
    private val emsApi: EmsApiService
) {
    // PMS operations
    suspend fun fetchPmsAttendanceHistory(empId: String): Result<...>
    suspend fun fetchPmsLeaveBalance(empId: String): Result<...>
    suspend fun fetchPmsHolidays(): Result<...>

    // EMS operations
    suspend fun fetchEmsPayrollSummary(...): Result<...>
    suspend fun fetchEmsPayrollDetail(id: String): Result<...>

    // Combined operations
    suspend fun fetchDashboardData(): DashboardDataResult
}
```

---

## 📝 Migration Guide

### Step 1: Update Base URLs

**File**: `data/api/RetrofitClient.kt`

Replace placeholder URLs with your actual AWS endpoints:

```kotlin
private const val PMS_BASE_URL = "https://your-actual-pms-url.amazonaws.com/"
private const val EMS_BASE_URL = "https://your-actual-ems-url.amazonaws.com/"
```

### Step 2: Update Login Call

**Old Code** (MainActivity.kt):
```kotlin
viewModel.login(email, password)
```

**New Code**:
```kotlin
viewModel.dualLogin(email, password)
```

### Step 3: Update Login Observer

**Old Code**:
```kotlin
viewModel.loginResult.observe(this) { result ->
    result.onSuccess { ... }
    result.onFailure { ... }
}
```

**New Code**:
```kotlin
viewModel.loginState.observe(this) { state ->
    when (state) {
        is LoginState.Success -> { ... }
        is LoginState.PartialSuccess -> { ... }
        is LoginState.Error -> { ... }
    }
}
```

### Step 4: Update Token Fetching

**Old Code**:
```kotlin
val token = sessionManager.fetchPrimaryToken()
```

**New Code**:
```kotlin
val pmsToken = sessionManager.fetchPmsToken()
val emsToken = sessionManager.fetchEmsToken()
```

### Step 5: Update API Calls

**Old Code**:
```kotlin
val api = RetrofitClient.api
```

**New Code**:
```kotlin
// For PMS data
val pmsApi = RetrofitClient.pmsApi

// For EMS data
val emsApi = RetrofitClient.emsApi
```

---

## 🔄 Token Management

### Token Lifecycle

1. **Login**: Both tokens obtained and stored
2. **API Calls**: Tokens automatically attached by interceptors
3. **Token Expiry**: Detected via 401 responses
4. **Re-login**: User must login again to refresh tokens

### Token Validation

```kotlin
// Check if both tokens exist
if (sessionManager.hasBothTokens()) {
    // Proceed with app
} else {
    // Redirect to login
}
```

### Clearing Tokens

```kotlin
// Clear everything
sessionManager.clearSession()

// Clear only tokens (keep user info)
sessionManager.clearTokensOnly()

// Clear specific token
sessionManager.clearPmsToken()
sessionManager.clearEmsToken()
```

---

## 🎯 Best Practices

### 1. Separate Concerns

- ✅ PMS repositories use `RetrofitClient.pmsApi`
- ✅ EMS repositories use `RetrofitClient.emsApi`
- ❌ Never mix APIs in same repository

### 2. Error Handling

```kotlin
// Good: Handle errors gracefully
val result = repository.fetchPmsAttendanceHistory(empId)
result.onSuccess { data -> /* Use data */ }
    .onFailure { error -> /* Show error */ }

// Bad: Ignore errors
val data = repository.fetchPmsAttendanceHistory(empId) // Could throw
```

### 3. Loading States

```kotlin
// Separate loading states for PMS and EMS
val isPmsLoading = MutableLiveData<Boolean>()
val isEmsLoading = MutableLiveData<Boolean>()

// UI can show partial loading
```

### 4. Token Safety

```kotlin
// Always check token before use
val token = sessionManager.fetchPmsToken()
if (token == null) {
    // Handle missing token (redirect to login)
    return
}
```

---

## 🐛 Troubleshooting

### Issue: Login fails for one system

**Solution**: 
- Check if credentials are same for both systems
- Review error messages in `DualLoginResult.errors`
- Partial login is handled gracefully

### Issue: Token not attaching to requests

**Solution**:
- Verify interceptor is configured correctly
- Check if endpoint path contains "auth/login" (skipped)
- Ensure token is saved in SessionManager

### Issue: 401 Unauthorized errors

**Solution**:
- Token may have expired
- User needs to re-login
- Implement token refresh logic if supported

### Issue: Mixed data from wrong system

**Solution**:
- Verify you're using correct API instance
- PMS data → `RetrofitClient.pmsApi`
- EMS data → `RetrofitClient.emsApi`

---

## 📊 Example: Dashboard Activity

```kotlin
class DashboardActivity : AppCompatActivity() {
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observe PMS data
        viewModel.attendanceData.observe(this) { attendance ->
            // Update attendance UI
        }
        
        // Observe EMS data
        viewModel.payrollSummaryData.observe(this) { payroll ->
            // Update payroll UI
        }
        
        // Observe loading states
        viewModel.isPmsLoading.observe(this) { loading ->
            // Show PMS loading indicator
        }
        
        viewModel.isEmsLoading.observe(this) { loading ->
            // Show EMS loading indicator
        }
        
        // Fetch all data
        viewModel.fetchAllDashboardData()
    }
}
```

---

## 🚀 Future Enhancements

1. **Token Refresh**: Implement automatic token refresh before expiry
2. **Offline Support**: Cache data with Room database
3. **Retry Logic**: Automatic retry on network failures
4. **Analytics**: Track login success rates for both systems
5. **Biometric Auth**: Add fingerprint/face login
6. **Secure Storage**: Migrate to EncryptedSharedPreferences or Android Keystore

---

## 📞 Support

For issues or questions:
1. Check logs with tag "DualLoginRepo", "CombinedRepo", "LoginViewModel"
2. Verify AWS endpoints are accessible
3. Ensure credentials are correct for both systems
4. Review error messages in UI

---

## ✅ Checklist

Before deployment:

- [ ] Update PMS_BASE_URL with actual AWS endpoint
- [ ] Update EMS_BASE_URL with actual AWS endpoint
- [ ] Test login flow with valid credentials
- [ ] Test partial login scenario
- [ ] Verify token storage and retrieval
- [ ] Test API calls with both tokens
- [ ] Verify error handling
- [ ] Test logout and session clearing
- [ ] Test app restart with existing tokens
- [ ] Verify no localhost references remain

---

**Last Updated**: April 21, 2026  
**Version**: 1.0.0  
**Architecture**: MVVM with Repository Pattern
