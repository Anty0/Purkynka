# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/anty/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve annotations, line numbers, and source file names
-keepattributes *Annotation*,SourceFile,LineNumberTable

-keep public class org.jsoup.** {
public *;
}

#-keep class org.apache.** {
#*;
#}
#-keep org.acra.** {
#*;
#}
#-keep public class org.apache.** {
#public *;
#}
#-keep public class android.net.http.** {
#public *;
#}
-keep class com.google.android.gms.** { *; }
-keep class android.support.v7.widget.SearchView { public *; }
#-dontwarn com.google.android.gms.**
#-keep class org.arca.ErrorReporter { *; }
#-dontwarn org.arca.ErrorReporter

#-keep public class org.acra.ErrorReporter {
#	public void addCustomData(java.lang.String,java.lang.String);
#}
#-keep public class org.acra.ErrorReporter {
#	public org.acra.ErrorReporter$ReportsSenderWorker handleSilentException(java.lang.Throwable);
#}

#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}

-keep class cz.anty.purkynkamanager.BuildConfig {
    *;
}
#-keep class org.acra.ReportingInteractionMode {
#    *;
#}
#-keep class org.acra.sender.HttpSender$Method {
#    *;
#}
#-keep class org.acra.sender.HttpSender$Type {
#    *;
#}
#-keep class org.acra.ReportField {
#	*;
#}

# keep this class so that logging will show 'ACRA' and not a obfuscated name like 'a'.
# Note: if you are removing log messages elsewhere in this file then this isn't necessary
-keep class org.acra.ACRA {
    *;
}

# keep this around for some enums that ACRA needs
-keep class org.acra.ReportingInteractionMode {
    *;
}

-keepnames class org.acra.sender.HttpSender$** {
    *;
}

-keepnames enum org.acra.ReportField {
    *;
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}

# keep this otherwise it is removed by ProGuard
-keep public class org.acra.ErrorReporter {
    public void handleSilentException(java.lang.Throwable);
}