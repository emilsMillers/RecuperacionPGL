package com.example.recuperacionpgl

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recuperacionpgl.ui.theme.RecuperacionPGLTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max
import kotlin.math.min

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController) {
    RecuperacionPGLTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val tabs = listOf("Results", "Fighters", "Events")
        val pagerState = rememberPagerState(pageCount = {tabs.size},initialPage = 2)

        var searchText by remember { mutableStateOf("796") }
        var year by remember { mutableStateOf("2024") }
        var events by remember { mutableStateOf<List<Event>>(emptyList()) }
        var selectedEventId by remember { mutableStateOf(788) }

        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(navController)
            },
            content = {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("MMA STATER") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            actions = {
                                TextField(
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                        year = it
                                    },
                                    label = { Text("Search") },
                                    modifier = Modifier.fillMaxWidth(0.5f)
                                )

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            pagerState.scrollToPage(2)
                                            events = fetchEvents(year)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )
                    },
                    content = { paddingValues ->
                        Box(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = max(1f, min(scale * zoom, 3f))
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                )
                        ) {
                            Column {
                                TabRow(
                                    selectedTabIndex = pagerState.currentPage,
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    tabs.forEachIndexed { index, title ->
                                        Tab(
                                            text = { Text(title) },
                                            selected = pagerState.currentPage == index,
                                            onClick = {
                                                scope.launch {
                                                    pagerState.scrollToPage(index)
                                                }
                                            }
                                        )
                                    }
                                }
                                HorizontalPager(
                                    state = pagerState
                                ) { page ->
                                    when (page) {
                                        0 -> ResultsScreen(searchText)
                                        1 -> FightersScreen(selectedEventId)
                                        2 -> EventsScreen(year, events,
                                            onEventClick = { eventId ->
                                                searchText = eventId.toString()
                                                scope.launch {
                                                    pagerState.scrollToPage(0)
                                                }
                                            },
                                            onEventLongClick = { eventId ->
                                                selectedEventId = eventId
                                                scope.launch {
                                                    pagerState.scrollToPage(1)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        )
        LaunchedEffect(Unit) {
            events = fetchEvents(year)
        }
    }
}



@Composable
fun DrawerContent(navController: NavController) {
    Column(
        modifier = Modifier
            .background( Color(0xFF4B0000))
    ) {
        IconButton(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp)
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(year: String, events: List<Event>, onEventClick: (Int) -> Unit, onEventLongClick: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            Text("Events for $year", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(events) { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = {
                            onEventClick(event.eventId)
                        },
                        onLongClick = {
                            onEventLongClick(event.eventId)
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Event ID: ${event.eventId}", style = MaterialTheme.typography.titleSmall)
                    Text("Name: ${event.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Day: ${event.day}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun ResultsScreen(eventId: String) {
    var fights by remember { mutableStateOf<List<Fight>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        scope.launch {
            fights = fetchFights(eventId)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        itemsIndexed(fights) { index, fight ->
            val isFirstFight = index == 0
            val sortedFighters = fight.fighters.sortedBy { it.moneyline ?: 0 }
            val topFighter = sortedFighters.firstOrNull()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(if (isFirstFight) Color(0xFFFFD700) else MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    fight.fighters.forEach { fighter ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val textColor = if (fighter.winner == true) Color.Green else Color.Red
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${fighter.firstName} ${fighter.lastName}",
                                    color = textColor,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (fighter == topFighter) {
                                    Text(
                                        text = " ★",
                                        color = Color.Yellow,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                            Text("Wins: ${fighter.preFightWins ?: 0}")
                            Text("Losses: ${fighter.preFightLosses ?: 0}")
                            Text("Draws: ${fighter.preFightDraws ?: 0}")
                            Text("Moneyline: ${fighter.moneyline ?: "N/A"}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FightersScreen(eventId: Int) {
    var fighter1Details by remember { mutableStateOf<FighterDetails?>(null) }
    var fighter2Details by remember { mutableStateOf<FighterDetails?>(null) }
    val scope = rememberCoroutineScope()

    var fighter1Id by remember { mutableStateOf<Int?>(null) }
    var fighter2Id by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(eventId) {
        scope.launch {
            val mainFight = fetchMainFight(eventId.toString())
            mainFight?.let { fight ->
                if (fight.fighters.size >= 2) {
                    fighter1Id = fight.fighters[0].fighterId
                    fighter2Id = fight.fighters[1].fighterId
                    fighter1Details = fetchFighterDetails(fighter1Id!!)
                    fighter2Details = fetchFighterDetails(fighter2Id!!)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            FighterDetailsCard(fighterDetails = fighter1Details)
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        item {
            FighterDetailsCard(fighterDetails = fighter2Details)
        }
    }
}




@Composable
fun FighterDetailsCard(fighterDetails: FighterDetails?) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(fighterDetails) {
        fighterDetails?.let {
            val searchTerm = "${it.firstName} ${it.lastName} MMA"
            val fetchedImageUrl = fetchFirstImageUrl(searchTerm, "AIzaSyDEfxjwY6jCfsp09zLMpcFOIQwueBS14RM", "e0f816f1c692041a6")
            imageUrl = fetchedImageUrl
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF4E4E4E))
            .border(width = 1.dp, color = Color(0xFF4B0000))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageUrl?.let { url ->
                Image(
                    painter = rememberImagePainter(
                        data = url,
                        builder = {
                            crossfade(true)
                        }
                    ),
                    contentDescription = "Fighter Image",
                    modifier = Modifier
                        .height(200.dp)
                        .width(100.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                fighterDetails?.let {
                    Text(
                        text = "${it.firstName} ${it.lastName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Nickname: ${it.nickname ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Birth Date: ${it.birthDate ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Height: ${it.height ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Weight: ${it.weight ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Reach: ${it.reach ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row {
                        Text(
                            text = "Wins: ${it.wins ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                        Text(
                            text = "Losses: ${it.losses ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = "Draws: ${it.draws ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Blue,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Text(
                        text = "No Contests: ${it.noContests ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "TKOs: ${it.technicalKnockouts ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Submissions: ${it.submissions ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Title Wins: ${it.titleWins ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    it.careerStats?.let { stats ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Career Stats:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Sig Strikes Landed Per Minute: ${stats.sigStrikesLandedPerMinute ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Sig Strike Accuracy: ${stats.sigStrikeAccuracy ?: "N/A"}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Takedown Average: ${stats.takedownAverage ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Submission Average: ${stats.submissionAverage ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Knockout Percentage: ${stats.knockoutPercentage ?: "N/A"}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Technical Knockout Percentage: ${stats.technicalKnockoutPercentage ?: "N/A"}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                            Text(
                                text = "Decision Percentage: ${stats.decisionPercentage ?: "N/A"}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red)
                            )
                        }
                    }
                }
            }
        }
    }
}




data class Event(
    val eventId: Int,
    val name: String,
    val day: String
)

suspend fun fetchEvents(year: String): List<Event> {
    return withContext(Dispatchers.IO) {
        val url = URL("https://api.sportsdata.io/v3/mma/scores/json/Schedule/ufc/$year?key=b282091d6ab74b39b157dd2625dcea8b")
        val connection = url.openConnection() as HttpURLConnection
        val events = mutableListOf<Event>()
        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonArray = JSONArray(response.toString())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val eventId = jsonObject.getInt("EventId")
                val name = jsonObject.getString("Name")
                val day = jsonObject.getString("Day")
                events.add(Event(eventId, name, day))
            }
        } finally {
            connection.disconnect()
        }
        events
    }
}

data class Fight(
    val fightId: Int,
    val fighters: List<Fighter>,
    val active: Boolean
)

data class Fighter(
    val fighterId: Int,
    val firstName: String,
    val lastName: String,
    val preFightWins: Int?,
    val preFightLosses: Int?,
    val preFightDraws: Int?,
    val moneyline: Int?,
    val winner: Boolean?
)

suspend fun fetchFights(eventId: String): List<Fight> {
    return withContext(Dispatchers.IO) {
        val url = URL("https://api.sportsdata.io/v3/mma/scores/json/Event/$eventId?key=b282091d6ab74b39b157dd2625dcea8b")
        val connection = url.openConnection() as HttpURLConnection
        val fights = mutableListOf<Fight>()
        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())
            val jsonFights = jsonObject.getJSONArray("Fights")
            for (i in 0 until jsonFights.length()) {
                val jsonFight = jsonFights.getJSONObject(i)
                val fightId = jsonFight.getInt("FightId")
                val active = jsonFight.getBoolean("Active")
                if (active) {
                    val fighters = mutableListOf<Fighter>()
                    val jsonFighters = jsonFight.getJSONArray("Fighters")
                    for (j in 0 until jsonFighters.length()) {
                        val jsonFighter = jsonFighters.getJSONObject(j)
                        val fighter = Fighter(
                            fighterId = jsonFighter.getInt("FighterId"),
                            firstName = jsonFighter.getString("FirstName"),
                            lastName = jsonFighter.getString("LastName"),
                            preFightWins = jsonFighter.optInt("PreFightWins"),
                            preFightLosses = jsonFighter.optInt("PreFightLosses"),
                            preFightDraws = jsonFighter.optInt("PreFightDraws"),
                            moneyline = jsonFighter.optInt("Moneyline"),
                            winner = jsonFighter.optBoolean("Winner")
                        )
                        fighters.add(fighter)
                    }
                    fights.add(Fight(fightId, fighters, active))
                }
            }
        } finally {
            connection.disconnect()
        }
        fights
    }
}

suspend fun fetchMainFight(eventId: String): Fight? {
    val fights = fetchFights(eventId)
    return fights.firstOrNull()
}


suspend fun fetchFighterDetails(fighterId: Int): FighterDetails {
    return withContext(Dispatchers.IO) {
        val url = URL("https://api.sportsdata.io/v3/mma/scores/json/Fighter/$fighterId?key=b282091d6ab74b39b157dd2625dcea8b")
        val connection = url.openConnection() as HttpURLConnection
        lateinit var fighterDetails: FighterDetails
        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())
            fighterDetails = FighterDetails(
                fighterId = jsonObject.getInt("FighterId"),
                firstName = jsonObject.getString("FirstName"),
                lastName = jsonObject.getString("LastName"),
                nickname = jsonObject.optString("Nickname"),
                birthDate = jsonObject.optString("BirthDate"),
                height = jsonObject.optDouble("Height"),
                weight = jsonObject.optDouble("Weight"),
                reach = jsonObject.optDouble("Reach"),
                wins = jsonObject.optInt("Wins"),
                losses = jsonObject.optInt("Losses"),
                draws = jsonObject.optInt("Draws"),
                noContests = jsonObject.optInt("NoContests"),
                technicalKnockouts = jsonObject.optInt("TechnicalKnockouts"),
                technicalKnockoutLosses = jsonObject.optInt("TechnicalKnockoutLosses"),
                submissions = jsonObject.optInt("Submissions"),
                submissionLosses = jsonObject.optInt("SubmissionLosses"),
                titleWins = jsonObject.optInt("TitleWins"),
                titleLosses = jsonObject.optInt("TitleLosses"),
                titleDraws = jsonObject.optInt("TitleDraws"),
                careerStats = jsonObject.optJSONObject("CareerStats")?.let {
                    CareerStats(
                        fighterId = it.getInt("FighterId"),
                        firstName = it.getString("FirstName"),
                        lastName = it.getString("LastName"),
                        sigStrikesLandedPerMinute = it.optDouble("SigStrikesLandedPerMinute"),
                        sigStrikeAccuracy = it.optDouble("SigStrikeAccuracy"),
                        takedownAverage = it.optDouble("TakedownAverage"),
                        submissionAverage = it.optDouble("SubmissionAverage"),
                        knockoutPercentage = it.optDouble("KnockoutPercentage"),
                        technicalKnockoutPercentage = it.optDouble("TechnicalKnockoutPercentage"),
                        decisionPercentage = it.optDouble("DecisionPercentage")
                    )
                }
            )
        } finally {
            connection.disconnect()
        }
        fighterDetails
    }
}

data class FighterDetails(
    val fighterId: Int,
    val firstName: String,
    val lastName: String,
    val nickname: String?,
    val birthDate: String?,
    val height: Double?,
    val weight: Double?,
    val reach: Double?,
    val wins: Int?,
    val losses: Int?,
    val draws: Int?,
    val noContests: Int?,
    val technicalKnockouts: Int?,
    val technicalKnockoutLosses: Int?,
    val submissions: Int?,
    val submissionLosses: Int?,
    val titleWins: Int?,
    val titleLosses: Int?,
    val titleDraws: Int?,
    val careerStats: CareerStats?
)

data class CareerStats(
    val fighterId: Int,
    val firstName: String,
    val lastName: String,
    val sigStrikesLandedPerMinute: Double?,
    val sigStrikeAccuracy: Double?,
    val takedownAverage: Double?,
    val submissionAverage: Double?,
    val knockoutPercentage: Double?,
    val technicalKnockoutPercentage: Double?,
    val decisionPercentage: Double?
)
suspend fun fetchFirstImageUrl(searchTerm: String, apiKey: String, cseId: String): String? {
    return withContext(Dispatchers.IO) {
        val url = "https://www.googleapis.com/customsearch/v1?key=$apiKey&cx=$cseId&q=$searchTerm&searchType=image"
        val connection = URL(url).openConnection() as HttpURLConnection
        var imageUrl: String? = null
        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            val jsonObject = JSONObject(response.toString())
            val items = jsonObject.optJSONArray("items")
            if (items != null && items.length() > 0) {
                val firstItem = items.getJSONObject(0)
                imageUrl = firstItem.optString("link")
            }
        } finally {
            connection.disconnect()
        }
        imageUrl
    }
}


