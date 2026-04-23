# Repository Migration Examples

This file shows how to migrate your existing repositories to use the new dual AWS architecture.

---

## Example 1: AttendanceRepository Migration

### ❌ OLD VERSION (Before)

```kotlin
class AttendanceRepository {

    private val api = RetrofitClient.api  // Single API

    suspend fun getAttendanceHistory(empId: String): List<AttendanceResponse> {
        return try {
            val token = MyApplication.sessionManager.fetchPrimaryToken()
                ?: throw IllegalStateException("Primary token not found")

            val response = api.getAttendanceHistory("Bearer $token", empId)

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch attendance: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Exception", e)
            throw e
        }
    }
}
```

### ✅ NEW VERSION (After)

```kotlin
class AttendanceRepository {

    private val pmsApi = RetrofitClient.pmsApi  // PMS-specific API

    suspend fun getAttendanceHistory(empId: String): Result<List<AttendanceResponse>> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getAttendanceHistory(empId)

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch attendance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Exception", e)
            Result.failure(e)
        }
    }
}
```

**Key Changes**:
- ✅ Use `RetrofitClient.pmsApi` instead of `RetrofitClient.api`
- ✅ No need to manually pass token (interceptor handles it)
- ✅ Return `Result<T>` for better error handling
- ✅ Removed manual token fetching

---

## Example 2: LeaveRepository Migration

### ❌ OLD VERSION (Before)

```kotlin
class LeaveRepository {

    private val api = RetrofitClient.api

    suspend fun getLeaveBalance(empId: String): List<LeaveBalanceItem> {
        return withContext(Dispatchers.IO) {
            try {
                val token = MyApplication.sessionManager.fetchPrimaryToken()
                    ?: throw IllegalStateException("Primary token not found")

                val response = api.getLeaveBalance("Bearer $token", empId)

                if (!response.isSuccessful) {
                    throw Exception("Failed to fetch leave balance: ${response.code()}")
                }

                val body = response.body() ?: throw Exception("Empty response body")

                // Map backend response to UI model
                listOf(
                    LeaveBalanceItem(
                        leaveType = "Casual Leave",
                        remainingLeaves = body.casualLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.casualLeave ?: 0,
                        year = "2026"
                    ),
                    // ... more items
                )
            } catch (e: Exception) {
                Log.e("LeaveRepo", "Exception", e)
                throw e
            }
        }
    }
}
```

### ✅ NEW VERSION (After)

```kotlin
class LeaveRepository {

    private val pmsApi = RetrofitClient.pmsApi

    suspend fun getLeaveBalance(empId: String): Result<LeaveBalanceResponse> {
        return try {
            // Token automatically attached by PMS interceptor
            val response = pmsApi.getLeaveBalance(empId)

            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to fetch leave balance: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Exception", e)
            Result.failure(e)
        }
    }

    // Alternative: If you still need mapped UI model
    suspend fun getLeaveBalanceMapped(empId: String): Result<List<LeaveBalanceItem>> {
        return try {
            val response = pmsApi.getLeaveBalance(empId)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty response body")
                
                val mappedList = listOf(
                    LeaveBalanceItem(
                        leaveType = "Casual Leave",
                        remainingLeaves = body.casualLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.casualLeave ?: 0,
                        year = "2026"
                    ),
                    LeaveBalanceItem(
                        leaveType = "Sick Leave",
                        remainingLeaves = body.sickLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.sickLeave ?: 0,
                        year = "2026"
                    ),
                    LeaveBalanceItem(
                        leaveType = "Earned Leave",
                        remainingLeaves = body.earnedLeave ?: 0,
                        usedLeaves = 0,
                        totalLeaves = body.earnedLeave ?: 0,
                        year = "2026"
                    )
                )
                
                Result.success(mappedList)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LeaveRepo", "Exception", e)
            Result.failure(e)
        }
    }
}
```

**Key Changes**:
- ✅ Use `RetrofitClient.pmsApi`
- ✅ No manual token management
- ✅ Return `Result<T>` for better error handling
- ✅ Keep mapping logic separate if needed

---

## Example 3: PayrollRepository Migration (EMS)

### ❌ OLD VERSION (Before)

```kotlin
class PayrollViewModel : ViewModel() {

    private val api = RetrofitClient.secondaryApi  // Confusing naming

    fun fetchPayrollSummary(month: Int?, year: Int?, page: Int = 0) {
        val empId = MyApplication.sessionManager.fetchEmpIdSecondary()
        if (empId == null) {
            _errorMessage.value = "User ID not found. Please login again."
            return
        }

        val request = PayrollSummaryRequest(
            month = month,
            year = year,
            empId = empId,
            page = page
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPayrollSummary(request)
                if (response.isSuccessful) {
                    _summaryData.value = response.body()
                } else {
                    _errorMessage.value = "Failed to fetch summary: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### ✅ NEW VERSION (After)

```kotlin
class PayrollViewModel : ViewModel() {

    private val emsApi = RetrofitClient.emsApi  // Clear naming

    fun fetchPayrollSummary(month: Int?, year: Int?, page: Int = 0) {
        val empId = MyApplication.sessionManager.fetchEmpIdEms()
        if (empId == null) {
            _errorMessage.value = "Employee ID not found. Please login again."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Token automatically attached by EMS interceptor
                val response = emsApi.getPayrollSummary(month, year, empId, page)
                
                if (response.isSuccessful) {
                    _summaryData.value = response.body()
                } else {
                    _errorMessage.value = "Failed to fetch summary: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

**Key Changes**:
- ✅ Use `RetrofitClient.emsApi` instead of `secondaryApi`
- ✅ Use `fetchEmpIdEms()` instead of `fetchEmpIdSecondary()`
- ✅ No manual token passing
- ✅ Clear naming convention

---

## Example 4: Using CombinedRepository

Instead of managing multiple repositories, use `CombinedRepository`:

```kotlin
class DashboardViewModel : ViewModel() {

    private val combinedRepository = CombinedRepository(
        pmsApi = RetrofitClient.pmsApi,
        emsApi = RetrofitClient.emsApi
    )

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Fetch all data in one call
                val result = combinedRepository.fetchDashboardData()
                
                // Access individual results
                result.attendanceData?.let { attendance ->
                    _attendanceData.value = attendance
                }
                
                result.payrollSummaryData?.let { payroll ->
                    _payrollData.value = payroll
                }
                
                // Check for errors
                if (result.hasErrors) {
                    Log.w("DashboardVM", "Some data failed: ${result.errorMessages}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

## Migration Checklist

### For Each Repository:

1. [ ] Change `RetrofitClient.api` → `RetrofitClient.pmsApi` (for PMS data)
2. [ ] Change `RetrofitClient.secondaryApi` → `RetrofitClient.emsApi` (for EMS data)
3. [ ] Remove manual token fetching (`fetchPrimaryToken()`, `fetchSecondaryToken()`)
4. [ ] Remove manual token passing in API calls (no more `"Bearer $token"`)
5. [ ] Update Employee ID methods:
   - `fetchEmpIdPrimary()` → `fetchEmpIdPms()`
   - `fetchEmpIdSecondary()` → `fetchEmpIdEms()`
6. [ ] Change return type to `Result<T>` for better error handling
7. [ ] Test the repository with actual API calls

### For Each ViewModel:

1. [ ] Update repository initialization to use new API instances
2. [ ] Update token/empId fetching methods
3. [ ] Handle `Result<T>` returns properly
4. [ ] Add separate loading states for PMS and EMS if needed
5. [ ] Test UI with actual data

---

## Common Patterns

### Pattern 1: Simple Repository Method

```kotlin
suspend fun fetchData(param: String): Result<DataType> {
    return try {
        val response = pmsApi.endpoint(param)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response"))
        } else {
            Result.failure(Exception("Failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Pattern 2: Repository with EMS API

```kotlin
suspend fun fetchEmsData(param: String): Result<DataType> {
    return try {
        val response = emsApi.endpoint(param)
        if (response.isSuccessful) {
            Result.success(response.body() ?: throw Exception("Empty"))
        } else {
            Result.failure(Exception("Failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Pattern 3: ViewModel Usage

```kotlin
fun loadData() {
    viewModelScope.launch {
        _isLoading.value = true
        
        repository.fetchData(empId)
            .onSuccess { data -> _data.value = data }
            .onFailure { error -> _errorMessage.value = error.message }
        
        _isLoading.value = false
    }
}
```

---

## Testing Your Migration

### 1. Test Login Flow

```kotlin
// Should work with new dual login
viewModel.dualLogin(email, password)

// Check tokens are saved
assert(sessionManager.fetchPmsToken() != null)
assert(sessionManager.fetchEmsToken() != null)
```

### 2. Test PMS API Calls

```kotlin
// Should use PMS token automatically
val result = attendanceRepository.getAttendanceHistory(empId)
result.onSuccess { /* Verify data */ }
```

### 3. Test EMS API Calls

```kotlin
// Should use EMS token automatically
val result = payrollRepository.getPayrollSummary(month, year)
result.onSuccess { /* Verify data */ }
```

### 4. Test Error Handling

```kotlin
// Test with invalid token
sessionManager.clearPmsToken()
val result = repository.fetchData()
assert(result.isFailure) // Should fail gracefully
```

---

## Need Help?

If you encounter issues during migration:

1. Check logs for token attachment issues
2. Verify you're using correct API instance (pmsApi vs emsApi)
3. Ensure SessionManager methods are updated
4. Review error messages in Result failures
5. Refer to `DUAL_AWS_INTEGRATION_GUIDE.md` for detailed documentation
