# PROR UIUX Design Specification

## コンセプト
**Industrial Neo-Brutalism meets Premium Dark Mode**
鍛造された暗いメタリックチャコールとブラッシュドスチールの質感。
Blood OrangeとNeon Redのグロー効果とエネルギーライン。
ミニマルだがインパクトのあるレイアウト。高精度・高パフォーマンスの印象。

---

## カラーパレット

### Primary
| 名前 | Hex | 用途 |
|------|-----|------|
| Charcoal | #1A1A1A | メインSurface |
| Brushed Steel | #8A8A8A | セカンダリテキスト, ボーダー |
| Dark Steel | #555555 | disabled, subtle border |

### Accent
| 名前 | Hex | 用途 |
|------|-----|------|
| Blood Orange | #FF4500 | CTA, 選択状態, アクセント |
| Neon Red | #FF1744 | グロー効果, エネルギーライン |
| Ember | #CC3700 | pressed/muted accent |
| PR Gold | #FFD54F | PR達成, トロフィー |
| Error Red | #FF3333 | エラー, 残り10秒警告 |

### Surface階層
| Level | Hex | 用途 |
|-------|-----|------|
| Base | #0A0A0A | 背景 (OLED黒に近い) |
| Surface | #111111 | カード背景 |
| SurfaceContainer | #1A1A1A | 入力フィールド背景 |
| SurfaceHigh | #222222 | elevated cards |
| SurfaceBright | #2A2A2A | hover/active states |
| SurfaceMax | #333333 | modal/sheet背景 |

### OnColors
| 名前 | Hex |
|------|-----|
| OnSurface (Primary text) | #F0F0F0 |
| OnSurfaceVariant | #8A8A8A |
| OnAccent | #0A0A0A |

---

## タイポグラフィ

### Metrics/Numbers (重要な数値)
- FontFamily: Heavy/ExtraBold (system default bold)
- 大きな数値: displaySmall〜displayMedium, ExtraBold, Blood Orange
- セットの重量/回数: titleMedium, Bold, OnSurface

### Labels/Description
- FontFamily: System sans-serif
- セクションヘッダー: titleSmall, uppercase, Brushed Steel (#8A8A8A), letterSpacing 1.5sp
- Body: bodyMedium, OnSurface (#F0F0F0)
- Caption: labelSmall, OnSurfaceVariant

---

## コンポーネント

### MetalCard (旧GlassCard)
- Background: Surface (#111111)
- Border: 1dp, Brushed Steel 20% opacity
- CornerRadius: 12dp (シャープ寄り、Neo-Brutalism)
- Shadow: subtle drop shadow (2dp elevation)
- gradient: false by default (フラットメタリック)
- onClick対応

### AccentCard (旧AccentGlassCard)
- Background: Blood Orange 10% opacity
- Border: 1.5dp, Blood Orange 40% opacity
- CornerRadius: 12dp
- Glow: subtle Blood Orange outer glow (shadow with accent color)

### Buttons
- Primary: Blood Orange bg, #0A0A0A text, 12dp角, Bold
- Secondary: Transparent bg, Blood Orange border 1dp, Blood Orange text
- Destructive: Error Red bg

### Input Fields
- Background: SurfaceContainer (#1A1A1A)
- Border: 1dp Dark Steel, focus時 Blood Orange
- CornerRadius: 8dp (よりシャープ)
- Text: OnSurface

### Bottom Navigation
- Background: #0A0A0A (OLED黒)
- Selected: Blood Orange icon + text
- Unselected: Brushed Steel icon + text
- Indicator: Blood Orange 10% opacity
- Top border: 0.5dp Dark Steel

### TopAppBar
- Background: Transparent (OLED黒と一体化)
- Title: titleLarge, OnSurface, Bold

---

## 画面別改善

### 共通パターン
- TopAppBar: containerColor=Transparent
- Card → MetalCard に統一
- セクションヘッダー: titleSmall, uppercase, Brushed Steel, letterSpacing 1.5sp
- リスト行: 右シェブロンアイコン (Brushed Steel)

### HomeScreen
1. QuickStart: AccentCard, Blood Orange play icon 36dp, padding 28dp
2. StatCards: MetalCard, spacing 16dp, 数値はBlood Orange+Bold
3. Recent Workouts: MetalCard, 右シェブロン, セクションヘッダー uppercase
4. 最下部 16dp padding

### ActiveWorkoutScreen
1. 経過時間: labelMedium, Blood Orange
2. Finishボタン: Icons.Filled.Check, Blood Orange bg, labelLarge+Bold
3. Add Exercise: height 52dp, Secondary button style
4. スーパーセットラベル: pill形状, Neon Red 15% opacity bg, 8dp角
5. セットテーブルヘッダー: Set/Previous/Weight/Reps/RIR/Done (labelSmall, Brushed Steel)
6. ExerciseCard: MetalCard

### SettingsScreen
1. セクションヘッダー: uppercase, Brushed Steel, labelSmall, letterSpacing 1.5sp
2. HorizontalDivider: 0.5dp, Dark Steel
3. リーディングアイコン各設定に追加
4. 現在値: Blood Orange
5. Data セクション: MetalCard wrap

### ExerciseListScreen
1. 右シェブロンアイコン
2. FilterChip selected: Blood Orange bg, OnAccent text
3. FAB: Blood Orange bg, OnAccent content, 12dp角

### ExerciseDetailScreen
1. PR値: PR Gold (#FFD54F)
2. 筋肉グループ/器具: pill chip (Dark Steel border)
3. MetalCard統一

### CalendarScreen
1. 月名: titleLarge, Bold
2. ワークアウト日ドット: Blood Orange glow
3. 今日: Blood Orange 5% opacity背景
4. 選択日: Bold
5. ワークアウトカード: MetalCard, 右シェブロン

### ProgressScreen
1. Exercise selector: 8dp角, Blood Orange focus
2. PR summary値: PR Gold
3. Line chart: Blood Orange線, 10% opacity fill, 4dp data point, 高さ240dp
4. SegmentedButton: selected=Blood Orange/OnAccent
5. セクションヘッダー: uppercase, Brushed Steel

### RoutineListScreen
1. ルーティンカード: MetalCard, Blood Orange play icon
2. Browse Programs: AccentCard + アイコン + 説明
3. Program browser: SurfaceMax bg, MetalCardプリセット

### RoutineEditorScreen
1. テキストフィールド: Blood Orange focus, 8dp角
2. ExerciseテンプレートCard: MetalCard
3. Add exercise: Secondary button

### WorkoutDetailScreen
1. Duration: displaySmall (36sp, ExtraBold), Blood Orange
2. ExerciseCard: MetalCard
3. PR: PR Gold

### WorkoutSummaryScreen
1. タイトル: headlineLarge, Bold
2. 統計値: titleLarge + ExtraBold + Blood Orange
3. Back to Home: full-width, Blood Orange bg, OnAccent text, 52dp, Icons.Filled.Home
4. PRセクション: PR Gold border

### PlateCalculatorScreen
1. Input: Blood Orange focus
2. Bar weight chips: selected=Blood Orange/OnAccent
3. バーベルビジュアル: MetalCard wrap

### RmCalculatorScreen
1. テーブル: 交互行背景 (Surface / Transparent)
2. 1RM行: Blood Orange highlight
3. Median値: Blood Orange

### RestTimerOverlay
1. CircularProgressIndicator stroke: 8dp, Blood Orange
2. 時刻表示: displayMedium (45sp), Bold
3. Surface: SurfaceMax + Dark Steel上ボーダー
4. 折りたたみ: padding 14dp
5. 残り10秒未満: Error Red

### PrAchievementOverlay
1. Border: 2.5dp, PR Gold
2. Trophy/PR icon: PR Gold glow effect

---

## 優先順位

| # | 変更 | 影響 | 工数 |
|---|------|------|------|
| 1 | Color.kt パレット変更 | 全画面 | 低 |
| 2 | Bottom Nav | 全画面 | 低 |
| 3 | TopAppBar transparent | 全画面 | 低 |
| 4 | MetalCard (旧GlassCard) | 全カード | 中 |
| 5 | Button標準化 | インタラクション統一 | 中 |
| 6 | Input field標準化 | フォーム統一 | 低 |
| 7 | セクションヘッダー | 視覚リズム | 低 |
| 8 | Home改善 | 最初に見る画面 | 中 |
| 9 | ActiveWorkout改善 | 最も使う画面 | 中 |
| 10 | RestTimer改善 | 毎ワークアウト | 低 |
| 11 | WorkoutSummary | 達成感の演出 | 中 |
| 12 | Settings icons | 低頻度画面 | 低 |
| 13 | Progress chart | 分析価値 | 高 |
