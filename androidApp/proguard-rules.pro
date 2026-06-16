# Ktor / SLF4J — no binding is bundled in the fat APK
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Obfuscation breaks reflection-heavy libraries (Ktor, serialization)
-dontobfuscate
