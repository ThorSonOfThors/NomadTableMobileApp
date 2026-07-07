package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.models.ChatHeaderResponse
import com.example.myapplication.models.MessageDto
import com.example.myapplication.models.Participant
import com.example.myapplication.models.SendMessageRequest
import com.example.myapplication.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

@Composable
fun ChatScreen(
    chatId: Long,
    userId: Long,
    navController: NavController
) {
    var activityTitle by remember { mutableStateOf("") }
    var participantCount by remember { mutableStateOf(0) }
    var participants by remember { mutableStateOf<List<Participant>>(emptyList()) }
    var headerLoading by remember { mutableStateOf(true) }

    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val messageIndexMap = remember(messages) {
        messages.withIndex().associate { it.value.id to it.index }
    }

    var highlightedMessageId by remember { mutableStateOf<Long?>(null) }

    // Helper function to get profile image URL - matching ProfileScreen format
    fun getProfileImageUrl(profileImageId: Long?): String? {
        return if (profileImageId != null) {
            // Using the same URL format as ProfileScreen
            "http://192.168.1.2:8081/api/users/profile-image/$profileImageId"
        } else {
            null
        }
    }

    // Load chat header
    LaunchedEffect(chatId) {
        RetrofitInstance.api.getChatHeader(chatId)
            .enqueue(object : Callback<ChatHeaderResponse> {
                override fun onResponse(
                    call: Call<ChatHeaderResponse>,
                    response: Response<ChatHeaderResponse>
                ) {
                    headerLoading = false
                    if (response.isSuccessful) {
                        response.body()?.let {
                            activityTitle = it.activityTitle
                            participantCount = it.participantCount
                            participants = it.participants

                            // Log participant profile images for debugging
                            participants.forEach { participant ->
                                println(
                                    "Participant: ${participant.name}, profileImageId=${participant.profileImageId}"
                                )
                                // Also log the full URL
                                println(
                                    "Image URL: ${getProfileImageUrl(participant.profileImageId)}"
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ChatHeaderResponse>, t: Throwable) {
                    headerLoading = false
                    t.printStackTrace()
                }
            })
    }

    // Load messages
    LaunchedEffect(chatId) {
        RetrofitInstance.api.getMessages(chatId)
            .enqueue(object : Callback<List<MessageDto>> {
                override fun onResponse(
                    call: Call<List<MessageDto>>,
                    response: Response<List<MessageDto>>
                ) {
                    if (response.isSuccessful) {
                        messages = response.body() ?: emptyList()
                        RetrofitInstance.api.markMessagesSeen(chatId, userId)
                    }
                }

                override fun onFailure(call: Call<List<MessageDto>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    // Mark messages as seen
    LaunchedEffect(chatId) {
        RetrofitInstance.api.markMessagesSeen(chatId, userId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    if (headerLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        //---------------------------------------------------------
        // Activity Navbar - Enhanced with proper profile images
        //---------------------------------------------------------
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("activityDetails/$chatId")
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activityTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$participantCount participants",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Participant avatars with proper profile images
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    participants.take(5).forEach { participant ->
                        val profileImageUrl = getProfileImageUrl(participant.profileImageId)

                        // Debug log
                        println("Displaying avatar for ${participant.name}: URL=$profileImageUrl")

                        if (profileImageUrl != null) {
                            // Show profile image if available
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = participant.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                onSuccess = {
                                    println("Image loaded: $profileImageUrl")
                                },
                                onError = {
                                    println("Failed to load: $profileImageUrl")
                                    println(it.result.throwable)
                                }
                            )
                        } else {
                            // Show initials if no profile image
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = participant.name.first().toString(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    if (participants.size > 5) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${participants.size - 5}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        //---------------------------------------------------------
        // Chat Messages
        //---------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState,
            reverseLayout = false
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                val mine = message.senderId == userId
                val highlighted = highlightedMessageId == message.id

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .pointerInput(message.id) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    if (dragAmount > 60) {
                                        replyingTo = message
                                    }
                                }
                            )
                        },
                    horizontalAlignment = if (mine) Alignment.End else Alignment.Start
                ) {
                    if (!mine) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                    }

                    // Reply preview card
                    if (message.replyToMessageId != null) {
                        Card(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clickable {
                                    val repliedId = message.replyToMessageId
                                    val index = messageIndexMap[repliedId]
                                    if (index != null) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(index)
                                            highlightedMessageId = repliedId
                                            kotlinx.coroutines.delay(1500)
                                            highlightedMessageId = null
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "↩ ${message.replySenderName ?: "Unknown"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = message.replyPreview ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Message bubble
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (mine) 16.dp else 4.dp,
                            bottomEnd = if (mine) 4.dp else 16.dp
                        ),
                        color = when {
                            highlighted -> Color.Yellow.copy(alpha = 0.3f)
                            mine -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shadowElevation = if (highlighted) 4.dp else 0.dp
                    ) {
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = when {
                                highlighted -> MaterialTheme.colorScheme.onSurface
                                mine -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Timestamp and status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = message.sentAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (mine) {
                            when (message.status) {
                                "sending" -> {
                                    Text(
                                        "✓",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                "sent" -> {
                                    Text(
                                        "✓✓",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                "seen" -> {
                                    Text(
                                        "✓✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        //---------------------------------------------------------
        // Reply Indicator
        //---------------------------------------------------------
        replyingTo?.let { reply ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable {
                        val index = messageIndexMap[reply.id]
                        if (index != null) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                highlightedMessageId = reply.id
                                kotlinx.coroutines.delay(1500)
                                highlightedMessageId = null
                            }
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Replying to ${reply.senderName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = reply.content,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { replyingTo = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        //---------------------------------------------------------
        // Send Message Bar
        //---------------------------------------------------------
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text(
                            text = "Type a message...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    ),
                    singleLine = true,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                FloatingActionButton(
                    onClick = {
                        if (input.isBlank()) return@FloatingActionButton

                        val request = SendMessageRequest(
                            senderId = userId,
                            content = input,
                            replyToMessageId = replyingTo?.id,
                            replyPreview = replyingTo?.content,
                            replySenderName = replyingTo?.senderName
                        )

                        RetrofitInstance.api.sendMessage(chatId, request)
                            .enqueue(object : Callback<MessageDto> {
                                override fun onResponse(
                                    call: Call<MessageDto>,
                                    response: Response<MessageDto>
                                ) {
                                    if (response.isSuccessful) {
                                        response.body()?.let {
                                            replyingTo = null
                                            messages = messages + it
                                        }
                                        input = ""
                                    }
                                }

                                override fun onFailure(call: Call<MessageDto>, t: Throwable) {
                                    t.printStackTrace()
                                }
                            })
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }
}