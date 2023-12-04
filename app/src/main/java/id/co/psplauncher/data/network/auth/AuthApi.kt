package id.co.psplauncher.data.network.auth

import id.co.psplauncher.data.network.model.ModelLogin
import id.co.psplauncher.data.network.response.LoginResponse
import id.co.psplauncher.data.network.response.UpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("katalis/login")
    suspend fun login(
        @Body info: ModelLogin
    ): Response<LoginResponse>

    @GET("main_a/info/google-play/{id}")
    suspend fun checkUpdate(
        @Path("id") id: String
    ): Response<UpdateResponse>
}