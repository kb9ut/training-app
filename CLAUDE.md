# PROR - パーソナルレコード特化トレーニング記録アプリ

パッケージ `com.kb9ut.pror` | Git: https://github.com/kb9ut/training-app.git
Kotlin + Jetpack Compose + Material 3 + Hilt + Room + DataStore + Vico Charts
Clean Architecture (data→domain→ui) / MVVM / 完全オフライン / OLED Dark固定

## 開発ルール
- ビルド確認: `./gradlew compileDebugKotlin` (ktlint+detekt自動実行, ignoreFailures=true)
- 並列作業: 独立サブタスクはAgentで並列実行
- アプリ名 **PROR** 統一。競合アプリ具体名は記載禁止
- 全テキストはstring resources使用 (values/strings.xml + values-ja/strings.xml)
- 新画面: Screen sealed class にルート定義 → ProrApp.kt にcomposable登録

## デザイン方針
**Industrial Neo-Brutalism + Premium Dark Mode**
- Primary: Dark Metallic Charcoal (#1A1A1A) + Brushed Steel (#8A8A8A)
- Accent: Blood Orange (#FF4500) | PR Gold: #FFD54F | Error: #FF3333
- Neon Glow: Energy lines in Blood Orange / Neon Red (#FF1744)
- Surface階層: #0A0A0A→#111111→#1A1A1A→#222222→#2A2A2A→#333333
- Typography: Heavy rugged serif for metrics, clean sans-serif for description
- Texture: コンクリート/ラバージムマット風の微細テクスチャ
- GlassCard → MetalCard (steel border, industrial shadow)
- 詳細仕様: UIUX_DESIGN_SPEC.md 参照

## タスク → TASK.md 参照
