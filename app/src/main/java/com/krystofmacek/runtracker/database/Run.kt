package com.krystofmacek.runtracker.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


/** Table representing record of Run
*   img = image for run (nullable)
*   timestamp = date of the run
*   avgSpeedInKMH = average running speed km/h
*   distanceInMeters = distance traveled in Meters
*   timeInMillis = time length of the run in Milliseconds
*   caloriesBurned = burned calories during run
 *
 *
 *   An entity class defines a table, and each instance of that class represents a row in the table. Each property defines a column.
 *
* */
@Entity(tableName = "run_table")
class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0F,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
) {
    // room handles generation of ID
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}