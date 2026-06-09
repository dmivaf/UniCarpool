package com.exampledmitryvafin.unicarpool.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.exampledmitryvafin.unicarpool.data.database.AppDatabase
import com.exampledmitryvafin.unicarpool.data.datasource.DataStoreManager
import com.exampledmitryvafin.unicarpool.repository.ParticipacionRepository
import com.exampledmitryvafin.unicarpool.repository.UsuarioRepository
import com.exampledmitryvafin.unicarpool.repository.ViajeRepository
import com.exampledmitryvafin.unicarpool.ui.components.BottomNavBar
import com.exampledmitryvafin.unicarpool.ui.screens.auth.LoginScreen
import com.exampledmitryvafin.unicarpool.ui.screens.auth.RegisterScreen
import com.exampledmitryvafin.unicarpool.ui.screens.create.CreateRideScreen
import com.exampledmitryvafin.unicarpool.ui.screens.detail.RideDetailScreen
import com.exampledmitryvafin.unicarpool.ui.screens.home.HomeScreen
import com.exampledmitryvafin.unicarpool.ui.screens.myrides.MyRidesScreen
import com.exampledmitryvafin.unicarpool.ui.screens.onboarding.OnboardingScreen
import com.exampledmitryvafin.unicarpool.ui.screens.profile.ProfileScreen
import com.exampledmitryvafin.unicarpool.ui.screens.splash.SplashScreen
import com.exampledmitryvafin.unicarpool.viewmodel.AuthViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.AuthViewModelFactory
import com.exampledmitryvafin.unicarpool.viewmodel.ViajeViewModel
import com.exampledmitryvafin.unicarpool.viewmodel.ViajeViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun NavGraph(
    navController: NavHostController,
    context: android.content.Context
) {
    // Dependencias para AuthViewModel
    val database = AppDatabase.getInstance(context)
    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    val viajeRepository = ViajeRepository(database.viajeDao())
    val dataStoreManager = DataStoreManager(context)
    val participacionRepository = ParticipacionRepository(database.participacionDao())

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(usuarioRepository, dataStoreManager)
    )

    val viajeViewModel: ViajeViewModel = viewModel(
        factory = ViajeViewModelFactory(viajeRepository)
    )

    val onboardingCompleted by dataStoreManager.isOnboardingCompleted().collectAsState(initial = false)

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Destinations.Splash.route
    ) {
        // Pantallas de autenticación
        composable(Destinations.Splash.route) {
            val onboardingCompleted by dataStoreManager.isOnboardingCompleted().collectAsState(initial = false)
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            LaunchedEffect(Unit) {
                delay(2000) // Duración del splash
                when {
                    !onboardingCompleted -> navController.navigate(Destinations.Onboarding.route)
                    isLoggedIn -> navController.navigate(Destinations.Home.route)
                    else -> navController.navigate(Destinations.Login.route)
                }
            }

            SplashScreen(onSplashComplete = {})
        }

        composable(Destinations.Onboarding.route) {
            OnboardingScreen(
                dataStoreManager = dataStoreManager,
                onComplete = {
                    // Siempre ir a Login después del onboarding
                    navController.navigate(Destinations.Login.route) {
                        popUpTo(Destinations.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Destinations.Register.route)
                }
            )
        }

        composable(Destinations.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Destinations.Login.route)
                }
            )
        }

        // Pantalla principal con BottomNavigation (SOLO ESTA RUTA)
        composable(Destinations.Home.route) {
            MainScreenWithBottomBar(
                navController = navController,
                viajeViewModel = viajeViewModel,
                authViewModel = authViewModel
            )
        }
    }
}

// Componente que contiene la barra inferior
@Composable
fun MainScreenWithBottomBar(
    navController: NavHostController,
    viajeViewModel: ViajeViewModel,
    authViewModel: AuthViewModel
) {
    // Creamos un NavController anidado para la sección principal
    val bottomNavController = rememberNavController()

    // Obtener el userId y userName actual
    val userId by authViewModel.currentUserId.collectAsState()
    val userName by authViewModel.currentUserName.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Destinations.Search.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Search.route) {
                HomeScreen(
                    viajeViewModel = viajeViewModel,
                    onRideClick = { rideId ->
                        bottomNavController.navigate(Destinations.RideDetail.pass(rideId))
                    }
                )
            }

            // 👇 ESTA ES LA VERSIÓN CORRECTA (con parámetros)
            composable(Destinations.Create.route) {
                CreateRideScreen(
                    currentUserId = if (userId > 0) userId else 1L,
                    currentUserName = userName,
                    onRideCreated = {
                        // Volver a la pantalla de búsqueda después de crear el viaje
                        bottomNavController.popBackStack()
                        android.widget.Toast.makeText(
                            context,
                            "Viaje creado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            composable(Destinations.MyRides.route) {
                MyRidesScreen(
                    currentUserId = userId,
                    onRideClick = { rideId ->
                        bottomNavController.navigate(Destinations.RideDetail.pass(rideId))
                    }
                )
            }

            composable(Destinations.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        // Navegar al login y limpiar el historial
                        navController.navigate(Destinations.Login.route) {
                            popUpTo(Destinations.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dentro de MainScreenWithBottomBar, en el NavHost anidado, añade:

            composable(
                route = Destinations.RideDetail.route,
                arguments = listOf(navArgument("rideId") { type = NavType.LongType })
            ) { backStackEntry ->
                val rideId = backStackEntry.arguments?.getLong("rideId") ?: -1L
                if (rideId != -1L) {
                    RideDetailScreen(
                        rideId = rideId,
                        currentUserId = userId,
                        currentUserName = userName,
                        onBack = { bottomNavController.popBackStack() }
                    )
                }
            }

            composable(Destinations.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        navController.navigate(Destinations.Login.route) {
                            popUpTo(Destinations.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}