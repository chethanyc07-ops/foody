# EcoPack AI - Food Packaging Material Recommendation System

An AI-based intelligent food packaging material recommendation system and supplier analytical platform designed to maximize food shelf life while minimizing environmental impact.

---

## 🌐 Web Version (Ready for Vercel Deployment)

The web version is built with **Next.js 15**, **React 19**, **Tailwind CSS**, and the **Gemini API**.

### How to Deploy on Vercel:

1. **Push to GitHub**:
   - Push this repository to your GitHub account.

2. **Deploy on Vercel**:
   - Go to [vercel.com/new](https://vercel.com/new) and select this repository.
   - **Framework Preset**: `Next.js`
   - **Root Directory**: `web` (or leave default root with `vercel.json` configured)
   - **Environment Variables**:
     - `NEXT_PUBLIC_GEMINI_API_KEY`: *(Your Google AI Studio Gemini API Key)*
   - Click **Deploy**!

### Run Web Version Locally:
```bash
cd web
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000).

---

## 📱 Android Version (Kotlin & Jetpack Compose)

The native Android app is in the `app/` directory and can be compiled using Android Studio or Gradle:
```bash
gradle :app:assembleDebug
```
The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
