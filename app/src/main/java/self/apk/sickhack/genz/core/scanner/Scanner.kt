package self.apk.sickhack.genz.core.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import self.apk.sickhack.genz.core.codec.Codec
import self.apk.sickhack.genz.core.net.HttpClient
import self.apk.sickhack.genz.core.net.HttpResponse
import self.apk.sickhack.genz.core.payloads.Payloads
import java.util.concurrent.atomic.AtomicInteger

/**
 * Auto-scan engine. Menjalankan SEMUA kategori payload x metode (GET/POST) x
 * injection point (tiap query param) dengan concurrency terbatas, lalu
 * menganalisis tiap respons.
 */
object Scanner {

    data class Finding(
        val severity: String,
        val category: String,
        val evidence: String,
        val url: String,
        val payload: String
    ) {
        fun line(): String = "[$severity] $category — $evidence"
        fun full(): String =
            "[$severity] $category\n  URL:    $url\n  PAYLOAD: $payload\n  EVIDENCE: $evidence"
    }

    data class ScanProgress(val text: String, val done: Int, val total: Int)

    data class Task(val category: String, val method: String, val point: String, val payload: String)

    private const val CONCURRENCY = 3

    private val sqlErrorSignatures = listOf(
        "mysql_fetch", "syntax error", "you have an error in your sql syntax",
        "ORA-", "unclosed quotation", "SQLSTATE", "ODBC", "Microsoft OLE DB",
        "Warning: mysql", "PostgreSQL", "pg_query", "SQLite", "sqlite3",
        "SQLServer", "PSQLException", "JDBC", "DB2 SQL error", "mysqli_",
        "PDOException", "Supplied argument is not a valid MySQL", "SQL command not properly ended"
    )

    private val lfiSignatures = listOf(
        "root:x:0:0", "www-data:x:", "daemon:x:", "[boot loader]", "uid=",
        "[fonts]", "mysql:x:", "nobody:x:", "bin:x:", "sys:x:"
    )

    private val ssrfSignatures = listOf(
        "instance-id", "ami-id", "accountId", "meta-data", "hostname",
        "availability-zone", "security-credentials", "local-ipv4"
    )

    private val timeMarkers = listOf(
        "SLEEP(", "sleep(", "pg_sleep", "WAITFOR", "BENCHMARK(", "dbms_pipe", "IF(1=1", "SLEEP(5)"
    )

    private val severityFor = mapOf(
        "SQLi-Error" to "HIGH",
        "SQLi-Union" to "HIGH",
        "SQLi-Auth" to "MEDIUM",
        "SQLi-Blind" to "HIGH",
        "XSS" to "MEDIUM",
        "LFI" to "HIGH",
        "RFI" to "INFO",
        "RCE" to "HIGH",
        "SSTI" to "HIGH",
        "SSRF" to "HIGH",
        "OpenRedirect" to "MEDIUM",
        "CRLF" to "MEDIUM",
        "XXE" to "HIGH"
    )

    /** Inject payload sebagai query param (mengganti param lama jika ada). */
    fun injectParam(url: String, param: String, payload: String): String {
        val encoded = Codec.preserveEncode(payload)
        val u = url.toHttpUrlOrNull()
        if (u == null) {
            return url + (if (url.contains("?")) "&" else "?") + param + "=" + encoded
        }
        val b = u.newBuilder()
            .removeAllQueryParameters(param)
            .addEncodedQueryParameter(param, encoded)
        return b.build().toString()
    }

    private fun getParams(target: String): List<String> {
        val u = target.toHttpUrlOrNull() ?: return emptyList()
        return u.queryParameterNames.filter { it.isNotBlank() }
    }

    private fun buildTasks(target: String, points: List<String>): List<Task> {
        val t = mutableListOf<Task>()
        fun add(cat: String, method: String, payloads: List<String>) {
            for (p in points) for (pl in payloads) t.add(Task(cat, method, p, pl))
        }
        // GET sweep
        add("SQLi-Error", "GET", Payloads.sqliBasic + Payloads.sqliErrorBased)
        add("SQLi-Union", "GET", Payloads.sqliUnion + Payloads.sqliDios)
        add("SQLi-Auth", "GET", Payloads.sqliAuthBypass)
        add("SQLi-Blind", "GET", Payloads.sqliBlind + Payloads.sqliMssql + Payloads.sqliPostgres + Payloads.sqliOracle)
        add("XSS", "GET", Payloads.xssRaw + Payloads.xssEncoded + Payloads.xssWafBypass)
        add("LFI", "GET", Payloads.lfi)
        add("RFI", "GET", Payloads.rfi)
        add("RCE", "GET", Payloads.rce)
        add("SSTI", "GET", Payloads.ssti)
        add("SSRF", "GET", Payloads.ssrf)
        add("OpenRedirect", "GET", Payloads.openRedirect)
        add("CRLF", "GET", Payloads.crlf)
        // POST sweep
        add("SQLi-Error", "POST", Payloads.sqliBasic + Payloads.sqliErrorBased)
        add("SQLi-Union", "POST", Payloads.sqliUnion)
        add("SQLi-Blind", "POST", Payloads.sqliBlind)
        add("XSS", "POST", Payloads.xssRaw + Payloads.xssEncoded)
        add("LFI", "POST", Payloads.lfi)
        add("RCE", "POST", Payloads.rce)
        add("SSTI", "POST", Payloads.ssti)
        add("SSRF", "POST", Payloads.ssrf)
        add("CRLF", "POST", Payloads.crlf)
        // XXE -> body-based (application/xml)
        for (pl in Payloads.xxe) t.add(Task("XXE", "POST", "BODY", pl))
        return t
    }

    suspend fun fullScan(
        targetRaw: String,
        onProgress: (ScanProgress) -> Unit = {},
        isCancelled: () -> Boolean = { false }
    ): List<Finding> {
        val target = if (targetRaw.startsWith("http://") || targetRaw.startsWith("https://")) {
            targetRaw.trim()
        } else {
            "http://" + targetRaw.trim()
        }
        val findings = java.util.Collections.synchronizedList(mutableListOf<Finding>())
        val points = getParams(target).toMutableList()
        if (points.isEmpty()) points.add("sickhack")
        if ("sickhack" !in points) points.add("sickhack")

        val tasks = buildTasks(target, points)
        val total = tasks.size
        if (total == 0) return emptyList()

        // Baseline (panjang body normal per metode) untuk deteksi perbedaan
        val baselineGet = try { HttpClient.get(target).body.length } catch (e: Exception) { -1 }
        val baselinePost = try {
            HttpClient.execute("POST", target, body = "sickhack=1").body.length
        } catch (e: Exception) { -1 }

        val done = AtomicInteger(0)
        coroutineScope {
            repeat(CONCURRENCY) { worker ->
                launch(Dispatchers.IO) {
                    var i = worker
                    while (i < tasks.size) {
                        if (isCancelled()) break
                        val task = tasks[i]
                        i += CONCURRENCY
                        val idx = done.incrementAndGet()
                        onProgress(
                            ScanProgress(
                                "[$idx/$total] ${task.category} ${task.method} via '${task.point}'",
                                idx, total
                            )
                        )
                        try {
                            val resp: HttpResponse? = if (task.category == "XXE") {
                                HttpClient.execute("POST", target, body = task.payload, contentType = "application/xml")
                            } else {
                                HttpClient.execute(task.method, injectParam(target, task.point, task.payload))
                            }
                            if (resp == null || resp.statusCode == 0) continue
                            val bl = if (task.method == "GET") baselineGet else baselinePost
                            val f = detect(task, resp, bl)
                            if (f != null) findings.add(f)
                        } catch (e: Exception) {
                            // skip task on network error
                        }
                    }
                }
            }
        }
        return findings.toList().sortedBy { it.severity }
    }

    private fun detect(task: Task, resp: HttpResponse, baselineLen: Int): Finding? {
        return when (task.category) {
            "SQLi-Error" -> detectSqli(task, resp)
            "SQLi-Union" -> detectUnion(task, resp, baselineLen)
            "SQLi-Auth" -> detectAuth(task, resp)
            "SQLi-Blind" -> detectBlind(task, resp)
            "XSS" -> detectXss(task, resp)
            "LFI" -> detectLfi(task, resp)
            "RFI" -> detectRfi(task, resp, baselineLen)
            "RCE" -> detectRce(task, resp)
            "SSTI" -> detectSsti(task, resp)
            "SSRF" -> detectSsrf(task, resp)
            "OpenRedirect" -> detectRedirect(task, resp)
            "CRLF" -> detectCrlf(task, resp)
            "XXE" -> detectXxe(task, resp)
            else -> null
        }
    }

    private fun bodyHasSignatures(body: String, sigs: List<String>): Boolean {
        val lower = body.lowercase()
        return sigs.any { lower.contains(it.lowercase()) }
    }

    private fun detectSqli(task: Task, resp: HttpResponse): Finding? {
        if (bodyHasSignatures(resp.body, sqlErrorSignatures)) {
            return Finding(
                severityFor.getValue("SQLi-Error"), "SQLi (error-based)",
                "String error SQL bocor di respons", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectBlind(task: Task, resp: HttpResponse): Finding? {
        val isTimeBased = timeMarkers.any { task.payload.contains(it) }
        if (isTimeBased && resp.durationMs > 5000) {
            return Finding(
                severityFor.getValue("SQLi-Blind"), "SQLi (blind/time-based)",
                "Delay ${resp.durationMs}ms > 5s dengan payload SLEEP/WAITFOR", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectUnion(task: Task, resp: HttpResponse, baselineLen: Int): Finding? {
        if (bodyHasSignatures(resp.body, sqlErrorSignatures)) {
            return Finding(
                severityFor.getValue("SQLi-Union"), "SQLi (union)",
                "Error SQL + payload union", resp.url, task.payload
            )
        }
        if (baselineLen > 0) {
            val diff = Math.abs(resp.body.length - baselineLen)
            if (diff > 400 && diff > baselineLen / 2) {
                return Finding(
                    "MEDIUM", "SQLi (union/possible)",
                    "Panjang body beda jauh dari baseline (${baselineLen} -> ${resp.body.length})", resp.url, task.payload
                )
            }
        }
        return null
    }

    private fun detectAuth(task: Task, resp: HttpResponse): Finding? {
        val ok = resp.statusCode in 200..302
        val suspicious = resp.body.lowercase().contains("welcome") ||
            resp.body.lowercase().contains("dashboard") ||
            resp.body.lowercase().contains("logged in")
        if (ok && suspicious) {
            return Finding(
                severityFor.getValue("SQLi-Auth"), "SQLi (auth bypass/possible)",
                "Status ${resp.statusCode} + indikator halaman login berhasil", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectXss(task: Task, resp: HttpResponse): Finding? {
        val lower = resp.body.lowercase()
        val reflected = resp.body.contains(task.payload) ||
            (task.payload.contains("<script") && lower.contains("<script")) ||
            (task.payload.contains("onerror=") && lower.contains("onerror=")) ||
            (task.payload.contains("onload=") && lower.contains("onload="))
        if (reflected) {
            return Finding(
                severityFor.getValue("XSS"), "XSS (reflected)",
                "Payload ter-reflect di body respons", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectLfi(task: Task, resp: HttpResponse): Finding? {
        if (bodyHasSignatures(resp.body, lfiSignatures)) {
            return Finding(
                severityFor.getValue("LFI"), "LFI",
                "Konten file lokal terlihat di respons (passwd/hosts/uid)", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectRfi(task: Task, resp: HttpResponse, baselineLen: Int): Finding? {
        if (task.payload.contains("php://") && resp.body.contains("base64")) {
            return Finding(
                "MEDIUM", "RFI (php filter)",
                "Wrapper php:// merespons dengan data ter-encode", resp.url, task.payload
            )
        }
        if (baselineLen > 0 && task.payload.contains("http") && resp.body.length - baselineLen > 3000) {
            return Finding(
                severityFor.getValue("RFI"), "RFI (possible)",
                "Body jauh lebih besar dari baseline — kemungkinan include jarak jauh", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectRce(task: Task, resp: HttpResponse): Finding? {
        val lower = resp.body.lowercase()
        val hit = lower.contains("uid=") ||
            lower.contains("root:x:0:0") ||
            lower.contains("www-data:x:") ||
            (task.payload.contains("sleep") && resp.durationMs > 4000)
        if (hit) {
            return Finding(
                severityFor.getValue("RCE"), "RCE (command injection)",
                "Output eksekusi perintah terlihat di respons", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectSsti(task: Task, resp: HttpResponse): Finding? {
        if (task.payload.contains("7*7") && resp.body.contains("49")) {
            return Finding(
                severityFor.getValue("SSTI"), "SSTI",
                "Ekspresi 7*7 dievaluasi menjadi 49", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectSsrf(task: Task, resp: HttpResponse): Finding? {
        val isMetadataTarget = task.payload.contains("169.254.169.254") ||
            task.payload.contains("metadata.google") ||
            task.payload.contains("100.100.100.200")
        if (isMetadataTarget && bodyHasSignatures(resp.body, ssrfSignatures)) {
            return Finding(
                severityFor.getValue("SSRF"), "SSRF",
                "Response berisi data metadata cloud (${task.payload.take(40)})", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectRedirect(task: Task, resp: HttpResponse): Finding? {
        val final = resp.url.lowercase()
        val payload = task.payload.lowercase()
        if (final.contains("evil.com") || (payload.contains("evil.com") && final != resp.url)) {
            return Finding(
                severityFor.getValue("OpenRedirect"), "Open Redirect",
                "URL akhir ${resp.url}", resp.url, task.payload
            )
        }
        val loc = resp.responseHeaders.entries
            .firstOrNull { it.key.equals("Location", true) }?.value.orEmpty()
        if (loc.contains("evil.com") || loc.contains("javascript:")) {
            return Finding(
                severityFor.getValue("OpenRedirect"), "Open Redirect",
                "Header Location ter-inject: $loc", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectCrlf(task: Task, resp: HttpResponse): Finding? {
        val hdrs = resp.responseHeaders
        val injected = hdrs.keys.any { it.contains("X-Injected", true) } ||
            hdrs.values.any { it.contains("injected", true) } ||
            hdrs.values.any { it.contains("http://evil.com", true) }
        if (injected) {
            return Finding(
                severityFor.getValue("CRLF"), "CRLF Injection",
                "Header respons ter-inject: ${hdrs.keys.joinToString(", ")}", resp.url, task.payload
            )
        }
        return null
    }

    private fun detectXxe(task: Task, resp: HttpResponse): Finding? {
        val lower = resp.body.lowercase()
        val hit = bodyHasSignatures(resp.body, lfiSignatures) ||
            lower.contains("win.ini") ||
            bodyHasSignatures(resp.body, ssrfSignatures) ||
            lower.contains("entity") && lower.contains("parsing")
        if (hit) {
            return Finding(
                severityFor.getValue("XXE"), "XXE",
                "Konten file/entitas XML ter-resolve di respons", resp.url, task.payload
            )
        }
        return null
    }
}
