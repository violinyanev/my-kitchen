package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.shared.R
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    onOrderChange: (RecipeOrder) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.sort_by_heading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.title_hint),
                selected = recipeOrder is RecipeOrder.Title,
                onSelect = {
                    onOrderChange(RecipeOrder.Title(recipeOrder.orderType))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultRadioButton(
                text = "Date",
                selected = recipeOrder is RecipeOrder.Date,
                onSelect = {
                    onOrderChange(RecipeOrder.Date(recipeOrder.orderType))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sort_order_heading),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = "Ascending",
                selected = recipeOrder.orderType is OrderType.Ascending,
                onSelect = {
                    onOrderChange(recipeOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultRadioButton(
                text = "Descending",
                selected = recipeOrder.orderType is OrderType.Descending,
                onSelect = {
                    onOrderChange(recipeOrder.copy(OrderType.Descending))
                }
            )
        }
    }
}

class OrderSectionPreviewParameterProvider : PreviewParameterProvider<RecipeOrder> {
    override val values = sequenceOf(
        RecipeOrder.Title(OrderType.Descending),
        RecipeOrder.Title(OrderType.Ascending),
        RecipeOrder.Date(OrderType.Descending),
        RecipeOrder.Date(OrderType.Ascending)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun OrderSectionPreview(
    @PreviewParameter(OrderSectionPreviewParameterProvider::class) recipeOrder: RecipeOrder
) {
    MyApplicationTheme {
        OrderSection(
            recipeOrder = recipeOrder,
            onOrderChange = {}
        )
    }
}
