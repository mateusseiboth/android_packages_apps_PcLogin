# PcLogin

Login screen of LineageOS for PCs: the PC starts on a screen with the people who use it, like a
ChromeOS-style Android desktop. Pick a user to sign in (their PIN or password, if they set one, is asked
by the lock screen), add a user, or use the PC as a guest. Written from scratch in Kotlin and Jetpack
Compose (Material 3, wallpaper colors); the layout follows the Googlebook login screen.

## How the PC gets here

The device runs in headless system user mode with the system user (user 0) as a login screen, the AOSP
configuration of `build/make/target/product/hsu_as_login.mk`:

- `ro.fw.mu.headless_system_user=true`: user 0 runs the system but is not a person.
- `config_canSwitchToHeadlessSystemUser=true` and `config_hsumBootStrategy=1`: every boot after the first
  ends on user 0, whose home screen is this app.
- First boot: the system creates the first admin user (`config_createInitialAdminUserOnHsum`, flag
  `android.multiuser.create_initial_user`) and boots into it, so the setup wizard sets up the device in
  that user.
- `config_userSwitchingMustGoThroughLoginScreen=true`: "Sign out" in the system tray stops the user and
  comes back here (SystemUI's `UserLogoutInteractor`, `UserManager.logoutUser`).

`vendor/pc/ui` (branch `login`, `pc_login.mk`) holds those settings; this repository only has the app.

## What it shows

- **User pods**: picture (the user's photo or the system's default picture) and name, oldest user first.
  Large pods (190dp picture) up to four users, small ones (120dp) after that. A click switches to the
  user (`ActivityManager.switchUser`); the pod shows progress until the switch.
- **Bottom bar**: Accessibility (SystemUI's accessibility quick access dialog), **Use as guest** (a new
  guest, removed when it signs out) and **Add user** (a full user named "New user", who goes through
  the setup wizard).
- Add user and Use as guest are hidden when the device has a single user, when a policy or the system
  user's restrictions forbid adding users (`DISALLOW_ADD_USER`, `DISALLOW_ADD_GUEST`, also when an
  admin policy sets them on any user) or when the product turns guests off
  (`config_showGuestButton`), and disabled at the user limit.
- Guests left by earlier sessions are removed when the screen comes back.
- The wallpaper shows through, blurred when the device can blur and darker when it cannot.

## Code

- `core/`: plain Kotlin, no Android. `LoginRoster` (who is listed, stale guests), `PodSize`,
  `AdditionRules` (Add user and Use as guest: shown, disabled or hidden, as a rule table),
  `LoginController` (sign in, add and sign in, busy and error state) over the `UserDirectory` port.
- `src/`: `AndroidUserDirectory` (UserManager, ActivityManager), `LoginViewModel` (one worker thread,
  user change broadcasts), `UserAvatars`, `AccessibilityLauncher`, `LoginActivity` (home, blur) and
  the Compose UI in `ui/`.
- `etc/`: privileged permissions, and `preinstalled-packages-pclogin.xml`, which installs the app for
  the system user only.

## Tests

```sh
core/run_tests.sh                        # JUnit tests of core/, with external/kotlinc and JUnit from prebuilts
python3 -m unittest discover -s tests    # UI strings: both languages, same placeholders, no em dash
tools/typecheck.sh                       # compiles core + Compose UI against the prebuilts (needs a previous build's out/)
```

In Soong: `PcLoginCoreTests` (`java_test_host`).

## Build integration

- local manifest: `<project path="packages/apps/PcLogin" name="android_packages_apps_PcLogin" remote="pc" revision="lineage-24.0" />`
- `vendor/pc/ui/pc_login.mk` (branch `login`) adds `PcLogin`; it pulls `privapp-permissions-pclogin.xml`
  and `preinstalled-packages-pclogin.xml` through `required`.

## Not done yet

- Not built or run on a device yet: only the host tests and the Compose typecheck have run.
- The Googlebook login also handles apps locked to a user and entry and exit animations; neither is
  here.
- Removing a user is done in Settings, not on this screen.
