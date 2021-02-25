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

<a href="https://github.intuit.com/player/hooks/search?l=kotlin"><img src="https://img.shields.io/github/languages/top/intuit/hooks.svg?logo=Kotlin&logoColor=orange" alt="GitHub top language" /></a>
<a href="https://github.com/intuit/auto"><img src="https://img.shields.io/badge/release-auto.svg?colorA=888888&colorB=9B065A&label=auto&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAACzElEQVR4AYXBW2iVBQAA4O+/nLlLO9NM7JSXasko2ASZMaKyhRKEDH2ohxHVWy6EiIiiLOgiZG9CtdgG0VNQoJEXRogVgZYylI1skiKVITPTTtnv3M7+v8UvnG3M+r7APLIRxStn69qzqeBBrMYyBDiL4SD0VeFmRwtrkrI5IjP0F7rjzrSjvbTqwubiLZffySrhRrSghBJa8EBYY0NyLJt8bDBOtzbEY72TldQ1kRm6otana8JK3/kzN/3V/NBPU6HsNnNlZAz/ukOalb0RBJKeQnykd7LiX5Fp/YXuQlfUuhXbg8Di5GL9jbXFq/tLa86PpxPhAPrwCYaiorS8L/uuPJh1hZFbcR8mewrx0d7JShr3F7pNW4vX0GRakKWVk7taDq7uPvFWw8YkMcPVb+vfvfRZ1i7zqFwjtmFouL72y6C/0L0Ie3GvaQXRyYVB3YZNE32/+A/D9bVLcRB3yw3hkRCdaDUtFl6Ykr20aaLvKoqIXUdbMj6GFzAmdxfWx9iIRrkDr1f27cFONGMUo/gRI/jNbIMYxJOoR1cY0OGaVPb5z9mlKbyJP/EsdmIXvsFmM7Ql42nEblX3xI1BbYbTkXCqRnxUbgzPo4T7sQBNeBG7zbAiDI8nWfZDhQWYCG4PFr+HMBQ6l5VPJybeRyJXwsdYJ/cRnlJV0yB4ZlUYtFQIkMZnst8fRrPcKezHCblz2IInMIkPzbbyb9mW42nWInc2xmE0y61AJ06oGsXL5rcOK1UdCbEXiVwNXsEy/6+EbaiVG8eeEAfxvaoSBnCH61uOD7BS1Ul8ESHBKWxCrdyd6EYNKihgEVrwOAbQruoytuBYIFfAc3gVN6iawhjKyNCEpYhVJXgbOzARyaU4hCtYizq5EI1YgiUoIlT1B7ZjByqmRWYbwtdYjoWoN7+LOIQefIqKawLzK6ID69GGpQgwhhEcwGGUzfEPAiPqsCXadFsAAAAASUVORK5CYII=" alt="Auto Release" /></a>
<a href="https://build.intuit.com/fuego-player/job/player/job/hooks/job/master/"><img src="https://build.intuit.com/fuego-player/buildStatus/buildIcon?job=player/hooks/master" alt="Build Status" /></a>
<a href="https://build.intuit.com/fuego-player/job/player/job/hooks/job/master/"><img src="https://build.intuit.com/fuego-player/buildStatus/buildDescriptionIcon?job=player/hooks/master" alt="Description" /></a>
<a href="https://build.intuit.com/fuego-player/job/player/job/hooks/job/master/"><img src="https://build.intuit.com/fuego-player/buildStatus/coverageIcon?job=player/hooks/master" alt="Code Coverage" /></a>
<a href="https://build.intuit.com/fuego-player/job/player/job/hooks/job/master/"><img src="https://build.intuit.com/fuego-player/buildStatus/testIcon?job=player/hooks/master" alt="Test Status" /></a>
<a href="https://intuit-teams.slack.com/archives/C01DA2M0Z0B"><img src="https://img.shields.io/badge/slack-join%20the%20conversation-ff69b4.svg" alt="Slack" /></a>
<a href="https://ktlint.gihub.io"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="KtLint" /></a><!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all_contributors-2-orange.svg" alt="All Contributors" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</div>

<br />

Hooks represent "pluggable" points in a software model. They provide a mechanism for tapping into such points to get updates, or apply additional functionality to some typed object. Included in the hooks library are:
- A variety of hooks to support different plugin behavior: `Basic, Waterfall, Bail, Loop`
- Asynchronous support built on Kotlin [coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- Support for additional [hook context](TODO) and [interceptors](TODO)

Along with the base library, we created a Kotlin compiler plugin to enable hooks to be created with a simple typed-based DSL, limiting the redundancy and overhead required to subclass a hook.

Visit our [site](https://github.intuit.com/pages/player/hooks) for information about how to use hooks.

## Inspiration

At Intuit, we're big fans of [tapable](https://github.com/webpack/tapable). We use it in some of our core systems to
enable teams to augment and extend our frameworks to solve their customer problems. Since our backend systems are
primarily JVM-based, we really missed tapable when working in service code. Hooks is our implementation of tapable as a
library for the JVM plus an [Arrow Meta](https://meta.arrow-kt.io/) Compiler Plugin to make it easier to use.

## Structure

- [hooks](https://github.intuit.com/player/hooks/tree/master/hooks) - The actual implementation of the hooks
- [compiler-plugin](https://github.intuit.com/player/hooks/tree/master/compiler-plugin) - An Arrow Meta compiler plugin that generates hook subclasses for you
- [gradle-plugin](https://github.intuit.com/player/hooks/tree/master/gradle-plugin) - A gradle plugin to make using the compiler plugin easier
- [maven-plugin](https://github.intuit.com/player/hooks/tree/master/maven-plugin) - A maven Kotlin plugin extension to make using the compiler plugin easier
- [example-library](https://github.intuit.com/player/hooks/tree/master/example-library) - A library that exposes extension points for consumers using the hooks' `call` function
- [example-application](https://github.intuit.com/player/hooks/tree/master/example-application) - The Application that demonstrates extending a library by calling the hooks' `tap` function

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

Linting is done with [ktlint](https://github.com/pinterest/ktlint) and configured using [/JLLeitschuh's ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle).

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
    <td align="center"><a href="https://github.intuit.com/dstone3"><img src="https://github.intuit.com/avatars/u/461??s=100" width="100px;" alt=""/><br /><sub><b>David Stone</b></sub></a><br /><a href="https://github.intuit.com/player/hooks/commits?author=dstone3" title="Documentation">📖</a> <a href="https://github.intuit.com/player/hooks/commits?author=dstone3" title="Tests">⚠️</a> <a href="https://github.intuit.com/player/hooks/commits?author=dstone3" title="Code">💻</a></td>
    <td align="center"><a href="https://github.intuit.com/JZUCKER"><img src="https://github.intuit.com/avatars/u/9138??s=100" width="100px;" alt=""/><br /><sub><b>Jeremiah Zucker</b></sub></a><br /><a href="https://github.intuit.com/player/hooks/commits?author=JZUCKER" title="Tests">⚠️</a> <a href="https://github.intuit.com/player/hooks/commits?author=JZUCKER" title="Code">💻</a> <a href="https://github.intuit.com/player/hooks/commits?author=JZUCKER" title="Documentation">📖</a> <a href="#infra-JZUCKER" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## License

[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B23410%2Fgit%40github.intuit.com%3Aplayer%2Fhooks.svg?type=large)](https://app.fossa.com/projects/custom%2B23410%2Fgit%40github.intuit.com%3Aplayer%2Fhooks?ref=badge_large)