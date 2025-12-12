# Project Context: Learning-App

## Project Overview

**Learning-App** is an Android application designed for language learning, featuring gamification elements like leaderboards, streaks, and a friend system. The application is built using Java and leverages Firebase for backend services.

### Key Technologies

*   **Language:** Java (JDK 11)
*   **Platform:** Android (Min SDK: 24, Target SDK: 36)
*   **Build System:** Gradle (Kotlin DSL)
*   **Backend / Cloud Services:** Google Firebase
    *   **Authentication:** User sign-up/sign-in
    *   **Firestore:** Real-time database for user profiles, progress, and social features
    *   **Storage:** Cloud storage for assets like user avatars
*   **UI Libraries:**
    *   **Material Design:** Core UI components (BottomNavigationView, etc.)
    *   **CircleImageView:** Circular image views for profiles
    *   **Glide:** Efficient image loading and caching

## Architecture & Core Components

The application follows a standard Android architecture using Activities and Fragments.

### Key Activities
*   **`UserWelcomeActivity`**: The entry point of the application (Launcher).
*   **`UserLoginActivity` & `UserRegisterActivity`**: Handles user authentication flow.
*   **`UserDashboardActivity`**: The main hub of the app, likely hosting `BottomNavigationView` to switch between different functional fragments.
*   **`UserLearningReasonActivity` & `UserChoosePathActivity`**: Onboarding flows for new users.
*   **`UserSettingsActivity`**: Account management.

### Key Fragments & Logic
*   **`LeaderboardFragment`**: Displays user rankings.
*   **`FriendsFragment`**: Manages following/followers lists.
*   **`UserProfileFragment`**: Displays user stats and details.
*   **`LeaderboardAdapter` / `FriendsAdapter`**: RecyclerView adapters for displaying lists of data.

## Building and Running

The project uses the Gradle wrapper for build management.

### Prerequisites
*   JDK 11 or higher
*   Android Studio
*   A connected Android device or Emulator

### Common Commands

*   **Build Project:**
    ```bash
    ./gradlew assembleDebug
    ```

*   **Run Tests:**
    ```bash
    ./gradlew test
    ```

*   **Install on Device:**
    ```bash
    ./gradlew installDebug
    ```

## Development Conventions

*   **Source Code:** Located in `app/src/main/java/com/example/learning_app/`.
*   **Resources:** Layouts, strings, and drawables are in `app/src/main/res/`.
*   **Firebase Integration:** Firebase is initialized via the `google-services` plugin. Ensure `google-services.json` is present in the `app/` directory (note: strictly handle this file securely as it contains configuration details).
*   **UI Styling:** The app uses a mix of standard Android views and custom drawables (e.g., `button_background_green.xml`) found in `res/drawable/`.
