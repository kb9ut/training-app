# PROR タスク一覧

## 修正タスク (緊急)
- [x] Strong CSVインポートエラー修正 (セミコロン区切り、カラム名マッチング、ANR)
- [x] アプリ名を「PROR」に変更
- [x] 全コードベースからironlog/reppen表記を除去しprorに統一 (パッケージ com.kb9ut.pror、クラス名、ファイル名)
- [x] アプリ上部余白修正 (TopAppBar windowInsets二重適用)
- [x] アプリロゴ変更 (ウェイトプレート+P、Blood Orangeアクセント)
- [x] UIUX方針変更: Industrial Neo-Brutalism + Premium Dark Mode (カラーパレット適用済み)
- [x] RIR (Reps In Reserve) 記録機能追加 (DB v4→v5, UI, CSV import)

## UIUX画面別リデザイン
詳細仕様: UIUX_DESIGN_SPEC.md

- [x] HomeScreen, ActiveWorkoutScreen, SettingsScreen
- [x] ExerciseList/Detail, CalendarScreen, ProgressScreen
- [x] RoutineList/Editor, WorkoutDetail/Summary
- [x] PlateCalc/RmCalc, RestTimerOverlay, PrAchievementOverlay

## UI修正
- [x] HomeScreen構成変更: ①ルーティン選択 → ②モチベーション記録(Stats) → ③クイックスタート → ④その他

## バグ修正
- [x] Strongファイルインポート時、CSV内の"Note"行が空セットとして取り込まれ、履歴に空白セット行が表示される
- [x] 実機テスト時にログが永遠に流れ続ける問題の調査・修正
- [x] Strong CSV英語種目名とアプリ内蔵日本語種目名が混在する問題 (名前マッピング追加)
- [x] 進捗画面にデータのない種目が表示される問題 (データあり種目のみに変更)
- [x] 重量表示を小数第2位まで対応 (FormatUtils.formatWeightValue追加、全画面適用)
- [x] 最大重量PRはreps>=1のセットに限定
- [x] ダークモードの色が全体的に見づらい (BrushedSteel/DarkSteel/カード背景のコントラスト改善)
- [x] 推定1RMグラフでポイントタップ時に日付・詳細を表示 (InspectableLineChart追加)
- [x] ナビバーがホーム→種目遷移後にホームボタンで種目のままになる (popUpToパターン統一)
- [x] Importしたワークアウト名が「Imported Workout」になるものがある (日付フォールバック)

## 中長期
- Google Drive バックアップ
- セット動画記録、音声入力、AI提案、Wear OS
