/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. Per GPL-3.0 Section 4 & Section 5.
 */

package moe.rukamori.archivetune.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import moe.rukamori.archivetune.LocalPlayerAwareWindowInsets
import moe.rukamori.archivetune.R
import moe.rukamori.archivetune.constants.EchoBrainAllowAlternativeVersionsKey
import moe.rukamori.archivetune.constants.EchoBrainEnabledKey
import moe.rukamori.archivetune.constants.EchoBrainSimilarity
import moe.rukamori.archivetune.constants.EchoBrainSimilarityKey
import moe.rukamori.archivetune.ui.component.EnumListPreference
import moe.rukamori.archivetune.ui.component.IconButton
import moe.rukamori.archivetune.ui.component.PreferenceGroup
import moe.rukamori.archivetune.ui.component.SwitchPreference
import moe.rukamori.archivetune.ui.utils.backToMain
import moe.rukamori.archivetune.utils.rememberEnumPreference
import moe.rukamori.archivetune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EchoBrainSettings(navController: NavController) {
    val (enabled, onEnabledChange) =
        rememberPreference(
            EchoBrainEnabledKey,
            defaultValue = false,
        )
    val (similarity, onSimilarityChange) =
        rememberEnumPreference(
            EchoBrainSimilarityKey,
            defaultValue = EchoBrainSimilarity.BALANCED,
        )
    val (allowAlternativeVersions, onAllowAlternativeVersionsChange) =
        rememberPreference(
            EchoBrainAllowAlternativeVersionsKey,
            defaultValue = false,
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.echo_brain)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back_button_desc),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(bottom = SettingsDimensions.ScreenBottomPadding),
        ) {
            PreferenceGroup(title = stringResource(R.string.queue)) {
                item {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.echo_brain)) },
                        description = stringResource(R.string.echo_brain_desc),
                        icon = { Icon(painterResource(R.drawable.auto_awesome), null) },
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                    )
                }
                item(visible = enabled) {
                    EnumListPreference(
                        title = { Text(stringResource(R.string.echo_brain_similarity)) },
                        icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                        selectedValue = similarity,
                        onValueSelected = onSimilarityChange,
                        valueText = {
                            when (it) {
                                EchoBrainSimilarity.STRICT -> stringResource(R.string.echo_brain_similarity_strict)
                                EchoBrainSimilarity.BALANCED -> stringResource(R.string.echo_brain_similarity_balanced)
                                EchoBrainSimilarity.DISCOVERY -> stringResource(R.string.echo_brain_similarity_discovery)
                                EchoBrainSimilarity.FLEXIBLE -> stringResource(R.string.echo_brain_similarity_flexible)
                            }
                        },
                    )
                }
                item(visible = enabled) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.echo_brain_alternative_versions)) },
                        description = stringResource(R.string.echo_brain_alternative_versions_desc),
                        icon = { Icon(painterResource(R.drawable.shuffle), null) },
                        checked = allowAlternativeVersions,
                        onCheckedChange = onAllowAlternativeVersionsChange,
                    )
                }
            }
        }
    }
}
