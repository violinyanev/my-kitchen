import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.myapplication.recipes.domain.model.Recipe
import com.example.myapplication.recipes.presentation.notes.components.OrderSection
import com.example.myapplication.recipes.presentation.notes.components.RecipeViewModel
import com.example.myapplication.recipes.presentation.notes.components.RecipesEvent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scaffoldState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add recipe")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Your recipe", style = MaterialTheme.typography.bodyMedium)
                IconButton(
                    onClick = {
                        viewModel.onEvent(RecipesEvent.ToggleOrderSection)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search, // TODO fix icon to sort
                        contentDescription = "Sort"
                    )
                }
                AnimatedVisibility(
                    visible = state.isOrderSelectionVisible,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    OrderSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        recipeOrder = state.recipeOrder,
                        onOrderChange = {
                            viewModel.onEvent(RecipesEvent.Order(it))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn() {
                    items(state.recipes) { recipe ->
                        RecipeItem(
                            recipe = recipe,
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                } // TODO onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeList(recipes: List<Recipe>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            for (r in recipes) {
                RecipeItem(r, modifier)
            }
        }
    }
}

@Preview
@Composable
fun RecipeListPreview() {
    val recipes = listOf(
        Recipe(
            title = "Delicious Chocolate Cake",
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud" +
                "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            color = 3,
            timestamp = 1
        ),
        Recipe(
            title = "Carrot on a stick",
            content = "Whatsup doc... Feeling like a lonely tune?",
            color = 3,
            timestamp = 1
        )
    )

    RecipeList(recipes)
}
