package com.mike.uniadmin.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.dataModel.groupchat.ChatRepository
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.Group
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

object GroupDetails {
    var groupName: MutableState<String> = mutableStateOf("")
    var groupImageLink: MutableState<String> = mutableStateOf("")
}

@Composable
fun UniGroups(context: Context, navController: NavController) {
    val application = context.applicationContext as UniAdmin
    val chatRepository = remember { application.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val groups by chatViewModel.groups.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyList())
    val user by userViewModel.user.observeAsState(initial = null)
    var showAddGroup by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var signedInUser by remember { mutableStateOf(User()) }
    LaunchedEffect(currentUser) {
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }
    user?.let {
        signedInUser = it
    }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        chatViewModel.fetchGroups()
    }

    val userGroups = groups.filter { it.members.contains(signedInUser.id) }

    Column(
        modifier = Modifier
            .background(CC.primary())
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp)
                .height(100.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Uni Groups",
                style = CC.titleTextStyle(context)
                    .copy(fontWeight = FontWeight.ExtraBold, fontSize = 35.sp)
            )
            IconButton(onClick = {
                showAddGroup = !showAddGroup
            }) {
                Icon(
                    if (showAddGroup) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Add Group",
                    tint = CC.textColor()
                )
            }
        }

        AnimatedVisibility(
            visible = showAddGroup,
            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
        ) {
            AddGroupSection(signedInUser, context, chatViewModel, users)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (userGroups.isEmpty()) {
            Text(
                text = "No groups available",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.animateContentSize()
            ) {
                items(userGroups) { group ->
                    if (group.name.isNotEmpty() && group.description.isNotEmpty()) {
                        GroupItem(
                            group,
                            context,
                            navController,
                            chatViewModel,
                            userViewModel,
                            signedInUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddGroupSection(
    user: User, context: Context, chatViewModel: ChatViewModel, users: List<User>
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var expanded by remember { mutableStateOf(false) }
    var imageLink by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = groupName,
            onValueChange = { groupName = it },
            label = "Group Name",
            enabled = true,
            singleLine = true,
            context = context
        )
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = "Description",
            enabled = true,
            singleLine = true,
            context = context
        )
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = imageLink,
            onValueChange = { imageLink = it },
            label = "Image link",
            enabled = true,
            singleLine = true,
            context = context
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { expanded = !expanded }, colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), contentColor = CC.textColor()
            ), shape = RoundedCornerShape(10.dp), modifier = Modifier.width(200.dp)
        ) {
            Text(
                "Select Members",
                modifier = Modifier.padding(8.dp),
                style = CC.descriptionTextStyle(context)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.background(CC.secondary(), RoundedCornerShape(10.dp))
            ) {
                items(users) { user ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMembers = if (selectedMembers.contains(user.id)) {
                                selectedMembers - user.id
                            } else {
                                selectedMembers + user.id
                            }
                        }
                        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(CC.extraColor1(), CircleShape)
                                .size(50.dp)
                                .clip(CircleShape), contentAlignment = Alignment.Center
                        ) {
                            if (user.profileImageLink.isNotBlank()) {
                                AsyncImage(
                                    model = user.profileImageLink,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("${user.firstName[0]}${user.lastName[0]}")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = CC.descriptionTextStyle(context),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Checkbox(checked = selectedMembers.contains(user.id), onCheckedChange = {
                            selectedMembers = if (it) {
                                selectedMembers + user.id
                            } else {
                                selectedMembers - user.id
                            }
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (groupName.isBlank() || groupDescription.isBlank()) {
                    Toast.makeText(context, "Please enter group name and description", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                MyDatabase.generateGroupId { id ->
                    val newGroup = GroupEntity(
                        id = id,
                        name = groupName,
                        description = groupDescription,
                        groupImageLink = imageLink,
                        admin = user.id,
                        members = selectedMembers.toList()
                    )
                    chatViewModel.saveGroup(newGroup, onSuccess = {
                        if (it) {
                            chatViewModel.fetchGroups()
                        }
                    })
                }
            }, modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor1(), contentColor = CC.textColor()
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Create", style = CC.descriptionTextStyle(context))
        }
    }
}

@Composable
fun EditGroupSection(
    group: GroupEntity,
    context: Context,
    chatViewModel: ChatViewModel,
    users: List<User>,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf(group.name) }
    var groupDescription by remember { mutableStateOf(group.description) }
    var selectedMembers by remember { mutableStateOf(group.members.toSet()) }
    var expanded by remember { mutableStateOf(false) }
    var imageLink by remember { mutableStateOf(group.groupImageLink) }

    Column(modifier = Modifier.fillMaxWidth()) {
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = groupName,
            onValueChange = { groupName = it },
            label = "Group Name",
            enabled = true,
            singleLine = true,
            context = context
        )
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = "Description",
            enabled = true,
            singleLine = true,
            context = context
        )
        CC.SingleLinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = imageLink,
            onValueChange = { imageLink = it },
            label = "Image link",
            enabled = true,
            singleLine = true,
            context = context
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { expanded = !expanded }, colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), contentColor = CC.textColor()
            ), shape = RoundedCornerShape(10.dp), modifier = Modifier.width(200.dp)
        ) {
            Text(
                "Select Members",
                modifier = Modifier.padding(8.dp),
                style = CC.descriptionTextStyle(context)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.background(CC.secondary(), RoundedCornerShape(10.dp))
            ) {
                items(users) { user ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMembers = if (selectedMembers.contains(user.id)) {
                                selectedMembers - user.id
                            } else {
                                selectedMembers + user.id
                            }
                        }
                        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(CC.extraColor1(), CircleShape)
                                .size(50.dp)
                                .clip(CircleShape), contentAlignment = Alignment.Center
                        ) {
                            if (user.profileImageLink.isNotBlank()) {
                                AsyncImage(
                                    model = user.profileImageLink,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("${user.firstName[0]}${user.lastName[0]}")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = CC.descriptionTextStyle(context),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Checkbox(checked = selectedMembers.contains(user.id), onCheckedChange = {
                            selectedMembers = if (it) {
                                selectedMembers + user.id
                            } else {
                                selectedMembers - user.id
                            }
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val updatedGroup = group.copy(
                    name = groupName,
                    description = groupDescription,
                    groupImageLink = imageLink,
                    members = selectedMembers.toList()
                )
                chatViewModel.saveGroup(updatedGroup, onSuccess = {
                    if (it) {
                        chatViewModel.fetchGroups()
                    }
                })
                onDismiss()
            }, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor1(), contentColor = CC.textColor()
            )
        ) {
            Text("Save Changes", style = CC.descriptionTextStyle(context))
        }
    }
}

@Composable
fun GroupItem(
    group: GroupEntity,
    context: Context,
    navController: NavController,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    user: User
) {
    var showEditGroup by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                GroupDetails.groupName.value = group.name
                GroupDetails.groupImageLink.value = group.groupImageLink
                navController.navigate("GroupChat/${group.id}")
            }, colors = CardDefaults.cardColors(containerColor = CC.primary())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(50.dp)
            ) {
                if (group.groupImageLink.isNotBlank()) {
                    AsyncImage(
                        model = group.groupImageLink,
                        contentDescription = "Group Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Groups,
                        "Group Image",
                        tint = CC.textColor(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (group.admin == user.id) {
                    IconButton(onClick = { showEditGroup = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Group",
                            tint = CC.textColor()
                        )
                    }
                }

            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = group.name, style = CC.titleTextStyle(context)
                )
                Text(
                    text = group.description, style = CC.descriptionTextStyle(context)
                )
            }
        }
    }

    if (showEditGroup) {
        AlertDialog(containerColor = CC.primary(),
            onDismissRequest = { showEditGroup = false },
            title = { Text("Edit Group", style = CC.titleTextStyle(context)) },
            text = {
                userViewModel.users.value?.let {
                    EditGroupSection(group = group,
                        context = context,
                        chatViewModel = chatViewModel,
                        users = it,
                        onDismiss = { showEditGroup = false })
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEditGroup = false }, colors = ButtonDefaults.buttonColors(
                        containerColor = CC.tertiary(), contentColor = CC.textColor()
                    )
                ) {
                    Text("Close", style = CC.descriptionTextStyle(context))
                }
            })
    }
}

