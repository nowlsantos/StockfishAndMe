# StockfishAndMe ProGuard / R8 Rules

# Keep ViewModels and Presentation layer
-keep class com.nowlsantos.stockfishandme.presentation.** { *; }

# Keep Engine integration
-keep class com.nowlsantos.stockfishandme.engine.** { *; }

# Keep Domain models and logic
-keep class com.nowlsantos.stockfishandme.domain.** { *; }

# Keep DI modules and Koin classes
-keep class com.nowlsantos.stockfishandme.di.** { *; }
-keep class com.nowlsantos.stockfishandme.** { *; }

# Keep attributes for reflection and serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
