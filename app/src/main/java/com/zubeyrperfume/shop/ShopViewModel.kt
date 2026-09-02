package com.zubeyrperfume.shop

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object Shop : Screen()
    data object Cart : Screen()
    data object Track : Screen()
    data object More : Screen()
    data object Checkout : Screen()
    data class Detail(val productId: String) : Screen()
    data class Done(val code: String, val total: Int) : Screen()
}

enum class SortBy(val label: String) {
    Featured("Featured"),
    PriceLow("Price: low to high"),
    PriceHigh("Price: high to low"),
    Rating("Best rated"),
    Name("Name A–Z")
}

class ShopViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx get() = getApplication<Application>()

    var catalogue by mutableStateOf(Catalogue())
        private set
    var loading by mutableStateOf(true)
        private set
    var live by mutableStateOf(false)
        private set

    val stack = mutableStateListOf<Screen>(Screen.Home)
    val current: Screen get() = stack.last()

    /* filters */
    var query by mutableStateOf("")
    var category by mutableStateOf<String?>(null)
    var brand by mutableStateOf<String?>(null)
    var sort by mutableStateOf(SortBy.Featured)

    /* cart */
    private val lines = mutableStateListOf<CartLine>()

    /* checkout */
    var customer by mutableStateOf(Customer())
    var deliveryId by mutableStateOf("")
    var paymentId by mutableStateOf("cod")
    var reference by mutableStateOf("")
    var placing by mutableStateOf(false)
        private set
    var checkoutError by mutableStateOf<String?>(null)

    /* tracking */
    var tracking by mutableStateOf(false)
        private set
    var tracked by mutableStateOf<TrackedOrder?>(null)
    var trackError by mutableStateOf<String?>(null)

    init {
        catalogue = Store.offline(ctx)
        lines.addAll(Store.loadCart(ctx))
        customer = Store.loadCustomer(ctx)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loading = true
            val (data, fromNetwork) = Store.catalogue(ctx)
            if (data.products.isNotEmpty()) catalogue = data
            live = fromNetwork
            if (deliveryId.isBlank()) deliveryId = catalogue.shipping.firstOrNull()?.id.orEmpty()
            loading = false
        }
    }

    /* ---------------------------------------------------------- navigation */

    fun go(screen: Screen) {
        if (screen is Screen.Home || screen is Screen.Shop || screen is Screen.Cart ||
            screen is Screen.Track || screen is Screen.More
        ) {
            stack.clear()
            stack.add(screen)
        } else {
            stack.add(screen)
        }
    }

    fun openTab(screen: Screen) = go(screen)

    fun back(): Boolean {
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        if (current != Screen.Home) {
            go(Screen.Home)
            return true
        }
        return false
    }

    fun openProduct(id: String) = go(Screen.Detail(id))

    /* ------------------------------------------------------------- filters */

    fun filtered(): List<Product> {
        var list = catalogue.products.filter { it.matches(query) }
        category?.let { c -> list = list.filter { it.cats.contains(c) } }
        brand?.let { b -> list = list.filter { it.brand.equals(b, true) } }
        list = when (sort) {
            SortBy.Featured -> list.sortedByDescending { it.tags.contains("best") }
            SortBy.PriceLow -> list.sortedBy { it.fromPrice }
            SortBy.PriceHigh -> list.sortedByDescending { it.fromPrice }
            SortBy.Rating -> list.sortedByDescending { it.rate }
            SortBy.Name -> list.sortedBy { it.title.lowercase() }
        }
        return list
    }

    fun clearFilters() {
        query = ""
        category = null
        brand = null
        sort = SortBy.Featured
    }

    fun showCategory(id: String) {
        clearFilters()
        category = id
        go(Screen.Shop)
    }

    fun showBrand(name: String) {
        clearFilters()
        brand = name
        go(Screen.Shop)
    }

    /* ---------------------------------------------------------------- cart */

    val cartLines: List<CartLine> get() = lines

    fun rows(): List<CartRow> = lines.mapNotNull { l ->
        val p = catalogue.product(l.productId) ?: return@mapNotNull null
        val b = p.size(l.ml) ?: p.sizes.firstOrNull() ?: return@mapNotNull null
        CartRow(p, b, l.qty)
    }

    val cartCount: Int get() = lines.sumOf { it.qty }

    fun subtotal(): Int = rows().sumOf { it.line }

    fun delivery(): ShipOption? =
        catalogue.shipping.firstOrNull { it.id == deliveryId } ?: catalogue.shipping.firstOrNull()

    fun deliveryFee(): Int {
        val d = delivery() ?: return 0
        val free = catalogue.shop.freeOver
        if (d.freeOver && free > 0 && subtotal() >= free) return 0
        return d.fee
    }

    fun total(): Int = subtotal() + deliveryFee()

    fun payment(): PayMethod = PayMethod.all.firstOrNull { it.id == paymentId } ?: PayMethod.all.last()

    fun add(product: Product, bottle: Bottle, qty: Int = 1) {
        val i = lines.indexOfFirst { it.productId == product.id && it.ml == bottle.ml }
        val capped = { n: Int -> n.coerceIn(1, if (bottle.stock > 0) bottle.stock else 99) }
        if (i >= 0) lines[i] = lines[i].copy(qty = capped(lines[i].qty + qty))
        else lines.add(CartLine(product.id, bottle.ml, capped(qty)))
        persistCart()
    }

    fun setQty(row: CartRow, qty: Int) {
        val i = lines.indexOfFirst { it.productId == row.product.id && it.ml == row.bottle.ml }
        if (i < 0) return
        if (qty <= 0) lines.removeAt(i)
        else lines[i] = lines[i].copy(qty = qty.coerceAtMost(if (row.bottle.stock > 0) row.bottle.stock else 99))
        persistCart()
    }

    fun remove(row: CartRow) {
        lines.removeAll { it.productId == row.product.id && it.ml == row.bottle.ml }
        persistCart()
    }

    fun clearCart() {
        lines.clear()
        persistCart()
    }

    private fun persistCart() = Store.saveCart(ctx, lines.toList())

    /* ------------------------------------------------------------ checkout */

    fun checkoutProblem(): String? {
        if (rows().isEmpty()) return "Your bag is empty."
        if (customer.name.trim().length < 2) return "Please give the name the rider should ask for."
        if (customer.phone.filter { it.isDigit() }.length < 9) return "Please give a phone number we can reach you on."
        val email = customer.email.trim()
        if (email.isNotEmpty() && !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email))
            return "That email address does not look right."
        if (deliveryFee() > 0 && customer.address.trim().length < 4)
            return "Please give an address or a landmark so the rider can find you."
        if (payment().needsRef && reference.trim().length < 4)
            return "Paste the transaction number from your payment message, or choose cash on delivery."
        return null
    }

    fun placeOrder() {
        val problem = checkoutProblem()
        if (problem != null) {
            checkoutError = problem
            return
        }
        val d = delivery() ?: run { checkoutError = "Please choose how you would like it delivered."; return }
        viewModelScope.launch {
            placing = true
            checkoutError = null
            try {
                val order = Store.placeOrder(rows(), customer, d, payment(), reference.trim())
                Store.saveCustomer(ctx, customer)
                Store.rememberOrder(ctx, order.code)
                clearCart()
                reference = ""
                stack.clear()
                stack.add(Screen.Home)
                stack.add(Screen.Done(order.code, order.total))
            } catch (e: Exception) {
                checkoutError = e.message ?: "We could not place that order. Please try again."
            } finally {
                placing = false
            }
        }
    }

    /* ------------------------------------------------------------ tracking */

    fun track(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            tracking = true
            trackError = null
            tracked = null
            try {
                tracked = Store.track(code)
            } catch (e: Exception) {
                trackError = e.message ?: "We could not find that order."
            } finally {
                tracking = false
            }
        }
    }

    fun savedOrderCodes(): List<String> = Store.myOrders(ctx)
}
