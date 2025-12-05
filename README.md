# QuickBite - Android Food Ordering App

QuickBite is a modern Android application built with **Kotlin** and **Jetpack Compose**. It features a dual-interface system serving both Restaurant Administrators and Customers. The app utilizes **Firebase** for real-time database management, authentication, and order tracking.

## 📱 Project Overview

QuickBite provides a seamless experience for food ordering.
*   **For Customers:** It acts as a self-service kiosk or delivery app allowing users to browse categories, search products, manage a cart, checkout, and track orders in real-time.
*   **For Admins:** It provides a dashboard to manage the menu (add/edit/delete items and categories) and update administrator profiles.

## ✨ Features

### 🛒 Customer (Kiosk) Features
*   **Authentication:**
    *   Secure Login & Registration with Email/Password.
    *   **Google Sign-In** integration.
*   **Menu Browsing:**
    *   Visual category grid.
    *   Filtered product lists based on category selection.
    *   Detailed product views with descriptions, prices, and availability status.
*   **Cart & Checkout:**
    *   Real-time cart management (add items, adjust quantities, remove items).
    *   Pre-filled checkout forms using saved user profile data.
    *   Secure order placement.
*   **Order Tracking:**
    *   Visual timeline of order status (Pending -> Confirmed -> Processing -> Shipped -> Delivered).
    *   View order history and details.
*   **Profile Management:** Edit personal details, shipping address, and profile picture.

### 🛠️ Admin Features
*   **Admin Dashboard:** Quick access to key management tools.
*   **Menu Management:**
    *   Create new Categories and Products with images.
    *   Edit existing product details (price, stock/availability, description).
    *   Delete items from the database.
    *   Images are converted and stored securely as Base64 strings in Firestore.
*   **Admin Profile:** Update admin credentials and information.

## 🛠️ Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Backend:** Firebase Firestore (NoSQL Database)
*   **Auth:** Firebase Authentication
*   **Image Loading:** Coil
*   **Navigation:** Jetpack Navigation Compose

---

## 🚀 Installation & Setup Guide

To run this project locally, you must connect it to your own Firebase project. Follow these steps carefully.

### 1. Prerequisites
*   Android Studio (latest version recommended).
*   JDK 17 or higher.

### 2. Clone the Repository
bash git clone https://github.com/Rjjjj1412/CS220-Finals-Project.git


### 3. Firebase Configuration (Crucial Step)

This project relies on Firebase. You must generate your own configuration files.

#### A. Create a Firebase Project
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Create a new project named `QuickBite`.
3.  Add an **Android App** to the project using the package name: `com.example.quickbitefinalproject`.

#### B. Add `google-services.json`
1.  Download the `google-services.json` file provided by Firebase after creating the Android App.
2.  Move this file into the **app** directory of the project:
    `QuickBiteFinalProject/app/google-services.json`

#### C. Configure Google Sign-In (`default_web_client_id`)
To make Google Sign-In work, you need the Web Client ID from Firebase Authentication.
1.  In Firebase Console, go to **Authentication** > **Sign-in method** > **Google**.
2.  Enable it and copy the **Web Client ID**.
3.  Open the file in your Android project: `res/values/strings.xml`.
4.  Paste the ID into the `default_web_client_id` string: 
```xml
<resources>
    <string name="app_name">QuickBiteFinalProject</string>

    <!-- PASTE YOUR CLIENT ID BELOW -->
    <string name="default_web_client_id">
        YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com
    </string>
</resources>
```

### 4. Setup Firestore Database

1.  In Firebase Console, go to **Firestore Database** and click **Create Database**.
2.  Start in **Test Mode** or **Production Mode**.
3.  **IMPORTANT:** Go to the **Rules** tab and paste the following rules to ensure the App functions correctly (allowing Users to manage their own carts and Admins to manage menus):
```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // ============================
    // HELPER FUNCTIONS
    // ============================
    function isSignedIn() {
      return request.auth != null;
    }

    function isAdmin() {
      // Check that the user is signed in before trying to access their data
      return isSignedIn() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // ============================
    // USERS & CARTS
    // ============================
    match /users/{userId} {
      // A user can read/write their own document. An admin can read/write ANY user document.
      allow read, write: if isSignedIn() && (request.auth.uid == userId || isAdmin());

      // Cart sub-collection rule remains the same
      match /cart/{cartItemId} {
        allow read, write, update, delete:
          if isSignedIn() && request.auth.uid == userId;
      }
    }

    // ============================
    // ORDERS
    // ============================
    match /orders/{orderId} {
      // Anyone signed in can create an order
      allow create: if isSignedIn();

      // User can read their own order, Admin can read any order
      allow read: if isSignedIn() && (resource.data.userId == request.auth.uid || isAdmin());
      
      // Only Admin can update/delete an order record
      allow update, delete: if isAdmin();
    }

    // ============================
    // MENU & CATEGORIES (PUBLIC READ, ADMIN WRITE)
    // ============================
    match /categories/{categoryId} {
      // Anyone can read categories
      allow read: if true;
      // Only admins can write to categories
      allow write: if isAdmin();
    }

    match /menu_items/{itemId} {
      // Anyone can read menu items
      allow read: if true;
      // Only admins can write to menu_items
      allow write: if isAdmin();
    }
  }
}
```

### 5. Seeding an Admin Account
Since the app checks for an `admin` role:
1.  Register a user via the App or Firebase Console.
2.  Go to Firestore, find the `users` collection, and find the document with that User's UID.
3.Add/Update the field: `role`: `"admin"`.

### 6. Build and Run
Sync your Gradle project in Android Studio and run the app on an Emulator or Physical Device.

---

## 📂 Project Structure

*   **`ui/kiosk`**: Contains all Customer-facing screens (`MainMenu(MainCategoryCard)`, `Cart`, `OrderDetails`, `Register`, `Login(Auth)`, `User Profile`).
*   **`ui/admin`**: Contains Admin management screens (`AdminPanel`, `MenuManagement`, `EditItem`, `AddItem`, `EditProfile`, `AdminLogin`).
*   **`navigation`**: Contains `NavGraph.kt` which manages all application routes.


