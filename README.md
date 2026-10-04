# EcoPack AI - Food Packaging Material Recommendation System

An AI-driven intelligent food packaging material recommendation system and supplier analytical platform designed to maximize food shelf life while minimizing environmental impact.

---

## 🌐 Web Version (Vercel Ready)

The web application is located at the root of this repository and is pre-configured for **1-click Vercel deployment**.

### Deploy to Vercel:
1. Push this repository to **GitHub**.
2. Go to [vercel.com/new](https://vercel.com/new) and select this repository.
3. Vercel will automatically detect **Next.js** from `package.json`.
4. In **Environment Variables**, add:
   - `NEXT_PUBLIC_GEMINI_API_KEY`: *(Your Google AI Studio Gemini API Key)*
5. Click **Deploy**. Vercel will build and host your live website.

### Run Locally:
```bash
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000).

---

## 📱 Android Version (Kotlin & Jetpack Compose)

The native Android app lives in the `app/` module and can be compiled using Android Studio:
```bash
gradle :app:assembleDebug
```
The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
