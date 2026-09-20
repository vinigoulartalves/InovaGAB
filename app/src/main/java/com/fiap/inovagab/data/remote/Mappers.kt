package com.fiap.inovagab.data.remote

import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.model.StatusProjeto
import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.remote.dto.EstrategiaDetalheDto
import com.fiap.inovagab.data.remote.dto.EstrategiaResumoDto
import com.fiap.inovagab.data.remote.dto.IdeiaDetalheDto
import com.fiap.inovagab.data.remote.dto.IdeiaResumoDto
import com.fiap.inovagab.data.remote.dto.ProjetoDetalheDto
import com.fiap.inovagab.data.remote.dto.ProjetoResumoDto
import com.fiap.inovagab.data.remote.dto.RankingItemDto
import com.fiap.inovagab.data.remote.dto.UsuarioResumoDto
import java.math.BigDecimal
import java.time.Instant

fun UsuarioResumoDto.toUser(pontos: Int = 0): User = User(
    uid = id,
    nome = nome,
    email = email,
    perfil = runCatching { Perfil.valueOf(perfil) }.getOrDefault(Perfil.OPERADOR),
    pontos = pontos
)

fun RankingItemDto.toUser(): User = User(
    uid = "rank-$posicao",
    nome = nome,
    email = "",
    perfil = Perfil.OPERADOR,
    pontos = pontos
)

fun EstrategiaResumoDto.toOrientacao(): Orientacao = Orientacao(
    id = id,
    titulo = titulo,
    descricao = descricao.orEmpty(),
    criadoEm = parseInstantMillis(criadoEm)
)

fun EstrategiaDetalheDto.toOrientacao(): Orientacao = Orientacao(
    id = id,
    titulo = titulo,
    descricao = descricao,
    criadoEm = parseInstantMillis(criadoEm)
)

fun IdeiaResumoDto.toIdeia(): Ideia = Ideia(
    id = id,
    titulo = titulo,
    area = area,
    autorNome = autorNome,
    status = enumValue(status, StatusIdeia.ENVIADA),
    prioridade = enumValue(prioridade, PrioridadeIdeia.MEDIA),
    estrategiaId = estrategiaId,
    criadoEm = parseInstantMillis(criadoEm)
)

fun IdeiaDetalheDto.toIdeia(): Ideia = Ideia(
    id = id,
    titulo = titulo,
    descricao = descricao,
    area = area,
    autorId = autorId,
    autorNome = autorNome,
    status = enumValue(status, StatusIdeia.ENVIADA),
    prioridade = enumValue(prioridade, PrioridadeIdeia.MEDIA),
    estrategiaId = estrategiaId,
    versao = versao,
    criadoEm = parseInstantMillis(criadoEm)
)

fun ProjetoResumoDto.toProjeto(): Projeto = Projeto(
    id = id,
    nome = nome,
    status = enumValue(status, StatusProjeto.PLANEJADO),
    estrategiaId = estrategiaId,
    responsavelId = responsavelId,
    investimento = investimento.toDouble(),
    retornoFinanceiro = retornoFinanceiro.toDouble(),
    criadoEm = parseInstantMillis(criadoEm)
)

fun ProjetoDetalheDto.toProjeto(): Projeto = Projeto(
    id = id,
    nome = nome,
    descricao = descricao,
    ideiaId = ideiaId.orEmpty(),
    responsavel = responsavelNome,
    responsavelId = responsavelId,
    etapa = etapa,
    status = enumValue(status, StatusProjeto.PLANEJADO),
    estrategiaId = estrategiaId,
    versao = versao,
    investimento = investimento.toDouble(),
    retornoFinanceiro = retornoFinanceiro.toDouble(),
    reducaoCustos = reducaoCustos.toDouble(),
    ganhoProdutividade = ganhoProdutividade.toDouble(),
    prazo = prazo,
    criadoEm = parseInstantMillis(criadoEm)
)

fun Double.toMoneyBigDecimal(): BigDecimal = BigDecimal.valueOf(this)

private inline fun <reified T : Enum<T>> enumValue(raw: String, default: T): T =
    runCatching { enumValueOf<T>(raw) }.getOrDefault(default)

fun parseInstantMillis(value: String?): Long {
    if (value.isNullOrBlank()) return System.currentTimeMillis()
    return runCatching { Instant.parse(value).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
}
