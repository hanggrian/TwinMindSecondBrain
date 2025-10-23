package com.twinmind.wireframe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var container: FragmentContainerView
    lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)
        navigation = findViewById(R.id.navigation)

        val badge = navigation.getOrCreateBadge(R.id.digestMenu)
        badge.backgroundColor = ContextCompat.getColor(this, R.color.orange)
        badge.isVisible = true
        badge.number = 2

        val navController =
            (supportFragmentManager.findFragmentById(R.id.container)!! as NavHostFragment)
                .navController
        NavigationUI.setupWithNavController(navigation, navController)
        navigation.setOnItemSelectedListener {
            when (navigation.selectedItemId) {
                it.itemId -> return@setOnItemSelectedListener false
                R.id.notesMenu ->
                    navController.navigate(
                        when (it.itemId) {
                            R.id.chatsMenu -> R.id.notesToChatsAction
                            R.id.searchMenu -> R.id.notesToSearchAction
                            else -> R.id.notesToTodoAction
                        },
                    )
                R.id.chatsMenu ->
                    navController.navigate(
                        when (it.itemId) {
                            R.id.notesMenu -> R.id.chatsToNotesAction
                            R.id.searchMenu -> R.id.chatsToSearchAction
                            else -> R.id.chatsToTodoAction
                        },
                    )
                R.id.searchMenu ->
                    navController.navigate(
                        when (it.itemId) {
                            R.id.notesMenu -> R.id.searchToNotesAction
                            R.id.chatsMenu -> R.id.searchToChatsAction
                            else -> R.id.searchToTodoAction
                        },
                    )
                R.id.todoMenu ->
                    navController.navigate(
                        when (it.itemId) {
                            R.id.notesMenu -> R.id.todoToNotesAction
                            R.id.chatsMenu -> R.id.todoToChatsAction
                            else -> R.id.todoToSearchAction
                        },
                    )
            }
            true
        }
    }
}
