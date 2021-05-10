# v0.9.1 (Mon May 10 2021)

#### 🐛 Bug Fix

- Upgrade to Kotlin 1.5 [#13](https://github.com/intuit/hooks/pull/13) ([@stabbylambda](https://github.com/stabbylambda))

#### ⚠️ Pushed to `main`

- rename master to main (jeremiah_zucker@intuit.com)

#### 🏠 Internal

- Upgrade binary compatibility tool to 0.5.0 [#14](https://github.com/intuit/hooks/pull/14) (jeremiah_zucker@intuit.com)
- Setup Auto for Canary and Next [#12](https://github.com/intuit/hooks/pull/12) (jeremiah_zucker@intuit.com)

#### Authors: 2

- David Stone ([@stabbylambda](https://github.com/stabbylambda))
- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.9.0 (Wed Mar 17 2021)

:tada: This release contains work from a new contributor! :tada:

Thank you, David Stone ([@stabbylambda](https://github.com/stabbylambda)), for all your work!

### Release Notes

#### Hooks with type parameters ([#8](https://github.com/intuit/hooks/pull/8))

Enhance DSL to adds the ability to generate Hooks with type parameters. The use case for this is when some piece of data is known only to the consumer of a library and the consumers of the taps, but not necessarily the library itself. As an example:

```kotlin
class FooHooks<T> : Hooks() {
    open val beforeCalc = syncHook<(T) -> Unit>()
}

data class Foo<T>(val t: T)  {
    public val hooks = FooHooksImpl<T>()

    fun calc() {
        hooks.beforeCalc.call(t)
        // ...
    }
}

fun runCalcsWithLog() {
    val f = Foo<String>("hi")
    f.hooks.beforeCalc.tap("hi") { x -> println(x) }
}
```

---

#### 🚀 Enhancement

- Hooks with type parameters [#8](https://github.com/intuit/hooks/pull/8) ([@stabbylambda](https://github.com/stabbylambda))

#### 🐛 Bug Fix

- Update to use Typed Quotes [#7](https://github.com/intuit/hooks/pull/7) ([@stabbylambda](https://github.com/stabbylambda))

#### 📝 Documentation

- update docs for gradle plugin portal [#6](https://github.com/intuit/hooks/pull/6) ([@sugarmanz](https://github.com/sugarmanz))

#### Authors: 2

- David Stone ([@stabbylambda](https://github.com/stabbylambda))
- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.8.2 (Tue Mar 02 2021)

#### 🐛 Bug Fix

- Publish to Gradle Plugin Portal [#5](https://github.com/intuit/hooks/pull/5) ([@sugarmanz](https://github.com/sugarmanz))

#### Authors: 1

- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.8.1 (Tue Mar 02 2021)

#### ⚠️ Pushed to `master`

- Fix orchid deploy ([@sugarmanz](https://github.com/sugarmanz))

#### Authors: 1

- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.8.0 (Tue Mar 02 2021)

#### 🚀 Enhancement

- Finalize publishing and fix links [#4](https://github.com/intuit/hooks/pull/4) ([@sugarmanz](https://github.com/sugarmanz))

#### Authors: 1

- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.7.4 (Tue Mar 02 2021)

:tada: This release contains work from new contributors! :tada:

Thanks for all your work!

:heart: Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

:heart: Andrew Lisowski ([@hipstersmoothie](https://github.com/hipstersmoothie))

#### 🐛 Bug Fix

- test signing first [#3](https://github.com/intuit/hooks/pull/3) ([@sugarmanz](https://github.com/sugarmanz))
- Configure CI [#2](https://github.com/intuit/hooks/pull/2) ([@sugarmanz](https://github.com/sugarmanz))
- migrate links [#1](https://github.com/intuit/hooks/pull/1) ([@sugarmanz](https://github.com/sugarmanz))

#### ⚠️ Pushed to `master`

- don't do other publishing yet ([@sugarmanz](https://github.com/sugarmanz))
- prepare 0.7.3-SNAPSHOT ([@sugarmanz](https://github.com/sugarmanz))
- version 0.7.2-SNAPSHOT ([@sugarmanz](https://github.com/sugarmanz))
- specify subproj ([@sugarmanz](https://github.com/sugarmanz))
- move auto download ([@sugarmanz](https://github.com/sugarmanz))
- initialize public repo ([@hipstersmoothie](https://github.com/hipstersmoothie))

#### Authors: 2

- Andrew Lisowski ([@hipstersmoothie](https://github.com/hipstersmoothie))
- Jeremiah Zucker ([@sugarmanz](https://github.com/sugarmanz))

---

# v0.7.0 (Sat Jan 23 2021)

### Release Notes

_From #38_

- Configure [KtLint Gradle Plugin](https://github.com/JLLeitschuh/ktlint-gradle)
- Enable [explicit API mode](https://kotlinlang.org/docs/reference/whatsnew14.html#explicit-api-mode-for-library-authors)
- Setup [API matching tool](https://github.com/Kotlin/binary-compatibility-validator)
- Generate [Dokka API docs](https://kotlin.github.io/dokka/1.4.20/)
- Migrate to [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

---

#### 🚀 Enhancement

- Add logo & fix build redeclaration issue [#39](https://github.intuit.com/player/hooks/pull/39) ([@JZUCKER](https://github.intuit.com/JZUCKER))
- Quality-of-Life Tooling [#38](https://github.intuit.com/player/hooks/pull/38) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 1

- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.6.0 (Tue Jan 12 2021)

### Release Notes

_From #36_

Implement Kotlin Maven Plugin Extension

---

#### 🚀 Enhancement

- [PLAYA-7426] Implement Kotlin Maven Plugin Extension [#36](https://github.intuit.com/player/hooks/pull/36) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 1

- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.5.0 (Wed Jan 06 2021)

#### 🚀 Enhancement

- repo renaming to hooks [#37](https://github.intuit.com/player/hooks/pull/37) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 1

- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.4.0 (Wed Jan 06 2021)

### Release Notes

_From #31_

#### Gradle Plugin

Implement Gradle plugin for the compiler plugin. When added to a project, it will automatically include the correct dependencies.

#### Renaming

Remove all `tapable` references from the package name.

---

#### 🚀 Enhancement

- [PLAYA-7369] Gradle plugin wrapper [#31](https://github.intuit.com/player/tapable-kt/pull/31) ([@JZUCKER](https://github.intuit.com/JZUCKER) [@dstone3](https://github.intuit.com/dstone3))

#### Authors: 2

- David Stone ([@dstone3](https://github.intuit.com/dstone3))
- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.3.0 (Tue Jan 05 2021)

### Release Notes

_From #33_



---

#### 🚀 Enhancement

- Upgrade Kotlin and Gradle stuff [#34](https://github.intuit.com/player/tapable-kt/pull/34) ([@dstone3](https://github.intuit.com/dstone3))

#### 🐛 Bug Fix

- Better name + some README [#33](https://github.intuit.com/player/tapable-kt/pull/33) ([@dstone3](https://github.intuit.com/dstone3))
- Add MIT LICENSE [#30](https://github.intuit.com/player/tapable-kt/pull/30) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### 📝 Documentation

- Open Source Readiness [#27](https://github.intuit.com/player/tapable-kt/pull/27) ([@JZUCKER](https://github.intuit.com/JZUCKER))
- InnerSource Readiness [#25](https://github.intuit.com/player/tapable-kt/pull/25) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 2

- David Stone ([@dstone3](https://github.intuit.com/dstone3))
- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.2.0 (Fri Nov 13 2020)

#### 🚀 Enhancement

- Better compiler errors [#20](https://github.intuit.com/player/tapable-kt/pull/20) ([@dstone3](https://github.intuit.com/dstone3))

#### Authors: 1

- David Stone ([@dstone3](https://github.intuit.com/dstone3))

---

# v0.1.4 (Fri Nov 13 2020)

#### 🐛 Bug Fix

- fix publishing [#21](https://github.intuit.com/player/tapable-kt/pull/21) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 1

- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.1.3 (Tue Nov 10 2020)

#### 🐛 Bug Fix

- Break out generators [#19](https://github.intuit.com/player/tapable-kt/pull/19) ([@dstone3](https://github.intuit.com/dstone3))

#### Authors: 1

- David Stone ([@dstone3](https://github.intuit.com/dstone3))

---

# v0.1.2 (Tue Nov 10 2020)

#### 🐛 Bug Fix

- Add a validation phase for hooks [#13](https://github.intuit.com/player/tapable-kt/pull/13) ([@dstone3](https://github.intuit.com/dstone3))

#### Authors: 1

- David Stone ([@dstone3](https://github.intuit.com/dstone3))

---

# v0.1.1 (Mon Nov 09 2020)

#### 🐛 Bug Fix

- Extract test dependencies [#17](https://github.intuit.com/player/tapable-kt/pull/17) ([@dstone3](https://github.intuit.com/dstone3))

#### Authors: 1

- David Stone ([@dstone3](https://github.intuit.com/dstone3))

---

# v0.1.0 (Mon Nov 09 2020)

#### 🚀 Enhancement

- Bail Result [#16](https://github.intuit.com/player/tapable-kt/pull/16) ([@JZUCKER](https://github.intuit.com/JZUCKER))

#### Authors: 1

- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))

---

# v0.0.2 (Fri Nov 06 2020)

### Release Notes

_From #7_

First published alpha release of Intuit Tapable for the JVM. This includes the complete set of working hooks, as well as a compiler plugin to help generate specific hook implementations. This project as a whole is based on Webpack's [Tapable](https://github.com/webpack/tapable) for JS.

---

#### 🐛 Bug Fix

- Initial build & release [#7](https://github.intuit.com/player/tapable-kt/pull/7) ([@JZUCKER](https://github.intuit.com/JZUCKER))
- Fix the suspend function and add back the convenience tap [#6](https://github.intuit.com/player/tapable-kt/pull/6) ([@dstone3](https://github.intuit.com/dstone3))
- Implement the compiler plugin [#5](https://github.intuit.com/player/tapable-kt/pull/5) ([@JZUCKER](https://github.intuit.com/JZUCKER) [@dstone3](https://github.intuit.com/dstone3))
- Add Async Parallel and Bail [#4](https://github.intuit.com/player/tapable-kt/pull/4) ([@dstone3](https://github.intuit.com/dstone3))
- Some cleanup [#3](https://github.intuit.com/player/tapable-kt/pull/3) ([@dstone3](https://github.intuit.com/dstone3))
- Sync Hooks + Interceptors [#2](https://github.intuit.com/player/tapable-kt/pull/2) ([@dstone3](https://github.intuit.com/dstone3))
- Basic sync hook [#1](https://github.intuit.com/player/tapable-kt/pull/1) ([@dstone3](https://github.intuit.com/dstone3) [@JZUCKER](https://github.intuit.com/JZUCKER))

#### ⚠️ Pushed to `master`

- It helps to check in the gradle wrapper ([@dstone3](https://github.intuit.com/dstone3))
- Update README.md ([@dstone3](https://github.intuit.com/dstone3))
- Initial commit ([@dstone3](https://github.intuit.com/dstone3))

#### Authors: 2

- David Stone ([@dstone3](https://github.intuit.com/dstone3))
- Jeremiah Zucker ([@JZUCKER](https://github.intuit.com/JZUCKER))
