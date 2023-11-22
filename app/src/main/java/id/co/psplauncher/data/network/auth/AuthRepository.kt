package id.co.psplauncher.data.network.auth

import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.BaseRepository
import id.co.psplauncher.data.network.model.ModelLogin
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
}