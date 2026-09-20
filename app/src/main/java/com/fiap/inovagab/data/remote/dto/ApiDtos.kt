package com.fiap.inovagab.data.remote.dto

import com.squareup.moshi.Json
import java.math.BigDecimal

data class LoginRequestDto(
    val email: String,
    val senha: String
)

data class LoginResponseDto(
    val accessToken: String,
    val expiresAt: String,
    val refreshToken: String,
    val usuario: UsuarioResumoDto
)

data class RefreshRequestDto(
    val refreshToken: String
)

data class LogoutRequestDto(
    val refreshToken: String
)

data class UsuarioResumoDto(
    val id: String,
    val nome: String,
    val email: String,
    val perfil: String
)

data class ResponsavelResumoDto(
    val id: String,
    val nome: String
)

data class PagedResultDto<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)

data class EstrategiaResumoDto(
    val id: String,
    val titulo: String,
    val descricao: String? = null,
    val categoria: String? = null,
    val campanha: String? = null,
    val criadoEm: String? = null
)

data class EstrategiaDetalheDto(
    val id: String,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String,
    val inicioVigencia: String,
    val fimVigencia: String? = null,
    val ativa: Boolean,
    val versao: Int,
    val criadoEm: String? = null
)

data class EstrategiaCreateRequestDto(
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String,
    val inicioVigencia: String,
    val fimVigencia: String? = null,
    val ativa: Boolean = true
)

data class EstrategiaUpdateRequestDto(
    val versao: Int,
    val titulo: String,
    val descricao: String,
    val categoria: String,
    val campanha: String,
    val inicioVigencia: String,
    val fimVigencia: String? = null,
    val ativa: Boolean
)

data class IdeiaResumoDto(
    val id: String,
    val titulo: String,
    val area: String,
    val status: String,
    val prioridade: String,
    val autorNome: String,
    val estrategiaId: String,
    val criadoEm: String
)

data class IdeiaDetalheDto(
    val id: String,
    val titulo: String,
    val area: String,
    val status: String,
    val prioridade: String,
    val autorNome: String,
    val estrategiaId: String,
    val criadoEm: String,
    val descricao: String,
    val autorId: String,
    val estrategiaVersao: Int,
    val versao: Int,
    val atualizadoEm: String? = null
)

data class IdeiaCreateRequestDto(
    val titulo: String,
    val descricao: String,
    val area: String,
    val estrategiaId: String
)

data class IdeiaAvaliacaoRequestDto(
    val versao: Int,
    val status: String,
    val prioridade: String? = null,
    val justificativa: String? = null
)

data class ProjetoResumoDto(
    val id: String,
    val nome: String,
    val status: String,
    val estrategiaId: String,
    val responsavelId: String,
    val investimento: BigDecimal,
    val retornoFinanceiro: BigDecimal,
    val criadoEm: String
)

data class ProjetoDetalheDto(
    val id: String,
    val nome: String,
    val status: String,
    val estrategiaId: String,
    val responsavelId: String,
    val investimento: BigDecimal,
    val retornoFinanceiro: BigDecimal,
    val criadoEm: String,
    val descricao: String,
    val ideiaId: String? = null,
    val estrategiaVersao: Int,
    val responsavelNome: String,
    val etapa: String,
    val reducaoCustos: BigDecimal,
    val ganhoProdutividade: BigDecimal,
    val prazo: String,
    val versao: Int,
    val atualizadoEm: String? = null
)

data class ProjetoCreateRequestDto(
    val nome: String,
    val descricao: String,
    val estrategiaId: String,
    val responsavelId: String,
    val etapa: String,
    val status: String,
    val investimento: BigDecimal,
    val retornoFinanceiro: BigDecimal,
    val reducaoCustos: BigDecimal,
    val ganhoProdutividade: BigDecimal,
    val prazo: String
)

data class ProjetoUpdateRequestDto(
    val versao: Int,
    val nome: String,
    val descricao: String,
    val responsavelId: String,
    val etapa: String,
    val status: String,
    val investimento: BigDecimal,
    val retornoFinanceiro: BigDecimal,
    val reducaoCustos: BigDecimal,
    val ganhoProdutividade: BigDecimal,
    val prazo: String
)

data class DashboardRelatorioDto(
    val investimentoTotal: BigDecimal,
    val retornoTotal: BigDecimal,
    val lucroTotal: BigDecimal,
    @Json(name = "roiPercentual") val roiPercentual: BigDecimal?,
    val reducaoCustosTotal: BigDecimal,
    val ganhoProdutividadeMedio: BigDecimal?,
    val projetosAtrasados: Int
)

data class RankingResponseDto(
    val items: List<RankingItemDto>,
    val atualizadoEm: String
)

data class RankingItemDto(
    val posicao: Int,
    val nome: String,
    val pontos: Int
)
