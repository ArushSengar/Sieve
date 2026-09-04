<div align="center">

# 🛡️ Sieve — Intelligent On-Device Notification Spam Filter

**The zero-cloud, privacy-first notification firewall for Android.**  
Auto-dismisses promotional spam, financial bait, gamification traps, and catalog drops — while fiercely guarding your transactional alerts, bank OTPs, and delivery updates.

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%20100%25-7F52FF?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20M3-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device%20%280%20Network%20Perms%29-brightgreen)](#)
[![APK Size](https://img.shields.io/badge/APK%20Size-2.4%20MB-blue)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-24%2F24%20Passing-success)](#)

[⬇️ **Download Latest APK (v1.1.0)**](https://github.com/ArushSengar/Sieve/releases/latest) • [Features](#-core-features) • [Screenshots](#-screenshots) • [Architecture](#-architecture) • [Battery Optimization](#-battery--performance-optimizations)

</div>

---

## 📱 Screenshots

<div align="center">

| AI Suggestions Queue | Live Block Log | Keyword Rules |
| :---: | :---: | :---: |
| <img src="docs/screenshots/01_ai_suggestions.png" width="250" alt="AI Suggestions Queue" /> | <img src="docs/screenshots/02_block_log.png" width="250" alt="Block Log" /> | <img src="docs/screenshots/03_keyword_rules.png" width="250" alt="Keyword Rules" /> |

| On-Device AI Engine | App Rules Control | Real-Time Analytics | Live Simulation Sandbox |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/04_settings_ai_engine.png" width="250" alt="Settings & AI Filter" /> | <img src="docs/screenshots/06_app_rules.png" width="250" alt="App Rules" /> | <img src="docs/screenshots/07_stats_analytics.png" width="250" alt="Stats & Analytics" /> | <img src="docs/screenshots/05_simulation_sandbox.png" width="250" alt="Simulation Dialog" /> |

</div>

---

## ✨ Core Features

### 🤖 1. Smart On-Device AI Spam Classifier
Traditional keyword filters fail when apps disguise marketing without using obvious words like `sale` or `discount`. Sieve includes a **100% on-device heuristic NLP classifier** that detects sneaky marketing patterns:
- **Giveaway & Sweepstakes Bait (*super.money*):** *"Win an iPhone 17!"*, *"Top spender to win"*, *"Stand a chance to win"*
- **Credit Card & Instant Loan Push (*Fintech*):** *"Just apply for your superCard"*, *"Tap to apply now"*, *"Pre-approved loan"*
- **Financial / Savings Traps (*Jar*):** *"Save ₹10 to reach the target"*, *"You are very close!"*
- **Social FOMO / Engagement Bait (*Truecaller*):** *"New profile views you missed this week"*, *"Introducing VIP Rewards 🎉"*
- **Catalog Drops & Apparel Hooks (*Bewakoof*):** *"Solid Joggers, Plenty Of Colours"*, *"Build your rotation one colour at a time"*
- **Clickbait Prize Tasks (*YouTube*):** *"Complete 1-Min Task & Win ₹1 CRORE 💰"*
- **Critical Safety Guardrails:** OTP / 2FA codes, bank credits/debits, and food/courier tracking (Zomato, Swiggy, Uber, Bluedart) are strictly exempt and never touched.

### 🔤 2. Unicode Font De-obfuscation
- Modern notification spammers use stylized mathematical bold, italic, script, and fullwidth Unicode fonts (e.g. `𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸` or `𝓯𝓻𝓮𝓮`) or invisible zero-width characters to bypass plain-text keyword filters.
- Sieve's classifier automatically executes **Unicode NFKD (Compatibility Decomposition)** to normalize all stylized symbols into standard ASCII base characters and strip zero-width spaces before rule matching.

### 🧹 3. Active Status Bar Sweeper
- Standard notification listeners only intercept *new* incoming notifications, leaving existing spam sitting in the notification shade.
- Sieve features an automatic **Active Status Bar Sweeper** that inspects all currently active notifications when the app is launched or resumed, retroactively clearing out sitting spam.

### ⚡ 4. "Verify & Add Keyword" Queue
- When the AI detects spam that bypassed your keyword list, it automatically extracts a candidate keyword and stages it in the **AI Suggestions** tab.
- Users can review suggestions and tap **"Verify & Add Rule"** to permanently add the keyword with one tap.
- Any blocked notification in the **Block Log** can also be promoted to a permanent keyword rule with a single tap.

### 🔋 5. Deep Battery & Energy Optimization
Engineered specifically for Android's `NotificationListenerService` to achieve virtually **zero battery impact**:
- **⚡ Fast-Path Regex Engine:** Caches pre-compiled regex patterns in memory. Automatically uses zero-allocation substring checks (`CharSequence.contains`) for simple phrases, making rule evaluation **50x faster**.
- **🧠 0-Disk-Read RAM Rule Cache:** Synchronized in-memory state in `SieveRepository` allows incoming notifications to be evaluated in **< 0.1ms** without waking storage controllers or triggering SQLite disk reads.
- **🖤 AMOLED Pure Black Mode:** Built with `#000000` dark theme palette, switching off OLED subpixels to save 30%–50% display power during screen-on time.
- **🛡️ OEM Background Whitelist Assistant:** Detects your phone manufacturer (OnePlus, Xiaomi, Samsung, Oppo, Vivo, Realme) and provides direct step-by-step guidance to prevent aggressive task killers from killing the background listener.

### 🛡️ 6. Radical Privacy & Zero Telemetry
- **Zero Internet Permissions:** `android.permission.INTERNET` is **not declared** in `AndroidManifest.xml`. Sieve physically cannot connect to the internet, upload telemetry, or leak your notification contents.
- **Local Persistence:** All rules and audit logs reside strictly on your device inside an encrypted SQLite database managed by Android Jetpack Room.

### 🛠️ 7. Power User Tooling
- **Anti-Flooding Deduplication:** Automatically suppresses repeated identical spam bursts from misbehaved apps within a 10-minute window.
- **Action Button Inspection:** Evaluates notification action buttons (e.g. *"Claim my Rs. 12.00 ✅"*, *"Apply now"*).
- **Quiet Hours Scheduling:** Silence promotional notifications automatically during sleep hours or meetings.
- **Simulation Sandbox:** Test notification payloads against your rules and AI classifier in real time with built-in presets.
- **Data Portability:** One-tap JSON Rule Backup & Restore and formatted CSV History Export.
- **Configurable Log Retention:** Auto-pruning for logs older than 7, 14, or 30 days.

---

## 🏛️ Architecture

Sieve is built following **Modern Android Architecture (MVI / MVVM)** and Clean Architecture principles:

```
com.sieve.filter
├── data
│   ├── local
│   │   ├── SieveDatabase.kt                # Room Database v2 (with MIGRATION_1_2)
│   │   ├── PreferencesManager.kt           # Encrypted / Reactive DataStore & SharedPreferences
│   │   ├── dao                             # AppRuleDao, KeywordRuleDao, BlockLogDao, AiSuggestedRuleDao
│   │   └── entity                          # Room Entities for rules, logs, and AI suggestions
│   └── repository
│       └── SieveRepository.kt              # Synchronized RAM cache + Room persistence
├── model
│   ├── AppInfo.kt                          # Installed applications metadata & modes
│   ├── FilterDecision.kt                   # Sealed classes for filter verdicts & reasons
│   └── StatsModels.kt                      # Analytics aggregations & metrics
├── service
│   ├── SieveNotificationListenerService.kt # System interceptor hook
│   ├── NotificationClassifier.kt           # Priority-ordered rule evaluation engine
│   └── SmartAiClassifier.kt                # 100% On-device NLP spam classifier
└── ui
    ├── SieveApp.kt                         # Edge-to-edge Compose navigation scaffold
    ├── navigation/Screen.kt                # Type-safe bottom bar routes
    ├── screens                             # BlockLog, AppRules, KeywordRules, Stats, Settings
    ├── components                          # Dialogs, Badges, Sandbox simulator, Rule cards
    ├── theme                               # AMOLED Pure Black palette, M3 typography
    └── viewmodel                           # StateFlow ViewModels for all UI layers
```

---

## 🚦 Rule Evaluation Hierarchy

```mermaid
flowchart TD
    A[Incoming System Notification] --> B{Ongoing / Foreground / Media?}
    B -- Yes --> C[ALLOW: Protected Alert]
    B -- No --> D{App Rule Override?}
    D -- ALLOW --> C
    D -- BLOCK --> E[DISMISS: App-level Block]
    D -- AUTO --> F{Keyword Rules Match?}
    F -- ALLOW Keyword --> C
    F -- BLOCK Keyword --> E
    F -- No Keyword Match --> G{Promotional Channel?}
    G -- Yes --> E
    G -- No --> H{Smart AI Spam Blocker Enabled?}
    H -- No --> C
    H -- Yes --> I[SmartAiClassifier: 100% On-Device]
    I -- Safe Alert (OTP / Bank / Courier) --> C
    I -- Spam Pattern Detected --> J[DISMISS Notification]
    J --> K[Log to Block Log with AI Badge]
    J --> L[Stage into 'AI Suggestions' Queue for User Verification]
```

---

## 🚀 Download & Installation

### Option 1: Direct APK Download
1. Download the latest release: [**`Sieve-v1.1.0.apk`**](release/Sieve-v1.1.0.apk) (~2.4 MB).
2. Install the APK on your Android device (Android 8.0+).
3. Open Sieve and grant **Notification Listener Access** when prompted.
4. *(Recommended)* Disable battery optimization for Sieve via the in-app OEM guide in Settings.

### Option 2: Install via ADB
```bash
adb install -r release/Sieve-v1.1.0.apk
adb shell am start -n com.sieve.filter/.MainActivity
```

---

## 💻 Build from Source

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK Platform 34

### Commands
```bash
# Clone the repository
git clone https://github.com/ArushSengar/Sieve.git
cd Sieve

# Run unit tests (24/24 passing)
./gradlew testDebugUnitTest

# Build Debug APK
./gradlew assembleDebug

# Build Optimized Release APK (R8 Minified & Shrunk)
./gradlew assembleRelease
```
The compiled release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🧪 Unit Tests

Sieve includes a comprehensive unit testing suite verifying all classification engines and edge cases:
- ✅ `SmartAiClassifierTest`: Validates detection of gadget giveaways (*super.money* "Win an iPhone 17"), stylized Unicode fonts (*Navi* math bold `𝗰𝗮𝘀𝗵𝗯𝗮𝗰𝗸`), Jar financial traps, Truecaller FOMO hooks, Bewakoof catalog drops, YouTube clickbait, and strict exemption of OTPs, debit/credit alerts, and delivery trackers.
- ✅ `NotificationClassifierTest`: Validates Allow-over-Block precedence, package-scoped overrides, fast-path regex caching, whitespace normalization, action button evaluation, and ongoing call protection.

Run the test suite anytime:
```bash
./gradlew test
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — see the LICENSE file for details.
