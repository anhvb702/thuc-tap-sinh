# 4B-HOME — Home Gacha box and landing flow

- **Task ID & mục tiêu:** 4B-HOME. Add the distinct home gacha entry and compact random dish dialog.
- **Input:** `HomeScreen` receives diet-filtered recipes (`visibleRecipes` from `MainScaffold`), pantry `PantryGridState`, and navigation callbacks. User opens the box, reel finishes, rerolls, adds missing ingredients, or opens recipe.
- **Expected output:** Box sits immediately after pantry summary and is distinct from pantry-based recommendations. The chosen recipe comes only from the supplied visible list via `pickRandomRecipe`; the dialog shows pantry availability, missing list, daily-shopping action when needed, cook navigation, and reroll. Empty filtered list gives diet-filter guidance. Landing has reduced-motion accommodation, readable announcement semantics and light haptic.
- **Boundaries/risks:** Empty pantry and empty recipe pool; one recipe; screen reader and reduced motion; do not bypass supplied diet filtering.
- **Six mutations:** remove filtering by restricting the input to supplied recipes; replace random picker with first item; omit missing ingredients; expose add-shopping action with no missing items; remove landing announcement; remove landing haptic. UI/presentation tests should catch the observable semantics where testable.
