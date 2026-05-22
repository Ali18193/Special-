<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/AI-Gemini-4285F4?style=for-the-badge&logo=google&logoColor=white"/>
<img src="https://img.shields.io/badge/UI-Material%203-6750A4?style=for-the-badge&logo=material-design&logoColor=white"/>

<br/><br/>

```
███████╗██╗      ██████╗ ██╗    ██╗██████╗ ██╗      █████╗ ███╗   ██╗
██╔════╝██║     ██╔═══██╗██║    ██║██╔══██╗██║     ██╔══██╗████╗  ██║
█████╗  ██║     ██║   ██║██║ █╗ ██║██████╔╝██║     ███████║██╔██╗ ██║
██╔══╝  ██║     ██║   ██║██║███╗██║██╔═══╝ ██║     ██╔══██║██║╚██╗██║
██║     ███████╗╚██████╔╝╚███╔███╔╝██║     ███████╗██║  ██║██║ ╚████║
╚═╝     ╚══════╝ ╚═════╝  ╚══╝╚══╝ ╚═╝     ╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝
```

### *Your day, intelligently structured.*

**FlowPlan AI** is a smart Android planner powered by Google Gemini.  
Type your tasks in plain language — let AI do the organizing.

</div>

---

## ✨ Features

| Feature | Description |
|---|---|
| 🧠 **AI Task Parsing** | Type raw text like *"meeting at 3pm, gym after, finish report by Friday"* and Gemini structures it automatically |
| ⏱️ **Pomodoro Timer** | Built-in focus timer with 25-min work / 5-min break cycles to keep you in flow |
| 📊 **Progress Analytics** | Dynamic charts showing your daily and weekly task completion trends |
| 🎨 **Material 3 Themes** | Switch between **Light**, **Dark**, and **Sunset** themes with full Material You support |
| 📅 **Daily & Weekly View** | Plan your day or the whole week — all in one place |

---

## 📱 Screenshots

> *Coming soon — app in active development*

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable)
- A [Google Gemini API key](https://aistudio.google.com/app/apikey) (free)
- Android device or emulator (API 26+)

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/Ali18193/Special-.git
cd Special-

# 2. Create your environment file
cp .env.example .env

# 3. Add your Gemini API key to .env
GEMINI_API_KEY=your_api_key_here
```

### Run

1. Open the project in **Android Studio**
2. Let Gradle sync complete
3. Remove the signing line from `app/build.gradle.kts`:
   ```kotlin
   // Remove this line:
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
4. Hit **Run ▶** on your emulator or device

---

## 🛠️ Tech Stack

```
FlowPlan AI
├── Language        → Kotlin
├── UI Framework    → Jetpack Compose + Material 3
├── AI Engine       → Google Gemini API
├── Build System    → Gradle (Kotlin DSL)
├── DI              → KSP (Kotlin Symbol Processing)
└── Testing         → Roborazzi (screenshot tests)
```

---

## 🔐 Security

API keys are managed via `.env` and the `secrets` Gradle plugin — never hardcoded in source.  
The `.env` file is listed in `.gitignore` and will not be committed.

---

## 📌 Roadmap

- [ ] Cloud sync across devices
- [ ] Widget support for home screen
- [ ] Calendar integration (Google Calendar)
- [ ] Voice input for task creation
- [ ] Smart reminders based on task priority

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

## 📄 License

This project is open source. See [LICENSE](LICENSE) for details.

---

<div align="center">

Made with Google AI Studio and Kotlin

*FlowPlan AI — because your time deserves better than a sticky note.*

</div>
