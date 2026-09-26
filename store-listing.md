# Play Console submission — Smart Calculator Pro

Copy-paste material for the store listing, plus the answers to the declarations
Play requires before production. Everything here matches what the app actually does;
check it still does before you submit.

---

## App details

| Field | Value |
| --- | --- |
| App name (30 char max) | `Smart Calculator Pro` |
| Package | `com.holymanzion.calculator` |
| Version | 1.2 (versionCode 3) |
| Category | Tools |
| Tags | Calculator, Unit converter, Utilities |
| Contact email | holymanzion@gmail.com |
| Privacy policy URL | https://holymanzion.github.io/smart-calculator-pro/privacy_policy.html |

---

## Short description (80 characters max)

```
Calculator, unit converter and everyday tools. Fully offline, zero tracking.
```

*(75 characters.)*

---

## Full description (4000 characters max)

```
Smart Calculator Pro is a calculator and a box of everyday tools in one app — and it works entirely offline.

It requests no permissions at all. Not even internet access. Android itself blocks the app from sending anything anywhere, so your numbers stay on your phone.

A CALCULATOR THAT SHOWS ITS WORKING

Type a whole expression and see it, instead of a running total that hides what you entered. A live preview shows the result as you type.

• Correct order of operations — 2 + 3 × 4 is 14, not 20
• Decimal arithmetic that behaves: 0.1 + 0.2 is 0.3
• Scientific keypad — trigonometry with DEG/RAD, inverse functions, logarithms, roots, powers, factorial, π and e
• Percent that matches expectations: 200 + 10% is 220, while 200 × 10% is 20
• History that survives restarts — tap any entry to reuse its result

EIGHT TOOLS BEYOND THE KEYPAD

Unit Converter — ten categories covering length, weight, temperature, area, volume, speed, time, digital storage, energy and pressure. Decimal and binary storage units are listed separately, so you can see why a 1 TB drive shows up as 931 GB.

Discount — sale price, what you save, and the effective discount. Stacked discounts multiply rather than add: 20% off then a further 10% off is 28% off, not 30%.

Price & Bill — add tax and a tip, then split between any number of people. Tip is calculated on the pre-tax amount by default, so you are not tipping on the tax.

Loan / EMI — monthly repayment, total interest and total payable from the amount, rate and term.

Percentage — X% of Y, X is what percent of Y, percentage change, and increase or decrease by a percentage.

Date Calculator — days between two dates, add or subtract time from a date, and your exact age with a countdown to your next birthday.

BMI — body mass index in metric or imperial, with the healthy weight range for your height.

BUILT TO STAY OUT OF THE WAY

• Material You — the app picks up your wallpaper colours on Android 12 and above
• Light, dark, or follow the system
• Long-press any result to copy it
• Choose your currency symbol, or show none at all
• Landscape layout with the full scientific keypad

NO ADVERTISING. NO ANALYTICS. NO ACCOUNTS.

There is nothing to sign up for, nothing to subscribe to, and nothing watching what you calculate.
```

---

## Release notes (500 characters max)

**For the closed-testing / production release of v1.2:**

```
• Long-press any result to copy it
• Choose your currency symbol, or turn it off
• New: age calculator with a countdown to your next birthday
• New: increase or decrease a value by a percentage
• Results now follow your thousands-separator setting
```

---

## Data safety form

Play asks this in Policy → App content → Data safety. Answer:

| Question | Answer |
| --- | --- |
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | *(not asked once you answer No above)* |
| Do you provide a way for users to request that their data is deleted? | **No** — nothing is collected; local data is removed by clearing history or uninstalling |

This is defensible and easy to verify: the app declares **no permissions** in its
manifest, so it cannot reach the network at all.

---

## Content rating questionnaire

Category: **Utility, Productivity, Communication, or Other**

Answer **No** to every content question — violence, sexuality, language, controlled
substances, gambling, user-generated content, data sharing, location sharing and
in-app purchases. Expected outcome: rated for everyone (PEGI 3 / ESRB Everyone).

---

## App access

Select **All functionality is available without special access**. There is no login,
no paywall and no gated feature.

---

## Ads

Select **No, my app does not contain ads**.

---

## Target audience

Age groups: **13+** is the simplest honest answer. The app contains nothing aimed at
children and collects no data, but declaring a child audience pulls in the Families
policy and extra review for no benefit here.

---

## Graphics checklist

| Asset | Requirement | Status |
| --- | --- | --- |
| App icon | 512 × 512 PNG, 32-bit | `store-assets/icon-512.png` |
| Feature graphic | 1024 × 500 PNG or JPG | `store-assets/feature-graphic.png` |
| Phone screenshots | 2–8, min 320px, 16:9 or 9:16 | **You need to capture these** |
| Tablet screenshots | Optional | Not supplied |

Screenshots must show the real app. Capture them on your phone
(Power + Volume Down), or with the device connected:

```
adb exec-out screencap -p > screenshot.png
```

Good ones to include: the calculator with a worked expression, the unit converter,
the discount tool, the date/age tool, and the settings screen.

---

## Closed testing requirement

A new personal developer account must run a **closed test with at least 12 testers who
stay opted in for 14 continuous days** before applying for production access. Start
that clock as early as possible — it is the long pole, not the build.

1. Test and release → Testing → **Closed testing** → create a track
2. Add 12+ tester emails (they must be Google accounts, and each has to actually opt in)
3. Upload the v1.2 bundle and roll out
4. Wait 14 days with testers opted in
5. Apply for production access

Organisation accounts are exempt. Verify the current rule in Console, since Google
changes these periodically.
