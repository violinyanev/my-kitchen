package com.ultraviolince.mykitchen.recipes.presentation.recipes.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.ultraviolince.mykitchen.R
import com.ultraviolince.mykitchen.recipes.domain.util.OrderType
import com.ultraviolince.mykitchen.recipes.domain.util.RecipeOrder
import com.ultraviolince.mykitchen.ui.theme.MyApplicationTheme

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    recipeOrder: RecipeOrder = RecipeOrder.Date(OrderType.Descending),
    onOrderChange: (RecipeOrder) -> Unit
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = "Sort options"
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Sort by criteria"
                }
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.sort_title),
                selected = recipeOrder is RecipeOrder.Title,
                onSelect = {
                    onOrderChange(RecipeOrder.Title(recipeOrder.orderType))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultRadioButton(
                text = stringResource(R.string.sort_date),
                selected = recipeOrder is RecipeOrder.Date,
                onSelect = {
                    onOrderChange(RecipeOrder.Date(recipeOrder.orderType))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Sort order"
                }
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.sort_ascending),
                selected = recipeOrder.orderType is OrderType.Ascending,
                onSelect = {
                    onOrderChange(recipeOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultRadioButton(
                text = stringResource(R.string.sort_descending),
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
private fun OrderSectionPreview(
    @PreviewParameter(OrderSectionPreviewParameterProvider::class) recipeOrder: RecipeOrder
) {
    MyApplicationTheme {
        OrderSection(
            recipeOrder = recipeOrder,
            onOrderChange = {}
        )
    }
}
