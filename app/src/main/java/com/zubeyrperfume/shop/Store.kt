package com.zubeyrperfume.shop

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Everything that talks to zubeyrperfume.com, plus the copy of the catalogue
 * that ships inside the app so the shop still opens with no signal.
 *
 * Change SITE if the shop ever moves to another address.
 */
object Store {

    const val SITE = "https://zubeyrperfume.com"
    private const val API = "$SITE/wp-json/zubeyr/v1"

    private const val PREFS = "zubeyr"
    private const val KEY_CART = "cart"
    private const val KEY_CUSTOMER = "customer"
    private const val KEY_ORDERS = "orders"
    private const val KEY_CACHE = "catalogue"

    /* ------------------------------------------------------------ catalogue */

    /** Live catalogue, or the last one we saw, or the one bundled in the app. */
    suspend fun catalogue(context: Context): Pair<Catalogue, Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = get("$API/data")
            val parsed = Catalogue.parse(JSONObject(body))
            if (parsed.products.isNotEmpty()) {
                prefs(context).edit().putString(KEY_CACHE, body).apply()
                return@withContext parsed to true
            }
        } catch (_: Exception) {
        }
        offline(context) to false
    }

    fun offline(context: Context): Catalogue {
        prefs(context).getString(KEY_CACHE, null)?.let {
            try {
                val c = Catalogue.parse(JSONObject(it))
                if (c.products.isNotEmpty()) return c
            } catch (_: Exception) {
            }
        }
        return try {
            val seed = context.assets.open("seed.json").bufferedReader().use(BufferedReader::readText)
            Catalogue.parse(JSONObject(seed))
        } catch (_: Exception) {
            Catalogue()
        }
    }

    /* --------------------------------------------------------------- orders */

    suspend fun placeOrder(
        rows: List<CartRow>,
        customer: Customer,
        delivery: ShipOption,
        payment: PayMethod,
        reference: String
    ): PlacedOrder = withContext(Dispatchers.IO) {
        val items = JSONArray()
        rows.forEach {
            items.put(
                JSONObject()
                    .put("id", it.product.id)
                    .put("ml", it.bottle.ml)
                    .put("qty", it.qty)
            )
        }
        val cust = JSONObject()
            .put("name", customer.name)
            .put("phone", customer.phone)
            .put("email", customer.email)
            .put("addr", customer.address.ifBlank { delivery.name })
            .put("note", customer.note)
            .put("sms", customer.sms)
            .put("subscribe", customer.subscribe)

        val body = JSONObject()
            .put("items", items)
            .put("deliveryId", delivery.id)
            .put("delivery", delivery.name)
            .put("paymentId", payment.id)
            .put("payment", payment.name)
            .put("ref", reference)
            .put("cust", cust)
            .put("source", "android")

        val res = JSONObject(post("$API/orders", body.toString()))
        PlacedOrder(res.optString("code", ""), res.optInt("total", 0))
    }

    suspend fun track(code: String): TrackedOrder = withContext(Dispatchers.IO) {
        val o = JSONObject(get("$API/track/" + code.trim().uppercase()))
        val events = ArrayList<Pair<String, String>>()
        val arr = o.optJSONArray("events")
        if (arr != null) for (i in 0 until arr.length()) {
            val e = arr.optJSONObject(i) ?: continue
            events.add(e.optString("s", "") to e.optString("at", ""))
        }
        TrackedOrder(
            code = o.optString("code", code),
            status = o.optString("status", "new"),
            placedAt = o.optString("at", ""),
            delivery = o.optString("delivery", ""),
            total = o.optInt("total", 0),
            events = events
        )
    }

    suspend fun subscribe(email: String, name: String) = withContext(Dispatchers.IO) {
        post("$API/subscribe", JSONObject().put("email", email).put("name", name).toString())
        Unit
    }

    /* ---------------------------------------------------------- local state */

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadCart(context: Context): List<CartLine> {
        val raw = prefs(context).getString(KEY_CART, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                arr.optJSONObject(i)?.let {
                    CartLine(it.optString("id", ""), it.optInt("ml", 0), it.optInt("qty", 1))
                }
            }.filter { it.productId.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCart(context: Context, lines: List<CartLine>) {
        val arr = JSONArray()
        lines.forEach {
            arr.put(JSONObject().put("id", it.productId).put("ml", it.ml).put("qty", it.qty))
        }
        prefs(context).edit().putString(KEY_CART, arr.toString()).apply()
    }

    fun loadCustomer(context: Context): Customer {
        val raw = prefs(context).getString(KEY_CUSTOMER, null) ?: return Customer()
        return try {
            val o = JSONObject(raw)
            Customer(
                o.optString("name", ""),
                o.optString("phone", ""),
                o.optString("email", ""),
                o.optString("addr", ""),
                "",
                o.optBoolean("sms", true),
                false
            )
        } catch (_: Exception) {
            Customer()
        }
    }

    fun saveCustomer(context: Context, c: Customer) {
        val o = JSONObject()
            .put("name", c.name).put("phone", c.phone)
            .put("email", c.email).put("addr", c.address).put("sms", c.sms)
        prefs(context).edit().putString(KEY_CUSTOMER, o.toString()).apply()
    }

    fun myOrders(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_ORDERS, null) ?: return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun rememberOrder(context: Context, code: String) {
        if (code.isBlank()) return
        val list = (listOf(code) + myOrders(context)).distinct().take(20)
        prefs(context).edit().putString(KEY_ORDERS, list.joinToString(",")).apply()
    }

    /* ---------------------------------------------------------------- http */

    private fun get(url: String): String = call(url, null)

    private fun post(url: String, body: String): String = call(url, body)

    private fun call(url: String, body: String?): String {
        val c = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            requestMethod = if (body == null) "GET" else "POST"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ZubeyrPerfume-Android/1.0")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }
        try {
            if (body != null) c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val ok = c.responseCode in 200..299
            val stream = if (ok) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (!ok) throw ShopError(readMessage(text, c.responseCode))
            return text
        } finally {
            c.disconnect()
        }
    }

    private fun readMessage(text: String, code: Int): String {
        return try {
            val m = JSONObject(text).optString("message", "")
            if (m.isNotBlank()) m else "The shop could not be reached (error $code)."
        } catch (_: Exception) {
            "The shop could not be reached (error $code)."
        }
    }
}

class ShopError(message: String) : Exception(message)
