package com.kb9ut.pror.data.local.converter

import androidx.room.TypeConverter
import com.kb9ut.pror.data.local.entity.EquipmentCategory
import com.kb9ut.pror.data.local.entity.GroupType
import com.kb9ut.pror.data.local.entity.MuscleGroup
import com.kb9ut.pror.data.local.entity.SetType

class Converters {
    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun fromEquipmentCategory(value: EquipmentCategory): String = value.name

    @TypeConverter
    fun toEquipmentCategory(value: String): EquipmentCategory = EquipmentCategory.valueOf(value)

    @TypeConverter
    fun fromSetType(value: SetType): String = value.name

    @TypeConverter
    fun toSetType(value: String): SetType = SetType.valueOf(value)

    @TypeConverter
    fun fromGroupType(value: GroupType): String = value.name

    @TypeConverter
    fun toGroupType(value: String): GroupType = GroupType.valueOf(value)
}
