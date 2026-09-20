package com.fiap.inovagab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.fiap.inovagab.core.testing.TestTags
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.support.EvidenceRecorder
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL

/**
 * Jornadas E2E com backend real. Credenciais via argumentos de instrumentação (nunca no APK).
 * Sem API/emulador: testes são ignorados (Assume), não aprovados silenciosamente.
 */
@RunWith(AndroidJUnit4::class)
class Sprint2JourneyInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun arg(name: String): String? =
        InstrumentationRegistry.getArguments().getString(name)

    private fun requireBackend() {
        val baseUrl = arg("apiBaseUrl") ?: BuildConfig.API_BASE_URL
        val healthUrl = baseUrl.trimEnd('/') + "/health/ready"
        val ok = runCatching {
            val conn = URL(healthUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        }.getOrDefault(false)
        assumeTrue("API indisponível em $healthUrl", ok)
    }

    private fun requireCreds(prefix: String): Pair<String, String> {
        val email = arg("${prefix}Email")
        val pass = arg("${prefix}Password")
        assumeTrue(
            "credenciais ausentes: defina ${prefix}Email e ${prefix}Password nos args de instrumentação",
            !email.isNullOrBlank() && !pass.isNullOrBlank()
        )
        return email!! to pass!!
    }

    @Before
    fun clearDataAndRequireApi() {
        // A regra já cria uma Activity limpa para cada teste. Recriá-la aqui deixa
        // duas raízes Compose concorrendo pelo idling resource no emulador.
        InovaGabApp.instance.sessionManager.clear()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag(TestTags.LOGIN_EMAIL)
                .fetchSemanticsNodes().isNotEmpty()
        }
        requireBackend()
        composeRule.waitForIdle()
    }

    private fun login(email: String, password: String) {
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextClearance()
        composeRule.onNodeWithTag(TestTags.LOGIN_EMAIL).performTextInput(email)
        composeRule.onNodeWithTag(TestTags.LOGIN_SENHA).performTextClearance()
        composeRule.onNodeWithTag(TestTags.LOGIN_SENHA).performTextInput(password)
        composeRule.onNodeWithTag(TestTags.LOGIN_ENTRAR).performClick()
    }

    private fun waitForLoggedProfile(expected: Perfil) {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            if (InovaGabApp.instance.sessionManager.currentUser.value?.perfil == expected) return
            Thread.sleep(100)
        }
        throw AssertionError("Login não concluiu para o perfil $expected em 30 segundos")
    }

    private fun waitForInvalidLoginMessage() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val found = device.wait(
            Until.hasObject(By.text("E-mail ou senha inválidos.")),
            30_000
        )
        if (!found) throw AssertionError("Mensagem de credenciais inválidas não apareceu em 30 segundos")
    }

    private fun logoutFromHome() {
        composeRule.onNodeWithText("Sair").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun A01_login_invalido_mostra_erro() {
        login("gestor@inovagab.local", "senha-invalida-teste")
        waitForInvalidLoginMessage()
        composeRule.onNodeWithTag(TestTags.LOGIN_ERRO).assertIsDisplayed()
        EvidenceRecorder.record(composeRule, "A01", "login_erro_credencial", "passed")
    }

    @Test
    fun A02_operador_home_orientacoes_ideias_ranking() {
        val (email, pass) = requireCreds("operador1")
        login(email, pass)
        waitForLoggedProfile(Perfil.OPERADOR)
        composeRule.onNodeWithTag(TestTags.HOME_OPERADOR).assertIsDisplayed()
        EvidenceRecorder.record(composeRule, "A02", "home_operador", "passed")

        composeRule.onNodeWithText("Ver orientações").performClick()
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithText("Orientações Estratégicas").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        EvidenceRecorder.record(composeRule, "A02", "orientacoes_lista", "passed")
        composeRule.onNodeWithText("Voltar").performClick()

        composeRule.onNodeWithText("Cadastrar ideia").performClick()
        composeRule.onNodeWithTag(TestTags.IDEIA_FORM_TITULO).assertIsDisplayed()
        EvidenceRecorder.record(composeRule, "A02", "ideia_form_nova", "passed")
        composeRule.onNodeWithText("Cancelar").performClick()

        composeRule.onNodeWithText("Minhas ideias").performClick()
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithText("Minhas ideias").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        EvidenceRecorder.record(composeRule, "A02", "minhas_ideias", "passed")
        composeRule.onNodeWithText("Voltar").performClick()

        composeRule.onNodeWithText("Ranking").performClick()
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithText("Ranking de Inovação").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        EvidenceRecorder.record(composeRule, "A02", "ranking", "passed")
        composeRule.onNodeWithText("Voltar").performClick()
        logoutFromHome()
    }

    @Test
    fun A03_gestor_gestao_ideias_ia_indisponivel_ou_ok() {
        val (email, pass) = requireCreds("gestor")
        login(email, pass)
        waitForLoggedProfile(Perfil.GESTOR)
        composeRule.onNodeWithTag(TestTags.HOME_GESTOR).assertIsDisplayed()
        EvidenceRecorder.record(composeRule, "A03", "home_gestor", "passed")

        composeRule.onNodeWithText("Gerenciar ideias").performClick()
        composeRule.waitUntil(25_000) {
            runCatching {
                composeRule.onNodeWithText("Gestão de Ideias").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        EvidenceRecorder.record(composeRule, "A03", "gestao_ideias", "passed")

        val iaNodes = composeRule.onAllNodesWithTag(TestTags.GESTAO_ANALISAR_IA)
        if (iaNodes.fetchSemanticsNodes().isNotEmpty()) {
            iaNodes[0].performClick()
            composeRule.waitForIdle()
            Thread.sleep(2000)
            EvidenceRecorder.record(
                composeRule,
                "A03",
                "gestao_analise_ia",
                "passed",
                note = "IA opt-in; sem chave espera indisponível na UI"
            )
        }
        composeRule.onNodeWithText("Voltar").performClick()

        composeRule.onNodeWithText("Projetos").performClick()
        composeRule.waitForIdle()
        EvidenceRecorder.record(composeRule, "A03", "projetos_lista", "passed")
        composeRule.onNodeWithText("Voltar").performClick()
        logoutFromHome()
    }

    @Test
    fun A04_lider_dashboard_filtros_graficos() {
        val (email, pass) = requireCreds("lider")
        login(email, pass)
        waitForLoggedProfile(Perfil.LIDER)
        composeRule.onNodeWithTag(TestTags.HOME_LIDER).assertIsDisplayed()
        EvidenceRecorder.record(composeRule, "A04", "home_lider", "passed")

        composeRule.onNodeWithText("Gerenciar orientações").performClick()
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithText("Orientações Estratégicas").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
        EvidenceRecorder.record(composeRule, "A04", "lider_orientacoes", "passed")
        composeRule.onNodeWithText("Voltar").performClick()

        composeRule.onNodeWithText("Dashboard").performClick()
        composeRule.waitUntil(25_000) {
            runCatching {
                composeRule.onNodeWithTag(TestTags.DASHBOARD_APLICAR_FILTROS).assertExists()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithTag(TestTags.DASHBOARD_APLICAR_FILTROS).performClick()
        composeRule.waitForIdle()
        EvidenceRecorder.record(composeRule, "A04", "dashboard_filtros", "passed")
        composeRule.onNodeWithText("Voltar").performClick()
        logoutFromHome()
    }
}
