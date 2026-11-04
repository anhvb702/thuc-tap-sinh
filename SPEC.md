# KMM App spec

Every requirement is a row with an id. Agents build against approved rows; commits cite the ids they serve. `pullboard spec check` lints this file.

Statuses: approved = decided · draft = proposed · pending = open question · fact = true today · wont = won't build, decided and kept · retired = no longer applies, kept. Ids are permanent: never delete or renumber a row. Tiers: must · aim.

Write each requirement as one row under its section, in this format. The fence keeps the example out of the spec, so no placeholder id is ever committed; your own rows go below, outside it.

```text
- G1 [draft, must] The first thing the client needs, in one line. | gate: the test that proves it
- K1 [draft, must] A constraint, e.g. runs locally with no account. | gate: review
```

## G · Goals: what the client asked for

- G1 [approved, must] The app builds and opens on an Android emulator (Pixel_8) and its first screen shows without a crash. | gate: ./gradlew :androidApp:assembleDebug passes, and a screenshot of the running app from the emulator is attached to the PR

## K · Constraints
