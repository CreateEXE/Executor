# VRM Avatar Renderer - Quick Start Guide

Get your VRM avatar rendering app running in **5 minutes**.

## Prerequisites Checklist

- [ ] Android Studio (Koala 2024.1.2+)
- [ ] JDK 11 installed
- [ ] Android SDK API 35 downloaded
- [ ] A physical device or emulator with API 24+
- [ ] 500MB free disk space

## 1. Clone & Open

```bash
git clone <repo-url>
cd VRM-Avatar-Renderer
```

**In Android Studio**: 
- File → Open → Select project root
- Wait for Gradle sync (2-3 minutes)

## 2. Verify Setup

In Android Studio terminal:

```bash
./gradlew --version          # Check Gradle
./gradlew tasks              # Verify project loads
```

**Expected output**:
```
Gradle 8.7.0
Kotlin version: 2.0.0
```

## 3. Build Debug APK

```bash
./gradlew assembleDebug
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

Time: ~2 minutes (first build slower due to dependency download)

## 4. Deploy to Device

### Option A: Physical Device
```bash
adb devices  # Verify device connected
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Emulator
```bash
# In Android Studio Device Manager
# Click "Create Device" if needed (API 34 recommended)
# Select emulator in toolbar
./gradlew installDebug
```

**Expected result**: App icon appears on home screen

## 5. Test Avatar Loading

1. **Prepare a test avatar**: 
   - Download from [VRM models site](https://hub.vroid.com/)
   - Or use: [Alicia VRM](https://github.com/vrm-c/UniVRM/tree/master/Tests/Models) (free)
   - Save `.glb` file to device Downloads folder

2. **In the app**:
   - Tap **"Load Avatar (GLB)"**
   - Select your avatar file
   - Wait for loading (~2 seconds)
   - Tap **"Play Walk"** to see animation

3. **Verify working**:
   - Avatar appears on screen ✅
   - Animation plays smoothly ✅
   - Button controls respond ✅

## Common Issues & Fixes

### Issue: Gradle Sync Fails

**Solution**:
```bash
./gradlew --stop
rm -rf .gradle/
./gradlew sync
```

### Issue: App Crashes on Launch

**Solution**:
```bash
adb logcat | grep "VRM\|Filament"
# Check error message
# Likely: OpenGL ES 3.0 not supported on device/emulator
```

**For Emulator**: Enable GPU acceleration in AVD settings

### Issue: "Filament not supported"

**Solution**: 
- Device must support OpenGL ES 3.0
- Use physical Android 7.0+ device, or
- Emulator with GPU support enabled

### Issue: File Picker Won't Work

**Solution**:
```bash
# Grant permission manually
adb shell pm grant com.vrm.avatarrenderer android.permission.READ_EXTERNAL_STORAGE
```

## Development Workflow

### Hot Reload (Kotlin Hot Swapping)

```bash
# Edit code, then in IDE press:
# macOS: Control + Option + R
# Windows/Linux: Control + Shift + F10
```

### Rebuild & Reinstall

```bash
./gradlew installDebug
```

### Run Tests

```bash
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # Device tests
```

## Project Structure Quick Reference

```
Project Root/
├── app/build.gradle.kts         ← Modify dependencies here
├── app/src/main/
│   ├── kotlin/com/vrm/avatarrenderer/
│   │   ├── MainActivity.kt       ← UI entry point
│   │   └── rendering/
│   │       ├── FilamentRenderView.kt
│   │       ├── AvatarRenderer.kt
│   │       └── AnimationController.kt
│   ├── res/values/
│   │   ├── strings.xml
│   │   └── styles.xml
│   └── AndroidManifest.xml
└── settings.gradle.kts
```

## Key Files to Edit

### Add New Animation

Edit `AnimationController.kt`:

```kotlin
animationDefs = mapOf(
    "Walk" to 1.5f,
    "MyNewAnimation" to 2.0f,  // ← Add here
    // ...
)
```

### Change UI Layout

Edit `MainActivity.kt`:

```kotlin
loadAvatarButton = Button(this).apply {
    text = "New Label"  // ← Change button text
}
```

### Adjust Camera

Edit `FilamentRenderView.kt`:

```kotlin
view.camera = engine.createCamera().apply {
    setPosition(0.0, 2.0, 5.0)  // ← Change view distance
}
```

## Build Variants

### Debug Build
- Symbols included (debuggable)
- Faster compilation
- ProGuard disabled
- ~50 MB APK

**Use for**: Development, testing

### Release Build
```bash
./gradlew assembleRelease
```

- Optimized (30% smaller)
- Symbols removed (update mapping.txt)
- ProGuard enabled
- ~35 MB APK

**Use for**: Production deployment

## Environment Variables

Optional, set before building:

```bash
# Gradle heap size (if getting OOM)
export GRADLE_OPTS="-Xmx6g"

# Disable offline mode
export GRADLE_OPTS="$GRADLE_OPTS -Dorg.gradle.java.home=/path/to/jdk11"
```

## CI/CD Pipeline

To test locally what GitHub Actions runs:

```bash
./gradlew assembleDebug assembleRelease test lint --scan
```

**Expected time**: 5-8 minutes

## IDE Setup Tips

### Android Studio

**Recommended Settings**:
```
Preferences → Build, Execution, Deployment → Build Tools
├─ Gradle JDK: Default (bundled)
├─ Gradle scripts language: Kotlin DSL
└─ Parallel builds: 4

Preferences → Languages & Frameworks → Kotlin
├─ Incremental compilation: ON
└─ Coroutines support: ON
```

### Code Completion

```
⌘+Space (macOS) or Ctrl+Space (Windows/Linux)
```

### Run Configurations

- ✅ Default created automatically
- Click green "▶" to run
- Switch between device/emulator in toolbar

## Next Steps

1. ✅ **Build works?** → Modify animations in `AnimationController.kt`
2. ✅ **Avatar loads?** → Customize UI colors in `styles.xml`
3. ✅ **Ready for release?** → Read `README.md` for deployment

## Documentation

| Document | Purpose |
|----------|---------|
| `README.md` | Full setup & features |
| `ARCHITECTURE.md` | Technical design details |
| `COMPATIBILITY.md` | Version verification results |
| `build.yml` | GitHub Actions CI/CD workflow |

## Support

**Build failing?**
```bash
./gradlew clean build --info
# Look for error in detailed output
```

**Need help?**
- Check `ARCHITECTURE.md` for component details
- Review `COMPATIBILITY.md` for version issues
- See `README.md` for troubleshooting section

---

**Status**: Production ready  
**Last Updated**: May 19, 2026  
**Estimated Setup Time**: 5 minutes
