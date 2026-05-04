# Architecture

Clean Architecture, three layers:

- `presentation/` — Compose screens, ViewModels (Loading / Data / Error UDF)
- `domain/` — pure Kotlin: models, repository contracts, use cases
- `data/` — Retrofit API, repositories, mappers, cache, Firebase auth

Single-activity, typed Navigation. Backend: Ktor, see
[server repo](https://github.com/mikhail0vvlad/powerlifting-assistant-server).
