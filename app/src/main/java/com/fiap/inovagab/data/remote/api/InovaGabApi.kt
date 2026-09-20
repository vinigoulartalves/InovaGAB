package com.fiap.inovagab.data.remote.api

import com.fiap.inovagab.data.remote.dto.DashboardRelatorioDto
import com.fiap.inovagab.data.remote.dto.EstrategiaCreateRequestDto
import com.fiap.inovagab.data.remote.dto.EstrategiaDetalheDto
import com.fiap.inovagab.data.remote.dto.EstrategiaResumoDto
import com.fiap.inovagab.data.remote.dto.EstrategiaUpdateRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaAvaliacaoRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaCreateRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaDetalheDto
import com.fiap.inovagab.data.remote.dto.IdeiaResumoDto
import com.fiap.inovagab.data.remote.dto.LoginRequestDto
import com.fiap.inovagab.data.remote.dto.LoginResponseDto
import com.fiap.inovagab.data.remote.dto.LogoutRequestDto
import com.fiap.inovagab.data.remote.dto.PagedResultDto
import com.fiap.inovagab.data.remote.dto.ProjetoCreateRequestDto
import com.fiap.inovagab.data.remote.dto.ProjetoDetalheDto
import com.fiap.inovagab.data.remote.dto.ProjetoResumoDto
import com.fiap.inovagab.data.remote.dto.ProjetoUpdateRequestDto
import com.fiap.inovagab.data.remote.dto.RankingResponseDto
import com.fiap.inovagab.data.remote.dto.RefreshRequestDto
import com.fiap.inovagab.data.remote.dto.ResponsavelResumoDto
import com.fiap.inovagab.data.remote.dto.UsuarioResumoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): LoginResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<Unit>

    @GET("api/v1/auth/me")
    suspend fun me(): UsuarioResumoDto
}

interface EstrategiasApi {
    @GET("api/v1/estrategias")
    suspend fun list(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("vigente") vigente: Boolean? = null
    ): PagedResultDto<EstrategiaResumoDto>

    @GET("api/v1/estrategias/{id}")
    suspend fun get(@Path("id") id: String): EstrategiaDetalheDto

    @POST("api/v1/estrategias")
    suspend fun create(@Body body: EstrategiaCreateRequestDto): EstrategiaDetalheDto

    @PUT("api/v1/estrategias/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: EstrategiaUpdateRequestDto
    ): EstrategiaDetalheDto

    @DELETE("api/v1/estrategias/{id}")
    suspend fun delete(
        @Path("id") id: String,
        @Header("If-Match") ifMatch: String
    ): Response<Unit>
}

interface IdeiasApi {
    @GET("api/v1/ideias")
    suspend fun list(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PagedResultDto<IdeiaResumoDto>

    @GET("api/v1/ideias/{id}")
    suspend fun get(@Path("id") id: String): IdeiaDetalheDto

    @POST("api/v1/ideias")
    suspend fun create(@Body body: IdeiaCreateRequestDto): IdeiaDetalheDto

    @PATCH("api/v1/ideias/{id}/avaliacao")
    suspend fun avaliar(
        @Path("id") id: String,
        @Body body: IdeiaAvaliacaoRequestDto
    ): IdeiaDetalheDto
}

interface ProjetosApi {
    @GET("api/v1/projetos")
    suspend fun list(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PagedResultDto<ProjetoResumoDto>

    @GET("api/v1/projetos/{id}")
    suspend fun get(@Path("id") id: String): ProjetoDetalheDto

    @POST("api/v1/projetos")
    suspend fun create(@Body body: ProjetoCreateRequestDto): ProjetoDetalheDto

    @PUT("api/v1/projetos/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: ProjetoUpdateRequestDto
    ): ProjetoDetalheDto
}

interface RelatoriosApi {
    @GET("api/v1/relatorios/dashboard")
    suspend fun dashboard(
        @Query("estrategiaId") estrategiaId: String? = null
    ): DashboardRelatorioDto
}

interface RankingApi {
    @GET("api/v1/ranking")
    suspend fun ranking(): RankingResponseDto
}

interface UsuariosApi {
    @GET("api/v1/usuarios/responsaveis")
    suspend fun responsaveis(): List<ResponsavelResumoDto>
}
