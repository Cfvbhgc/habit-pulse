package com.habitpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.habitpulse.app.ui.navigation.NavGraph
import com.habitpulse.app.ui.theme.HabitPulseTheme

/**
 * The single activity that hosts the entire HabitPulse application UI.
 *
 * Uses Jetpack Compose with a single [NavGraph] composable to manage all screens.
 * The activity retrieves the shared [com.habitpulse.app.data.repository.HabitRepository]
 * from the [HabitPulseApp] application class and passes it to the navigation graph,
 * which in turn distributes it to individual screen ViewModels.
 *
 * Edge-to-edge rendering is enabled for a modern, immersive look that extends
 * content behind the system bars.
 */
class MainActivity : ComponentActivity() {

    /**
     * Initialises the Compose UI with the application theme and navigation graph.
     *
     * Retrieves the repository from the application class and sets up the navigation
     * controller that manages the back stack for all screens.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HabitPulseApp
        val repository = app.repository

        setContent {
            HabitPulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}
