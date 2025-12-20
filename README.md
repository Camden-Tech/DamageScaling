# DamageScaling

A lightweight Minecraft/Paper plugin that visually scales player health without changing the server-side values. Damage and healing are applied to a hidden “real” health pool that is persisted per player, while the visible hearts are recalculated using configurable scaling curves.

## Requirements
- Minecraft/Paper API 1.21.10 or compatible.
- Permission to place the plugin JAR in your server’s `plugins` directory and reload or restart the server.

## Setup
1. Drop the built JAR into `plugins/`.
2. Start or reload your server to generate the default `config.yml`.
3. Adjust the scaling settings (see below) and reload if needed.

## Commands & Permissions
- `/damagescaling <enable|disable>` — toggles the scaling visuals at runtime.
  - Permission: `damagescaling.admin` (defaults to `op`).

When scaling is disabled, the plugin restores each player’s real health so hearts match the true values again.

## Configuration
The default `config.yml` is generated in `plugins/DamageScaling/config.yml`:

```yaml
enabled: true

scaling:
  mode: squared_divided_by_max
  priority:
    - squared_divided_by_max
    - linear_fraction
    - exponential_curve
  minimum-display-health: 0.1
  options:
    squared_divided_by_max: {}
    linear_fraction:
      fraction: 0.6
    exponential_curve:
      exponent: 1.25
      multiplier: 1.0
```

- `enabled`: Whether scaling visuals run at startup. `/damagescaling` updates this value and saves the file.
- `scaling.mode`: Fallback mode if the priority list is empty or invalid.
- `scaling.priority`: First valid entry is used; invalid names are skipped. Leave empty to always use `scaling.mode`.
- `scaling.minimum-display-health`: Smallest visible health (in hearts) when the player is still alive.
- `scaling.options`: Tunables per mode:
  - `squared_divided_by_max`: `display = (realHealth²) / maxHealth` (steeper falloff at low health).
  - `linear_fraction`: `display = realHealth * fraction` with `fraction >= 0`.
  - `exponential_curve`: `display = maxHealth * multiplier * (realHealth / maxHealth)^exponent` with positive exponent and non-negative multiplier.

All computed display values are clamped between 0 and the player’s max health.

## Quirks & Behavior
- **Real vs. visible health:** While scaling is enabled, `Player#getHealth()` returns the scaled value shown in hearts. The plugin keeps the unscaled “real” health separately and persists it per player in `plugins/DamageScaling/playerdata/<uuid>.yml`.
- **Event handling:** Damage and healing events are intercepted. The plugin applies their amounts to the real health pool, then zeroes the event amount and directly sets the player’s visual health based on the chosen scaling curve.
- **Minimum display:** If a player is alive but the scaled value drops below `minimum-display-health`, hearts stay at the minimum until the real health actually hits zero (at which point the player dies).
- **Toggling scaling:** Disabling scaling immediately restores hearts to the underlying real health. Re-enabling recomputes the displayed hearts for all online players.
- **Respawns:** On respawn, real health is reset to the player’s max health and the scaled display is applied on the next tick.

## Building
This is a Maven project. From the repository root:

```bash
mvn package
```

The assembled JAR will be in `target/`.
