# Zubeyr Perfume — Android app

A native Android shop for **zubeyrperfume.com**. It reads the catalogue, prices,
stock, delivery options and shop details straight from the WordPress plugin you
already run, and sends orders back to it.

```
GET  https://zubeyrperfume.com/wp-json/zubeyr/v1/data          catalogue + settings + shipping
POST https://zubeyrperfume.com/wp-json/zubeyr/v1/orders        places an order
GET  https://zubeyrperfume.com/wp-json/zubeyr/v1/track/{code}  order status
```

Nothing else is needed on the server. Orders placed in the app land in
**Zubeyr Perfume → Orders** in wp-admin exactly like orders from the website,
stock is reduced by the same code, and the SMS and email letters go out as usual.

## What the app does

- Home with the hero, categories, best sellers, sale, new arrivals and brands
- Search and filter by category, brand, price, rating
- Product page with sizes, stock, note pyramid, longevity and projection, seasons
- Bag that survives closing the app, with quantities capped at real stock
- Checkout: name, phone, email, address, note, SMS opt-in, newsletter opt-in
- Delivery options and fees read from your Shipping tab, free over your threshold
- Payment: Chapa, Telebirr, CBE Birr, bank transfer, cash on delivery, SantimPay,
  card — with your own numbers shown, and a reference field where one is needed
- Thank-you screen with the tracking code, and an order tracker
- Call, WhatsApp, Telegram and email buttons
- **Works offline**: the 27 perfumes ship inside the app, and the last live
  catalogue is cached, so the shop opens with no signal and updates when online

Prices are never sent from the phone. The server prices every line, exactly as
the website does.

## Building the APK

### Option 1 — GitHub, no software to install (easiest)

1. Create a repository on github.com and upload this whole folder to it.
2. Open the **Actions** tab → **Build APK** → **Run workflow**.
3. Wait about five minutes, then download the **zubeyr-perfume-apk** artifact.
4. Unzip it, copy the `.apk` to a phone and install it (allow "install from
   unknown sources" when Android asks).

The workflow is already written, at `.github/workflows/build-apk.yml`.

### Option 2 — Android Studio

1. Install Android Studio (free).
2. **File → Open** → choose this folder. Let it download Gradle and the SDK the
   first time; it will offer to create the Gradle wrapper — accept.
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. The file appears at `app/build/outputs/apk/release/app-release.apk`.

### Option 3 — Command line

With JDK 17, Gradle 8.7 and the Android SDK installed:

```bash
gradle assembleRelease
```

## Before you publish

- **Signing.** The release build is signed with Android's debug key so it always
  installs. For Google Play, create your own key
  (`keytool -genkey -v -keystore zubeyr.jks -alias zubeyr -keyalg RSA -validity 10000`)
  and point `signingConfigs` in `app/build.gradle.kts` at it.
- **Your own numbers.** Telebirr, CBE Birr and the bank account shown at checkout
  come from **Zubeyr Perfume → Settings** in wp-admin. Fill those in there and
  the app picks them up — no rebuild needed.
- **Permalinks.** In WordPress go to **Settings → Permalinks → Save Changes**
  once, or the REST routes stay switched off and the app will only show the
  bundled catalogue.

## Changing things

| What | Where |
|---|---|
| Website address | `SITE` in `app/src/main/java/com/zubeyrperfume/shop/Store.kt` |
| App name | `app/src/main/res/values/strings.xml` |
| Colours | `app/src/main/java/com/zubeyrperfume/shop/ui/Theme.kt` |
| Icon | `app/src/main/res/mipmap-*/ic_launcher.png` |
| Bundled catalogue | `app/src/main/assets/seed.json` |
| Version number | `versionName` / `versionCode` in `app/build.gradle.kts` |

Perfumes with no photograph are drawn as a bottle from the glass and cap colours
stored with the product, so the shop never shows an empty grey box. Import
photographs in wp-admin and the app shows them instead.
