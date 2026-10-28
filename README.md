# 🏋️ FitBro - Ultimate Fitness & Gym Management Android App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![SDK](https://img.shields.io/badge/API-28%2B-blue.svg)](https://developer.android.com/about/versions/pie)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

**FitBro** is a feature-packed, multi-role Android application designed to bridge the gap between fitness enthusiasts, personal trainers, and gym administrators. Built with modern Android architectural principles, FitBro delivers real-time workout tracking, custom fitness plans, e-commerce shop integration with Stripe payments, geolocation gym discovery via Google Maps, and comprehensive analytics.

<img width="1920" height="1080" alt="m1" src="https://github.com/user-attachments/assets/38b7c7cf-3a39-43ec-93b9-46c3fe394334" />

---

## ✨ Features Breakdown

### 👤 User / Member Portal
- **Dashboard & Daily Tracking**: Track daily step counts using hardware sensor integration, view workout streaks, active routines, and daily progress.
- **Interactive Workout Plans**: View personal and trainer-assigned workout plans broken down by days and exercises.
- **Active Workout Mode**: Real-time workout execution screen with timers, set tracking, and rep completion logging.
- **Fitness & Weight Analytics**: Log body weight history with interactive visual charts powered by MPAndroidChart.
- **Gym Locator (Google Maps)**: Find nearby affiliated gyms and personal trainers with real-time location services.
- **FitBro Store & Checkout**: Browse fitness gear and supplements, view item details, and execute secure digital checkout powered by **Stripe**.
- **Trainer Custom Request**: Submit custom workout plan requests directly to certified trainers.

### 🏋️ Trainer Portal
- **Client & Plan Management**: Review and accept custom workout plan requests from users.
- **Workout Builder**: Interactively create personalized multi-day training routines with custom exercises, sets, reps, and instructions.
- **Exercise Library**: Manage exercises with multimedia attachments (images/videos uploaded via Cloudinary).
- **Assigned Gyms**: Manage associated gym locations and view connected clients.

### 🛡️ Admin Portal
- **User Management**: Search, filter, view details, and manage permissions for all registered users, trainers, and admins.
- **Gym Network**: Add, edit, or remove gym locations with address geocoding and metadata.
- **System Statistics**: Real-time overview of active users, trainer counts, gym enrollments, and platform usage metrics.
- **Global Workout Templates**: Create and maintain global workout plan templates available to all users.

---

## 🛠 Tech Stack & Libraries

- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **UI & Layouts**: Material Design 3, ViewBinding, Dynamic Fragments, SwipeRefreshLayout
- **Data Persistence**: 
  - **Firebase Firestore**: Cloud NoSQL Realtime Database
  - **Room Database**: Local SQLite caching for offline capability
- **Authentication**: Firebase Authentication (Email/Password with Role-Based Access Control)
- **Networking & Media**:
  - **Retrofit 2 & Gson**: REST API integration
  - **Glide**: High-performance image loading and caching
  - **Cloudinary SDK**: Cloud image & asset hosting
- **Payments & E-Commerce**: Stripe Android SDK (`com.stripe:stripe-android`)
- **Maps & Location**: Google Maps SDK & Fused Location Provider API
- **Push Notifications**: Firebase Cloud Messaging (FCM) & Background Sync Service
- **Data Visualization**: MPAndroidChart for progress tracking

---

## 📁 Project Structure

```text
lk.codehive.ravishka.fitbro/
├── data/
│   ├── local/              # Room Database, DAOs, Type Converters
│   └── repository/         # Workout & Data Repository layer
├── models/                 # Data Models (User, Gym, WorkoutPlan, Exercise, etc.)
├── services/               # Firebase Cloud Messaging Services
├── ui/
│   ├── adapters/           # RecyclerView Adapters
│   ├── fragments/          # UI Fragments (Home, Plans, Fitness, Shop, Admin, Trainer)
│   ├── ActiveWorkoutActivity.java
│   ├── AdminActivity.java
│   ├── HomeActivity.java
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── RegisterActivity.java
│   ├── SplashActivity.java
│   ├── TrainerActivity.java
│   └── TrainingActivity.java
├── utils/                  # Step Counter, Notification Helper, Sync Managers
├── viewmodels/             # MVVM ViewModels
└── FitBroApp.java          # Application Class
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / 2024.2.1 or newer
- **JDK**: Java 11 or higher
- **Android SDK**: Min SDK 28 (Android 9.0), Target SDK 36

### Configuration & API Keys

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/FitBro.git
   cd FitBro
   ```

2. **Firebase Setup**:
   - Create a project in [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with package name `lk.codehive.ravishka.fitbro`.
   - Download `google-services.json` and place it into the `app/` directory (`app/google-services.json`).
   - Enable **Firebase Auth** (Email/Password), **Firestore**, and **Firebase Storage**.

3. **Google Maps API Key**:
   - Generate a Google Maps API Key in Google Cloud Console.
   - Replace the key in `app/src/main/AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_GOOGLE_MAPS_API_KEY" />
     ```

4. **Stripe & Cloudinary Setup**:
   - Provide your Stripe publishable key and Cloudinary credentials in the app configuration or environment setup.

5. **Build and Run**:
   - Open the project in Android Studio.
   - Sync Gradle project.
   - Run on an Emulator or connected Android Device (API 28+).

---

## 📱 Screenshots & Demo

<img width="1920" height="1080" alt="a1" src="https://github.com/user-attachments/assets/ae1194df-2734-4b96-a9ff-21ed5581084e" />

<img width="1920" height="1080" alt="a2" src="https://github.com/user-attachments/assets/c401f1e0-82c4-4b87-9375-d50c91646d94" />

<img width="1920" height="1080" alt="a3" src="https://github.com/user-attachments/assets/c3863ac1-50cd-4366-b48d-eeb88343ae48" />

<img width="1920" height="1080" alt="t1" src="https://github.com/user-attachments/assets/e956e962-935a-45b0-b2d9-395a5fe952f0" />

<img width="1920" height="1080" alt="t2" src="https://github.com/user-attachments/assets/c369f349-8d9e-4682-b665-beec83a1317e" />

<img width="1920" height="1080" alt="ad1" src="https://github.com/user-attachments/assets/53697ce1-395c-4755-baf5-50f3e8b35955" />

<img width="1920" height="1080" alt="ad2" src="https://github.com/user-attachments/assets/27cc0646-7fed-4cf1-ab25-99cca9987802" />

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
