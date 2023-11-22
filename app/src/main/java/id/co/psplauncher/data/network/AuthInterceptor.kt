package id.co.psplauncher.data.network

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import id.co.psplauncher.data.local.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(@ApplicationContext context: Context): Interceptor {
    private val userPreferences: UserPreferences = UserPreferences(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        Log.d("request : ", requestBuilder.toString())
        userPreferences.getAccessToken().let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 200) {
            val allHeaders = response.headers
            Log.d("token : ", allHeaders.toString())
            val tokenBearer = allHeaders.get("Authorization")

            if (tokenBearer != null) {
                val tmp = tokenBearer.split(" ")

                runBlocking { userPreferences.saveAccessToken(tmp[1]) }
            }
        }
        Log.d(
            "Respone Message : ",
            response.message + "\n" + response.body + "\n" + "Respone Code :" + response.code + "\n" + "Requet :" + response.request + "\n"
        )
        return response
    }

}