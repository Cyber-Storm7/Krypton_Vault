package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.VaultRepository

class KryptonApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: VaultRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = VaultRepository(database.vaultDao())
        com.example.util.SecurityPreferencesManager.init(this)
    }

    companion object {
        lateinit var instance: KryptonApplication
            private set
    }
}
