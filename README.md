# Sieve — On-Device Notification Spam Filter

**Sieve** is a native Android (Kotlin) app that listens to system notifications via Android's `NotificationListenerService`, auto-dismisses promotional and spam notifications, and leaves genuinely important transactional notifications (e.g. Zomato order status, Uber ride arrival, bank OTPs) untouched.

- **100% On-Device:** Zero cloud servers, zero external network requests, zero telemetry, no account needed.
- **Single Activity with Jetpack Compose & Material 3:** Modern, reactive, sleek dark/light design.
- **Local Persistence:** Room (SQLite) for rules and dismissed notification audit log.
- **Hybrid Classifier Engine:** Prioritizes explicit transactional channels, user overrides, and keyword rules with strict Allow-over-Block precedence.

---

## Architecture Overview

```
Sieve
├── app/src/main/
│   ├── java/com/sieve/filter/
│   │   ├── SieveApplication.kt                # Application singleton for Room & Repository
│   │   ├── MainActivity.kt                    # Single Activity, Edge-to-edge Compose host
│   │   ├── model/                             # Core models (AppRuleMode, RuleAction, FilterDecision, AppInfo)
│   │   ├── data/
│   │   │   ├── local/                         # Room DB (SieveDatabase, AppRuleEntity, KeywordRuleEntity, BlockLogEntity)
│   │   │   │   ├── dao/                       # AppRuleDao, KeywordRuleDao, BlockLogDao
│   │   │   │   └── entity/
│   │   │   └── repository/                    # SieveRepository
│   │   ├── service/
│   │   │   ├── NotificationClassifier.kt      # Pure hybrid rule evaluation engine
│   │   │   └── SieveNotificationListenerService.kt # System notification interceptor
│   │   └── ui/
│   │       ├── SieveApp.kt                    # Root Scaffold with Material 3 Bottom Navigation
│   │       ├── navigation/                    # Screen routes
│   │       ├── screens/                       # BlockLogScreen, AppRulesScreen, KeywordRulesScreen
│   │       ├── components/                    # PermissionBanner, AddKeywordDialog, Rule cards
│   │       ├── theme/                         # Indigo/Slate color palette, Typography, Dark/Light Theme
│   │       └── viewmodel/                     # BlockLogViewModel, AppRulesViewModel, KeywordRulesViewModel
│   └── AndroidManifest.xml                    # Declares BIND_NOTIFICATION_LISTENER_SERVICE
└── app/src/test/
    └── java/com/sieve/filter/service/NotificationClassifierTest.kt # 10 unit test cases
```

---

## Core Features & Screens

1. **Block Log (Home):**
   - Reverse-chronological audit log of every dismissed notification.
   - Shows app name, package, notification title, snippet, matched rule badge, and relative timestamp.
   - **Quick Undo:** One-tap **"Always Allow App"** button to instantly correct false positives.
   - Search bar and clear all logs action.

2. **App Rules:**
   - Lists installed and observed apps on the device.
   - 3-state segmented toggle:
     - `AUTO`: Evaluates channel heuristics and keyword rules.
     - `ALLOW`: Bypasses filter; all notifications are kept.
     - `BLOCK`: Dismisses all notifications from this app.

3. **Keyword Rules (Advanced):**
   - Filter chips for `All`, `Block List`, and `Allow List`.
   - Pre-seeded with common Indian & global promotional keywords (`% off`, `cashback`, `flash sale`, `limited time`, `flat ₹`, etc.) and transactional keywords (`delivered`, `out for delivery`, `otp`, `order confirmed`).
   - Floating action button to add custom keyword rules (global or app-scoped).
   - Instant swipe/tap to delete and "Reset to Defaults".

4. **Permission Status Banner:**
   - Automatically detects if `Notification Access` has been granted.
   - Displays clear explanation of on-device privacy guarantees and deep-links directly to Android's Notification Access settings screen.

---

## Classification Hierarchy

1. **Persistent/Ongoing notifications** (calls, music players, navigation) are protected and never dismissed.
2. **App Overrides:** Explicit `ALLOW` or `BLOCK` takes precedence immediately.
3. **Channel Introspection:** Promotional channel identifiers (e.g. `offers`, `deals`, `marketing`, `promotions`) trigger dismissal unless superseded by an allow rule.
4. **Keyword Matching:**
   - **ALLOW keywords ALWAYS beat BLOCK keywords.** (e.g. `"Your order is delivered! Get 20% off your next meal"` is kept because `"delivered"` takes priority).
   - Package-scoped rules take precedence over global rules.
5. **Fallback:** If no spam indicator matches, notification is left untouched.

---

## Testing & Verification

### Run Unit Tests
```bash
gradlew test
```
All 10 unit tests for `NotificationClassifier` pass with 100% success rate:
- App-level ALLOW override
- App-level BLOCK override
- Spam keyword detection
- Allow keyword precedence over block keyword (e.g. order delivery notifications)
- OTP with promo preservation
- Promotional channel detection
- Important transactional channel protection
- Ongoing notification protection
- Package-scoped rule override
- Case-insensitivity & whitespace normalization

### Build APK
```bash
gradlew assembleDebug
```
Output APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`
