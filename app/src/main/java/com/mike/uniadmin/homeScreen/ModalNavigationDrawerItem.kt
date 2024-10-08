package com.mike.uniadmin.homeScreen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.attendance.deleteDataFromPreferences
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.Update
import com.mike.uniadmin.model.groupchat.GroupChatViewModel
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.model.users.UserStateEntity
import com.mike.uniadmin.model.users.UserViewModel
import com.mike.uniadmin.settings.switchColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ModalNavigationDrawerItem(
    activity: MainActivity,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: GroupChatViewModel,
    signedInUserLoading: Boolean?,
    signedInUser: UserEntity?,
    showBottomSheet: (Boolean) -> Unit,
    userStatus: UserStateEntity?,
    update: Update

) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    BoxWithConstraints {
        val columnWidth = maxWidth
        val columnHeight = columnWidth * 0.45f


        Column(
            modifier = Modifier
                .background(
                    CC.primary()
                )
                .fillMaxHeight()
                .fillMaxWidth(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(columnHeight),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (signedInUserLoading == true) {
                    CircularProgressIndicator(color = CC.textColor())
                } else if (signedInUser != null) {
                    SideProfile(signedInUser)
                } else {
                    Icon(Icons.Default.AccountCircle, "", tint = CC.textColor())
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                SideBarItem(icon = Icons.Default.AccountCircle,
                    text = "Profile",
                    onClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                        navController.navigate("profile")
                    })
                SideBarItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    text = "Assignments"
                ) {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate("assignments")
                }
                SideBarItem(
                    icon = Icons.Outlined.ChatBubble,
                    text = "Uni Chat",
                    onClicked = {
                        // Check if biometrics is enabled
                        if (UniConnectPreferences.biometricEnabled.value) {
                            // Show biometric prompt
                            activity.promptManager.showBiometricPrompt(
                                title = "Authenticate",
                                description = "Please authenticate to continue"
                            ) { success ->
                                if (success) {
                                    // If authentication is successful, proceed with actions
                                    scope.launch { drawerState.close() }
                                    userViewModel.fetchUsers()
                                    chatViewModel.fetchGroups()
                                    navController.navigate("uniChat")
                                }
                            }
                        } else {
                            // If biometrics are disabled, proceed directly
                            scope.launch { drawerState.close() }
                            userViewModel.fetchUsers()
                            chatViewModel.fetchGroups()
                            navController.navigate("uniChat")
                        }
                    }
                )

                SideBarItem(icon = Icons.Default.Notifications,
                    text = "Notifications",
                    onClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                        userViewModel.fetchUsers()
                        chatViewModel.fetchGroups()
                        navController.navigate("notifications")
                    })
                SideBarItem(icon = Icons.Default.Settings, text = "Settings", onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate("settings")
                })
                SideBarItem(icon = Icons.Default.Share, text = "Share App", onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "${signedInUser?.firstName} invites you to join Uni Connect! Get organized and ace your studies.\n Download now: ${update.updateLink}"
                        ) // Customize the text
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                    scope.launch { drawerState.close() }
                })
                SideBarItem(icon = Icons.Default.ArrowDownward,
                    text = "More",
                    onClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                        userViewModel.fetchUsers()
                        chatViewModel.fetchGroups()
                        showBottomSheet(true)
                    })

                var role by remember { mutableStateOf(UniConnectPreferences.userType.value) }

                if (signedInUser?.userType == "admin") {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            if (role == "admin") "Admin" else "Student",
                            style = CC.descriptionTextStyle()
                        )
                        Switch(
                            checked = role == "admin",
                            onCheckedChange = { isAdmin ->
                                role = if (isAdmin) "admin" else "student"
                                UniConnectPreferences.saveUserType(role)
                            },
                            colors = switchColors()
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        showSignOutDialog = true

                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Sign Out",
                        style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (showSignOutDialog) {
                    SignOut(
                        userStatus = userStatus,
                        onVisibleChange = { visible -> showSignOutDialog = visible },
                        context = context,
                        navController = navController,
                        userViewModel = userViewModel

                    )
                }
            }
        }
    }
}

@Composable
fun SideBarItem(icon: ImageVector, text: String, onClicked: () -> Unit) {
    Spacer(modifier = Modifier.height(10.dp))
    TextButton(onClick = onClicked) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                icon, "", tint = CC.textColor()
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = CC.descriptionTextStyle())

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOut(
    userStatus: UserStateEntity?,
    onVisibleChange: (Boolean) -> Unit,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel
) {
    BasicAlertDialog(onDismissRequest = { onVisibleChange(false) }) {
        Column(
            modifier = Modifier
                .background(CC.primary(), RoundedCornerShape(10.dp))
                .width(300.dp)
                .height(200.dp)
                .padding(16.dp) // Added padding for better spacing
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Are you sure you want to sign out?",
                    style = CC.titleTextStyle(),
                    textAlign = TextAlign.Center // Center-align the text
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Signing out will clear app settings and data.",
                style = CC.descriptionTextStyle(),
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        userStatus?.copy(
                            online = "offline",
                            lastDate = CC.getTimeStamp(),
                            lastTime = CC.getTimeStamp()
                        )?.let { updatedUserStatus ->
                            MyDatabase.writeUserActivity(updatedUserStatus, onSuccess = { success ->
                                if (success) {
                                    UniConnectPreferences.clearAllData()
                                    userViewModel.deleteAllTables()
                                    deleteDataFromPreferences(context)
                                    navController.navigate("login") {
                                        popUpTo("homeScreen") { inclusive = true }
                                    }
                                    Toast.makeText(
                                        context, "Signed Out Successfully!", Toast.LENGTH_SHORT
                                    ).show()
                                    onVisibleChange(false)
                                    FirebaseAuth.getInstance().signOut()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error signing out. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.extraColor1()
                    )
                ) {
                    Text("Sign Out", style = CC.descriptionTextStyle())
                }

                TextButton(
                    onClick = { onVisibleChange(false) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    )
                ) {
                    Text("Cancel", style = CC.descriptionTextStyle())
                }
            }
        }
    }
}

@Composable
fun SideProfile(user: UserEntity) {
    BoxWithConstraints {
        val columnWidth = maxWidth
        val iconSize = columnWidth * 0.45f

        val density = LocalDensity.current
        val textSize = with(density) { (columnWidth * 0.07f).toSp() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    CC
                        .extraColor2()
                        .copy(0.5f)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.extraColor1(), CircleShape)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageLink.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${user.firstName[0]}${user.lastName[0]}",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = textSize),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                user.firstName + " " + user.lastName,
                style = CC.titleTextStyle().copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                ),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            val userType = UniConnectPreferences.userType.value.uppercase(Locale.ROOT)

            Text(userType, style = CC.descriptionTextStyle())
        }
    }
}



