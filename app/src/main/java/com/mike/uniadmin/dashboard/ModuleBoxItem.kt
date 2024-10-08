package com.mike.uniadmin.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.model.modules.ModuleViewModel
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ModuleBox(
    module: ModuleEntity,
    navController: NavController,
    onClicked: (ModuleEntity) -> Unit
) {
    ModuleBoxContent(
        imageContent = {
            AsyncImage(
                model = module.moduleImageLink,
                contentDescription = module.moduleName,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        },
        bodyContent = {
            Text(
                module.moduleCode,
                style = CC.descriptionTextStyle().copy(color = CC.secondary()),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Text(
                module.moduleName,
                style = CC.titleTextStyle()
                    .copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CC.textColor().copy(0.8f)),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            val visits = when (module.visits) {
                0 -> "Never visited"
                1 -> "Visited once"
                else -> "Visited ${module.visits} times"
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    visits, style = CC.descriptionTextStyle().copy(color = CC.tertiary())
                )
                IconButton(onClick = {
                    onClicked(module)
                    UniConnectPreferences.saveModuleName(module.moduleName)
                    UniConnectPreferences.saveModuleID(module.moduleCode)
                    navController.navigate("moduleResource/${module.moduleCode}")
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = CC.textColor()
                    )
                }
            }
        }
    )
}

@Composable
fun LoadingModuleBox() {
    ModuleBoxContent(
        imageContent = {
            CC.ColorProgressIndicator(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxSize()
            )
        },
        bodyContent = {
            LoadingPlaceholder(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.5f)
            ) // Adjusted to a fraction of width
            LoadingPlaceholder(
                modifier = Modifier
                    .height(25.dp)
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
            )
            LoadingPlaceholder(
                modifier = Modifier
                    .height(25.dp)
                    .fillMaxWidth(0.5f)
            )
        }
    )
}

@Composable
fun ModuleBoxContent(
    imageContent: @Composable BoxScope.() -> Unit,
    bodyContent: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints {
        val boxWidth = maxWidth * 0.4f // Each item takes 40% of the screen width
        val adaptiveWidth = boxWidth.coerceIn(minimumValue = 150.dp, maximumValue = 185.dp)
        val boxHeight = adaptiveWidth * 1.15f

        Column(
            modifier = Modifier
                .padding(end = 10.dp)
                .shadow(
                    elevation = 4.dp, shape = RoundedCornerShape(16.dp),
                )
                .width(adaptiveWidth)
                .height(boxHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.4f)
                    .background(randomColor.random(), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxWidth(),
                content = imageContent
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(CC.surfaceContainer(), RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                content = bodyContent
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ModuleBoxList(
    modules: List<ModuleEntity>,
    navController: NavController,
    moduleViewModel: ModuleViewModel
) {
    BoxWithConstraints {

        if (modules.isEmpty()) {
            LazyRow(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxWidth(),
            ) {
                // Display loading placeholders when the list is empty
                items(3) {
                    LoadingModuleBox()
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(),
            ) {
                items(modules) { module ->
                    ModuleBox(
                        module = module,
                        navController = navController,
                        onClicked = { updatedModule ->
                            moduleViewModel.saveModule(
                                updatedModule.copy(
                                    visits = updatedModule.visits.plus(
                                        1
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}


