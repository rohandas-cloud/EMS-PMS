# 🚀 Environment Setup - Quick Reference

## ✅ Changes Made

Your codebase is now **fully environment-friendly**! Here's what was done:

### 1. **Build Configuration** (`app/build.gradle.kts`)
- ✅ Added `debug` build type with localhost URLs
- ✅ Added `release` build type with AWS URLs  
- ✅ Enabled `buildConfig = true` feature
- ✅ Configured BuildConfig fields for both environments

### 2. **API Configuration** (`ApiConfig.kt`) - NEW FILE
- ✅ Central configuration manager
- ✅ Reads URLs from BuildConfig
- ✅ Provides endpoint constants
- ✅ Auto-validates configuration
- ✅ Logs environment details on startup

### 3. **Retrofit Client** (`RetrofitClient.kt`)
- ✅ Removed hardcoded URLs
- ✅ Now uses `ApiConfig.PMS_BASE_URL` and `ApiConfig.EMS_BASE_URL`
- ✅ Uses timeout values from ApiConfig
- ✅ Conditional logging based on environment
- ✅ Auto-validates on initialization

### 4. **EMS API Service** (`EmsApiService.kt`)
- ✅ Removed dummy API references
- ✅ Restored real attendance endpoints
- ✅ All endpoints now use proper backend structure

### 5. **Attendance ViewModel** (`AttendanceViewModel.kt`)
- ✅ Switched from DummyApiClient to real RetrofitClient.emsApi
- ✅ Removed dummy data conversion logic
- ✅ Uses real API calls

---

## 🎯 How to Use

### For Local Development (Testing with localhost)

```bash
# Build debug variant
./gradlew clean assembleDebug

# Install on device/emulator
./gradlew installDebug
```

**Configuration Used:**
- PMS: `http://10.0.2.2:8080/`
- EMS: `http://10.0.2.2:8081/`
- Environment: `development`

### For AWS Production

```bash
# Build release variant
./gradlew clean assembleRelease

# Install on device/emulator
./gradlew installRelease
```

**Configuration Used:**
- PMS: `https://dd6gzv507q8ms.cloudfront.net/`
- EMS: `https://d3lpelprx5afbv.cloudfront.net/`
- Environment: `production`

---

## 🔄 Switch Environments in Android Studio

1. **Open Build Variants panel** (View → Tool Windows → Build Variants)
2. **Select variant**:
   - `app → debug` → Localhost
   - `app → release` → AWS
3. **Click Run** ▶️

---

## 📝 To Change URLs

**Only edit ONE file**: `/app/build.gradle.kts`

```kotlin
buildTypes {
    debug {
        // Change these for localhost
        buildConfigField("String", "PMS_BASE_URL", "\"http://YOUR_IP:PORT/\"")
        buildConfigField("String", "EMS_BASE_URL", "\"http://YOUR_IP:PORT/\"")
    }
    release {
        // Change these for AWS
        buildConfigField("String", "PMS_BASE_URL", "\"https://your-aws-url/\"")
        buildConfigField("String", "EMS_BASE_URL", "\"https://your-aws-url/\"")
    }
}
```

Then rebuild:
```bash
./gradlew clean build
```

---

## ✨ Key Features

✅ **Same endpoints work everywhere** - Only base URL changes  
✅ **No code changes needed** - Just switch build variant  
✅ **Automatic validation** - App checks URLs on startup  
✅ **Debug logging** - See which environment is active  
✅ **Type-safe** - BuildConfig generates constants  
✅ **Production-ready** - Ready for deployment  

---

## 🧪 Verify It Works

1. **Run the app**
2. **Check Logcat** for:
   ```
   D/ApiConfig: ========================================
   D/ApiConfig: Environment: development (or production)
   D/ApiConfig: PMS Base URL: http://...
   D/ApiConfig: EMS Base URL: http://...
   ```

3. **Test login** - Should connect to correct backend
4. **Check API calls** - All use same endpoints

---

## 📚 Full Documentation

See [ENVIRONMENT_CONFIG_GUIDE.md](./ENVIRONMENT_CONFIG_GUIDE.md) for:
- Detailed architecture explanation
- Custom environment setup
- Troubleshooting guide
- Security best practices
- Staging environment configuration

---

## ⚠️ Important Notes

1. **After changing URLs in build.gradle.kts**:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

2. **For Android Emulator**:
   - Use `10.0.2.2` for localhost (NOT `localhost` or `127.0.0.1`)

3. **For Physical Device**:
   - Use your computer's actual IP address
   - Example: `http://192.168.1.100:8080/`

4. **BuildConfig errors in IDE**:
   - These are normal before first build
   - Run `./gradlew build` to generate BuildConfig
   - Errors will disappear

---

## 🎉 You're All Set!

Your app now supports:
- ✅ Localhost development
- ✅ AWS production
- ✅ Easy switching between environments
- ✅ No hardcoded URLs in code
- ✅ Consistent API endpoints

**Just change the build variant and rebuild!**
