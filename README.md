# DamageScaling

DamageScaling is a lightweight Paper plugin that **rescales the hearts you see without changing the real health stored by the server**. Incoming damage and healing are applied to a hidden real health pool, then converted to visible hearts using configurable curves. This keeps combat readable while preserving vanilla mechanics for other plugins.

## Why use it?
- **Player-friendly visuals:** Show dramatic heart loss early or smooth out spikes with your preferred curve.
- **Multiple scaling modes with priorities:** Provide a list of acceptable modes; the first valid one is used, with a fallback mode if none apply.
- **Per-player persistence:** Real health is stored in `plugins/DamageScaling/playerdata/<uuid>.yml` so visuals remain consistent across reconnects.
- **Safe toggling:** Enable or disable scaling at runtime—hearts are immediately restored to the real values when turned off.

## Quick start
1. Drop the built JAR into your server’s `plugins/` directory.
2. Start or reload the server to generate `plugins/DamageScaling/config.yml`.
3. Optionally adjust the scaling section (see examples below).
4. Use `/damagescaling enable` to turn visuals on, or `/damagescaling disable` to switch back to vanilla hearts.

Command & permission:
- `/damagescaling <enable|disable>` — toggles the visual scaling.
  - Permission: `damagescaling.admin` (defaults to `op`).

## Configuration reference
Default configuration (`plugins/DamageScaling/config.yml`):

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

Key settings:
- `enabled`: Whether scaling visuals run at startup. Updated when you run `/damagescaling`.
- `scaling.mode`: Fallback mode if the priority list is empty or invalid.
- `scaling.priority`: The first valid entry is used; invalid names are skipped.
- `scaling.minimum-display-health`: Smallest visible health (in hearts) while the player is still alive.
- `scaling.options`: Per-mode tunables:
  - `squared_divided_by_max`: `display = (realHealth²) / maxHealth` (steeper early drop for dramatic heart loss).
  - `linear_fraction`: `display = realHealth * fraction` (`fraction >= 0`).
  - `exponential_curve`: `display = maxHealth * multiplier * (realHealth / maxHealth)^exponent` (positive exponent, non-negative multiplier).

All display values are clamped between 0 and the player’s max health.

## Usage examples
### 1) Hardcore warning curve
Create a harsher early drop so players panic sooner:
```yaml
scaling:
  priority: [squared_divided_by_max]
  options:
    squared_divided_by_max: {}
  minimum-display-health: 0.2
```
With 20 max health and 14 real health, hearts display roughly 9.8—visibly worse than vanilla’s 14.

### 2) Casual smoothing
Keep hearts steadier by scaling linearly:
```yaml
scaling:
  priority: [linear_fraction]
  options:
    linear_fraction:
      fraction: 0.7
```
At 10 real health out of 20, players see 7 hearts instead of 10, softening the perceived danger.

### 3) Boss fight accentuation
Highlight late-fight tension with an exponential curve:
```yaml
scaling:
  priority: [exponential_curve]
  options:
    exponential_curve:
      exponent: 1.5
      multiplier: 1.0
```
A player at half real health (10/20) shows about 7.1 hearts—enough warning without hiding survivability.

## Runtime behavior
- **Event handling:** Damage/heal events are applied to the real health pool, the event amount is zeroed, and the visible hearts are set using the active curve.
- **Minimum display:** Hearts never drop below `minimum-display-health` until real health reaches zero (then the player dies normally).
- **Toggling scaling:** Disabling scaling restores hearts to the real values; re-enabling recomputes visuals for online players.
- **Respawns:** On respawn, real health resets to max and scaling is applied on the next tick.

## Building
This is a Maven project. From the repository root:

```bash
mvn package
```

The assembled JAR will be in `target/`.
