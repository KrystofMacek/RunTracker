package com.krystofmacek.runtracker.di

import android.content.Context
import androidx.room.Room
import com.krystofmacek.runtracker.database.RunDatabase
import com.krystofmacek.runtracker.other.Constants.RUN_DATABASE_NAME
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

}