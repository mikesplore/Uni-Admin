package com.mike.uniadmin.dashboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object Sidebar {
    var showSideBar: MutableState<Boolean> = mutableStateOf(false)
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    signedInUser: UserEntity,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel,
) {
    val loading by userViewModel.isLoading.observeAsState()
    val notifications by notificationViewModel.notifications.observeAsState()
    var expanded by remember { mutableStateOf(false) }
    val unreadCount = notifications?.size ?: 0

    TopAppBar(title = {
        Row(
            modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu Button
            IconButton(onClick = { Sidebar.showSideBar.value = !Sidebar.showSideBar.value }) {
                Icon(
                    Icons.Default.Menu, contentDescription = null, tint = CC.textColor()
                )
            }

            // Greeting and Name
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CC.getGreetingMessage(),
                    style = CC.descriptionTextStyle(context)
                        .copy(color = CC.textColor().copy(alpha = 0.5f))
                )
                Text(
                    text = signedInUser.firstName,
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }, actions = {
        // Notifications
        BoxWithConstraints(modifier = Modifier.padding(end = 10.dp)) {
            Box(modifier = Modifier
                .clickable { expanded = !expanded }

            ) {
                BadgedBox(badge = {
                    if (unreadCount > 0) {
                        Badge {
                            Text(text = unreadCount.toString())
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = CC.secondary(),
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .width(160.dp)
                    .background(CC.extraColor1())
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (notifications != null && notifications!!.isNotEmpty()) {
                        notifications!!.take(5).forEach { notification ->
                            NotificationTitleContent(notification, context)
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = {
                                navController.navigate("notifications")
                                expanded = false
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All", style = CC.descriptionTextStyle(context))
                        }
                    } else {
                        Text("No notifications", style = CC.descriptionTextStyle(context))
                    }
                }
            }
        }

        // Profile Image
        BoxWithConstraints(modifier = Modifier.padding(end = 10.dp)) {
            val size = 50.dp
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .border(1.dp, CC.textColor(), CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .clip(CircleShape)
                    .size(size),
                contentAlignment = Alignment.Center
            ) {
                if (loading == true) {
                    CircularProgressIndicator(color = CC.textColor())
                } else if (signedInUser.firstName.isEmpty()) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = CC.textColor()
                    )
                } else {
                    if (signedInUser.profileImageLink.isNotEmpty()) {
                        AsyncImage(
                            model = signedInUser.profileImageLink,
                            contentDescription = signedInUser.firstName,
                            modifier = Modifier
                                .clip(CircleShape)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            "${signedInUser.firstName[0]}${signedInUser.lastName[0]}",
                            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = CC.primary()
    )
    )
}

@Composable
fun NotificationTitleContent(
    notification: NotificationEntity, context: Context
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .padding(5.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = notification.title,
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
