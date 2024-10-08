package com.mike.uniadmin.uniChat.groupChat

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.model.groupchat.GroupChatEntity
import com.mike.uniadmin.model.groupchat.GroupEntity
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.model.users.UserViewModel
import com.mike.uniadmin.getGroupChatViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.homeScreen.UserItem
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.ChatBubble
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.ChatTopAppBar
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.DateHeader
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.GroupDetails
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.MessageInputRow
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.RowText
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.SearchBar
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.sendMessage
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun DiscussionScreen(
    navController: NavController, context: Context, targetGroupID: String
) {
    val chatViewModel = getGroupChatViewModel(context)
    val userViewModel = getUserViewModel(context)

    val chats by chatViewModel.chats.observeAsState(listOf())
    val user by userViewModel.user.observeAsState(initial = null)
    val group by chatViewModel.group.observeAsState(initial = null)
    val users by userViewModel.users.observeAsState(emptyList())

    var messageText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showUsers by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scrollState = rememberLazyListState()
    val groupPath = "Group Chat/$targetGroupID"


    LaunchedEffect(currentUser?.email) {
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }

    LaunchedEffect(Unit) {
        if (chats.isNotEmpty()) {
            scrollState.animateScrollToItem(chats.size - 1)
        }
        chatViewModel.fetchGroupChats()
        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchGroupChats(groupPath)
        chatViewModel.fetchGroupById(targetGroupID)
    }


    Scaffold(
        topBar = {
            GroupDetails.groupName.value?.let { groupName ->
                GroupDetails.groupImageLink.value?.let { imageLink ->
                    ChatTopAppBar(
                        navController = navController,
                        targetGroupID = targetGroupID,
                        name = groupName,
                        link = imageLink,
                        onSearchClick = { isSearchVisible = !isSearchVisible },
                        onShowUsersClick = { showUsers = !showUsers })
                }
            }
        }, snackbarHost = { SnackbarHost(snackbarHostState) }, content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Background()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp)
                        .imePadding()
                ) {
                    group?.let {
                        GroupUsersList(
                            isVisible = showUsers,
                            users = users,
                            navController = navController,
                            viewModel = userViewModel,
                            group = it
                        )
                    }
                    SearchBar(isSearchVisible = isSearchVisible,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it })
                    user?.let { currentUser ->
                        LazyColumn(
                            state = scrollState, modifier = Modifier
                                .animateContentSize()
                                .weight(1f)
                        ) {

                            val groupedChats =
                                chats.groupBy { CC.getDateFromTimeStamp(it.groupChat.date) }

                            groupedChats.forEach { (date, chatsForDate) ->
                                item {
                                    RowText()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DateHeader(date)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                items(chatsForDate.filter {
                                    it.groupChat.message.contains(
                                        searchQuery.text, ignoreCase = true
                                    )
                                }) { chat ->
                                    ChatBubble(
                                        chat = chat,
                                        isUser = chat.groupChat.senderID == currentUser.id,
                                        navController = navController
                                    )
                                }
                            }
                        }
                        MessageInputRow(
                            message = messageText,
                            onMessageChange = { messageText = it },
                            onSendClick = {
                                if (messageText.isNotBlank() && currentUser.firstName.isNotBlank()) {
                                    MyDatabase.generateChatID { chatID ->
                                        val chat = GroupChatEntity(
                                            message = messageText,
                                            senderID = currentUser.id,
                                            chatId = chatID,
                                            date = CC.getTimeStamp()
                                        )
                                        sendMessage(
                                            chat = chat, viewModel = chatViewModel, path = groupPath
                                        )
                                        messageText = ""
                                    }
                                }
                            },
                        )
                    } ?: run {
                        Text("Loading...", style = CC.descriptionTextStyle())
                    }
                }
            }
        })
}


@Composable
fun GroupUsersList(
    isVisible: Boolean,
    users: List<UserEntity>,
    navController: NavController,
    viewModel: UserViewModel,
    group: GroupEntity
) {
    val filteredUsers = users.filter { user ->
        group.members.contains(user.id)  // Filter users based on membership
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            items(filteredUsers) { user ->  // Use the filtered list
                UserItem(user, navController, viewModel)
            }
        }
    }
}









