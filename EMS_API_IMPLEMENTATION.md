# EMS API Implementation - Attendance & Leave Module

## ✅ Implementation Complete

All EMS Attendance and Leave APIs have been successfully integrated into the Android application.

---

## 📋 API Endpoints Implemented

### 1. ATTENDANCE MODULE

#### 1.1 Check-In / Check-Out
- **Endpoint**: `POST api/attendance`
- **Location**: `EmsApiService.markAttendance()`
- **ViewModel**: `AttendanceViewModel.markAttendance()`
- **UI**: `SecondActivity.kt` (Check-In/Check-Out button)
- **Logging**: ✅ Full request/response logging in Logcat

#### 1.2 Attendance History
- **Endpoint**: `GET api/attendance/employee/{empId}`
- **Location**: `EmsApiService.getAttendanceHistory()`
- **ViewModel**: `AttendanceViewModel.fetchAttendanceHistory()`
- **UI**: `Calender.kt`, `SecondActivity.kt`
- **Logging**: ✅ Logs all attendance records with details

#### 1.3 Today Attendance
- **Endpoint**: `GET api/attendance/today`
- **Location**: `EmsApiService.getTodayAttendance()`
- **ViewModel**: `AttendanceViewModel.fetchTodayAttendance()`
- **Logging**: ✅ Logs today's attendance details

#### 1.4 Monthly Attendance
- **Endpoint**: `GET api/attendance/monthly?empId={empId}&year={year}&month={month}`
- **Location**: `EmsApiService.getMonthlyAttendance()`
- **ViewModel**: `AttendanceViewModel.fetchMonthlyAttendance()`
- **Logging**: ✅ Logs all monthly records

---

### 2. LEAVE MODULE

#### 2.1 Leave Types (Dropdown)
- **Endpoint**: `GET api/leaveTypes`
- **Location**: `EmsApiService.getLeaveTypes()`
- **ViewModel**: `LeaveViewModel.fetchLeaveTypes()`
- **UI**: `ApplyLeaveActivity.kt` (AutoCompleteTextView)
- **Logging**: ✅ Logs all leave types with properties

#### 2.2 Apply Leave
- **Endpoint**: `POST api/leaves/apply`
- **Location**: `EmsApiService.applyLeave()`
- **ViewModel**: `LeaveViewModel.applyLeave()`
- **UI**: `ApplyLeaveActivity.kt` (Submit button)
- **Logging**: ✅ Logs request payload and response

#### 2.3 Leave History
- **Endpoint**: `GET api/leaves/history?empId={empId}`
- **Location**: `EmsApiService.getLeaveHistory()`
- **ViewModel**: `LeaveViewModel.fetchLeaveHistory()`
- **UI**: `LeaveRequestActivity.kt`, `SecondActivity.kt`
- **Logging**: ✅ Logs all leave records with status

#### 2.4 Leave Balance
- **Endpoint**: `GET api/leaves/balance/{empId}`
- **Location**: `EmsApiService.getLeaveBalance()`
- **ViewModel**: `LeaveViewModel.fetchLeaveBalance()`
- **UI**: `LeaveRequestActivity.kt`, `SecondActivity.kt`, `ApplyLeaveActivity.kt`
- **Logging**: ✅ Logs casual, sick, earned, and total leaves

#### 2.5 Leave Requests (HR)
- **Endpoint**: `GET api/leaves/requests?status={status}`
- **Location**: `EmsApiService.getLeaveRequests()`
- **ViewModel**: `LeaveViewModel.fetchLeaveRequests()`
- **Features**: Supports filtering by status (APPROVED, PENDING, REJECTED)
- **Logging**: ✅ Logs all leave requests

#### 2.6 Filter Leaves
- **Implementation**: Query parameter `?status=APPROVED|PENDING|REJECTED`
- **Location**: `getLeaveRequests(status: String?)`
- **Usage**: Pass status parameter to filter results

#### 2.7 Approve / Reject Leave
- **Endpoint**: `POST api/leaves/approve-reject`
- **Location**: `EmsApiService.approveRejectLeave()`
- **ViewModel**: `LeaveViewModel.approveRejectLeave()`
- **Logging**: ✅ Logs approval/rejection details

#### 2.8 Single Leave Details
- **Endpoint**: `GET api/leaves/{leaveApplicationId}`
- **Location**: `EmsApiService.getLeaveDetails()`
- **ViewModel**: `LeaveViewModel.fetchLeaveDetails()`
- **Logging**: ✅ Logs complete leave details

---

## 📁 Files Modified

### API Layer
1. **EmsApiService.kt** - Added 11 new API endpoints
2. **LeaveModels.kt** - Created new models (LeaveApplyResponse, LeaveApprovalRequest, LeaveApprovalResponse)

### Data Models
1. **LeaveResponse.kt** - Updated to match EMS API structure
2. **AttendanceResponse.kt** - Already compatible
3. **LeaveBalanceResponse.kt** - Already compatible

### ViewModels
1. **AttendanceViewModel.kt** - Complete rewrite with EMS APIs and extensive logging
2. **LeaveViewModel.kt** - Complete rewrite with EMS APIs and extensive logging

### UI Activities
1. **SecondActivity.kt** - Updated to use EMS empId and new ViewModel methods
2. **Calender.kt** - Updated to fetch EMS attendance data
3. **ApplyLeaveActivity.kt** - Updated to use EMS APIs for leave application
4. **LeaveRequestActivity.kt** - Updated to display EMS leave data

### Adapters
1. **LeaveBalanceAdapter.kt** - Updated to handle LeaveBalanceResponse
2. **LeaveHistoryAdapter.kt** - Updated to handle nullable fields

---

## 🔍 Logcat Tags

All API calls are logged with the following tags:
- **AttendanceVM** - All attendance-related logs
- **LeaveVM** - All leave-related logs
- **SecondActivity** - Dashboard activity logs
- **ApplyLeaveActivity** - Leave application logs
- **CalendarDialog** - Calendar dialog logs

### Log Format
```
========== API ACTION ==========
Parameter 1: value
Parameter 2: value
Response Code: 200
Is Successful: true
  Field 1: value
  Field 2: value
================================================
```

---

## 🧪 Testing Checklist

### Attendance Module
- [ ] Check-In: Tap "Check In" button → Verify logcat shows request/response
- [ ] Check-Out: Tap "Check Out" button → Verify working hours calculated
- [ ] History: Open Calendar → Verify attendance history loads
- [ ] Today: Check dashboard → Verify today's attendance displays
- [ ] Monthly: (If implemented) Verify monthly filter works

### Leave Module
- [ ] Balance: Open Leave Request → Verify leave balance displays
- [ ] Types: Open Apply Leave → Verify dropdown shows leave types
- [ ] Apply: Submit leave request → Verify success/failure response
- [ ] History: Open Leave Request → Verify history list displays
- [ ] Status: Check approved/pending/rejected leaves display correctly

---

## 📊 Response Handling

### Success Response
- Logs full response body in Logcat
- Updates LiveData for UI observation
- Shows success toast message

### Error Response
- Logs error code and error body
- Updates error LiveData
- Shows error toast to user
- Graceful degradation (empty lists, default values)

---

## 🎯 Key Features

1. **Automatic Token Management**: Auth interceptor handles Bearer token
2. **Employee ID from Session**: Uses `fetchEmpIdEms()` from SessionManager
3. **Null Safety**: All models use nullable fields with defaults
4. **Error Handling**: Comprehensive try-catch with user-friendly messages
5. **Loading States**: Progress indicators during API calls
6. **Real-time Updates**: LiveData observers for reactive UI
7. **Detailed Logging**: Every API call logged with request/response

---

## 🚀 Next Steps

1. Run the app and test each API endpoint
2. Monitor Logcat for request/response logs
3. Verify UI displays data correctly
4. Test error scenarios (network failure, invalid data)
5. Add pull-to-refresh if needed
6. Implement pagination for large lists

---

## 📝 Notes

- All endpoints use the EMS base URL: `https://d3lpelprx5afbv.cloudfront.net/`
- Authentication is handled automatically via `createEmsAuthInterceptor()`
- Session Manager stores EMS token and empId separately from PMS
- LeaveBalanceResponse shows Casual, Sick, and Earned leaves in a single response
- Attendance markAttendance works for both check-in and check-out (backend determines based on current state)
