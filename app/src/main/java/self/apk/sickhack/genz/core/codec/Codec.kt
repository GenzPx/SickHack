package self.apk.sickhack.genz.core.codec

import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Base64

/** Encoder / decoder / hash / helpers. */
object Codec {

    // ---------- Base64 ----------
    fun b64encode(s: String): String =
        Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))

    fun b64decode(s: String): String = try {
        String(Base64.getDecoder().decode(s.trim()), Charsets.UTF_8)
    } catch (e: Exception) {
        "INVALID BASE64"
    }

    // ---------- Base32 (RFC 4648) ----------
    private const val B32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun b32encode(s: String): String {
        val bytes = s.toByteArray(Charsets.UTF_8)
        val sb = StringBuilder()
        var buffer = 0
        var bits = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                sb.append(B32_ALPHABET[(buffer shr (bits - 5)) and 0x1F])
                bits -= 5
            }
        }
        if (bits > 0) {
            sb.append(B32_ALPHABET[(buffer shl (5 - bits)) and 0x1F])
        }
        while (sb.length % 8 != 0) sb.append('=')
        return sb.toString()
    }

    fun b32decode(s: String): String = try {
        val clean = s.trim().uppercase().filter { it != '=' }
        var buffer = 0
        var bits = 0
        val out = java.io.ByteArrayOutputStream()
        for (c in clean) {
            val v = B32_ALPHABET.indexOf(c)
            if (v < 0) continue
            buffer = (buffer shl 5) or v
            bits += 5
            if (bits >= 8) {
                out.write((buffer shr (bits - 8)) and 0xFF)
                bits -= 8
            }
        }
        String(out.toByteArray(), Charsets.UTF_8)
    } catch (e: Exception) {
        "INVALID BASE32"
    }

    // ---------- URL ----------
    fun urlEncode(s: String): String =
        URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    fun urlDecode(s: String): String = try {
        URLDecoder.decode(s.replace("+", "%2B"), "UTF-8")
    } catch (e: Exception) {
        "INVALID URL ENCODING"
    }

    private fun isHex(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'

    private fun pctChar(c: Char): String {
        val b = c.toString().toByteArray(Charsets.UTF_8)
        return b.joinToString("") { "%" + "%02X".format(it) }
    }

    /** Percent-encode semua karakter kecuali sequence %XX yang sudah ada.
     *  Dipakai supaya payload CRLF (`%0d%0a...`) tidak di-double-encode. */
    fun preserveEncode(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '%' && i + 2 < s.length && isHex(s[i + 1]) && isHex(s[i + 2])) {
                sb.append(s.substring(i, i + 3))
                i += 3
            } else if (c.isLetterOrDigit() || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append(c)
                i++
            } else {
                sb.append(pctChar(c))
                i++
            }
        }
        return sb.toString()
    }

    // ---------- Hex ----------
    fun hexEncode(s: String): String =
        s.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }

    fun hexDecode(s: String): String = try {
        val clean = s.trim().replace(" ", "")
        require(clean.length % 2 == 0)
        val bytes = ByteArray(clean.length / 2)
        for (i in bytes.indices) {
            bytes[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        String(bytes, Charsets.UTF_8)
    } catch (e: Exception) {
        "INVALID HEX"
    }

    // ---------- Binary / ASCII ----------
    fun toBinary(s: String): String =
        s.toByteArray(Charsets.UTF_8).joinToString(" ") {
            (it.toInt() and 0xFF).toString(2).padStart(8, '0')
        }

    fun fromBinary(s: String): String = try {
        val parts = s.trim().split(" ").filter { it.isNotBlank() }
        val bytes = ByteArray(parts.size)
        for (i in parts.indices) {
            bytes[i] = parts[i].toInt(2).toByte()
        }
        String(bytes, Charsets.UTF_8)
    } catch (e: Exception) {
        "INVALID BINARY"
    }

    fun toAsciiCodes(s: String): String =
        s.map { it.code.toString() }.joinToString(" ")

    fun fromAsciiCodes(s: String): String = try {
        s.trim().split(" ").filter { it.isNotBlank() }.map { it.toInt().toChar() }.joinToString("")
    } catch (e: Exception) {
        "INVALID ASCII CODES"
    }

    // ---------- Case transforms ----------
    fun rot13(s: String): String = s.map { c ->
        when (c) {
            in 'a'..'z' -> 'a' + (c - 'a' + 13) % 26
            in 'A'..'Z' -> 'A' + (c - 'A' + 13) % 26
            else -> c
        }
    }.joinToString("")

    fun reverse(s: String): String = s.reversed()

    fun upper(s: String): String = s.uppercase()

    fun lower(s: String): String = s.lowercase()

    fun toggleCase(s: String): String = s.map { c ->
        when {
            c.isUpperCase() -> c.lowercaseChar()
            c.isLowerCase() -> c.uppercaseChar()
            else -> c
        }
    }.joinToString("")

    // ---------- JS encoders ----------
    /** alert(1) -> String.fromCharCode(97,108,101,114,116,40,49,41) */
    fun toJsCharcode(s: String): String =
        "String.fromCharCode(" + s.map { it.code.toString() }.joinToString(",") + ")"

    /** alert -> \x61\x6c\x65\x72\x74 */
    fun toJsHex(s: String): String =
        s.toByteArray(Charsets.UTF_8).joinToString("") {
            "\\x" + (it.toInt() and 0xFF).toString(16).padStart(2, '0')
        }

    /** alert -> &#97;&#108;&#101;&#114;&#116; */
    fun toHtmlDecimal(s: String): String =
        s.map { "&#" + it.code + ";" }.joinToString("")

    fun toHtmlHex(s: String): String =
        s.map { "&#x" + it.code.toString(16) + ";" }.joinToString("")

    // ---------- HTML entities ----------
    fun htmlEncode(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(c)
            }
        }
    }

    fun htmlDecode(s: String): String {
        var out = s.replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
        out = Regex("&#(\\d+);").replace(out) { m ->
            m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: m.value
        }
        out = Regex("&#x([0-9a-fA-F]+);").replace(out) { m ->
            m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: m.value
        }
        return out
    }

    // ---------- Hashes ----------
    fun md5(s: String): String = hash(s, "MD5")
    fun sha1(s: String): String = hash(s, "SHA-1")
    fun sha224(s: String): String = hash(s, "SHA-224")
    fun sha256(s: String): String = hash(s, "SHA-256")
    fun sha384(s: String): String = hash(s, "SHA-384")
    fun sha512(s: String): String = hash(s, "SHA-512")

    private fun hash(s: String, alg: String): String = try {
        val d = MessageDigest.getInstance(alg).digest(s.toByteArray(Charsets.UTF_8))
        d.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        "ERROR: $alg"
    }

    // ---------- Hash identifier ----------
    fun identifyHash(h: String): String {
        val s = h.trim()
        val hexRe = Regex("^[0-9a-fA-F]+$")
        if (!hexRe.matches(s)) {
            return when {
                s.startsWith("\$2y\$") || s.startsWith("\$2a\$") || s.startsWith("\$2b\$") -> "bcrypt"
                s.startsWith("\$6\$") -> "sha512crypt"
                s.startsWith("\$5\$") -> "sha256crypt"
                s.startsWith("\$1\$") -> "md5crypt"
                s.startsWith("\$apr1\$") -> "Apache MD5 (apr1)"
                s.startsWith("\$P\$") -> "WordPress phpass"
                s.startsWith("\$H\$") -> "phpBB3"
                s.startsWith("\$B\$") -> "bcrypt (Blowfish \$B\$)"
                else -> "Unknown / non-hex format"
            }
        }
        return when (s.length) {
            16 -> "MySQL 3.x (16 hex)"
            32 -> "MD5"
            40 -> "SHA-1"
            56 -> "SHA-224"
            64 -> "SHA-256"
            96 -> "SHA-384"
            128 -> "SHA-512"
            else -> "Unknown hex length (${s.length})"
        }
    }

    // ---------- JWT ----------
    fun decodeJwt(token: String): String = try {
        val parts = token.trim().split(".")
        require(parts.size >= 2)
        val b64url = { p: String ->
            val pad = if (p.length % 4 == 0) "" else "=".repeat(4 - p.length % 4)
            String(Base64.getUrlDecoder().decode(p.replace('-', '+').replace('_', '/') + pad), Charsets.UTF_8)
        }
        var out = "== HEADER ==\n"
        out += try { prettyJson(b64url(parts[0])) } catch (e: Exception) { b64url(parts[0]) }
        out += "\n\n== PAYLOAD ==\n"
        out += try { prettyJson(b64url(parts[1])) } catch (e: Exception) { b64url(parts[1]) }
        if (parts.size >= 3) out += "\n\n== SIGNATURE ==\n" + parts[2]
        out
    } catch (e: Exception) {
        "INVALID JWT: ${e.message}"
    }

    // ---------- JSON formatter ----------
    fun prettyJson(s: String): String = try {
        val o = org.json.JSONObject(s)
        o.toString(2)
    } catch (e: Exception) {
        try {
            val a = org.json.JSONArray(s)
            a.toString(2)
        } catch (e2: Exception) {
            "INVALID JSON: ${e.message}"
        }
    }
}
