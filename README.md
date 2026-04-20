# Kotlin Compiler for Android Runtime

A port of the Kotlin compiler that runs on the Android runtime.

---

## Installation

### 1. Add repositories

**Kotlin DSL**
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
````

---

### 2. Add dependency

```kotlin
implementation("com.github.PranavPurwar:kotlinc-android:v2.3.20")
```

---

## Overview

This project provides a modified Kotlin compiler that can run inside an Android environment.

Internally:

* The upstream `kotlin-compiler` JAR is used as a base
* Certain files are patched for compatibility and improvements
* The result is repackaged as a patched compiler artifact

Only targeted parts of the compiler are changed; the rest remains identical to upstream.
