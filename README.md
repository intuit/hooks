<div align="center">
  <img
    src="docs/src/orchid/resources/assets/media/hooks_multi-3d-white-bg.svg"
    alt="Hooks Logo"
    width="300px"
    padding="40px"
  />
  <br />
  <br />
  <p>Hooks is a little module for plugins, in Kotlin</p>
</div>

---

<div align="center">
<a href="https://github.com/intuit/auto"><img src="https://img.shields.io/badge/release-auto.svg?colorA=888888&colorB=9B065A&label=auto&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAACzElEQVR4AYXBW2iVBQAA4O+/nLlLO9NM7JSXasko2ASZMaKyhRKEDH2ohxHVWy6EiIiiLOgiZG9CtdgG0VNQoJEXRogVgZYylI1skiKVITPTTtnv3M7+v8UvnG3M+r7APLIRxStn69qzqeBBrMYyBDiL4SD0VeFmRwtrkrI5IjP0F7rjzrSjvbTqwubiLZffySrhRrSghBJa8EBYY0NyLJt8bDBOtzbEY72TldQ1kRm6otana8JK3/kzN/3V/NBPU6HsNnNlZAz/ukOalb0RBJKeQnykd7LiX5Fp/YXuQlfUuhXbg8Di5GL9jbXFq/tLa86PpxPhAPrwCYaiorS8L/uuPJh1hZFbcR8mewrx0d7JShr3F7pNW4vX0GRakKWVk7taDq7uPvFWw8YkMcPVb+vfvfRZ1i7zqFwjtmFouL72y6C/0L0Ie3GvaQXRyYVB3YZNE32/+A/D9bVLcRB3yw3hkRCdaDUtFl6Ykr20aaLvKoqIXUdbMj6GFzAmdxfWx9iIRrkDr1f27cFONGMUo/gRI/jNbIMYxJOoR1cY0OGaVPb5z9mlKbyJP/EsdmIXvsFmM7Ql42nEblX3xI1BbYbTkXCqRnxUbgzPo4T7sQBNeBG7zbAiDI8nWfZDhQWYCG4PFr+HMBQ6l5VPJybeRyJXwsdYJ/cRnlJV0yB4ZlUYtFQIkMZnst8fRrPcKezHCblz2IInMIkPzbbyb9mW42nWInc2xmE0y61AJ06oGsXL5rcOK1UdCbEXiVwNXsEy/6+EbaiVG8eeEAfxvaoSBnCH61uOD7BS1Ul8ESHBKWxCrdyd6EYNKihgEVrwOAbQruoytuBYIFfAc3gVN6iawhjKyNCEpYhVJXgbOzARyaU4hCtYizq5EI1YgiUoIlT1B7ZjByqmRWYbwtdYjoWoN7+LOIQefIqKawLzK6ID69GGpQgwhhEcwGGUzfEPAiPqsCXadFsAAAAASUVORK5CYII=" alt="Auto Release" /></a>
<a href="https://circleci.com/gh/intuit/hooks"><img src="https://img.shields.io/circleci/project/github/intuit/hooks/main.svg?logo=circleci" alt="CircleCI" /></a>
<a href="https://maven-badges.herokuapp.com/maven-central/com.intuit.hooks/hooks"><img src="https://maven-badges.herokuapp.com/maven-central/com.intuit.hooks/hooks/badge.svg" alt="Maven Central" /></a>
<a href="https://plugins.gradle.org/plugin/com.intuit.hooks"><img src="https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/intui/hooks/com.intuit.hooks.gradle-plugin/maven-metadata.xml.svg?colorB=007ec6&label=gradlePluginPortal" alt="Gradle Plugin Portal" /></a>
<a href="https://app.fossa.com/projects/custom%2B23410%2Fgit%40github.com%3Aintuit%2Fhooks?ref=badge_shield" alt="FOSSA Status"><img src="https://app.fossa.com/api/projects/custom%2B23410%2Fgit%40github.com%3Aintuit%2Fhooks.svg?type=shield"/></a>
<a href="https://github.com/intuit/hooks/search?l=kotlin"><img src="https://img.shields.io/github/languages/top/intuit/hooks.svg?logo=Kotlin&logoColor=orange" alt="GitHub top language" /></a>
<a href="https://ktlint.gihub.io"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="KtLint" /></a><!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all_contributors-4-orange.svg" alt="All Contributors" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</div>

<br />

Hooks represent "pluggable" points in a software model. They provide a mechanism for tapping into such points to get updates, or apply additional functionality to some typed object. Included in the hooks library are:
- A variety of hooks to support different plugin behavior: `Basic, Waterfall, Bail, Loop`
- Asynchronous support built on Kotlin [coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- Support for additional [hook context](https://intuit.github.io/hooks/wiki/key-concepts/#hook-context) and [interceptors](https://intuit.github.io/hooks/wiki/key-concepts/#interceptors)

Along with the base library, we created a Kotlin compiler plugin to enable hooks to be created with a simple typed-based DSL, limiting the redundancy and overhead required to subclass a hook.

Visit our [site](https://intuit.github.io/hooks/) for information about how to use hooks.

## Inspiration

At Intuit, we're big fans of [tapable](https://github.com/webpack/tapable). We use it in some of our core systems to
enable teams to augment and extend our frameworks to solve their customer problems. Since our backend systems are
primarily JVM-based, we really missed tapable when working in service code. Hooks is our implementation of tapable as a
library for the JVM plus an [Arrow Meta](https://meta.arrow-kt.io/) Compiler Plugin to make it easier to use.

## Structure

- [hooks](https://github.com/intuit/hooks/tree/master/hooks) - The actual implementation of the hooks
- [compiler-plugin](https://github.com/intuit/hooks/tree/master/compiler-plugin) - An Arrow Meta compiler plugin that generates hook subclasses for you
- [gradle-plugin](https://github.com/intuit/hooks/tree/master/gradle-plugin) - A gradle plugin to make using the compiler plugin easier
- [maven-plugin](https://github.com/intuit/hooks/tree/master/maven-plugin) - A maven Kotlin plugin extension to make using the compiler plugin easier
- [example-library](https://github.com/intuit/hooks/tree/master/example-library) - A library that exposes extension points for consumers using the hooks' `call` function
- [example-application](https://github.com/intuit/hooks/tree/master/example-application) - The Application that demonstrates extending a library by calling the hooks' `tap` function

## :beers: Contributing :beers:

Feel free to make an [issue](https://github.com/intuit/hooks/issues) or open a [pull request](https://github.com/intuit/hooks/pulls) if you have an improvement and new plugin to propose!

Make sure to read our [code of conduct](./.github/CODE_OF_CONDUCT.md).

## :hammer: Start Developing :hammer:

To get set up, fork and clone the project.

### Build

Build and verify all checks:

```bash
./gradlew build
```

Publish locally to use in other projects:

```bash
./gradlew publishToMavenLocal
```

### Test

Recompile changes and run all tests:

```bash
./gradlew test
```

### Run example app

```bash
./gradlew run
```

### Cleaning

```bash
./gradlew clean
```

### Linting

Linting is done with [ktlint](https://github.com/pinterest/ktlint) and configured using [JLLeitschuh's ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle).

Format code according to linting standards:

```bash
./gradlew ktlintFormat
```

Verify code meets linting standards:

```bash
./gradlew ktlintCheck
```

### API Validation

To ensure that binary compatibility is maintained across non-breaking releases, the public API is validated using the Kotlin [binary compatibility validator](https://github.com/Kotlin/binary-compatibility-validator) tool.

Update the API dumps:

```bash
./gradlew apiDump
```

Verify the public API matches the API dumps:

```bash
./gradlew apiCheck
```

### Documentation

The docs site is built using the [Orchid](https://orchid.run/) tool and takes inspiration from the [stikt.io docs site](https://strikt.io/).

Run the docs locally:

```bash
./gradlew orchidServe
```

The [knit](https://github.com/Kotlin/kotlinx-knit) tool is also used to generate tests driven from markdown snippets to ensure documentation is maintained and up-to-date.

Update all generated markdown tests:

```bash
./gradlew knit
```

Verify the generated tests match the latest markdown changes:

```bash
./gradlew knitCheck
```

### Versioning

This project follows the [semantic versioning](https://semver.org/) strategy and uses [Auto](https://intuit.github.io/auto/) to automate releases on CI. PRs must be labeled with an appropriate Auto label to denote what type of release should occur when merged. With the binary compatibility validator tool, we can follow this set of rules to determine release types:

- Red on an API diff is breaking and requires a major bump
- Green on an API diff is a new feature and requires a minor bump
- No API diff, but has a code change requires a patch bump
- Else, apply the corresponding label for documentation or build, etc.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="http://www.jeremiahzucker.com"><img src="https://avatars1.githubusercontent.com/u/9255651?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Jeremiah Zucker</b></sub></a><br /><a href="https://github.com/intuit/hooks/commits?author=sugarmanz" title="Tests">⚠️</a> <a href="https://github.com/intuit/hooks/commits?author=sugarmanz" title="Code">💻</a> <a href="https://github.com/intuit/hooks/commits?author=sugarmanz" title="Documentation">📖</a> <a href="#infra-sugarmanz" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
    <td align="center"><a href="https://github.com/stabbylambda"><img src="https://avatars3.githubusercontent.com/u/124668?v=4?s=100" width="100px;" alt=""/><br /><sub><b>David Stone</b></sub></a><br /><a href="https://github.com/intuit/hooks/commits?author=stabbylambda" title="Documentation">📖</a> <a href="https://github.com/intuit/hooks/commits?author=stabbylambda" title="Tests">⚠️</a> <a href="https://github.com/intuit/hooks/commits?author=stabbylambda" title="Code">💻</a></td>
    <td align="center"><a href="http://hipstersmoothie.com/"><img src="https://avatars.githubusercontent.com/u/1192452?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Andrew Lisowski</b></sub></a><br /><a href="https://github.com/intuit/hooks/commits?author=hipstersmoothie" title="Documentation">📖</a> <a href="#infra-hipstersmoothie" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="https://github.com/intuit/hooks/commits?author=hipstersmoothie" title="Tests">⚠️</a> <a href="https://github.com/intuit/hooks/commits?author=hipstersmoothie" title="Code">💻</a></td>
    <td align="center"><a href="https://github.com/kharrop"><img src="https://avatars.githubusercontent.com/u/24794756?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Kelly Harrop</b></sub></a><br /><a href="#design-kharrop" title="Design">🎨</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## License

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B23410%2Fgit%40github.com%3Aintuit%2Fhooks.svg?type=large)](https://app.fossa.com/projects/custom%2B23410%2Fgit%40github.com%3Aintuit%2Fhooks?ref=badge_large)

This product includes software developed by the Apache Software Foundation (http://www.apache.org/).
