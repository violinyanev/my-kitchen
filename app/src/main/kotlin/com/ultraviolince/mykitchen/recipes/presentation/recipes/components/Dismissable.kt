package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.recipes.domain.model.Recipe
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dismissable(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            onDelete(item)
            true
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            //Box(Modifier.fillMaxSize().background(color))
            DeleteBackground(dismissState)
        }
    ) {
        content(item)
    }

    /*var isRemoved by remember {
        mutableStateOf(false)
    }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(key1 = isRemoved) {
        if(isRemoved) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        setOf(SwipeToDismissBoxValue.EndToStart)
        SwipeToDismissBox(state = state,
            backgroundContent =  {
                DeleteBackground(swipeDismissState = state)
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            content = {
                content(item)
            })
    }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBackground(
    swipeDismissState: SwipeToDismissBoxState
) {
    val color = if (swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
        Color.Red
    } else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = Color.White
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DismissablePreview(
) {
    var showDismissed by remember {
        mutableStateOf(false)
    }

    MyApplicationTheme {
        //val items = listOf("Item 1", "Item 2", "Item 3", "Item 4")
        val recipes by remember {
            mutableStateOf(
                List(3) { index ->
                    Recipe(
                        "Recipe $index",
                        content = "Lorem ipsum dolor sit amet $index",
                        timestamp = 5
                    )
                })
        }
        Column {
            LazyColumn {
                items(recipes) { recipe ->
                    Dismissable(item = recipe,
                        onDelete = {
                            showDismissed = true
                        }
                    ) {
                        RecipeItem(
                            recipe = recipe,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                })
                    }
                }
            }

            if (showDismissed) {
                Box {
                    Text("Dismissed!")
                }
            }
        }

    }
}
