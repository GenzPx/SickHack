package self.apk.sickhack.genz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import self.apk.sickhack.genz.TOOLS
import self.apk.sickhack.genz.core.payloads.Payloads
import self.apk.sickhack.genz.ui.theme.BlackBg
import self.apk.sickhack.genz.ui.theme.SurfaceHigh
import self.apk.sickhack.genz.ui.theme.TerminalGreen
import self.apk.sickhack.genz.ui.theme.TerminalGreenDim

@Composable
fun HomeScreen(onOpen: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBg)
    ) {
        // Header branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceHigh)
                .border(1.dp, TerminalGreenDim.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(">/", color = TerminalGreen, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(
                    "SICKHACK",
                    color = TerminalGreen,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
            Text(
                "// terminal pentest toolkit — by GenzPX",
                color = TerminalGreenDim,
                fontSize = 12.sp
            )
            Text(
                "// tools: ${TOOLS.size}   payloads: ${Payloads.totalCount()}",
                color = TerminalGreenDim,
                fontSize = 12.sp
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(TOOLS) { tool ->
                ToolTile(
                    name = tool.name,
                    desc = tool.desc,
                    icon = { Icon(tool.icon, contentDescription = tool.name, tint = TerminalGreen) },
                    onClick = { onOpen(tool.route) }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "// edukasi & authorized testing only\n// GenzPX — github.com/GenzPx/SickBar",
                        color = TerminalGreenDim,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ToolTile(
    name: String,
    desc: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .border(1.dp, TerminalGreenDim.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .background(SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(6.dp))
            Text(
                text = name,
                color = TerminalGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = desc,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 10.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
