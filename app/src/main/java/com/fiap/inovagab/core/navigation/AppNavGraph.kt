package com.fiap.inovagab.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

        composable(Routes.HOME_OPERADOR) { HomeOperadorScreen(onLogout = logout) }
        composable(Routes.IDEIA_FORM) { IdeiaFormScreen() }
        composable(Routes.MINHAS_IDEIAS) { MinhasIdeiasScreen() }

        composable(Routes.HOME_GESTOR) { HomeGestorScreen(onLogout = logout) }
        composable(Routes.GESTAO_IDEIAS) { GestaoIdeiasScreen() }
        composable(Routes.PROJETO_FORM) { ProjetoFormScreen() }

        composable(Routes.HOME_LIDER) { HomeLiderScreen(onLogout = logout) }
        composable(Routes.DASHBOARD) { DashboardScreen() }
        composable(Routes.ORIENTACAO_FORM) { OrientacaoFormScreen() }

        composable(Routes.ORIENTACOES_LIST) { OrientacoesListScreen() }
        composable(Routes.PROJETOS_LIST) { ProjetosListScreen() }
        composable(Routes.RANKING) { RankingScreen() }
    }
}
