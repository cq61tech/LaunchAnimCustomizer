-keep class com.cq61tech.launchanimcustomizer.xposed.ModuleMain { *; }
-keep class io.github.libxposed.service.** { *; }
-dontwarn io.github.libxposed.api.**
-dontwarn io.github.libxposed.service.**
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkNotNull(...);
    static void checkNotNullParameter(...);
    static void checkNotNullExpressionValue(...);
}
-keepclassmembers enum com.cq61tech.launchanimcustomizer.Curve { *; }
