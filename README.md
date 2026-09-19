# Smart Calculator Pro

An offline Android calculator and everyday-tools app, built with Kotlin and Jetpack
Compose (Material 3).

The app requests **no permissions at all** — not even `INTERNET` — so it is incapable
of sending anything anywhere. Policy pages:
[Privacy Policy](docs/privacy_policy.html) · [Terms](docs/terms.html).

## The calculator

- **Full expression entry.** You type a whole expression and see it, rather than a
  running total that hides what you entered. A live preview of the result sits under
  the expression as you type.
- **Correct precedence** — `2 + 3 × 4` is 14, `2^3^2` is 512 (right-associative), and
  `-2^2` is -4.
- **Decimal arithmetic that behaves.** Calculations run on `BigDecimal` at 34
  significant digits and display 12, so `0.1 + 0.2` is `0.3`.
- **Scientific keypad** — trigonometry with a DEG/RAD toggle, inverse trig behind the
  `2nd` key, logarithms, roots, powers, factorial, `π`, `e`, and `ans`.
- **Percent that matches expectations**: `200 + 10%` is 220, while `200 × 10%` is 20.
- **History** that survives restarts; tap any entry to reuse its result.

## The other tools

| Tool | What it does |
| --- | --- |
| **Unit Converter** | Ten categories — length, mass, temperature, area, volume, speed, time, digital storage, energy, pressure |
| **Discount** | Sale price, stacked discounts, tax, and the *effective* discount |
| **Price & Bill** | Tax, tip (pre- or post-tax) and splitting between people |
| **Loan / EMI** | Monthly repayment, total interest, total payable |
| **Percentage** | X% of Y, X is what % of Y, and percentage change |
| **Date Calculator** | Difference between dates, and adding or subtracting time |
| **BMI** | Body mass index in metric or imperial, with healthy weight range |

Plus dynamic colour (Material You) on Android 12+, a light/dark/system theme setting,
landscape layout, and a themed launcher icon.

## Building

The Android SDK location is read from `local.properties`. To build a debug APK:

```bash
./gradlew assembleDebug
```

To run the engine's unit tests:

```bash
./gradlew testDebugUnitTest
```

The APK lands in `app/build/outputs/apk/debug/`.

## Publishing to Google Play

Play distributes App Bundles, not APKs:

```bash
./gradlew bundleRelease
```

The bundle lands in `app/build/outputs/bundle/release/app-release.aab`.

Play rejects anything signed with a debug certificate, so you need your own upload
key first. Create one (it is valid for ~27 years, which Play requires):

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -storetype PKCS12 -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Then copy `keystore.properties.example` to `keystore.properties` and fill in the
password and alias you chose. The build picks it up automatically; without it, the
release build falls back to the debug key and is **not** publishable.

Keep `upload-keystore.jks` and `keystore.properties` backed up and out of version
control — both are git-ignored.

## Layout

```
app/src/main/java/com/holymanzion/calculator/
├── core/          Calculation engine — pure Kotlin, no Android dependencies
│   ├── Tokenizer.kt          Text to tokens
│   ├── Parser.kt             Recursive-descent parser
│   ├── Evaluator.kt          Tree walker, BigDecimal arithmetic
│   ├── NumberFormatter.kt    Result rendering, rounding, grouping
│   └── Calculator.kt         Entry point
├── tools/         Converter and finance/date/health maths — also pure Kotlin
├── data/          History and settings persistence
└── ui/            Compose screens
    ├── components/           Shared tool widgets
    └── tools/                One screen per tool
app/src/main/assets/          Privacy policy and terms (bundled and published)
docs/                         GitHub Pages copy of the same documents
```

Neither `core/` nor `tools/` touches the Android framework, so all of their behaviour
is covered by plain JVM unit tests in `app/src/test/`.

Navigation is a flat `Destination` enum behind a drawer rather than a navigation
library — there are no routes or arguments to model.

`app/src/main/assets` is the single source of truth for the legal documents; a Gradle
`syncLegalDocs` task copies them into `docs/` on every build so the published pages
cannot drift from what the app shows.

## Notes

- `minSdk` is 24; `compileSdk`/`targetSdk` are 36.
- Core library desugaring is enabled so `java.time` works below API 26.
- The release build type falls back to the debug key when `keystore.properties` is
  absent, so `assembleRelease` always produces something installable. That artifact is
  **not** publishable — see above.
