# Mobile Application Development Assignment (CSE3709)

This repository contains Android applications developed as part of the Mobile Application Development course.

---

## Student Details

* Name: Tanmay
* Course: B.Tech CSE (2024–28)
* Subject: Mobile Application Development (CSE3709)

---

## Repository Structure

```
MobileApplicationDev/
 ├── Q1_CurrencyConverter/
 ├── Q2_MediaPlayer/
 ├── Q3_SensorsApp/
 ├── Q4_GalleryApp/
```

Each question is implemented as a separate Android Studio project.

---

## Q1 — Currency Converter App

Features:

* Convert between INR, USD, EUR, JPY
* Uses realistic fixed exchange rates
* Displays:

  * Converted value
  * Exchange rate
  * Last updated time
* Includes Light/Dark mode toggle via Settings

---

## Q2 — Media Player App

Features:

* Play audio from device storage
* Stream video from URL
* Unified controls for both:

  * Play
  * Pause
  * Stop
  * Restart
  * Forward (+10s)
  * Backward (-10s)
  * Clear media
* Custom control system (no default VideoView controls)

---

## Q3 — Sensors App

Features:

* Displays real-time data from:

  * Accelerometer (X, Y, Z)
  * Light sensor (lux)
  * Proximity sensor
* Dashboard-style UI
* Splash screen on launch

---

## Q4 — Gallery App

Features:

* Capture photos using device camera
* Select folder using Storage Access Framework
* Display images in grid using RecyclerView
* Image detail screen:

  * Preview
  * Metadata (name, path, size, date)
* Delete images with confirmation
* Gallery refresh after deletion
* Clean UI

---

## Technologies Used

* Language: Java
* IDE: Android Studio
* UI: XML, Material Components
* APIs:

  * MediaPlayer
  * VideoView
  * SensorManager
  * MediaStore (Camera)
  * DocumentFile (Storage Access Framework)
  * RecyclerView

---

## Permissions Used

* Camera
* Internet
* Storage access (via SAF where required)

---

## How to Run

1. Clone the repository:

```
git clone https://github.com/tanmaysangwan/MobileApplicationDev.git
```

2. Open any project in Android Studio
3. Run on emulator or device

---

## Screenshots

[View screenshots in the Photos folder](./Photos)

---

## Status

* All four applications completed
* Features implemented as per assignment requirements
* Ready for submission

---

## Repository Link

https://github.com/tanmaysangwan/MobileApplicationDev
