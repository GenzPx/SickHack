package self.apk.sickhack.genz.core.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class HttpResponse(
    val method: String,
    val url: String,
    val statusCode: Int,
    val statusText: String,
    val requestHeaders: Map<String, String>,
    val responseHeaders: Map<String, String>,
    val requestBody: String,
    val body: String,
    val durationMs: Long
) {
    fun summary(): String = "[${statusCode}] ${method.uppercase()} ${url} (${durationMs}ms)"
}

object HttpClient {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /** Parse "Key: Value" lines into a header map. */
    fun parseHeaders(raw: String): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        raw.lines().forEach { line ->
            val idx = line.indexOf(':')
            if (idx > 0) {
                val k = line.substring(0, idx).trim()
                val v = line.substring(idx + 1).trim()
                if (k.isNotEmpty()) map[k] = v
            }
        }
        return map
    }

    private fun headersToOkHttp(headers: Map<String, String>): okhttp3.Headers {
        val b = okhttp3.Headers.Builder()
        headers.forEach { (k, v) -> if (k.isNotBlank()) b.add(k, v) }
        return b.build()
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse = execute("GET", url, headers, null)

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        form: Boolean = false
    ): HttpResponse {
        val reqBody = if (form) {
            val fb = FormBody.Builder()
            body.split("&").forEach { pair ->
                val idx = pair.indexOf('=')
                if (idx > 0) {
                    fb.add(
                        java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    )
                }
            }
            fb.build()
        } else {
            body.toRequestBody("application/x-www-form-urlencoded; charset=utf-8".toMediaType())
        }
        return executeWithBody("POST", url, headers, body, reqBody)
    }

    suspend fun execute(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        contentType: String? = null
    ): HttpResponse {
        val reqBody = if (body.isNullOrEmpty()) {
            null
        } else {
            val ct = contentType ?: "application/x-www-form-urlencoded; charset=utf-8"
            body.toRequestBody(ct.toMediaType())
        }
        return executeWithBody(method, url, headers, body ?: "", reqBody)
    }

    private suspend fun executeWithBody(
        method: String,
        url: String,
        headers: Map<String, String>,
        rawBody: String,
        reqBody: okhttp3.RequestBody?
    ): HttpResponse = withContext(Dispatchers.IO) {
        try {
            val rb = if (method.equals("GET", true) || method.equals("HEAD", true)) null else reqBody
            val reqBuilder = Request.Builder().url(url).method(method.uppercase(), rb)
            if (headers.isNotEmpty()) reqBuilder.headers(headersToOkHttp(headers))
            val start = System.currentTimeMillis()
            val call = client.newCall(reqBuilder.build())
            val response = call.execute()
            val duration = System.currentTimeMillis() - start
            response.use {
                val statusText = it.message
                val respHeaders = LinkedHashMap<String, String>()
                for (i in 0 until it.headers.size) {
                    val name = it.headers.name(i)
                    val value = it.headers.value(i)
                    if (respHeaders.containsKey(name)) {
                        respHeaders[name] = respHeaders[name] + "; " + value
                    } else {
                        respHeaders[name] = value
                    }
                }
                val body = try {
                    it.body?.string() ?: ""
                } catch (e: Exception) {
                    "ERROR READING BODY: ${e.message}"
                }
                HttpResponse(
                    method = method.uppercase(),
                    url = it.request.url.toString(),
                    statusCode = it.code,
                    statusText = statusText,
                    requestHeaders = headers,
                    responseHeaders = respHeaders,
                    requestBody = rawBody,
                    body = body,
                    durationMs = duration
                )
            }
        } catch (e: Exception) {
            HttpResponse(
                method = method.uppercase(),
                url = url,
                statusCode = 0,
                statusText = "NETWORK ERROR: ${e.message}",
                requestHeaders = headers,
                responseHeaders = emptyMap(),
                requestBody = rawBody,
                body = "",
                durationMs = 0
            )
        }
    }

    /** Simple GET returning only the body string (used by scanner). */
    suspend fun getBody(url: String, headers: Map<String, String> = emptyMap()): String =
        get(url, headers).body

    /** Convert an IP:port string "1.2.3.4:80" style usage for connect tests. */
    suspend fun tcpConnect(host: String, port: Int, timeoutMs: Int = 3000): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val sock = java.net.Socket()
                sock.connect(java.net.InetSocketAddress(host, port), timeoutMs)
                sock.close()
                true
            } catch (e: Exception) {
                false
            }
        }
}
