# compose

This example is a very basic setup which displays events in a list as they come in when interacting 
with start/stop/restart buttons. 

Definitely take a look at the `build.gradle.kts` file to see how the `kmp-tor` dependencies are defined 
which are documented and give a general idea of things (and points to further documentation). 

Check out the platform specific integration code `src/androidMain`, `src/destopMain`, and `src/iosMain` for defining 
`TorRuntime.Environment`. The remaining example code can be found in `src/commonMain`. For `android`, also take a 
look at the `AndroidManifest.xml` for necessary permissions.

When running `android`, long press the application icon on the device and enable notifications 
to see the foreground service notification stuff for API 33+ (sample app does not ask for permissions).

For running on `iOS` device, you will need to change the [Config.xcconfig](../../iosApp/Configuration/Config.xcconfig) 
file's `TEAM_ID` parameter to whatever your id is to sign the sample app and `LibTor.framework`. See the 
[Kotlin Tutorial Example](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-create-first-app.html#run-on-a-real-ios-device) 
for more information on that.

For running Jvm Desktop, execute from command line:
```
./gradlew :samples:compose:run -PKMP_TARGETS="JVM"
```
