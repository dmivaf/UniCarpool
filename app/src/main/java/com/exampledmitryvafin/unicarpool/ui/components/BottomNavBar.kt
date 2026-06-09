package com.exampledmitryvafin.unicarpool.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exampledmitryvafin.unicarpool.R
import com.exampledmitryvafin.unicarpool.navigation.Destinations
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    navController: NavController
) {
    val items = listOf(

        BottomNavItem(Destinations.Search.route, "Buscar", Icons.Default.Search),
        BottomNavItem(Destinations.Create.route, "Crear", Icons.Default.Add),
        BottomNavItem(Destinations.MyRides.route, "Mis Viajes", Icons.Default.DateRange),
        BottomNavItem(Destinations.Profile.route, "Perfil", Icons.Default.Person)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Evitar duplicados en el historial
                        popUpTo(Destinations.Search.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}