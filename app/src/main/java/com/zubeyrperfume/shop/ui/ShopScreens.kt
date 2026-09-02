package com.zubeyrperfume.shop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zubeyrperfume.shop.Bottle
import com.zubeyrperfume.shop.Product
import com.zubeyrperfume.shop.Screen
import com.zubeyrperfume.shop.ShopViewModel
import com.zubeyrperfume.shop.SortBy
import com.zubeyrperfume.shop.money

/* ==================================================================== HOME */

@Composable
fun HomeScreen(vm: ShopViewModel, onWhatsApp: () -> Unit) {
    val shop = vm.catalogue.shop
    val best = vm.catalogue.products.filter { it.tags.contains("best") }.take(10)
    val sale = vm.catalogue.products.filter { it.discount > 0 }.take(10)
    val fresh = vm.catalogue.products.filter { it.tags.contains("new") }.take(10)

    LazyColumn(Modifier.fillMaxSize()) {

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Ink)
                    .padding(horizontal = Gutter, vertical = 34.dp)
            ) {
                Text(
                    "Discover authentic Arabian luxury",
                    style = MaterialTheme.typography.displaySmall,
                    color = Paper
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    shop.slogan2,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gold
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { vm.clearFilters(); vm.go(Screen.Shop) },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)
                    ) { Text("Shop all perfumes", style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Gold)
                    ) { Text("Order on WhatsApp", color = Gold, style = MaterialTheme.typography.labelLarge) }
                }
            }
        }

        item {
            Surface(color = Mist, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Free delivery in Addis Ababa on orders over ${money(shop.freeOver)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Body,
                    modifier = Modifier.padding(horizontal = Gutter, vertical = 10.dp)
                )
            }
        }

        item {
            SearchField(
                value = vm.query,
                onValue = { vm.query = it },
                onSearch = { vm.go(Screen.Shop) },
                modifier = Modifier.padding(Gutter)
            )
        }

        if (vm.catalogue.categories.isNotEmpty()) {
            item { SectionTitle("Shop by category") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Gutter),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vm.catalogue.categories) { c ->
                        Surface(
                            color = hexColor(c.color, Ink),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(168.dp)
                                .clickable { vm.showCategory(c.id) }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    c.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Paper,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(c.sub, style = MaterialTheme.typography.bodySmall, color = Paper.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }

        productRow("Best sellers", best, vm)
        productRow("On sale now", sale, vm)
        productRow("Just arrived", fresh, vm)

        if (vm.catalogue.brands.isNotEmpty()) {
            item { SectionTitle("The houses we carry") }
            item {
                Column(Modifier.padding(horizontal = Gutter)) {
                    val brands = vm.catalogue.brands
                    brands.chunked(2).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { b ->
                                Surface(
                                    border = BorderStroke(1.dp, Line),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { vm.showBrand(b) }
                                ) {
                                    Text(
                                        b,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Ink)
                    .padding(Gutter)
            ) {
                Text(shop.name, style = MaterialTheme.typography.headlineSmall, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text(shop.address, style = MaterialTheme.typography.bodyMedium, color = Paper)
                Text(shop.phoneShown, style = MaterialTheme.typography.bodyMedium, color = Paper)
                Text(shop.email, style = MaterialTheme.typography.bodyMedium, color = Paper)
                Spacer(Modifier.height(10.dp))
                Text(
                    if (vm.live) "Prices and stock are live from the shop." else "Showing the last catalogue saved on this phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Paper.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.productRow(
    title: String,
    products: List<Product>,
    vm: ShopViewModel
) {
    if (products.isEmpty()) return
    item { SectionTitle(title, "See all") { vm.clearFilters(); vm.go(Screen.Shop) } }
    item {
        LazyRow(
            contentPadding = PaddingValues(horizontal = Gutter),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { p ->
                Box(Modifier.width(172.dp)) {
                    ProductCard(
                        product = p,
                        onOpen = { vm.openProduct(p.id) },
                        onAdd = { p.sizes.firstOrNull()?.let { vm.add(p, it) } }
                    )
                }
            }
        }
    }
    item { Spacer(Modifier.height(8.dp)) }
}

@Composable
fun SearchField(
    value: String,
    onValue: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        placeholder = { Text("Search a perfume, brand or note") },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Faint) },
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier.fillMaxWidth()
    )
}

/* ================================================================= CATALOG */

@Composable
fun CatalogScreen(vm: ShopViewModel) {
    val list = vm.filtered()
    var sortOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        SearchField(
            value = vm.query,
            onValue = { vm.query = it },
            onSearch = {},
            modifier = Modifier.padding(start = Gutter, end = Gutter, top = 10.dp, bottom = 6.dp)
        )

        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Gutter, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip("All", vm.category == null && vm.brand == null) { vm.clearFilters() }
            vm.catalogue.categories.forEach { c ->
                Chip(c.name, vm.category == c.id) {
                    vm.brand = null
                    vm.category = if (vm.category == c.id) null else c.id
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Gutter, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${list.size} perfumes" + (vm.brand?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = Faint
            )
            Box {
                Text(
                    vm.sort.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Gold,
                    modifier = Modifier.clickable { sortOpen = true }
                )
                DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                    SortBy.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.label) },
                            onClick = { vm.sort = s; sortOpen = false }
                        )
                    }
                }
            }
        }

        if (list.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(Gutter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))
                Text("Nothing matches that search", style = MaterialTheme.typography.headlineSmall, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Try a brand, a note like oud or vanilla, or clear the filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint
                )
                Spacer(Modifier.height(16.dp))
                QuietButton("Clear filters") { vm.clearFilters() }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(Gutter),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(list, key = { it.id }) { p ->
                    ProductCard(
                        product = p,
                        onOpen = { vm.openProduct(p.id) },
                        onAdd = { p.sizes.firstOrNull()?.let { vm.add(p, it) } }
                    )
                }
            }
        }
    }
}

/* ================================================================== DETAIL */

@Composable
fun ProductScreen(vm: ShopViewModel, productId: String, onWhatsApp: (Product) -> Unit) {
    val product = vm.catalogue.product(productId)
    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("That perfume is no longer listed.", color = Faint)
        }
        return
    }

    var chosen by remember(product.id) {
        mutableStateOf(product.sizes.firstOrNull { it.stock > 0 } ?: product.sizes.first())
    }
    var qty by remember(product.id) { mutableStateOf(1) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            ProductImage(
                product,
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Column(Modifier.padding(Gutter)) {
                Text(product.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = Gold)
                Text(product.name, style = MaterialTheme.typography.headlineMedium, color = Ink)
                Spacer(Modifier.height(6.dp))
                Text(
                    listOf(product.gender, product.family).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Faint
                )
                Spacer(Modifier.height(8.dp))
                Stars(product.rate, product.reviewCount)

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        money(chosen.price),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink,
                        fontWeight = FontWeight.Bold
                    )
                    if (chosen.old > chosen.price) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            money(chosen.old),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Faint,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
                Text("VAT included", style = MaterialTheme.typography.bodySmall, color = Faint)

                Spacer(Modifier.height(14.dp))
                Text("Bottle size", style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.sizes.forEach { s ->
                        Chip("${s.ml} ml · ${money(s.price)}", chosen.ml == s.ml) {
                            chosen = s
                            qty = 1
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (chosen.stock > 0) "${chosen.stock} in stock" else "Out of stock — ask us when it returns",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (chosen.stock > 0) Body else Sale
                )

                if (product.desc.isNotBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Text(product.desc, style = MaterialTheme.typography.bodyLarge, color = Body)
                }

                if (!product.notes.isEmpty) {
                    Spacer(Modifier.height(20.dp))
                    Text("The scent, top to base", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    NoteRow("Opens with", product.notes.top)
                    NoteRow("Heart", product.notes.mid)
                    NoteRow("Dries down to", product.notes.base)
                }

                if (product.longevity > 0 || product.projection > 0) {
                    Spacer(Modifier.height(18.dp))
                    Meter("Longevity", product.longevity)
                    Spacer(Modifier.height(8.dp))
                    Meter("Projection", product.projection)
                }

                if (product.seasons.isNotEmpty() || product.occasions.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("Best worn", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (product.seasons + product.occasions).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Body
                    )
                }

                if (product.smellsLike.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Text("If you like", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(product.smellsLike.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Body)
                }

                Spacer(Modifier.height(18.dp))
                QuietButton("Ask about this on WhatsApp", Modifier.fillMaxWidth()) { onWhatsApp(product) }
                Spacer(Modifier.height(20.dp))
            }
        }

        Surface(shadowElevation = 8.dp, color = Paper) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(Gutter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QtyStepper(qty, chosen.stock) { qty = it }
                GoldButton(
                    text = if (chosen.stock > 0) "Add to bag · ${money(chosen.price * qty)}" else "Out of stock",
                    enabled = chosen.stock > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    vm.add(product, chosen, qty)
                    vm.go(Screen.Cart)
                }
            }
        }
    }
}

@Composable
private fun NoteRow(label: String, notes: List<String>) {
    if (notes.isEmpty()) return
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Gold,
            modifier = Modifier.width(96.dp)
        )
        Text(notes.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = Body)
    }
}

@Composable
private fun Meter(label: String, value: Int) {
    Column {
        Text("$label  ${value.coerceIn(0, 5)}/5", style = MaterialTheme.typography.bodySmall, color = Faint)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (i < value) Gold else Line)
                )
            }
        }
    }
}

@Composable
fun QtyStepper(qty: Int, stock: Int, onChange: (Int) -> Unit) {
    val max = if (stock > 0) stock else 99
    Surface(border = BorderStroke(1.dp, Line), shape = RoundedCornerShape(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "–",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier
                    .clickable { onChange((qty - 1).coerceAtLeast(1)) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
            Text("$qty", style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(
                "+",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                modifier = Modifier
                    .clickable { onChange((qty + 1).coerceAtMost(max)) }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
        }
    }
}
