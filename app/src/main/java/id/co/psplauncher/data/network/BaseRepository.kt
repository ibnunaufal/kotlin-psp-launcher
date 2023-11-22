package id.co.psplauncher.data.network

import android.util.Log
import id.co.psplauncher.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.Response

abstract class BaseRepository {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>,
        userPreferences: UserPreferences,
    ) : Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall.invoke()
                val tokenBearer = response.headers()["Authorization"]
                if (tokenBearer != null) {
                    val tmp = tokenBearer.split(" ")
                    userPreferences.saveAccessToken(tmp[1])
                }
                if (response.isSuccessful) {
                    Resource.Success(response.body()!!)
                } else {
                    val message = if (response.code() == 401) {
                        "Username dan password tidak valid"
                    } else {
                        val errorJSONObject = JSONObject(response.errorBody()?.string().toString())
                        errorJSONObject.get("message").toString()
                    }
                    Resource.Failure(
                        false,
                        response.code(),
                        message.toResponseBody("text/json".toMediaTypeOrNull())
                    )
                }
            } catch (ex: Exception) {
                Log.e("Error", ex.message.toString())
                Resource.Failure(true, null, null)
            }
        }
    }
}