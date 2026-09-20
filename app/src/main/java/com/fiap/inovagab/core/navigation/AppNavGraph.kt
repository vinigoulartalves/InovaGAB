package com.fiap.inovagab.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fiap.inovagab.InovaGabApp
import com.fiap.inovagab.core.session.AppSession
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.ui.gestor.GestaoIdeiasScreen
import com.fiap.inovagab.ui.gestor.HomeGestorScreen
import com.fiap.inovagab.ui.gestor.ProjetoFormScreen
import com.fiap.inovagab.ui.lider.DashboardScreen
import com.fiap.inovagab.ui.lider.HomeLiderScreen
import com.fiap.inovagab.ui.lider.OrientacaoFormScreen
import com.fiap.inovagab.ui.login.LoginScreen
import com.fiap.inovagab.ui.operador.HomeOperadorScreen
import com.fiap.inovagab.ui.operador.IdeiaFormScreen
import com.fiap.inovagab.ui.operador.MinhasIdeiasScreen
import com.fiap.inovagab.ui.shared.OrientacoesListScreen
import com.fiap.inovagab.ui.shared.ProjetosListScreen
import com.fiap.inovagab.ui.shared.RankingScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null
) {
    val scope = rememberCoroutineScope()
    val app = InovaGabApp.instance
    var resolvedStart by remember { mutableStateOf(startDestination) }

    LaunchedEffect(Unit) {
        if (resolvedStart == null) {
            val restaurou = app.sessionManager.restoreSession()
            resolvedStart = if (restaurou) {
                rotaHome(AppSession.manager.currentUser.value?.perfil)
            } else {
                Routes.LOGIN
            }
        }
        app.sessionManager.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (resolvedStart == null) return

    val logout: () -> Unit = {
        scope.launch {
            app.authRepository.logout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = resolvedStart!!
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSucesso = { perfil ->
                    val destino = when (perfil) {
                        Perfil.OPERADOR -> Routes.HOME_OPERADOR
                        Perfil.GESTOR -> Routes.HOME_GESTOR
                        Perfil.LIDER -> Routes.HOME_LIDER
                    }
                    navController.navigate(destino) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME_OPERADOR) {
            HomeOperadorScreen(
                onVerOrientacoes = { navController.navigate(Routes.ORIENTACOES_LIST) },
                onCadastrarIdeia = { navController.navigate(Routes.ideiaFormNova()) },
                onMinhasIdeias = { navController.navigate(Routes.MINHAS_IDEIAS) },
                onRanking = { navController.navigate(Routes.RANKING) },
                onLogout = logout
            )
        }
        composable(
            route = Routes.IDEIA_FORM,
            arguments = listOf(
                navArgument(Routes.IDEIA_FORM_ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val ideiaId = entry.arguments?.getString(Routes.IDEIA_FORM_ARG_ID)
            IdeiaFormScreen(
                ideiaId = ideiaId,
                onBack = back,
                onSucesso = back
            )
        }
        composable(Routes.MINHAS_IDEIAS) {
            MinhasIdeiasScreen(
                onBack = back,
                onEditar = { id -> navController.navigate(Routes.ideiaFormEdicao(id)) }
            )
        }

        composable(Routes.HOME_GESTOR) {
            HomeGestorScreen(
                onVerOrientacoes = { navController.navigate(Routes.ORIENTACOES_LIST) },
                onGerenciarIdeias = { navController.navigate(Routes.GESTAO_IDEIAS) },
                onCadastrarProjeto = { navController.navigate(Routes.projetoFormNovo()) },
                onProjetos = { navController.navigate(Routes.PROJETOS_LIST) },
                onRanking = { navController.navigate(Routes.RANKING) },
                onLogout = logout
            )
        }
        composable(Routes.GESTAO_IDEIAS) {
            GestaoIdeiasScreen(
                onBack = back,
                onConverterProjeto = { ideiaId ->
                    navController.navigate(Routes.projetoFormConversao(ideiaId))
                }
            )
        }

        composable(
            route = Routes.PROJETO_FORM,
            arguments = listOf(
                navArgument(Routes.PROJETO_FORM_ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(Routes.PROJETO_FORM_ARG_IDEA) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString(Routes.PROJETO_FORM_ARG_ID)
            val ideiaId = entry.arguments?.getString(Routes.PROJETO_FORM_ARG_IDEA)
            ProjetoFormScreen(
                projetoId = id,
                ideiaConversaoId = ideiaId,
                onBack = back,
                onSucesso = back
            )
        }

        composable(Routes.HOME_LIDER) {
            HomeLiderScreen(
                onGerenciarOrientacoes = { navController.navigate(Routes.ORIENTACOES_LIST) },
                onVerProjetos = { navController.navigate(Routes.PROJETOS_LIST) },
                onDashboard = { navController.navigate(Routes.DASHBOARD) },
                onRanking = { navController.navigate(Routes.RANKING) },
                onLogout = logout
            )
        }
        composable(Routes.DASHBOARD) { DashboardScreen(onBack = back) }

        composable(
            route = Routes.ORIENTACAO_FORM,
            arguments = listOf(
                navArgument(Routes.ORIENTACAO_FORM_ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ORIENTACAO_FORM_ARG_ID)
            OrientacaoFormScreen(
                orientacaoId = id,
                onBack = back,
                onSucesso = back
            )
        }

        composable(Routes.ORIENTACOES_LIST) {
            OrientacoesListScreen(
                onBack = back,
                onCriar = { navController.navigate(Routes.orientacaoFormNova()) },
                onEditar = { id -> navController.navigate(Routes.orientacaoFormEdicao(id)) }
            )
        }
        composable(Routes.PROJETOS_LIST) {
            ProjetosListScreen(
                onBack = back,
                onCriar = { navController.navigate(Routes.projetoFormNovo()) },
                onEditar = { id -> navController.navigate(Routes.projetoFormEdicao(id)) }
            )
        }
        composable(Routes.RANKING) { RankingScreen(onBack = back) }
    }
}

private fun rotaHome(perfil: Perfil?): String = when (perfil) {
    Perfil.OPERADOR -> Routes.HOME_OPERADOR
    Perfil.GESTOR -> Routes.HOME_GESTOR
    Perfil.LIDER -> Routes.HOME_LIDER
    null -> Routes.LOGIN
}
