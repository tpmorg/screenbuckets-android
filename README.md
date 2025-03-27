# ScreenBuckets

ScreenBuckets is an Android app that captures, organizes, and makes screenshots searchable using vector embeddings in SQLite.

## Features

- **Floating Button Overlay**: Capture screenshots from any app using a floating button
- **Intelligent Organization**: Auto-categorize and tag screenshots for easy retrieval
- **Vector Search**: Find screenshots using semantic search with vector embeddings
- **Privacy-Focused**: Avoids capturing sensitive apps like banking or messaging
- **Text Extraction**: Uses OCR to extract text from screenshots for search
- **AI Integration**: Uses LLM APIs to analyze and describe screenshots

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture Pattern**: MVVM
- **Vector Storage**: SQLite with VSS extension 
- **Dependency Injection**: Hilt
- **Background Processing**: WorkManager
- **Image Loading**: Coil

## Technical Implementation

### Core Components

- **Capture Service**: Uses MediaProjection API to capture screenshots
- **Floating Button**: Overlay service with SYSTEM_ALERT_WINDOW permission
- **Database**: Room with SQLite-VSS extension for vector similarity search
- **Analysis**: Hybrid approach with OCR and LLM API integration
- **Search**: Combines text and vector similarity search for intelligent results

### Key Features

1. **Screenshot Capture**
   - Floating button overlay that works in any app
   - Uses MediaProjection API to capture the screen
   - Respects privacy by avoiding sensitive apps

2. **Intelligent Analysis**
   - OCR text extraction from screenshots
   - Vector embeddings generation for similarity search
   - AI-powered categorization and tagging
   - Automatic description generation

3. **Advanced Search**
   - Full-text search on extracted content
   - Semantic search using vector similarity
   - Filter by app, category, or tags
   - Visual gallery with relevant metadata

4. **Privacy & Security**
   - Local processing when possible
   - Detects and skips sensitive applications
   - Customizable privacy settings

## Getting Started

1. Add your LLM API key in the settings
2. Grant the necessary permissions (overlay and screen capture)
3. Use the floating button to capture screenshots
4. Search and organize your screenshots in the app

## License

MIT License