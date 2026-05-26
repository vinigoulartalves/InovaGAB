package com.fiap.inovagab.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.repository.AuthRepository
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

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    val authRepository = AuthRepository()

    val logout: () -> Unit = {
        authRepository.logout()
        SessionManager.clear()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onCadastrarIdeia = { navController.navigate(Routes.IDEIA_FORM) },
                onMinhasIdeias = { navController.navigate(Routes.MINHAS_IDEIAS) },
                onRanking = { navController.navigate(Routes.RANKING) },
                onLogout = logout
            )
        }
        composable(Routes.IDEIA_FORM) {
            IdeiaFormScreen(
                onBack = back,
                onSucesso = back
            )
        }
        composable(Routes.MINHAS_IDEIAS) { MinhasIdeiasScreen(onBack = back) }

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
        composable(Routes.GESTAO_IDEIAS) { GestaoIdeiasScreen(onBack = back) }

        composable(
            route = Routes.PROJETO_FORM,
            arguments = listOf(
                navArgument(Routes.PROJETO_FORM_ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString(Routes.PROJETO_FORM_ARG_ID)
            ProjetoFormScreen(
                projetoId = id,
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
