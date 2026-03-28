package com.kb9ut.pror.data.local.seed

import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.ExerciseEntity
import com.kb9ut.pror.data.local.entity.MuscleGroup
import java.util.Locale

object ExerciseSeedData {

    fun getAll(locale: Locale = Locale.getDefault()): List<ExerciseEntity> {
        val isJapanese = locale.language == "ja"
        return if (isJapanese) japaneseExercises() else englishExercises()
    }

    private fun englishExercises(): List<ExerciseEntity> = listOf(
        // Chest
        ExerciseEntity(name = "Barbell Bench Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Incline Barbell Bench Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Decline Barbell Bench Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Bench Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Incline Dumbbell Bench Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Dumbbell Fly", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Incline Dumbbell Fly", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Cable Fly", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Machine Chest Press", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Pec Deck", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Push Up", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Dip (Chest)", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // Back
        ExerciseEntity(name = "Barbell Row", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Deadlift", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Row", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Pull Up", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Chin Up", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Lat Pulldown", muscleGroup = MuscleGroup.LATS, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Seated Cable Row", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "T-Bar Row", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Machine Row", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.MACHINE),

        // Shoulders
        ExerciseEntity(name = "Overhead Press", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Shoulder Press", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Lateral Raise", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Front Raise", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Rear Delt Fly", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Face Pull", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Machine Shoulder Press", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Cable Lateral Raise", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.CABLE),

        // Biceps
        ExerciseEntity(name = "Barbell Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Hammer Curl", muscleGroup = MuscleGroup.BICEPS, secondaryMuscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Preacher Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Incline Dumbbell Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Cable Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Concentration Curl", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // Triceps
        ExerciseEntity(name = "Tricep Pushdown", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Overhead Tricep Extension", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Skull Crusher", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Close Grip Bench Press", muscleGroup = MuscleGroup.TRICEPS, secondaryMuscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dip (Tricep)", muscleGroup = MuscleGroup.TRICEPS, secondaryMuscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Tricep Kickback", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // Quadriceps
        ExerciseEntity(name = "Barbell Squat", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Front Squat", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Leg Press", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Leg Extension", muscleGroup = MuscleGroup.QUADRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Hack Squat", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Bulgarian Split Squat", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Goblet Squat", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Lunge", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),

        // Hamstrings
        ExerciseEntity(name = "Romanian Deadlift", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Lying Leg Curl", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Seated Leg Curl", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Stiff Leg Deadlift", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Romanian Deadlift", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "Nordic Hamstring Curl", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // Glutes
        ExerciseEntity(name = "Hip Thrust", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Glute Bridge", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Cable Pull Through", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Glute Kickback (Machine)", muscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),

        // Calves
        ExerciseEntity(name = "Standing Calf Raise", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Seated Calf Raise", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "Leg Press Calf Raise", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),

        // Abs
        ExerciseEntity(name = "Crunch", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Cable Crunch", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Hanging Leg Raise", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "Ab Wheel Rollout", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.OTHER),
        ExerciseEntity(name = "Plank", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // Traps
        ExerciseEntity(name = "Barbell Shrug", muscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Dumbbell Shrug", muscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // Forearms
        ExerciseEntity(name = "Wrist Curl", muscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "Reverse Wrist Curl", muscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.BARBELL)
    )

    private fun japaneseExercises(): List<ExerciseEntity> = listOf(
        // 胸
        ExerciseEntity(name = "バーベルベンチプレス", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "インクラインバーベルベンチプレス", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "デクラインバーベルベンチプレス", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルベンチプレス", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "インクラインダンベルベンチプレス", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ダンベルフライ", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "インクラインダンベルフライ", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ケーブルフライ", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "チェストプレスマシン", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "ペックデック", muscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "プッシュアップ", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "ディップス(胸)", muscleGroup = MuscleGroup.CHEST, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // 背中
        ExerciseEntity(name = "バーベルロウ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "デッドリフト", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルロウ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "懸垂", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "チンアップ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "ラットプルダウン", muscleGroup = MuscleGroup.LATS, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "シーテッドケーブルロウ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "Tバーロウ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "マシンロウ", muscleGroup = MuscleGroup.BACK, secondaryMuscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.MACHINE),

        // 肩
        ExerciseEntity(name = "オーバーヘッドプレス", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルショルダープレス", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "サイドレイズ", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "フロントレイズ", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "リアデルトフライ", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "フェイスプル", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "ショルダープレスマシン", muscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "ケーブルサイドレイズ", muscleGroup = MuscleGroup.SHOULDERS, equipmentCategory = EquipmentCategory.CABLE),

        // 二頭筋
        ExerciseEntity(name = "バーベルカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ハンマーカール", muscleGroup = MuscleGroup.BICEPS, secondaryMuscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "プリーチャーカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "インクラインダンベルカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ケーブルカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "コンセントレーションカール", muscleGroup = MuscleGroup.BICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // 三頭筋
        ExerciseEntity(name = "トライセプスプッシュダウン", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "オーバーヘッドトライセプスエクステンション", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "スカルクラッシャー", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ナローベンチプレス", muscleGroup = MuscleGroup.TRICEPS, secondaryMuscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ディップス(三頭筋)", muscleGroup = MuscleGroup.TRICEPS, secondaryMuscleGroup = MuscleGroup.CHEST, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "トライセプスキックバック", muscleGroup = MuscleGroup.TRICEPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // 大腿四頭筋
        ExerciseEntity(name = "バーベルスクワット", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "フロントスクワット", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "レッグプレス", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "レッグエクステンション", muscleGroup = MuscleGroup.QUADRICEPS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "ハックスクワット", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "ブルガリアンスプリットスクワット", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ゴブレットスクワット", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ランジ", muscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),

        // ハムストリングス
        ExerciseEntity(name = "ルーマニアンデッドリフト", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ライイングレッグカール", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "シーテッドレッグカール", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "スティッフレッグデッドリフト", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルルーマニアンデッドリフト", muscleGroup = MuscleGroup.HAMSTRINGS, secondaryMuscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.DUMBBELL),
        ExerciseEntity(name = "ノルディックハムストリングカール", muscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // 臀部
        ExerciseEntity(name = "ヒップスラスト", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "グルートブリッジ", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "ケーブルプルスルー", muscleGroup = MuscleGroup.GLUTES, secondaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "グルートキックバック(マシン)", muscleGroup = MuscleGroup.GLUTES, equipmentCategory = EquipmentCategory.MACHINE),

        // ふくらはぎ
        ExerciseEntity(name = "スタンディングカーフレイズ", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "シーテッドカーフレイズ", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),
        ExerciseEntity(name = "レッグプレスカーフレイズ", muscleGroup = MuscleGroup.CALVES, equipmentCategory = EquipmentCategory.MACHINE),

        // 腹筋
        ExerciseEntity(name = "クランチ", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "ケーブルクランチ", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.CABLE),
        ExerciseEntity(name = "ハンギングレッグレイズ", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),
        ExerciseEntity(name = "アブローラー", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.OTHER),
        ExerciseEntity(name = "プランク", muscleGroup = MuscleGroup.ABS, equipmentCategory = EquipmentCategory.BODYWEIGHT),

        // 僧帽筋
        ExerciseEntity(name = "バーベルシュラッグ", muscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "ダンベルシュラッグ", muscleGroup = MuscleGroup.TRAPS, equipmentCategory = EquipmentCategory.DUMBBELL),

        // 前腕
        ExerciseEntity(name = "リストカール", muscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.BARBELL),
        ExerciseEntity(name = "リバースリストカール", muscleGroup = MuscleGroup.FOREARMS, equipmentCategory = EquipmentCategory.BARBELL)
    )
}
