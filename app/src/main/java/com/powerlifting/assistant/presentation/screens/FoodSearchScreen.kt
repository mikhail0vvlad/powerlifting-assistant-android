@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.powerlifting.assistant.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerlifting.assistant.domain.model.FoodProduct
import com.powerlifting.assistant.presentation.viewmodel.FoodSearchViewModel
import kotlinx.coroutines.launch

@Composable
fun FoodSearchScreen(
    onBack: () -> Unit,
    vm: FoodSearchViewModel = hiltViewModel()
) {
    val query by vm.query.collectAsState()
    val searchState by vm.searchState.collectAsState()
    val history by vm.history.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var selectedProduct by remember { mutableStateOf<FoodProduct?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск продукта") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                // Кнопка «Очистить» отображается только когда в поле есть текст.
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                vm.clearQuery()
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                // Подсказка, когда поле ввода пустое.
                placeholder = { Text("Введите название продукта") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )

            Spacer(Modifier.height(16.dp))

            if (query.isBlank()) {
                // История поиска (если она непустая).
                if (history.isNotEmpty()) {
                    HistorySection(
                        history = history,
                        onItemClick = { product ->
                            vm.onProductClicked(product)
                            selectedProduct = product
                        },
                        onClearHistory = { vm.clearHistory() }
                    )
                }
            } else {
                when (val state = searchState) {
                    FoodSearchViewModel.SearchState.Idle -> Unit

                    FoodSearchViewModel.SearchState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is FoodSearchViewModel.SearchState.Results -> {
                        ProductList(
                            products = state.products,
                            onItemClick = { product ->
                                vm.onProductClicked(product)
                                selectedProduct = product
                            }
                        )
                    }

                    FoodSearchViewModel.SearchState.Empty -> {
                        Placeholder(
                            icon = Icons.Default.SearchOff,
                            text = "Ничего не нашлось"
                        )
                    }

                    FoodSearchViewModel.SearchState.Error -> {
                        Placeholder(
                            icon = Icons.Default.Refresh,
                            text = "Не удалось выполнить поиск"
                        ) {
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { vm.retry() }) {
                                Text("Обновить")
                            }
                        }
                    }
                }
            }
        }
    }

    selectedProduct?.let { product ->
        AddPortionDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onConfirm = { grams ->
                scope.launch {
                    vm.addMeal(product, grams)
                    selectedProduct = null
                    onBack()
                }
            }
        )
    }
}

@Composable
private fun HistorySection(
    history: List<FoodProduct>,
    onItemClick: (FoodProduct) -> Unit,
    onClearHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("История поиска", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onClearHistory) { Text("Очистить историю") }
    }
    Spacer(Modifier.height(8.dp))
    ProductList(products = history, onItemClick = onItemClick)
}

@Composable
private fun ProductList(
    products: List<FoodProduct>,
    onItemClick: (FoodProduct) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(products, key = { it.id }) { product ->
            ProductItem(product = product, onClick = { onItemClick(product) })
        }
    }
}

@Composable
private fun ProductItem(product: FoodProduct, onClick: () -> Unit) {
    ElevatedCard(shape = MaterialTheme.shapes.large, onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "${product.calories} ккал • Б ${product.proteinG} • Ж ${product.fatG} • У ${product.carbsG} (на 100 г)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun Placeholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        extra()
    }
}

@Composable
private fun AddPortionDialog(
    product: FoodProduct,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var grams by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(grams.toIntOrNull()?.coerceIn(1, 5000) ?: 100) },
                enabled = grams.toIntOrNull()?.let { it > 0 } == true
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text(product.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "На 100 г: ${product.calories} ккал, белок ${product.proteinG} г",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it.filter { ch -> ch.isDigit() }.take(5) },
                    label = { Text("Граммы") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        }
    )
}
