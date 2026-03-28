package com.kb9ut.pror.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("routineId"), Index("startedAt"), Index("finishedAt")]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val durationSeconds: Int? = null,
    val notes: String? = null,
    val routineId: Long? = null
)
