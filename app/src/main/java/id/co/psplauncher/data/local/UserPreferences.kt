package id.co.psplauncher.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "psp_mobile_data_store")

class UserPreferences @Inject constructor(@ApplicationContext context: Context){

    private val appContext = context.applicationContext


    val accessToken: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN] ?: ""
        }

    suspend fun saveAccessToken(accessToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    fun getAccessToken() = runBlocking(Dispatchers.IO) {
        accessToken.first()
    }

    suspend fun saveActivePackageList(activePackageList: String) {
        appContext.dataStore.edit { preferences ->
            preferences[ACTIVE_PACKAGE_LIST] = activePackageList
        }
    }
    val packageList: Flow<String>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ACTIVE_PACKAGE_LIST] ?: ""
        }


    fun getActivePackageList(): String {
        return runBlocking { packageList.first() }
    }


    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val ACTIVE_PACKAGE_LIST = stringPreferencesKey("active_package_list")
    }

}