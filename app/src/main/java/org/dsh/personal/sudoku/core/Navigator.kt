package org.dsh.personal.sudoku.core

import android.util.Log
import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState){
    fun navigate(route: NavKey){
        if (route in state.backStacks.keys){
            // This is a top level route, just switch to it.
            Log.d("Navigator", "Navigate to top level: $route")
            state.topLevelRoute = route
        } else {
            Log.d("Navigator", "Navigate to: $route")
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack(){

        val currentStack = state.backStacks[state.topLevelRoute] ?:
        error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()
        Log.d("Navigator", "Go back from: $currentRoute")
        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
        Log.d("Navigator", "Go back to: ${currentStack.last()}")

    }
}
