package com.exampledmitryvafin.unicarpool.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.exampledmitryvafin.unicarpool.data.dao.ParticipacionDao
import com.exampledmitryvafin.unicarpool.data.dao.UsuarioDao
import com.exampledmitryvafin.unicarpool.data.dao.ViajeDao
import com.exampledmitryvafin.unicarpool.data.entity.Participacion
import com.exampledmitryvafin.unicarpool.data.entity.Usuario
import com.exampledmitryvafin.unicarpool.data.entity.Viaje

@Database(
    entities = [Usuario::class, Viaje::class, Participacion::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun viajeDao(): ViajeDao
    abstract fun participacionDao(): ParticipacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unicarpool_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
