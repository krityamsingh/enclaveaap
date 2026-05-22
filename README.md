# Enclave

Enclave is a production-grade, open-source encrypted messenger for Android. It features full E2E encryption for 1-to-1 chats and secure server-side encryption for groups and channels.

## Architecture
```
enclave/
├── app/                  # UI, ViewModels, DI
├── core/
│   ├── crypto/           # TweetNaCl + Android Keystore
│   ├── domain/           # UseCases & Repository Interfaces
│   ├── data/             # Implementations & Data Sources
│   ├── database/         # Room DB
│   └── network/          # Firebase & Retrofit
├── admin-panel/          # React Admin Dashboard
├── functions/            # Firebase Cloud Functions
└── infra/                # Rules & Configs
```

## Setup

1. **Clone the repository.**
2. **Add `google-services.json`** to the `app/` directory. You must obtain this from your Firebase Console.
3. **Configure `local.properties`** at the root of the project with the following required keys:

| Key | Where to get |
|---|---|
| `GIPHY_API_KEY` | developers.giphy.com |
| `LIVEKIT_API_KEY` | Your LiveKit server config |
| `LIVEKIT_API_SECRET` | Your LiveKit server config |
| `LIVEKIT_URL` | Your VPS URL |
| `RAZORPAY_KEY_ID` | Razorpay Dashboard |
| `LIBRETRANSLATE_URL` | Your VPS URL |
| `KEYSTORE_PATH` | Path to your `.jks` |
| `KEYSTORE_PASSWORD` | - |
| `KEY_ALIAS` | - |
| `KEY_PASSWORD` | - |

*Note: Missing keys will cause the build to fail.*

## Self-Hosting

**LiveKit**
Follow the official guide to host on Ubuntu 22.04.

**LibreTranslate**
Self-host on the same VPS to ensure translations remain private.

## Build

*   **Debug:** `./gradlew assembleDevDebug`
*   **Prod AAB:** `./gradlew bundleProdRelease`

## Testing

*   `./gradlew test` (Unit tests)
*   `./gradlew connectedAndroidTest` (Instrumentation tests)

## License
GPL-3.0
