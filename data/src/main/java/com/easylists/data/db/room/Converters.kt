package com.easylists.data.db.room

import androidx.room.TypeConverter
import java.math.BigDecimal
import kotlin.text.toBigDecimal

class Converters {

    //region fromBigDecimal() :: for automatic conversion of BigDecimal to String
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }
    //endregion


    //region fromBigDecimal() :: for automatic conversion of String to BigDecimal
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.toBigDecimal()
    }
    //endregion

}
