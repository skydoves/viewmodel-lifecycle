
# ViewModel Lifecycle proguard rules #

# noinspection ShrinkerUnresolvedReference

-dontwarn androidx.lifecycle.**
-keep class androidx.lifecycle.** { *; }

-dontwarn com.skydoves.viewmodel.**
-keep class com.skydoves.viewmodel.** { *; }