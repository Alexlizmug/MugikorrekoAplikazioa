package com.example.taldea5

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(
    val erabiltzailea: String,
    val pasahitza: String
)

data class LangileaResponse(
    val id: Int,
    val izena: String,
    val abizena: String,
    val email: String? = null,
    val telefonoa: String? = null,
    val baimena: Int? = null,
    val erabiltzailea: String
)

data class ProduktuaDto(
    val id: Int,
    val izena: String?,
    val prezioa: Float?,
    val stock: Int? = null,
    val irudiaPath: String? = null,
    val produktuenMotakId: Int
)

data class EskaeraLineRequest(
    val produktuaId: Int?,
    val izena: String?,
    val prezioa: Double?,
    val data: String?,
    val egoera: Int?
)

data class ZerbitzuaCreateRequest(
    val prezioTotala: Double?,
    val data: String?,
    val erreserbaId: Int?,
    val mahaiakId: Int?,
    val eskaerak: List<EskaeraLineRequest>
)

data class ZerbitzuaResponse(
    val id: Int
)

data class FakturaCreateRequest(
    val prezioTotala: Double?,
    val data: String?,
    val mahaiakId: Int?
)

data class FakturaResponse(
    val id: Int
)

interface ApiService {

    @POST("api/Langileak/login")
    suspend fun login(@Body body: LoginRequest): Response<LangileaResponse>

    @GET("api/Produktuak")
    suspend fun getProduktuak(): Response<List<ProduktuaDto>>

    @POST("api/Zerbitzua")
    suspend fun createZerbitzua(@Body body: ZerbitzuaCreateRequest): Response<ZerbitzuaResponse>

    @POST("api/Faktura")
    suspend fun createFaktura(@Body body: FakturaCreateRequest): Response<FakturaResponse>
}
