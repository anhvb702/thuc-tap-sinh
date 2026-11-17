# UX: Zalo sharing and reminder test button

## Task

Add direct Zalo sharing with clipboard backup and a force-test reminder path.

## Contracts

- Zalo intent is `ACTION_SEND`, `text/plain`, targets `com.zing.zalo`, and includes the exact text; chooser fallback retains the text and has the approved title.
- Each shopping share button writes the formatted checklist to clipboard and launches the direct Zalo intent.
- A force-test broadcast bypasses both daily throttle dates and never writes either throttle key. Scheduled delivery still uses and updates its existing dates.
- Force-test performs the normal local database and recipe lookup. If `planDailyNotice` yields no notice, choose the first recipe allowed by the saved diet and show the dish suggestion; if no allowed recipe exists, show no notification.
- Settings requests Android 13 notification permission before sending, and reports the result.

## Six mutation targets

1. Remove the Zalo package target.
2. Remove chooser fallback.
3. Stop copying before sharing.
4. Re-enable throttle checks for force-test.
5. Write `last_sent` during force-test.
6. Skip diet filtering in fallback dish selection.
