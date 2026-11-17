# 4B-WEEKLY — Weekly cooked badge, confirmation and weekend shopping

- **Task ID & mục tiêu:** 4B-WEEKLY. Show cooked dishes from this week's cook history, make weekly shopping accessible without confirmation deadlock, and add week confirmation, compartment selection and Zalo copy.
- **Input:** Weekly plan, week-scoped cooked recipe IDs, shopping rows, unchecked IDs, and stock/confirm callbacks.
- **Expected output:** Matching dishes show `Đã nấu`; shopping opens irrespective of confirmation; confirmation is an explicit persisted per-week action; users can choose CHILL/FREEZE per item before stock; Zalo copy is a plain checked-list grouped by compartment.
- **Boundaries/risks:** Unedited days, empty shopping list, already-stocked rows, repeated recipe IDs and week changes.
- **Six mutations:** reverse cooked membership; match all weeks; gate shopping behind confirmation; omit confirmation callback; ignore compartment selection on stock; swap Zalo group labels. Unit/presentation tests must assert each data transformation.
