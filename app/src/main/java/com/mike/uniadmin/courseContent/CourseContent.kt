package com.mike.uniadmin.courseContent


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncement
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetail
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.backEnd.courses.CourseViewModel
import com.mike.uniadmin.backEnd.courses.CourseViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.randomColor

import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.mike.uniadmin.ui.theme.CommonComponents as CC

var background = randomColor.random()


@Composable
fun CourseContent(context: Context, targetCourseID: String) {

    val courseResource = context.applicationContext as? UniAdmin
    val courseRepository = remember { courseResource?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )


    val courseAnnouncementRepository = remember { courseResource?.courseAnnouncementRepository }
    val courseAnnouncementViewModel: CourseAnnouncementViewModel = viewModel(
        factory = CourseAnnouncementViewModelFactory(
            courseAnnouncementRepository
                ?: throw IllegalStateException("CourseAnnouncementRepository is null")
        )
    )

    val courseAssignmentRepository = remember { courseResource?.courseAssignmentRepository }
    val courseAssignmentViewModel: CourseAssignmentViewModel = viewModel(
        factory = CourseAssignmentViewModelFactory(
            courseAssignmentRepository
                ?: throw IllegalStateException("CourseAssignmentRepository is null")
        )
    )

    val courseDetailRepository = remember { courseResource?.courseDetailRepository }
    val courseDetailViewModel: CourseDetailViewModel = viewModel(
        factory = CourseDetailViewModelFactory(
            courseDetailRepository ?: throw IllegalStateException("CourseDetailsRepository is null")
        )
    )

    val courseTimetableRepository = remember { courseResource?.courseTimetableRepository }
    val courseTimetableViewModel: CourseTimetableViewModel = viewModel(
        factory = CourseTimetableViewModelFactory(
            courseTimetableRepository
                ?: throw IllegalStateException("CourseTimetableRepository is null")
        )
    )

    val userRepository = remember { courseResource?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("CourseTimetableRepository is null")
        )
    )

    val announcementsLoading by courseAnnouncementViewModel.isLoading.observeAsState(initial = false)
    val assignmentsLoading by courseAssignmentViewModel.isLoading.observeAsState(initial = false)
    val timetablesLoading by courseTimetableViewModel.isLoading.observeAsState(initial = false)
    val detailsLoading by courseDetailViewModel.isLoading.observeAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()
    val courseInfo by courseViewModel.fetchedCourse.observeAsState(initial = null)

    LaunchedEffect(targetCourseID) {
        background = randomColor.random()

        courseViewModel.getCourseDetailsByCourseID(targetCourseID)
        courseAnnouncementViewModel.getCourseAnnouncements(targetCourseID)
        courseAssignmentViewModel.getCourseAssignments(targetCourseID)
        courseTimetableViewModel.getCourseTimetables(targetCourseID)
        courseDetailViewModel.getCourseDetails(targetCourseID)
    }

    Scaffold(
        containerColor = CC.primary(),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(it)
                .fillMaxSize()
                .background(CC.primary()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .requiredHeight(200.dp)
                    .fillMaxWidth(0.9f),
                contentAlignment = Alignment.Center
            ) {
                val gradientColors = listOf(CC.extraColor2(), CC.textColor(), CC.extraColor1())

                courseInfo?.courseName?.let { it1 ->
                    AsyncImage(
                        model = courseInfo?.courseImageLink,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = it1, style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            brush = Brush.linearGradient(
                                colors = gradientColors
                            ),
                            fontSize = 30.sp
                        )
                    )
                }

            }
            //the tabs column starts here
            Column(
                modifier = Modifier
                    .background(CC.primary())
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                val tabRowHorizontalScrollState by remember { mutableStateOf(ScrollState(0)) }
                val tabTitles = listOf(
                    "Announcements",
                    "Assignments",
                    "Timetable",
                    "Details",
                )
                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(4.dp)
                            .width(screenWidth / tabTitles.size) // Divide by the number of tabs
                            .background(CC.textColor(), CircleShape)
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    modifier = Modifier.imePadding(),
                    containerColor = CC.primary()

                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                tabRowHorizontalScrollState.animateScrollTo(
                                    (screenWidth.value / tabTitles.size * index).toInt()
                                )
                            }
                        }, text = {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index) background else CC.extraColor2(),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(8.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = CC.descriptionTextStyle(context),
                                    color = if (selectedTabIndex == index) CC.textColor() else CC.secondary()
                                )
                            }
                        })
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        if (announcementsLoading) {
                            LoadingIndicator()
                        } else {
                            AnnouncementsItem(
                                targetCourseID, courseAnnouncementViewModel, context, userViewModel
                            )
                        }
                    }

                    1 -> {
                        if (assignmentsLoading) {
                            LoadingIndicator()
                        } else {
                            AssignmentsItem(
                                targetCourseID, courseAssignmentViewModel, context
                            )
                        }
                    }

                    2 -> {
                        if (timetablesLoading) {
                            LoadingIndicator()
                        } else {
                            TimetableItem(
                                targetCourseID, courseTimetableViewModel, context
                            )
                        }
                    }

                    3 -> {
                        if (detailsLoading) {
                            LoadingIndicator()
                        } else {
                            DetailsItem(targetCourseID, courseDetailViewModel, context)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(color = CC.textColor())
    }
}


@Composable
fun AnnouncementsItem(
    courseID: String,
    courseAnnouncementViewModel: CourseAnnouncementViewModel,
    context: Context,
    userViewModel: UserViewModel
) {
    var visible by remember { mutableStateOf(false) }
    val announcements =
        courseAnnouncementViewModel.announcements.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                onClick = { visible = !visible },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "Add announcement")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible) {
                AddAnnouncementItem(
                    courseID,
                    context,
                    visible,
                    onExpandedChange = { visible = !visible },
                    courseAnnouncementViewModel,
                    userViewModel
                )
            }
            if (announcements.value.isEmpty()) {
                Text("No Announcements", style = CC.descriptionTextStyle(context))
            } else {
                LazyColumn {
                    items(announcements.value) { announcement ->
                        AnnouncementCard(announcement, context)
                    }
                }
            }
        }
    }
}


@Composable
fun AnnouncementCard(
    courseAnnouncement: CourseAnnouncement, context: Context
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = courseAnnouncement.title, style = CC.titleTextStyle(context)
            )
            Text(
                text = courseAnnouncement.description, style = CC.descriptionTextStyle(context).copy(
                    color = CC.textColor().copy(0.5f), textAlign = TextAlign.Center
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = courseAnnouncement.author, style = CC.descriptionTextStyle(context).copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CC.textColor().copy(0.7f)
                    )
                )
                Text(
                    text = courseAnnouncement.date, fontSize = 12.sp, style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}


@Composable
fun AddAnnouncementItem(
    courseID: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    courseViewModel: CourseAnnouncementViewModel,
    userViewModel: UserViewModel
) {
    val user by userViewModel.signedInUser.observeAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var senderName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.getSignedInUser()
        user?.let {
            it.email.let { it1 ->
                userViewModel.findUserByEmail(it1, onUserFetched = { fetchedUser ->
                    senderName = fetchedUser?.firstName.toString()
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .border(
                width = 1.dp, color = CC.secondary(), shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .fillMaxWidth(0.9f)
    ) {
        Text(
            "Add new Announcement",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))
        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateAnnouncementID { iD ->
                        val newAnnouncement = CourseAnnouncement(
                            courseID = courseID,
                            announcementID = iD,
                            title = title,
                            description = description,
                            author = senderName,
                            date = CC.getCurrentDate(CC.getTimeStamp())
                        )
                        courseViewModel.saveCourseAnnouncement(
                            courseID = courseID, announcement = newAnnouncement
                        )
                        loading = false
                        onExpandedChange(expanded)


                    }

                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = background, modifier = Modifier.size(20.dp))
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

        }

    }
}

@Composable
fun AddTextField(
    label: String, value: String, onValueChange: (String) -> Unit, context: Context
) {
    TextField(
        value = value,
        textStyle = CC.descriptionTextStyle(context),
        onValueChange = onValueChange,
        placeholder = { Text(label, style = CC.descriptionTextStyle(context)) },
        modifier = Modifier
            .heightIn(min = 20.dp, max = 100.dp)
            .fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            focusedPlaceholderColor = CC.textColor().copy(0.5f),
            unfocusedContainerColor = CC.primary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedIndicatorColor = CC.textColor(),
            unfocusedIndicatorColor = CC.textColor(),
            cursorColor = CC.textColor()

        ),
        shape = RoundedCornerShape(10.dp)
    )

}

@Composable
fun AssignmentsItem(
    courseID: String, assignmentViewModel: CourseAssignmentViewModel, context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    val assignment = assignmentViewModel.assignments.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {assignmentViewModel.getCourseAssignments(courseID)}) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CC.textColor())
                }
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(30.dp),
                    containerColor = CC.extraColor2(),
                    content = {
                        Icon(
                            Icons.Default.Add,
                            "Add Assignment",
                            tint = CC.textColor(),
                            modifier = Modifier.size(30.dp)
                        )
                    },
                )

            }
            AnimatedVisibility(visible = expanded) {
                AddAssignmentItem(courseID,
                    assignmentViewModel,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            //assignmentCard
            if (assignment.value.isEmpty()) {
                Text("No Assignments", style = CC.descriptionTextStyle(context))
            } else {
                LazyColumn {
                    items(assignment.value) { assignment ->
                        AssignmentCard(assignment, context)
                    }
                }
            }
        }
    }
}


@Composable
fun AssignmentCard(
    assignment: CourseAssignment, context: Context
) {
    val currentDate = Date()
    val formatter =
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) // Use default locale
    val formattedDate = formatter.format(currentDate)
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = assignment.title, style = CC.titleTextStyle(context)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Published: ${assignment.publishedDate}",
                    style = CC.descriptionTextStyle(context)
                        .copy(fontSize = 12.sp)
                )
                Text(
                    text = if (assignment.dueDate < formattedDate) "Past Due" else if (assignment.dueDate == formattedDate) "Due Today" else "Due: ${assignment.dueDate}",
                    fontSize = 12.sp,
                    style = CC.descriptionTextStyle(context),
                    color = if (assignment.dueDate < formattedDate) Color.Red else CC.textColor()
                )
            }
            Text(
                text = assignment.description, style = CC.descriptionTextStyle(context).copy(color = CC.primary())
            )
        }
    }
}

@Composable
fun AddAssignmentItem(
    courseID: String,
    assignmentViewModel: CourseAssignmentViewModel,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth(0.9f)
    ) {
        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context = context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context = context
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Date Picker
        Button(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dueDate = dateFormatter.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor1()
            )
            ) {
            Text(
                text = if (dueDate.isEmpty()) "Pick Date" else "Date: $dueDate",
                style = CC.descriptionTextStyle(context)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Picker
        Button(onClick = {
            TimePickerDialog(
                context, { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    dueTime = timeFormatter.format(calendar.time)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
            ).show()
        },colors = ButtonDefaults.buttonColors(
            containerColor = CC.extraColor1()
        )) {
            Text(
                text = if (dueTime.isEmpty()) "Pick Time" else "Time: $dueTime",
                style = CC.descriptionTextStyle(context)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    val completeDueDate = "$dueDate at $dueTime"
                    MyDatabase.generateAssignmentID { iD ->
                        val newAssignment = CourseAssignment(
                            courseCode = courseID,
                            assignmentID = iD,
                            title = title,
                            description = description,
                            dueDate = completeDueDate,
                            publishedDate = CC.getCurrentDate(CC.getTimeStamp())
                        )
                        assignmentViewModel.saveCourseAssignment(courseID = courseID,
                            assignment = newAssignment,
                            onComplete = { success ->
                                if (success) {
                                    assignmentViewModel.getCourseAssignments(courseID)
                                    loading = false
                                    onExpandedChange(expanded)
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Log.e("Error", "Failed to write announcement")
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = CC.textColor(), modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }
        }
    }
}

@Composable
fun TimetableItem(
    courseID: String, timetableViewModel: CourseTimetableViewModel, context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    val timetables = timetableViewModel.timetables.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "add timetable")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddTimetableItem(courseID,
                    timetableViewModel,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            //timetable card
            if (timetables.value.isEmpty()) {
                Text("No Timetables", style = CC.descriptionTextStyle(context))
            } else {
                LazyColumn {
                    items(timetables.value) { timetable ->
                        Text("${timetable.day}s", style = CC.titleTextStyle(context))
                        Spacer(modifier = Modifier.height(20.dp))
                        TimetableCard(timetable, context)
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableCard(
    timetable: CourseTimetable, context: Context
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Start: ${timetable.startTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "End time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "End: ${timetable.endTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Venue: ${timetable.venue}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Lecturer: ${timetable.lecturer}",
                    style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}

@Composable
fun AddTimetableItem(
    courseID: String,
    timetableViewModel: CourseTimetableViewModel,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var day by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth(0.9f)
    ) {
        AddTextField(
            label = "Day", value = day, onValueChange = { day = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Start", value = startTime, onValueChange = { startTime = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "End", value = endTime, onValueChange = { endTime = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Venue", value = venue, onValueChange = { venue = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateTimetableID { iD ->
                        val newTimetable = CourseTimetable(
                            day = day,
                            courseID = courseID,
                            timetableID = iD,
                            startTime = startTime,
                            endTime = endTime,
                            venue = venue,
                            lecturer = lecturer
                        )
                        timetableViewModel.saveCourseTimetable(courseID = courseID,
                            timetable = newTimetable,
                            onCompletion = { success ->
                                if (success) {
                                    loading = false
                                    timetableViewModel.getCourseTimetables(courseID)
                                    onExpandedChange(expanded)
                                    startTime = ""
                                    endTime = ""
                                    venue = ""
                                    lecturer = ""
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = background, modifier = Modifier.size(20.dp))
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

        }

    }
}

@Composable
fun DetailsItem(
    courseID: String, detailsViewModel: CourseDetailViewModel, context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    val details = detailsViewModel.details.observeAsState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "add timetable")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddDetailsItem(
                    courseID,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded },
                    detailsViewModel = detailsViewModel
                )
            }
            //card here
            if (details.value == null) {
                Text("No Details", style = CC.descriptionTextStyle(context))
            } else {
                details.value?.let {
                    DetailsItemCard(it, context)
                }
            }

        }
    }

}


@Composable
fun DetailsItemCard(courseDetails: CourseDetail, context: Context) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = courseDetails.courseName, style = CC.titleTextStyle(context).copy(fontSize = 20.sp)
            )
            Text(
                text = "Course Code: ${courseDetails.courseCode}",
                style = CC.descriptionTextStyle(context)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = courseDetails.lecturer,
                    style = CC.descriptionTextStyle(context)
                        .copy(fontWeight = FontWeight.Medium)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Visits: ${courseDetails.numberOfVisits}",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalPostOffice,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Department: ${courseDetails.courseDepartment}",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Overview",
                style = CC.descriptionTextStyle(context)
                    .copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary())
            )
            Text(
                text = courseDetails.overview, style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Medium, color = CC.textColor(), fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Learning Outcomes",
                style = CC.descriptionTextStyle(context)
                    .copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary())
            )
            courseDetails.learningOutcomes.forEach { outcome ->
                Text(
                    text = "- $outcome", style = CC.descriptionTextStyle(context).copy(
                        fontWeight = FontWeight.Medium, color = CC.textColor(), fontSize = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Schedule",
                style = CC.descriptionTextStyle(context)
                    .copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary())
            )
            Text(
                text = courseDetails.schedule, style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Medium, color = CC.textColor(), fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Required Materials",
                style = CC.descriptionTextStyle(context)
                    .copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary())
            )
            Text(
                text = courseDetails.requiredMaterials, style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Medium, color = CC.textColor(), fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun AddDetailsItem(
    courseID: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    detailsViewModel: CourseDetailViewModel
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var numberOfVisits by remember { mutableStateOf("") }
    var courseDepartment by remember { mutableStateOf("") }
    var courseOutcome by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    var learningOutcomes by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var requiredMaterials by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val courseInfo by detailsViewModel.courseDetails.observeAsState()

    LaunchedEffect(courseID) {
        detailsViewModel.getCourseDetailsByCourseID(courseID) { result ->
            if (result) {
                courseName = courseInfo?.courseName.toString()
                courseCode = courseInfo?.courseCode.toString()
                numberOfVisits = courseInfo?.visits.toString()
            }

        }

    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AddTextField(
                label = "Lecturer", value = lecturer, onValueChange = { lecturer = it }, context
            )
            AddTextField(
                label = "Course Department",
                value = courseDepartment,
                onValueChange = { courseDepartment = it },
                context
            )
            AddTextField(
                label = "Course Outcome",
                value = courseOutcome,
                onValueChange = { courseOutcome = it },
                context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Overview", value = overview, onValueChange = { overview = it }, context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Learning Outcomes",
                value = learningOutcomes,
                onValueChange = { learningOutcomes = it },
                context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Schedule", value = schedule, onValueChange = { schedule = it }, context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Required Materials",
                value = requiredMaterials,
                onValueChange = { requiredMaterials = it },
                context
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    loading = true
                    val newDetails = CourseDetail(
                        courseName = courseName,
                        courseCode = courseCode,
                        numberOfVisits = numberOfVisits,
                        detailID = "2024$courseID",
                        lecturer = lecturer,
                        courseDepartment = courseDepartment,
                        overview = overview,
                        learningOutcomes = learningOutcomes.split(",").map { it.trim() },
                        schedule = schedule,
                        requiredMaterials = requiredMaterials
                    )
                    detailsViewModel.saveCourseDetail(courseID = courseID,
                        detail = newDetails,
                        onResult = { success ->
                            if (success) {
                                loading = false
                                onExpandedChange(!expanded)
                            } else {
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            }

                        })
                    Log.d(
                        "Course Details",
                        "The new course info for the course: $courseID is: $newDetails "
                    )

                }, modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = if (expanded) "Save" else "Edit")
            }
        }
    }
}


