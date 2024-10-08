package com.mike.uniadmin.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.R
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.model.users.UserViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val userViewModel = getUserViewModel(context)
    val currentUser by userViewModel.user.observeAsState()
    var updated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val email = UniConnectPreferences.userEmail.value

    LaunchedEffect(updated) {
        userViewModel.findUserByEmail(email) { user ->
            if (user != null) {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        // Display a loading indicator while data is being fetched
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(CC.primary())
        ) {
            CircularProgressIndicator(color = CC.textColor())
        }
    } else {
        // Display the profile screen content
        Scaffold(
            topBar = {
                TopAppBar(title = {}, navigationIcon = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor()
                        )
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                )
                )
            }, containerColor = CC.primary()
        ) {
            BoxWithConstraints {
                val columnWidth = maxWidth
                val rowHeight = columnWidth * 0.15f
                val density = LocalDensity.current
                val textSize = with(density) { (columnWidth * 0.07f).toSp() }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(it)
                        .background(CC.primary())
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .height(rowHeight)
                            .fillMaxWidth(0.9f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Profile",
                            style = CC.titleTextStyle(),
                            fontSize = textSize * 1.2f,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    DisplayImage(
                        currentUser,
                        context,
                        userViewModel,
                        updated,
                        onUpdateChange = { update ->
                            if (update) {
                                userViewModel.fetchUsers()
                                Toast.makeText(
                                    context,
                                    "Updated!, please relaunch the screen",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            updated = !updated
                        })
                    Spacer(modifier = Modifier.height(20.dp))
                    ProfileDetails(
                        userViewModel,
                        updated,
                        onUpdateChange = { updated = !updated })
                    Spacer(modifier = Modifier.height(50.dp))
                    DangerZone(context, userViewModel)
                }
            }
        }
    }
}


@Composable
fun DisplayImage(
    currentUser: UserEntity?,
    context: Context,
    viewModel: UserViewModel,
    updated: Boolean,
    onUpdateChange: (Boolean) -> Unit
) {
    var showImageLinkBox by remember { mutableStateOf(false) }
    var link by remember { mutableStateOf("") }
    var imageClicked by remember { mutableStateOf(false) }

    val boxSize by animateDpAsState(
        targetValue = if (imageClicked) 160.dp else 100.dp, label = "", animationSpec = tween(200)
    )
    val size by animateDpAsState(
        targetValue = if (imageClicked) 150.dp else 70.dp, label = "", animationSpec = tween(200)
    )


    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(randomColor.random(), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .height(boxSize)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = currentUser?.profileImageLink,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .blur(30.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { R.drawable.logo },
                onLoading = { R.drawable.logo })
            IconButton(
                onClick = {
                    imageClicked = !imageClicked
                },
                modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(size),
            ) {
                if (currentUser?.profileImageLink?.isNotEmpty() == true) {
                    AsyncImage(model = currentUser.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { R.drawable.logo },
                        onLoading = { R.drawable.logo }

                    )
                } else {
                    Text(
                        "${currentUser?.firstName?.get(0)}${currentUser?.lastName?.get(0)}",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                    )
                }
            }
        }

    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CC.CustomTextField(
            value = link,
            onValueChange = { link = it },
            label = "Image Link",
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(5.dp))
        Button(
            onClick = {
                currentUser?.let {
                    viewModel.writeUser(it.copy(profileImageLink = link), onSuccess = {
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                        viewModel.fetchUsers()
                        showImageLinkBox = false
                        onUpdateChange(updated)
                    })
                }
            }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), disabledContainerColor = CC.tertiary()

            ), enabled = link.isNotEmpty()

        ) {
            Text("Save", style = CC.descriptionTextStyle().copy(fontSize = 13.sp))
        }
    }
}


@Composable
fun MyDetails(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    fontSize: TextUnit = 18.sp,
    isEditing: Boolean
) {
    Column(
        modifier = Modifier
            .height(100.dp)  // Adjusted height to accommodate both the title and text field
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)  // Padding to separate the title from the text field
        )

        TextField(
            value = value,
            textStyle = CC.titleTextStyle().copy(
                fontSize = fontSize,
                color = if (isEditing) CC.textColor() else CC.textColor().copy(0.5f)
            ),
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = CC.tertiary(),
                focusedIndicatorColor = CC.tertiary(),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = CC.textColor(),
                disabledContainerColor = Color.Transparent,
                cursorColor = CC.textColor()
            ),
            enabled = isEditing,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )
    }
}




