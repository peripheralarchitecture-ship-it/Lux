# Lux

An ambient light meter for the Light Phone III, built with the [Light SDK](https://github.com/lightphone/light-sdk).

Tap **MEASURE**: Lux samples the light sensor for 3 seconds, then shows a time-weighted average (lux), the light band (NIGHT → DIRECT SUN) and the min–max spread on a log-scale radar. The sensor is only on during that window. No network, no permissions, no analytics.

## Layout
- `tool/` – the Lux tool (`com.thelightphone.lux`): `LuxScreen`, `LuxViewModel`, `LuxMath`
- `sdk/client/.../LightAmbientLight.kt` – **new SDK primitive** wrapping `Sensor.TYPE_LIGHT`, exposed as `lightContext.ambientLight`. The stock SDK seals the Android `Context`, so a tool cannot read sensors without this addition; it needs to be accepted upstream by Light for signed distribution.
- everything else is the unmodified SDK (see `SDK_README.md`)

## Build / run
```bash
./gradlew :tool:assembleDebug
./gradlew :tool:testDebugUnitTest
```
Run on a device or the LightOS emulator (see `docs/system_app`). The emulator's virtual light sensor is adjustable in its extended controls.

Not yet compiled or run on hardware at the time of writing.

## License
MIT, as the SDK.
