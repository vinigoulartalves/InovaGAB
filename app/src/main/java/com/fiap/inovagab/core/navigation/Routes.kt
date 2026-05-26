package com.fiap.inovagab.core.navigation

object Routes {
    const val LOGIN = "login"

    const val HOME_OPERADOR = "home_operador"
    const val IDEIA_FORM = "ideia_form"
    const val MINHAS_IDEIAS = "minhas_ideias"

    const val HOME_GESTOR = "home_gestor"
    const val GESTAO_IDEIAS = "gestao_ideias"

    const val PROJETO_FORM_ARG_ID = "projetoId"
    const val PROJETO_FORM = "projeto_form?$PROJETO_FORM_ARG_ID={$PROJETO_FORM_ARG_ID}"

    fun projetoFormNovo(): String = "projeto_form"

    fun projetoFormEdicao(id: String): String = "projeto_form?$PROJETO_FORM_ARG_ID=$id"

    const val HOME_LIDER = "home_lider"
    const val DASHBOARD = "dashboard"

    const val ORIENTACOES_LIST = "orientacoes_list"
    const val ORIENTACAO_FORM_ARG_ID = "orientacaoId"
    const val ORIENTACAO_FORM = "orientacao_form?$ORIENTACAO_FORM_ARG_ID={$ORIENTACAO_FORM_ARG_ID}"

    fun orientacaoFormNova(): String = "orientacao_form"

    fun orientacaoFormEdicao(id: String): String = "orientacao_form?$ORIENTACAO_FORM_ARG_ID=$id"

    const val PROJETOS_LIST = "projetos_list"
    const val RANKING = "ranking"
}
