package com.exampledmitryvafin.unicarpool.navigation

sealed class Destinations(val route: String) {
    object Splash : Destinations("splash")
    object Onboarding : Destinations("onboarding")
    object Login : Destinations("login")
    object Register : Destinations("register")
    object Home : Destinations("home")

    // Rutas del BottomNavigation
    object Search : Destinations("search")
    object Create : Destinations("create")
    object MyRides : Destinations("my_rides")
    object Profile : Destinations("profile")

    // NUEVA: Ruta con parámetro (el ID del viaje)
    object RideDetail : Destinations("ride_detail/{rideId}") {
        fun pass(rideId: Long): String = "ride_detail/$rideId"
    }
}