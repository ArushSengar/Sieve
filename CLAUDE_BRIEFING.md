# 📱 Sieve — Master Technical Specification & Project Dossier (v1.4.0)

> **Document Purpose:** Complete architectural, functional, design, and code-level specification for **Sieve** as of version `v1.4.0` (**Cupertino Sentinel Edition**). This file is prepared for ingestion by **Claude AI** or any engineering team continuing development.

---

## 1. 🛡️ Executive Summary & Privacy Mission

* **Application Name:** Sieve (Notification Sentinel)
* **Application ID / Package:** `com.sieve.filter`
* **Version:** `1.4.0` (Version Code `5`)
* **Target SDK:** Android 14 (API 34) | **Min SDK:** Android 8.0 (API 26)
* **Repository:** [https://github.com/ArushSengar/Sieve](https://github.com/ArushSengar/Sieve)
* **Core Privacy Mandate:** **100% On-Device Heuristic Evaluation & Zero Telemetry**.
  * No third-party analytics (No Firebase, Mixpanel, Sentry, or Google Analytics).
  * No network requests are made during notification evaluation or filtering.
  * No user notification data or text leaves the device.
  * All logs, rules, and statistics are stored strictly in local Room SQLite storage.
* **Core Purpose:** Intercepts system notifications via Android’s `NotificationListenerService`, analyzes them in real-time through deterministic per-app rules, keyword/regex matching, and localized natural-language heuristics (`SmartAiClassifier`), cancels commercial and promotional spam, and logs blocked entries into an on-device SQLite database.

---

## 2. 🏗️ Technology Stack & Dependencies

| Layer | Technology | Version / Notes |
| :--- | :--- | :--- |
| **Language** | Kotlin | `1.9.22` (100% Kotlin codebase) |
| **UI Framework** | Jetpack Compose + Material 3 | Compose BOM `2024.02.00` |
| **Design Language** | Apple iOS 18 Cupertino Sentinel | Custom Compose design system (squircles, inset groups, physics switches, activity ring) |
| **Local Persistence** | Android Room SQLite | `2.6.1` with KSP compiler, Schema v2, migration from v1 |
| **Asynchronous Engine** | Kotlin Coroutines & Flow | `1.8.0`, Unidirectional Data Flow (StateFlow / Flow) |
| **Architecture** | MVVM + Clean Architecture | Repository pattern, ViewModels, DAO abstractions |
| **Build System** | Gradle | `8.2` with Android Gradle Plugin `8.2.2` |
| **Code Shrinking & R8** | ProGuard / R8 | Full minification enabled for release builds (`minifyEnabled = true`, `shrinkResources = true`) |
| **Release Artifact** | Universal Release APK | `release/Sieve-v1.4.0.apk` (~2.46 MB, 0 errors, 0 compiler warnings) |

---

## 3. 🔍 Complete Notification Interception Pipeline

When any application on the device posts a notification, the Android OS invokes `SieveNotificationListenerService.onNotificationPosted(sbn: StatusBarNotification)`. Sieve evaluates the notification through an 8-stage deterministic decision pipeline:

```
[Incoming StatusBarNotification]
             │
             ▼
[Stage 1: Master Killswitch]
  - Is master filter enabled in PreferencesManager? ─────────────► NO: ALLOW (Pass-through)
             │ YES
             ▼
[Stage 2: System, Ongoing & Sticky Protection]
  - Is it ongoing/sticky (media playback, timers, phone calls)? ──► YES: ALLOW (Pass-through)
  - Is it from system Android/SystemUI (system alerts, updates)? ─► YES: ALLOW (Pass-through)
             │ NO
             ▼
[Stage 3: Protected Communications & Banking Whitelist]
  - Package in PROTECTED_COMMUNICATION_PACKAGES? ────────────────► YES: ALLOW (Guaranteed)
    - WhatsApp (`com.whatsapp`)
    - Telegram (`org.telegram.messenger`)
    - Signal (`org.thoughtcrime.securesms`)
    - Google Messages & OEM SMS apps (OnePlus, Xiaomi, Oppo, Vivo, Samsung)
    - Google Phone, Truecaller & OEM Dialers
    - Top UPI/Banking Apps (Google Pay, PhonePe, Paytm, BHIM, CRED, YONO SBI, HDFC, ICICI, Axis, Kotak, PNB, etc.)
             │ NO
             ▼
[Stage 4: Guaranteed Safe Financial & Transaction Content]
  - SmartAiClassifier.isGuaranteedSafe(title, text, subText)? ────► YES: ALLOW (Guaranteed)
    - OTP / 2FA tokens ("otp is", "verification code", "do not share")
    - Bank account credits/debits ("paid you", "debited by", "credited with", "a/c ending")
    - Transit updates ("driver arriving", "captain on the way", "ride otp")
    - Food delivery ("out for delivery", "order confirmed", "order delivered")
    - Devanagari Hindi transaction alerts ("खाते से काटे गए", "खाते में जमा", "रुपये प्राप्त हुए")
             │ NO
             ▼
[Stage 5: User Explicit Per-App Rules]
  - User configured AppRuleMode == ALWAYS_ALLOW ──────────────────► ALLOW (User Rule)
  - User configured AppRuleMode == ALWAYS_BLOCK ──────────────────► DISMISS & LOG (User Rule)
  - User configured AppRuleMode == AUTO (Default)
             │
             ▼
[Stage 6: Keyword & Regex Rule Matching]
  - Evaluates package-scoped rules first, then global rules.
  - Matches rule with Action == ALLOW ────────────────────────────► ALLOW (Keyword Rule)
  - Matches rule with Action == BLOCK ────────────────────────────► DISMISS & LOG (Keyword Rule)
             │ NO MATCH
             ▼
[Stage 7: Smart AI Heuristic Classifier]
  - If isAiFilterEnabled is ON:
    - Evaluates SmartAiClassifier.classify(payload) across 8 categories:
      1. Financial & Loan Bait (Win iPhone, instant loan, cashback claim, lottery)
      2. Shopping & Flash Sales (Flat 80% off, BOGO, clearance, coupon codes)
      3. Food & Delivery Marketing ("Tummy is calling", hungry nudges)
      4. Gaming, Betting & Fantasy (Dream11, Ludo coins, rummy, jackpot, poker)
      5. Streaming & Media Teasers (Hotstar/Netflix clickbait teasers)
      6. Daily Engagement & Rewards (VIP reward invite, streak reminders)
      7. Crypto & Trading Traps (500% ROI, forex bot, binary trading)
      8. Devanagari Hindi / Hinglish Commercial Traps (कैशबैक, स्पिन, फ्री, जीतें, गुल्लक)
    - If isSpam == true ──────────────────────────────────────────► DISMISS & LOG (AI Heuristic)
      (Also records suggestion to `ai_suggested_rules` table for review)
             │ NOT SPAM
             ▼
[Stage 8: Anti-Flooding & Duplicate Suppression]
  - Computes MD5 hash of `packageName + title + text` over a 10-minute LRU window.
  - Guaranteed safe financial/OTP content is explicitly exempted from deduplication.
  - If duplicate notification detected ──────────────────────────► DISMISS & LOG (Anti-Flooding)
             │
             ▼
[DEFAULT DECISION: ALLOW & PASS-THROUGH]
```

---

## 4. 🔄 Background Persistence & Multi-Device Resilience

Aggressive Android OEM battery managers (Xiaomi HyperOS, OnePlus/Oppo ColorOS, Samsung One UI, Vivo Funtouch OS) frequently attempt to kill or silently disconnect background notification listeners. Sieve employs a 4-tier survival architecture:

1. **Reboot & Update Receiver (`BootReceiver.kt`)**:
   - Listens for `Intent.ACTION_BOOT_COMPLETED`, `Intent.ACTION_MY_PACKAGE_REPLACED`, and `android.intent.action.QUICKBOOT_POWERON`.
   - Fires `SieveNotificationListenerService.tryRebind(context)` automatically upon boot or APK update without requiring the user to open the app.
2. **`MainActivity.onResume()` Re-binding Hook**:
   - If the user returns from system Settings after granting permission, `onResume()` executes `tryRebind(this)` instantly.
3. **OEM Component Toggle Workaround**:
   - Programmatically cycles the component state:
     ```kotlin
     pm.setComponentEnabledSetting(component, COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP)
     pm.setComponentEnabledSetting(component, COMPONENT_ENABLED_STATE_ENABLED, DONT_KILL_APP)
     ```
   - This forces Android's internal `NotificationManagerService` to rebuild its binder connection to Sieve.
4. **Android 13/14+ Sideloading "Restricted setting" Helper**:
   - On Android 13/14+, sideloaded APKs have Notification Listener access disabled by default with a system error *"Restricted setting"*.
   - Sieve's [PermissionBanner.kt](file:///c:/Users/Arush/OneDrive/Documents/Sieve/app/src/main/java/com/sieve/filter/ui/components/PermissionBanner.kt) detects this and displays an in-app Cupertino modal guide with a 1-tap shortcut to `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` (App Info ➔ 3 dots ➔ "Allow restricted settings").
5. **In-App OEM Battery Optimization Guide**:
   - [SettingsScreen.kt](file:///c:/Users/Arush/OneDrive/Documents/Sieve/app/src/main/java/com/sieve/filter/ui/screens/SettingsScreen.kt) detects `Build.MANUFACTURER` and shows exact device-specific instructions for OnePlus/Oppo/Realme, Xiaomi/Redmi/Poco, Samsung, and Vivo/iQOO.

---

## 5. 🎨 Design System: Apple iOS 18 Cupertino Sentinel

The entire user interface is custom-built in Jetpack Compose to adhere strictly to the **Apple Human Interface Guidelines**:

### Design Tokens (`Color.kt`, `Theme.kt`, `Type.kt`)
* **Surfaces**:
  * AMOLED Pure Black: `#000000`
  * Obsidian Background: `#0A0A0C`
  * Cupertino Card Background: `#1C1C1E` (Apple Inset Group standard)
  * Cupertino Elevated Card: `#242426`
  * Cupertino Secondary Fill: `#2C2C2E`
  * Specular Hairline: `0.5.dp` border with `#38383A`
  * Hairline Separator: `#2C2C2E`
* **Semantic Accents**:
  * Apple Blue: `#0A84FF`
  * Apple Green: `#30D158`
  * Apple Red: `#FF453A`
  * Apple Orange: `#FF9F0A`
  * Apple Purple: `#BF5AF2`
  * Apple Text Primary: `#FFFFFF`
  * Apple Text Secondary: `#8E8E93`
  * Apple Text Tertiary: `#636366`

### Signature Components (`CupertinoComponents.kt`)
1. **`CupertinoSwitch`**:
   - 51dp × 31dp capsule track with 27dp circular floating white thumb.
   - Animated with spring physics (`dampingRatio = 0.8f`, `stiffness = 600f`).
2. **`CupertinoActivityRing`**:
   - Apple Fitness/Health styled circular progress indicator with rounded caps and centered metric typography.
3. **`CupertinoInsetGroup`**:
   - Inset grouped table container with 20dp squircles, hairline borders, section titles, and footnotes.
4. **`CupertinoSearchBar`**:
   - Rounded pill input field with clear button and real-time query filtering.
5. **`CupertinoSegmentedControl`**:
   - Capsule segmented switcher with smooth animated sliding thumb.

---

## 6. 📱 The 5 Core Application Screens

### 1. Activity / Sentinel Feed (`BlockLogScreen.kt`)
* **Hero Sentinel Card**: Live circular shield gauge displaying all-time blocked notifications, AI vs Keyword pill counters, and "Sentinel Active" pulse.
* **Filtering & Search**: Inset search bar + Cupertino segmented control (`All`, `AI`, `Keywords`, `Other`).
* **Live Feed**: Inset cards with high-resolution app icons, notification title, message snippet, relative timestamp (`"2 mins ago"`), rule badges (`AI: Shopping`, `Keyword: cashback`), and quick action buttons (**Always Allow** / **Verify Rule**).
* **Cupertino Detail Sheet**: Tapping any log opens a Cupertino modal displaying full notification title, full body, channel ID, exact matched rule, timestamp, and delete/whitelist buttons.
* **Simulate & Test**: Top button opens [SimulateNotificationDialog.kt](file:///c:/Users/Arush/OneDrive/Documents/Sieve/app/src/main/java/com/sieve/filter/ui/components/SimulateNotificationDialog.kt) allowing instant live testing against realistic presets (PhonePe UPI, HDFC OTP, WhatsApp, Zomato, Flipkart, Dream11, MoneyView loan).

### 2. Apps Control (`AppRulesScreen.kt`)
* **List Virtualization**: High-performance Compose `items()` lazy column ensuring silky-smooth 120 FPS scrolling across 150+ installed apps.
* **3-State Mode Selector**: Every installed app features a segmented selector:
  * **Auto** (Default): Evaluated against keyword rules and AI heuristics.
  * **Allow**: Whitelisted; all notifications pass through unfiltered.
  * **Block**: Silenced; all notifications from this app are blocked.
* **Search & Bulk Actions**: Real-time app search and quick stats.

### 3. Keyword & Regex Rules (`KeywordRulesScreen.kt`)
* **Dual Categories**: User-created custom rules and pre-seeded default system rules.
* **Rule Pills**: BLOCK (Red) and ALLOW (Green) tags with regex match indicators.
* **Creation Flow**: FAB opens [AddKeywordDialog.kt](file:///c:/Users/Arush/OneDrive/Documents/Sieve/app/src/main/java/com/sieve/filter/ui/components/AddKeywordDialog.kt) featuring live rule testing, preset hint chips, and Cupertino segmented controls.

### 4. Insights & Analytics (`StatsScreen.kt`)
* **Activity Ring**: Apple Health-inspired circular gauge tracking today's blocked notifications against a 20-notification daily target.
* **Metric Cards**: Squircle cards for `Today`, `This Week`, and `All Time`.
* **Top Spammers Ranking**: Bar chart ranking apps by intercepted volume (e.g., Flipkart, Jar, LinkedIn, Zomato).
* **Rule Distribution**: Breakdown of AI heuristic dismissals vs Keyword rule dismissals.

### 5. Settings (`SettingsScreen.kt`)
* **Sentinel Controls**: Master filtering toggle, Smart AI toggle, AMOLED Pure Black toggle.
* **In-App OEM Optimization Guide**: Interactive dialog with manufacturer-tailored guides for OnePlus/Oppo, Xiaomi/Poco, Samsung, and Vivo/iQOO.
* **Data & Rule Portability**:
  * **Export Rules**: Exports all app rules and keyword rules to a formatted JSON file.
  * **Import Rules**: Imports and merges rules from a JSON backup.
  * **Reset Defaults**: Re-populates default keyword seeds.
  * **Clear Block History**: Purges log records from SQLite.

---

## 7. 🗄️ Database Architecture & Local Storage

* **Database Name:** `sieve_database`
* **Version:** `2` (Migration 1→2 adds `ai_suggested_rules` table with status and keyword indexes).
* **Automatic Rule Synchronization:** On every app launch, `SieveRepository.syncUpgradedDefaultRules()` runs idempotently to merge new default keywords into existing installations without altering user rules.

### Table Schemas

#### 1. `app_rules`
```sql
CREATE TABLE app_rules (
    packageName TEXT PRIMARY KEY NOT NULL,
    mode TEXT NOT NULL -- 'AUTO', 'ALWAYS_ALLOW', 'ALWAYS_BLOCK'
);
```

#### 2. `keyword_rules`
```sql
CREATE TABLE keyword_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    packageName TEXT, -- NULL denotes global rule across all apps
    pattern TEXT NOT NULL,
    action TEXT NOT NULL -- 'BLOCK', 'ALLOW'
);
CREATE INDEX index_keyword_rules_packageName ON keyword_rules(packageName);
```

#### 3. `block_logs`
```sql
CREATE TABLE block_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    packageName TEXT NOT NULL,
    title TEXT,
    textSnippet TEXT,
    matchedRule TEXT NOT NULL,
    channelId TEXT,
    timestamp INTEGER NOT NULL
);
CREATE INDEX index_block_logs_timestamp ON block_logs(timestamp);
CREATE INDEX index_block_logs_packageName ON block_logs(packageName);
```

#### 4. `ai_suggested_rules`
```sql
CREATE TABLE ai_suggested_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    packageName TEXT NOT NULL,
    suggestedKeyword TEXT NOT NULL,
    category TEXT NOT NULL,
    sampleTitle TEXT,
    sampleText TEXT,
    status TEXT NOT NULL, -- 'PENDING', 'ACCEPTED', 'REJECTED'
    timestamp INTEGER NOT NULL
);
CREATE INDEX index_ai_suggested_rules_status ON ai_suggested_rules(status);
CREATE INDEX index_ai_suggested_rules_packageName_suggestedKeyword ON ai_suggested_rules(packageName, suggestedKeyword);
```

---

## 8. 📂 Full File Map & Source Directory

```
c:\Users\Arush\OneDrive\Documents\Sieve\
├── app\
│   ├── build.gradle.kts                    # App-level build config, dependencies, R8 ProGuard rules
│   └── src\
│       ├── main\
│       │   ├── AndroidManifest.xml         # Permissions, activity, service, BootReceiver registration
│       │   └── java\com\sieve\filter\
│       │       ├── MainActivity.kt         # Edge-to-edge Compose container, onResume rebind hook
│       │       ├── SieveApplication.kt     # Application class, repository init, default rule sync
│       │       │
│       │       ├── data\
│       │       │   ├── local\
│       │       │   │   ├── SieveDatabase.kt # Room database v2, migration 1->2, seed dictionaries
│       │       │   │   ├── dao\
│       │       │   │   │   ├── AppRuleDao.kt
│       │       │   │   │   ├── BlockLogDao.kt
│       │       │   │   │   ├── KeywordRuleDao.kt
│       │       │   │   │   └── AiSuggestedRuleDao.kt
│       │       │   │   └── entity\
│       │       │   │       ├── AppRuleEntity.kt
│       │       │   │       ├── BlockLogEntity.kt
│       │       │   │       ├── KeywordRuleEntity.kt
│       │       │   │       └── AiSuggestedRuleEntity.kt
│       │       │   ├── preferences\
│       │       │   │   └── PreferencesManager.kt # DataStore preferences (Master, AI, AMOLED, Dedup)
│       │       │   └── repository\
│       │       │       └── SieveRepository.kt    # Data orchestration, cache management, JSON export/import
│       │       │
│       │       ├── model\
│       │       │   ├── AppRuleMode.kt      # Enum: AUTO, ALWAYS_ALLOW, ALWAYS_BLOCK
│       │       │   ├── FilterDecision.kt   # Class: shouldDismiss, matchedRule, reason
│       │       │   └── RuleAction.kt       # Enum: BLOCK, ALLOW
│       │       │
│       │       ├── receiver\
│       │       │   └── BootReceiver.kt     # BroadcastReceiver for boot and package update re-binding
│       │       │
│       │       ├── service\
│       │       │   ├── SieveNotificationListenerService.kt # Notification interception, OEM workaround, LRU dedup
│       │       │   ├── NotificationClassifier.kt   # Regex & keyword matching engine
│       │       │   └── SmartAiClassifier.kt        # Multi-category heuristic spam detector & safety validator
│       │       │
│       │       └── ui\
│       │           ├── SieveApp.kt         # Root Scaffold & 5-tab Cupertino Bottom Navigation Bar
│       │           ├── components\
│       │           │   ├── CupertinoComponents.kt   # Switch, InsetGroup, SearchBar, Ring, Badges, Rows
│       │           │   ├── PermissionBanner.kt     # Onboarding warning + Android 13/14+ Restricted Setting Dialog
│       │           │   ├── SimulateNotificationDialog.kt # Live testing simulator with realistic test presets
│       │           │   └── AddKeywordDialog.kt     # Add rule modal with live tester and segmented controls
│       │           ├── screens\
│       │           │   ├── BlockLogScreen.kt       # Activity feed, hero gauge, detail sheets, search, filters
│       │           │   ├── AppRulesScreen.kt       # Virtualized installed app list with 3-state selectors
│       │           │   ├── KeywordRulesScreen.kt   # Virtualized keyword list, add/delete, live testing
│       │           │   ├── StatsScreen.kt          # Health-style activity ring, top spammers, metrics
│       │           │   └── SettingsScreen.kt       # Inset groups, toggles, In-App OEM Guide dialog, Backup/Restore
│       │           ├── theme\
│       │           │   ├── Color.kt        # Apple system dark palette tokens
│       │           │   ├── Theme.kt        # Dynamic AMOLED vs Dark Surface theme
│       │           │   └── Type.kt         # Cupertino SF-Pro inspired typographic scale
│       │           └── viewmodel\
│       │               ├── BlockLogViewModel.kt
│       │               ├── AppRulesViewModel.kt
│       │               ├── KeywordViewModel.kt
│       │               └── StatsViewModel.kt
│       │
│       └── test\java\com\sieve\filter\service\
│           └── SmartAiClassifierTest.kt    # 30+ comprehensive unit tests covering all spam & safe patterns
│
├── build.gradle.kts                        # Top-level Gradle configuration
├── settings.gradle.kts                     # Project and plugin repositories
├── CLAUDE_BRIEFING.md                      # This technical specification document
└── release\
    └── Sieve-v1.4.0.apk                    # Signed & verified universal release binary (2.46 MB)
```

---

## 9. 🧪 Verification, Testing & Live Hardware Status

* **Unit Test Suite**: 30+ unit tests in `SmartAiClassifierTest.kt` passing cleanly (`BUILD SUCCESSFUL in 1s`).
* **Compiler Status**: Clean build with **0 errors and 0 compiler warnings**.
* **Live Hardware Verification**: Tested on physical hardware (`CPH2381` / OnePlus Nord CE 2 5G running Android 13).
  * System listener remained active and responsive.
  * Real-world spam from shopping and investment apps (e.g. Jar, Flipkart, LinkedIn promotional games) was blocked and logged immediately.
  * WhatsApp and YouTube notifications passed through seamlessly with zero false positives.
  * Memory footprint is lightweight with 120 FPS scrolling across large app lists.

---

## 10. 💡 Key Guidelines for Future Iterations

When extending Sieve with Claude or any AI assistant:

1. **Maintain Zero-Telemetry & 100% On-Device Rule**: Never add remote analytics, network calls, or cloud classification APIs. All heuristic models and regex evaluation must remain local on the device.
2. **Never Compromise Financial & Emergency Communications**: Always cross-reference `SmartAiClassifier.isGuaranteedSafe()` and `PROTECTED_COMMUNICATION_PACKAGES`. A false positive on an OTP, bank debit, UPI transfer, food delivery, or emergency call is strictly unacceptable.
3. **Preserve Apple Cupertino Aesthetics**: Always use predefined Apple design tokens (`AppleCard`, `AppleCardElevated`, `AppleHairline`, `AppleBlue`, `AppleGreen`, `AppleRed`). Do not introduce generic Android Material buttons, square corners, or bright default colors.
4. **Maintain List Virtualization**: Always use `LazyColumn` with individual `items()` rather than placing large `Column`s inside a single item, ensuring 120 FPS performance even on devices with hundreds of installed apps.
