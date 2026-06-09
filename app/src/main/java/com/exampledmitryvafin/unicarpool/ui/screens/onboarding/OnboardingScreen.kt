package com.exampledmitryvafin.unicarpool.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exampledmitryvafin.unicarpool.data.datasource.DataStoreManager
import kotlinx.coroutines.launch  // ← IMPORTANTE: Añade este import

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    dataStoreManager: DataStoreManager,
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Comparte tus viajes",
            description = "Crea viajes como conductor y ofrece tus plazas libres a otros estudiantes",
            icon = "🚗"
        ),
        OnboardingPage(
            title = "Encuentra viajes",
            description = "Busca viajes disponibles a tu destino y ahorra dinero en combustible",
            icon = "🔍"
        ),
        OnboardingPage(
            title = "Comunidad universitaria",
            description = "Conoce a otros estudiantes de tu universidad mientras compartes viaje",
            icon = "🎓"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()  // ← AÑADE ESTA LÍNEA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicadores (dots)
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .background(color, shape = MaterialTheme.shapes.small)
                    )
                }
            }

            // Botón Siguiente / Comenzar (CORREGIDO)
            Button(
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        // Guardar que ya se mostró el onboarding


                        coroutineScope.launch {
                            dataStoreManager.setOnboardingCompleted(true)
                        }
                        onComplete()
                    } else {
                        // Ahora se ejecuta dentro de una corutina
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            ) {
                Text(
                    if (pagerState.currentPage == pages.size - 1) "Comenzar" else "Siguiente"
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.icon,
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}