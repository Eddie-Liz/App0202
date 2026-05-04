---
name: android-retrace
description: "Decode obfuscated Android crash stack traces using R8 retrace. Use this skill whenever the user pastes an Android crash log containing minified class names (single-letter or short identifiers like `s8.a`, `a.b.c`), mentions a mapping.txt or ProGuard mapping file, asks to 'retrace' or 'deobfuscate' a stack trace, or shares a release build crash from an Android app. Trigger even if the user just pastes a crash log without explicitly asking for retrace — if the names look obfuscated, offer to retrace it."
---

# Android Retrace Skill

Decode obfuscated R8/ProGuard Android crash stack traces back to original class, method, and field names.

## Tool location

```
R8_JAR="/Applications/Android Studio.app/Contents/plugins/android/lib/r8.jar"
RETRACE_CLASS="com.android.tools.r8.retrace.Retrace"
```

Verify the jar exists before running:
```bash
ls "$R8_JAR" 2>/dev/null || echo "NOT FOUND"
```

If not found, search for alternatives:
```bash
find /Applications -name "r8.jar" 2>/dev/null | head -5
find ~/Library -name "r8.jar" 2>/dev/null | head -5
```

## Running retrace

### Stack trace from a file
```bash
java -cp "/Applications/Android Studio.app/Contents/plugins/android/lib/r8.jar" \
     com.android.tools.r8.retrace.Retrace \
     <mapping.txt> <stacktrace.txt>
```

### Stack trace from clipboard / inline text
Write the trace to a temp file first:
```bash
cat > /tmp/trace.txt << 'EOF'
<paste crash log here>
EOF

java -cp "/Applications/Android Studio.app/Contents/plugins/android/lib/r8.jar" \
     com.android.tools.r8.retrace.Retrace \
     <mapping.txt> /tmp/trace.txt
```

## Finding the mapping file

When the user doesn't specify a path, check common locations:

```bash
# Current build output
find . -path "*/mapping/release/mapping.txt" 2>/dev/null

# Archived mappings (project-specific convention: apk/<version>/mapping_V<version>.txt)
find . -name "mapping*.txt" 2>/dev/null | sort
```

Ask the user which version the crash came from if multiple mappings exist. The mapping **must match the exact build** that produced the crash — a mapping from a different version will produce wrong or garbled output.

## Interpreting output

After retracing, look for:

- `R8$$REMOVED$$CLASS$$<N>` in the **original** (pre-retrace) trace → R8 class merging removed the class entirely; the static initializer (`<clinit>`) is gone. Fix: add `-keep class <OriginalClass> { *; }` to ProGuard rules.
- `ClassCastException` involving `Continuation` → R8 stripped generic `Signature` from a renamed interface. Fix: add `-keep interface <package>.** { *; }` for the affected API interface.
- Methods showing as `<unknown>` after retrace → line number information missing; ensure `-keepattributes SourceFile,LineNumberTable` is in ProGuard rules.

## Common ProGuard fixes discovered via retrace

| Symptom | Root cause | Fix |
|---------|-----------|-----|
| NPE on class that shows as `R8$$REMOVED$$CLASS$$N` | R8 class merging | `-keep class com.example.TheClass { *; }` |
| `ClassCastException: X cannot be cast to Y` in Retrofit suspend fun | R8 strips generic `Signature` from renamed interface | `-keep interface com.example.api.** { *; }` |
| Stack frames show `SourceFile:1` only | Missing line number attributes | `-keepattributes SourceFile,LineNumberTable` |
| ML Kit / Firebase NPE at init | Static initializer lost after class merge | `-keep class com.google.mlkit.** { *; }` |
