# MerchGo Android

<p align="center">
  <img src="docs/images/Logo.svg" width="248" height="98" alt="MerchGo Logo"/>
</p>

<p align="center">

![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84)
![Min SDK](https://img.shields.io/badge/MinSDK-24-blue)
![Target SDK](https://img.shields.io/badge/TargetSDK-36-brightgreen)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Offline First](https://img.shields.io/badge/Offline_First-Supported-success)
![Material 3](https://img.shields.io/badge/Material-3-6750A4)
![License](https://img.shields.io/badge/License-MIT-yellow)

</p>

---

# About

**MerchGo** is an Android application built for the **Pitjarus Mobile Developer Technical Assessment**.

The application helps merchandisers perform their daily activities, including

- Attendance (Check In / Check Out)
- Store Visit
- Product Availability Reporting
- Promo Reporting
- Offline Data Collection
- Automatic Synchronization

The application is designed using **Offline First Architecture**, ensuring users can continue working even without an internet connection.

---

# ✨ Features

## Authentication

- Login
- Persistent Session
- Auto Login
- Logout

---

## Attendance

- Check In
- Check Out
- CameraX Selfie
- Timestamp
- Continue Working
- Attendance State Restoration

---

## Store

- Store List
- Search Store
- Add Store
- Store Detail

---

## Product

- Product Availability
- Add Product
- Assign Product to Store
- Barcode Support
- Availability Reporting

---

## Promo

- Add Promo
- Product Dropdown
- Promo Reporting

---

## Offline First

- Room Database
- Pending Queue
- WorkManager
- Auto Synchronization
- Retry Mechanism
- Cache First Repository

---

# 🏗 Architecture

The project follows **MVVM + Repository Pattern**.

```
UI (Fragment)

↓

ViewModel

↓

Repository

↓

───────────────

Remote API

Room Database

───────────────

↓

DataStore
```

The UI never communicates directly with Retrofit or Room.

---

# 📦 Tech Stack

| Category | Technology |
|------------|------------|
| Language | Kotlin |
| UI | XML + Material Design 3 |
| Architecture | MVVM |
| DI | Hilt |
| Local Database | Room |
| Networking | Retrofit + OkHttp |
| Serialization | Gson |
| Async | Kotlin Coroutines + Flow |
| Background | WorkManager |
| Preferences | DataStore |
| Navigation | Navigation Component |
| Image Loading | Coil |
| Logging | Timber |

---

# 📂 Project Structure

```
app
│
├── data
│   ├── api
│   ├── dao
│   ├── database
│   ├── entity
│   ├── model
│   ├── repository
│   ├── mapper
│   ├── sync
│   └── worker
│
├── datastore
│
├── di
│
├── domain
│
├── ui
│   ├── login
│   ├── attendance
│   ├── store
│   ├── detail
│   ├── product
│   ├── model
│   ├── splash
│   └── promo
│
├── utils
│
└── MainActivity
```

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/yourusername/merchgo-android.git
```

---

## Open Project

Open using

Android Studio Narwhal or newer.

---

## Build

```bash
./gradlew assembleDebug
```

---

## Run

Connect an Android device or emulator.

Minimum Android Version

Android 7.0 (API 24)

---

# 🔐 Demo Login

Use the following account.

Username :
```
fajar
```
Password :
````
123456
````

---

# 🌐 Backend API

This application communicates with the MerchGo Backend API.

Backend documentation is available below.

---

![Node](https://img.shields.io/badge/Node.js-22-green)
![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)
![Express](https://img.shields.io/badge/Express-5-black)
![Prisma](https://img.shields.io/badge/Prisma-ORM-2D3748)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED)
![Swagger](https://img.shields.io/badge/OpenAPI-3.0-85EA2D)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

# 📖 API Documentation

Swagger Documentation

## Production

```
https://dev-api.fhanafii.my.id/docs
```

OpenAPI JSON

```
https://dev-api.fhanafii.my.id/openapi.json
```

---

# 📋 Coding Standards

This project follows

- Kotlin Coding Convention
- Material Design 3
- MVVM Architecture
- Repository Pattern
- SOLID Principles
- Offline First Architecture
- Conventional Commits

---

# 🤝 Contributing

This repository is part of a technical assessment.

Contributions are welcome through pull requests.

---

# 📄 License

This project is licensed under the MIT License.