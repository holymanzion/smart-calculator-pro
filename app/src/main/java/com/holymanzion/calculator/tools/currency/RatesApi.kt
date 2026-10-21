package com.holymanzion.calculator.tools.currency

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONException
import org.json.JSONObject

/** Outcome of a rate fetch. Failure is expected and ordinary, not exceptional. */
sealed interface RatesResult {
    /** [body] is the untouched response, which is what gets cached. */
    data class Success(val rates: CurrencyRates, val body: String) : RatesResult

    /** No usable response. [offline] distinguishes "no connection" from "bad reply". */
    data class Failure(val offline: Boolean) : RatesResult
}

/**
 * Fetches exchange rates over HTTPS.
 *
 * Uses the platform's own HTTP client and JSON parser rather than adding Retrofit,
 * OkHttp and a JSON library for a single GET returning one flat object. The app's
 * dependency list stays limited to Compose.
 *
 * The endpoint needs no API key, which matters: a key shipped inside an APK is a key
 * anyone can extract, and protecting one properly would mean running a proxy server.
 *
 * Blocking. Call it from a background dispatcher.
 */
object RatesApi {

    /** Public, keyless, updated once daily. Base is USD; conversion is a ratio anyway. */
    private const val ENDPOINT = "https://open.er-api.com/v6/latest/USD"

    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    fun fetch(): RatesResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return RatesResult.Failure(offline = false)
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parse(body)?.let { RatesResult.Success(it, body) }
                ?: RatesResult.Failure(offline = false)
        } catch (_: IOException) {
            // No connection, DNS failure, timeout — all ordinary, all the same to the user.
            RatesResult.Failure(offline = true)
        } catch (_: JSONException) {
            RatesResult.Failure(offline = false)
        } finally {
            connection?.disconnect()
        }
    }

    /** Visible for the repository's cache, which stores the raw body verbatim. */
    fun parse(body: String): CurrencyRates? {
        val root = JSONObject(body)
        if (root.optString("result") != "success") return null

        val rateObject = root.optJSONObject("rates") ?: return null
        val rates = HashMap<String, Double>(rateObject.length())
        val keys = rateObject.keys()
        while (keys.hasNext()) {
            val code = keys.next()
            val value = rateObject.optDouble(code, Double.NaN)
            // A non-finite or non-positive rate would poison every conversion through it.
            if (value.isFinite() && value > 0.0) rates[code] = value
        }
        if (rates.isEmpty()) return null

        return CurrencyRates(
            base = root.optString("base_code", "USD"),
            rates = rates,
            fetchedAtMillis = System.currentTimeMillis(),
            // Provider timestamps are seconds; everything else here is milliseconds.
            providerUpdatedAtMillis = root.optLong("time_last_update_unix", 0L) * 1000L,
        )
    }
}
