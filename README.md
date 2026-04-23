# Kotlin & Java compilers — Android port

---

Patched Kotlin and Java compiler builds adjusted to run on Android. For use in IDEs, editors,
compilers, etc.

## Included artifacts

- Kotlin compiler (JitPack): `com.github.PranavPurwar:kotlinc-android:2.3.20`
- Java compiler (JitPack): `com.github.PranavPurwar:javac-android:26+35`

## Status

Both compilers have been ported to run on Android and Android-based build environments.

## Supported features

The patched compilers support the following, unless explicitly noted:

- Java 26 compilation support, Module system, JAVA_HOME and ct.sym are also supported.
- Kotlin 2.5 is supported, `fastJarFileSystem` is supported

Limitations & notes

- Non-JVM backends (JavaScript, Native, Wasm) are out of scope for this port. They might work, but
  not guaranteed.

## License & attribution

The patched artifacts contain upstream code under their original licenses (Apache 2.0 and other
compatible licenses). See license headers in source files for details.

---

Are you using this in your app? I'd love to see it!

Open a PR to add your project to the list below, or send me an email
at purwarpranav80@gmail.com with a brief description of your use case (e.g., 'Building a mobile Java
IDE').

## Featured

- [Cosmic IDE](https://github.com/Cosmic-IDE)
