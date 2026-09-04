# Sieve — Notification Spam Filter — Build Spec

## What It Is
Personal Android app that listens to all system notifications, auto-dismisses promotional/spam ones per app, and leaves genuinely important notifications (e.g. Zomato order status) untouched. Single-device, on-device only.

## Stack
- Platform: Native Android (Kotlin), Android Studio, minSdk 26 (needed for notification channel APIs)
- Backend: None — fully on-device
- DB: Room (SQLite) — rules + block log
- AI: None for MVP — rule engine only. Do not add an LLM/ML classifier until keyword rules prove insufficient in practice.
- Auth: None

## Core Features (MVP only)
1. **Notification listener** — background `NotificationListenerService`; one-time manual grant of Notification Access (this permission cannot be requested via a runtime dialog — deep-link to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` with an explanation screen).
2. **Hybrid classifier** — for each posted notification: check its channel ID/importance first (if the source app already separates channels, e.g. an "Offers" channel vs "Order Updates" channel, trust that). If the channel is unsplit or unrecognized, fall back to per-package keyword matching on title + text (block-list: "% off", "cashback", "flash sale", "limited time"; allow-list: "delivered", "out for delivery", "OTP", "order confirmed", "otp").
3. **Action** — notifications matched as spam get dismissed via `cancelNotification(key)`. Everything else is left alone, untouched.
4. **Block log** — every dismissed notification is recorded locally (app, title snippet, matched rule, timestamp) so false positives can be audited and corrected.
5. **Per-app override** — each app can be set to Auto / Always Allow / Always Block, overriding the rule engine entirely.

## UI Screens
- **Block Log** (home): reverse-chronological list of dismissed notifications, each with an "always allow this" undo action
- **App Rules**: every app seen so far, with an Auto/Allow/Block selector
- **Keyword Rules** (advanced): editable block/allow keyword lists, global or scoped to one app

## Data Model
- `AppRule(package_name TEXT PRIMARY KEY, mode TEXT)` — AUTO | ALLOW | BLOCK
- `KeywordRule(id INTEGER PK, package_name TEXT NULL, pattern TEXT, action TEXT)` — null package = global rule
- `BlockLog(id INTEGER PK, package_name TEXT, title TEXT, text_snippet TEXT, channel_id TEXT, matched_rule TEXT, timestamp INTEGER)`

## API Calls / Integrations
None external. Platform APIs only: `NotificationListenerService`, `NotificationManager` (channel introspection), Room.

## What NOT to build (MVP scope guard)
- No proxy-repost ("silence the source app entirely, repost only the important ones yourself") — v1 accepts a brief flash-then-cancel; the OS shows/buzzes a notification before your listener is even called, so true pre-emption isn't possible without this heavier v2 rework
- No on-device ML/NLP classifier — keyword rules only, until you have evidence they're not enough
- No cloud sync, no multi-device support, no account system
- No auto-deep-link into the OS's per-channel settings screen — nice-to-have for v2
- No handling for apps that skip standard notification channels entirely (rare, ignore for now)

## Constraints
- Personal use, single device — no distribution, no monetization, no privacy-policy/store-listing concerns
- Free tier / no paid services
- Notification Access is a sensitive permission — the grant screen must explain what the app can see (title/text of every notification, ability to dismiss them) since this is genuinely broad access, even for a device you own

## Known limitation worth being honest about
Because your listener only fires *after* Android has already displayed/buzzed the notification, "spam" notifications will still flash briefly before being cancelled. This is how most notification-cleaner apps on the Play Store already behave — it's a platform constraint, not a bug in this design. If that flash bothers you in practice, the fix is the v2 proxy-repost architecture, not a rule change.

---

## Opening System Prompt (paste into Antigravity)

You are building "Sieve", a native Android (Kotlin) app that filters notification spam. The app runs a `NotificationListenerService` that intercepts all incoming system notifications, classifies each one as important or promotional using a hybrid rule engine (notification channel metadata first, then per-app keyword matching on title/text), and silently cancels notifications classified as spam while leaving important ones untouched. All classification and storage happens on-device using Room (SQLite) — there is no backend, no cloud sync, and no account system. Every dismissed notification is logged locally so the user can review what was blocked and correct false positives by adjusting per-app or per-keyword rules.

## First Prompt to Send

Set up the Android Studio project skeleton: minSdk 26, Kotlin, single-activity app with three screens (Block Log, App Rules, Keyword Rules) behind a bottom nav. Add a `NotificationListenerService` stub that requests notification access and logs every `onNotificationPosted` call (package, title, text, channelId) to Logcat — no classification logic yet, just prove the listener receives real notifications from apps like Zomato. Add the Room database with the `AppRule`, `KeywordRule`, and `BlockLog` tables from the spec above.
