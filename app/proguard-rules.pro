# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep the main service class
-keep class com.yadli.surfingtile.SurfingTileService {
    public *;
}

# Keep configuration class
-keep class com.yadli.surfingtile.Config {
    public static final *;
    public static *;
}

# Keep utility classes
-keep class com.yadli.surfingtile.ProcessUtils {
    public static *;
}

# Keep ProcessResult inner class
-keep class com.yadli.surfingtile.ProcessUtils$ProcessResult {
    public *;
}

# Keep CompletableFuture and related classes
-keep class java.util.concurrent.CompletableFuture {
    public *;
}

# Keep ExecutorService implementations
-keep class java.util.concurrent.Executors {
    public static *;
}

# Keep logging functionality
-keepclassmembers class * {
    @android.util.Log *;
}

# Keep Android service lifecycle methods
-keepclassmembers class * extends android.app.Service {
    public void onCreate();
    public void onDestroy();
    public void onStartCommand(android.content.Intent, int, int);
    public android.os.IBinder onBind(android.content.Intent);
}

# Keep TileService methods
-keepclassmembers class * extends android.service.quicksettings.TileService {
    public void onStartListening();
    public void onStopListening();
    public void onClick();
    public void onTileAdded();
    public void onTileRemoved();
}

# Optimize but keep essential functionality
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keep class **.R$* {
    public static <fields>;
}

# Keep Android resource classes
-keep public class * extends android.content.res.Resources {
    public *;
}

# Keep Android manifest classes
-keep public class * extends android.app.Activity;
-keep public class * extends android.app.Application;
-keep public class * extends android.app.Service;
-keep public class * extends android.content.BroadcastReceiver;
-keep public class * extends android.content.ContentProvider;
-keep public class * extends android.preference.Preference;
-keep public class * extends android.view.View;
-keep public class * extends android.app.Fragment;

# Keep custom exceptions
-keep public class * extends java.lang.Exception;