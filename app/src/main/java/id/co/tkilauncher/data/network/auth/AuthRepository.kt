package id.co.tkilauncher.data.network.auth

import id.co.tkilauncher.data.local.UserPreferences
import id.co.tkilauncher.data.network.BaseRepository
import id.co.tkilauncher.data.network.model.ModelLogin
import javax.inject.Inject

class AuthRepository @Inject constructor (
    private val api: AuthApi,
    private val userPreferences: UserPreferences
): BaseRepository(){
    suspend fun login(
        username: String,
        password: String
    ) = safeApiCall({
        api.login(ModelLogin(username, password))
    },
        userPreferences
    )

    suspend fun checkUpdate(
        id: String
    ) = safeApiCall({
        api.checkUpdate(id)
    },
        userPreferences
    )

    suspend fun getPackageApp() = safeApiCall({
        api.getPackageApp()
    },
        userPreferences
    )
}