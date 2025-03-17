package com.example.taskmanager.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.taskmanager.R
import com.example.taskmanager.fragments.TaskListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        setupActionBarWithNavController(navController)
    }

    // for the "Up" button in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // for the sort menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sort, menu)
        return true
    }

    // for sort menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_priority -> {
                // Sort by priority
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).childFragmentManager.fragments[0].let { fragment ->
                    if (fragment is TaskListFragment) {
                        fragment.sortTasksByPriority()
                    }
                }
                true
            }

            R.id.action_sort_due_date -> {
                // Sort by due date
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).childFragmentManager.fragments[0].let { fragment ->
                    if (fragment is TaskListFragment) {
                        fragment.sortTasksByDueDate()
                    }
                }
                true
            }

            R.id.action_sort_alphabetically -> {
                // Sort alphabetically
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).childFragmentManager.fragments[0].let { fragment ->
                    if (fragment is TaskListFragment) {
                        fragment.sortTasksAlphabetically()
                    }
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}