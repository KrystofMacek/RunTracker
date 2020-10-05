package com.krystofmacek.runtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.krystofmacek.runtracker.database.RunDatabase
import com.krystofmacek.runtracker.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.krystofmacek.runtracker.other.Constants.KEY_NAME
import com.krystofmacek.runtracker.other.Constants.KEY_WEIGHT
import com.krystofmacek.runtracker.other.Constants.RUN_DATABASE_NAME
import com.krystofmacek.runtracker.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * @InstallIn specifies object inside which will dependencies of this module exist
 *  -> dependencies will be created in onCreate() method of specified object and live as long as the component
*/
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    // Creates and injects Database instance
    @Provides
    @Singleton
    fun provideRunDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunDatabase::class.java,
        RUN_DATABASE_NAME
    ).build()


    // Creates and injects DAO of Run database (using Database)
    @Provides
    @Singleton
    // as a parameter we specify our database - that will be now injected automatically as defined in provideRunDatabase()
    fun provideRunDatabaseDao(db: RunDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

}