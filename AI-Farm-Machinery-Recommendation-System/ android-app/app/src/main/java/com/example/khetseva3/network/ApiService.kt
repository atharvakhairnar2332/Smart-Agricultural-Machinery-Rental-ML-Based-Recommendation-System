package com.example.khetseva3.network
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import com.example.khetseva3.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.DELETE
interface ApiService {

    @POST("recommend")
    fun getRecommendation(
        @Body request: RecommendRequest
    ): Call<RecommendResponse>


    @POST("recommend")
    fun getRecommendationRaw(
        @Body request: RecommendRequest
    ): Call<ResponseBody>

    @DELETE("machine/{machine_id}")

    fun deleteMachine(

        @Path("machine_id")
        machineId: Int

    ): Call<Map<String, String>>

    @DELETE("undo-request/{machineId}/{phone}")
    fun undoRequest(

        @Path("machineId") machineId: Int,

        @Path("phone") phone: String

    ): Call<Map<String, String>>

    @GET("my-requests/{phone}")
    fun getMyRequests(
        @Path("phone") phone: String
    ): Call<List<MyRequest>>

    @PUT("change-password")
    fun changePassword(

        @Body request: ChangePasswordRequest

    ): Call<Map<String,String>>

    @GET("my-machines/{phone}")
    fun getMyMachines(
        @Path("phone") phone: String
    ): Call<MyMachinesResponse>

    @PUT("update-profile")
    fun updateProfile(

        @Body request: UpdateProfileRequest

    ): Call<Map<String, String>>

    @POST("machines/add")
    fun addMachine(
        @Body request: AddMachineRequest
    ): Call<Map<String, String>>

    @GET("profile/{phone}")
    fun getProfile(

        @Path("phone")
        phone: String

    ): Call<UpdateProfileRequest>

    @PUT("machines/update/{machine_id}")
    fun updateMachine(
        @Path("machine_id") machineId: Int,
        @Body request: AddMachineRequest
    ): Call<Map<String, String>>

    @GET("notifications/{phone}")
    fun getNotifications(
        @Path("phone") phone: String
    ): Call<List<MachineRequest>>

    @PUT("request-status/{id}")
    fun updateRequestStatus(
        @Path("id") id: Int,
        @Body body: UpdateStatusBody
    ): Call<Map<String, String>>

    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<Map<String, String>>

    @POST("request-machine")
    fun requestMachine(
        @Body body: RequestMachineBody
    ): Call<Map<String, String>>

    @Multipart
    @POST("upload-image")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ImageUploadResponse>
    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}

