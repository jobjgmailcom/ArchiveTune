# Echo Brain attribution and scope

ArchiveTune is licensed under GPLv3. This fork adds an ArchiveTune-specific Echo Brain planner and concurrency gate adapted from the GPLv3 Echo Brain source supplied by the repository owner. The source package documents the original MetroList integration and its FlowNeuro attribution.

The ArchiveTune adaptation is intentionally narrower at this stage: it uses ArchiveTune's existing local related-song graph and `YouTubeQueue` radio fallback, then performs a single safe insertion behind the active track. It does not replace ArchiveTune playback, remove original queue items, add a server, account, telemetry, cookies, TensorFlow, or Google services.

The changed files retain ArchiveTune's GPLv3 notices. Distributions of the resulting APK must provide corresponding source under GPLv3.
