package com.example.khetseva3
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay
import androidx.navigation.compose.rememberNavController
import android.util.Patterns
import com.example.khetseva3.network.AppConfig
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.History
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Language
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import com.example.khetseva3.model.*
import com.example.khetseva3.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            val prefs = context.getSharedPreferences(
                "USER",
                Context.MODE_PRIVATE
            )
            var isLoggedIn by remember {
                mutableStateOf(
                    prefs.getString("phone", null) != null
                )
            }
            var languageExpanded by remember { mutableStateOf(false) }
            var currentLanguage by remember { mutableStateOf("en") }
            var screen by rememberSaveable {
                mutableStateOf(
                    if (isLoggedIn) "role" else "login"
                )
            }
            var apiResult by remember { mutableStateOf<RecommendResponse?>(null) }
            var isForward by remember { mutableStateOf(true) }
            var previousTakeForm by rememberSaveable {
                mutableStateOf("takeTractorForm")
            }
            var previousScreen by rememberSaveable {
                mutableStateOf("role")
            }

            var profileName by remember {
                mutableStateOf(
                    prefs.getString("name", "") ?: ""
                )
            }
            var profileEmail by remember {
                mutableStateOf(
                    prefs.getString("email", "") ?: ""
                )
            }
            var profileCountry by remember {
                mutableStateOf(
                    prefs.getString("country", "") ?: ""
                )
            }
            var profileState by remember {
                mutableStateOf(
                    prefs.getString("state", "") ?: ""
                )
            }
            var profileCity by remember {
                mutableStateOf(
                    prefs.getString("city", "") ?: ""
                )
            }
            fun changeLanguage(languageCode: String) {
                currentLanguage = languageCode
                val locale = java.util.Locale(languageCode)
                java.util.Locale.setDefault(locale)
                val config = context.resources.configuration
                config.setLocale(locale)
                context.resources.updateConfiguration(
                    config,
                    context.resources.displayMetrics
                )
            }
            fun navigateForward(route: String) {
                isForward = true
                screen = route
            }
            fun goBack() {
                isForward = false
                screen = when (screen) {

                    "recommendation" -> previousTakeForm
                    "takeTractorForm",
                    "takeHarvesterForm",
                    "recently_viewed" -> "role"
                    "takeSeedDrillForm",
                    "takeRotavatorForm" -> "takeSelect"
                    "myMachines" -> "role"
                    "takeSelect" -> "role"
                    "rented_machines" -> "role"
                    "notifications" -> "role"

                    "giveTractorForm",
                    "giveHarvesterForm",
                    "giveSeedDrillForm",
                    "giveRotavatorForm" -> "giveSelect"

                    "giveSelect" -> "role"

                    "userProfile" -> previousScreen
                    "register" -> "login"

                    else -> "role"
                }
            }
            if (!isLoggedIn && screen !in listOf("login", "register")) {
                screen = "login"
            }
            MaterialTheme {

                key(currentLanguage) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White
                    ) {

                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = {
                                if (isForward) {
                                    slideInHorizontally { it } togetherWith
                                            slideOutHorizontally { -it }
                                } else {
                                    slideInHorizontally { -it } togetherWith
                                            slideOutHorizontally { it }
                                }
                            },
                            label = "screenAnimation"
                        ) { targetScreen ->

                            when (targetScreen) {

                                "login" -> {
                                    Box(Modifier.fillMaxSize()) {

                                        LoginScreen(

                                            onLogin = { phone, password ->

                                                val request = LoginRequest(
                                                    phone = phone,
                                                    password = password
                                                )
                                                RetrofitClient.api.login(request)
                                                    .enqueue(object : Callback<LoginResponse> {

                                                        override fun onResponse(
                                                            call: Call<LoginResponse>,
                                                            response: Response<LoginResponse>
                                                        ) {
                                                            if (response.isSuccessful) {

                                                                val user = response.body()

                                                                val prefs = context.getSharedPreferences(
                                                                    "USER",
                                                                    Context.MODE_PRIVATE
                                                                )
                                                                prefs.edit()
                                                                    .putString("name", user?.name)
                                                                    .putString("phone", user?.phone)
                                                                    .putString("email", user?.email)
                                                                    .putString("country", user?.country)
                                                                    .putString("state", user?.state)
                                                                    .putString(
                                                                        "location",
                                                                        "${user?.city}, ${user?.state}, ${user?.country}"
                                                                    )
                                                                    .putString("city", user?.city)
                                                                    .apply()
                                                                profileName = user?.name ?: ""

                                                                profileEmail = user?.email ?: ""

                                                                profileCountry = user?.country ?: ""

                                                                profileState = user?.state ?: ""

                                                                profileCity = user?.city ?: ""
                                                                isLoggedIn = true
                                                                Toast.makeText(
                                                                    context,
                                                                    "Login successful",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                navigateForward("role")

                                                            } else {

                                                                Toast.makeText(
                                                                    context,
                                                                    "Invalid phone or password",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                Log.e("LOGIN", "Login failed")
                                                            }
                                                        }
                                                        override fun onFailure(
                                                            call: Call<LoginResponse>,
                                                            t: Throwable
                                                        ) {
                                                            Log.e("LOGIN", "FAILED: ${t.message}")
                                                        }
                                                    }
                                                    )
                                            },
                                            onRegisterClick = {
                                                navigateForward("register")
                                            }
                                        )

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .statusBarsPadding()
                                                .padding(16.dp)
                                        ) {
                                            LanguageMenu(
                                                expanded = languageExpanded,
                                                onExpand = { languageExpanded = true },
                                                onDismiss = { languageExpanded = false },
                                                onSelect = {
                                                    changeLanguage(it)
                                                    languageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                "register" -> {
                                    Box(Modifier.fillMaxSize()) {

                                        RegisterScreen(onBack = { goBack() })

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {

                                            TextButton(onClick = { goBack() }) {
                                                Text(
                                                    text = stringResource(R.string.back),
                                                    color = Color.White
                                                )
                                            }
                                            LanguageMenu(
                                                expanded = languageExpanded,
                                                onExpand = { languageExpanded = true },
                                                onDismiss = { languageExpanded = false },
                                                onSelect = {
                                                    changeLanguage(it)
                                                    languageExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                else -> {

                                    Column(Modifier.fillMaxSize()) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .statusBarsPadding()
                                                .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Image(
                                                painter = painterResource(id = R.drawable.logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(120.dp)
                                            )

                                            Row(

                                                verticalAlignment = Alignment.CenterVertically,

                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {

                                                LanguageMenu(
                                                    expanded = languageExpanded,
                                                    onExpand = { languageExpanded = true },
                                                    onDismiss = { languageExpanded = false },
                                                    onSelect = {
                                                        changeLanguage(it)
                                                        languageExpanded = false
                                                    }
                                                )

                                                IconButton(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFF5F7FA)),
                                                    onClick = {
                                                        navigateForward("notifications")
                                                    }
                                                ) {
                                                    Icon(

                                                        Icons.Outlined.Notifications,

                                                        contentDescription = null,

                                                        tint = Color(0xFF2C3E50),

                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                IconButton(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFF5F7FA)),
                                                    onClick = {
                                                        previousScreen = screen
                                                        navigateForward("userProfile") }
                                                ) {
                                                    Icon(

                                                        Icons.Outlined.Person,

                                                        contentDescription = null,

                                                        tint = Color(0xFF2C3E50),

                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        if (targetScreen != "role") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp)
                                            ) {
                                                PremiumBackButton { goBack() }
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp)
                                        ) {

                                            when (targetScreen) {

                                                "role" -> RoleScreen(
                                                    onRentedMachines = {
                                                        navigateForward("rented_machines")
                                                    },
                                                    onRecentlyViewed = {

                                                        navigateForward("recently_viewed")
                                                    },
                                                    onTake = { navigateForward("takeSelect") },
                                                    onGive = { navigateForward("giveSelect") },
                                                    onBack = {},
                                                    onMyMachines = {
                                                        navigateForward("myMachines")
                                                    }

                                                )
                                                "takeSelect" -> MachinerySelectScreen(
                                                    title = stringResource(R.string.select_machine),
                                                    onTractorClick = { navigateForward("takeTractorForm") },
                                                    onHarvesterClick = { navigateForward("takeHarvesterForm") },
                                                    onSeedDrillClick = { navigateForward("takeSeedDrillForm") },
                                                    onRotavatorClick = { navigateForward("takeRotavatorForm") }
                                                )
                                                "recommendation" -> RecommendationScreen(
                                                    result = apiResult,
                                                    machineType = previousTakeForm,
                                                    onBack = { goBack() }
                                                )
                                                "takeTractorForm" ->
                                                    TakeTractorForm { farmSize, usage, soil, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "tractor",
                                                            pricing_type = pricing,
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                operation = usage,
                                                                soil_type = soil,
                                                                budget = budget
                                                            )
                                                        )
                                                        apiResult = null

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeTractorForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            }
                                                            )
                                                    }
                                                "takeHarvesterForm" ->
                                                    TakeHarvesterForm { farmSize, crop, budget, pricing ->

                                                        Log.e("API_TEST", "Button clicked")

                                                        val request = RecommendRequest(
                                                            type = "Harvester",
                                                            pricing_type = pricing,
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                crop_type = crop,
                                                                budget = budget
                                                            )
                                                        )

                                                        previousTakeForm = "takeHarvesterForm"
                                                        screen = "recommendation"
                                                        apiResult = null

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    Log.e("API_TEST", "Response received")

                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                    } else {
                                                                        Log.e("API_TEST", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API_TEST", "FAILED: ${t.message}")
                                                                }
                                                            }
                                                            )
                                                    }

                                                "takeSeedDrillForm" ->
                                                    TakeSeedDrillForm { farmSize, crop, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "seed drill",
                                                            pricing_type = pricing,
                                                            inputs = Inputs(
                                                                farm_size = farmSize.toFloat(),
                                                                crop_type = crop,
                                                                budget = budget
                                                            )
                                                        )
                                                        apiResult = null

                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeSeedDrillForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }

                                                "takeRotavatorForm" ->
                                                    TakeRotavatorForm { hp, soil, budget, pricing ->

                                                        val request = RecommendRequest(
                                                            type = "rotavator",
                                                            pricing_type = pricing,
                                                            inputs = Inputs(
                                                                tractor_hp = hp.toFloat(),
                                                                soil_type = soil,
                                                                budget = budget
                                                            )
                                                        )
                                                        apiResult = null
                                                        RetrofitClient.api.getRecommendation(request)
                                                            .enqueue(object : Callback<RecommendResponse> {

                                                                override fun onResponse(
                                                                    call: Call<RecommendResponse>,
                                                                    response: Response<RecommendResponse>
                                                                ) {
                                                                    if (response.isSuccessful) {
                                                                        apiResult = response.body()
                                                                        previousTakeForm = "takeRotavatorForm"
                                                                        navigateForward("recommendation")
                                                                    } else {
                                                                        Log.e("API", "Error: ${response.code()}")
                                                                    }
                                                                }

                                                                override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                                                                    Log.e("API", "FAILED: ${t.message}")
                                                                }
                                                            })
                                                    }
                                                "giveSelect" -> MachinerySelectScreen(
                                                    title = stringResource(R.string.select_machine),
                                                    onTractorClick = { navigateForward("giveTractorForm") },
                                                    onHarvesterClick = { navigateForward("giveHarvesterForm") },
                                                    onSeedDrillClick = { navigateForward("giveSeedDrillForm") },
                                                    onRotavatorClick = { navigateForward("giveRotavatorForm") }
                                                )
                                                "giveTractorForm" -> GiveTractorForm()
                                                "giveHarvesterForm" -> GiveHarvesterForm()
                                                "giveSeedDrillForm" -> GiveSeedDrillForm()
                                                "giveRotavatorForm" -> GiveRotavatorForm()
                                                "notifications" -> NotificationsScreen()
                                                "recently_viewed" -> RecentlyViewedScreen()
                                                "myMachines" -> MyMachinesScreen()
                                                "rented_machines" -> RentedMachinesScreen()
                                                "userProfile" -> {

                                                    val prefs = context.getSharedPreferences(
                                                        "USER",
                                                        Context.MODE_PRIVATE
                                                    )

                                                    UserProfileScreen(

                                                        name = profileName,

                                                        phone = prefs.getString("phone", "") ?: "",

                                                        email = profileEmail,

                                                        country = profileCountry,

                                                        state = profileState,

                                                        city = profileCity,

                                                        onEditProfile = {

                                                        },
                                                        onProfileUpdated = { n, e, c, s, ci ->

                                                            profileName = n
                                                            profileEmail = e
                                                            profileCountry = c
                                                            profileState = s
                                                            profileCity = ci
                                                        },
                                                        onPasswordChanged = {

                                                            prefs.edit().clear().apply()

                                                            isLoggedIn = false

                                                            navigateForward("login")
                                                        },
                                                        onLogout = {

                                                            prefs.edit().clear().apply()

                                                            isLoggedIn = false

                                                            navigateForward("login")
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LanguageMenu(
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Box {
        IconButton(onClick = onExpand,
                modifier = Modifier
                .clip(CircleShape)
            .background(Color(0xFFF5F7FA)),) {

            Icon(

                Icons.Outlined.Language,

                contentDescription = null,

                tint = Color(0xFF2C3E50),

                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(

            expanded = expanded,

            onDismissRequest = onDismiss,

            modifier = Modifier
                .background(Color.White)
        ) {
            DropdownMenuItem(
                colors = MenuDefaults.itemColors(

                    textColor = Color(0xFF2C3E50)
                ),
                text = { Text("English") },
                onClick = { onSelect("en") }
            )
            DropdownMenuItem(
                colors = MenuDefaults.itemColors(

                    textColor = Color(0xFF2C3E50)
                ),
                text = { Text("हिन्दी") },
                onClick = { onSelect("hi") }
            )
            DropdownMenuItem(
                colors = MenuDefaults.itemColors(

                    textColor = Color(0xFF2C3E50)
                ),
                text = { Text("मराठी") },
                onClick = { onSelect("mr") }
            )
        }
    }
}
@Composable
fun UserProfileScreen(

    name: String,
    phone: String,
    email: String,
    country: String,
    state: String,
    city: String,

    onEditProfile: () -> Unit,
    onProfileUpdated: (
        String,
        String,
        String,
        String,
        String
    ) -> Unit,
    onPasswordChanged: () -> Unit,
    onLogout: () -> Unit

) {

    var showPhoneInfo by remember {
        mutableStateOf(false)
    }

    var showEditProfileDialog by remember {
        mutableStateOf(false)
    }

    var editName by remember {
        mutableStateOf(name)
    }

    var showChangePasswordDialog by remember {
        mutableStateOf(false)
    }

    var currentPassword by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var editEmail by remember {
        mutableStateOf(email)
    }

    var editCountry by remember {
        mutableStateOf(country)
    }

    var editState by remember {
        mutableStateOf(state)
    }

    var editCity by remember {
        mutableStateOf(city)
    }
    val context = LocalContext.current
    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(20.dp))

        Text(
            text = "My Profile",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFE8F5E9)
        ) {

            Text(
                text = "Verified Account",

                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 8.dp
                ),

                color = Color(0xFF2E7D32),

                fontSize = 12.sp,

                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(30.dp))

        Spacer(Modifier.height(20.dp))

        Text(
            text = "ACCOUNT DETAILS",

            modifier = Modifier.fillMaxWidth(),

            fontSize = 12.sp,

            letterSpacing = 1.sp,

            color = Color(0xFF7F8C8D),

            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(22.dp))

        ProfileItem(
            label = "NAME",
            value = name
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = "PHONE",

                    fontSize = 11.sp,

                    letterSpacing = 1.sp,

                    color = Color.Gray
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = phone,

                    fontSize = 18.sp,

                    fontWeight = FontWeight.SemiBold,

                    color = Color(0xFF2C3E50)
                )
            }

            IconButton(
                onClick = {
                    showPhoneInfo = !showPhoneInfo
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF34495E)
                )
            }
        }

        AnimatedVisibility(
            visible = showPhoneInfo
        ) {

            Surface(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),

                shape = RoundedCornerShape(12.dp),

                color = Color(0xFFF5F7FA)
            ) {

                Text(

                    text =
                        "This phone number is linked to your account and cannot be changed.",

                    modifier = Modifier.padding(14.dp),

                    color = Color(0xFF2C3E50),

                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        HorizontalDivider()

        Spacer(Modifier.height(18.dp))

        ProfileItem(
            label = "EMAIL",
            value = email
        )

        Spacer(Modifier.height(28.dp))

        HorizontalDivider()

        Spacer(Modifier.height(20.dp))

        Text(
            text = "LOCATION",

            modifier = Modifier.fillMaxWidth(),

            fontSize = 12.sp,

            letterSpacing = 1.sp,

            color = Color(0xFF7F8C8D),

            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = city,

            modifier = Modifier.fillMaxWidth(),

            fontSize = 20.sp,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "$state, $country",

            modifier = Modifier.fillMaxWidth(),

            color = Color.Gray,

            fontSize = 15.sp
        )

        Spacer(Modifier.height(36.dp))

        Button(

            onClick = {
                showEditProfileDialog = true
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF34495E)
            )
        ) {

            Text(
                text = "Edit Account Information",
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(

            onClick = {
                showChangePasswordDialog = true
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF455A64)
            )
        ) {

            Text(
                text = "Change Password",
                color = Color.White
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onLogout
        ) {

            Text(
                text = "Logout",

                color = Color(0xFFC62828),

                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
    if (showChangePasswordDialog) {

        AlertDialog(
            shape = RoundedCornerShape(28.dp),

            containerColor = Color.White,

            tonalElevation = 0.dp,
            onDismissRequest = {
                showChangePasswordDialog = false
            },

            title = {

                Text(

                    text = "Change Password",

                    fontSize = 24.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF2C3E50)
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                        },
                        label = {
                            Text("Current Password")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                        },
                        label = {
                            Text("New Password")
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        label = {
                            Text("Confirm Password")
                        }
                    )
                }
            },
            confirmButton = {

                TextButton(

                    onClick = {

                        if (currentPassword.isBlank()) {

                            Toast.makeText(
                                context,
                                "Enter current password",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@TextButton
                        }

                        if (newPassword.length < 6) {

                            Toast.makeText(
                                context,
                                "Password must be at least 6 characters",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@TextButton
                        }

                        if (newPassword != confirmPassword) {

                            Toast.makeText(
                                context,
                                "Passwords do not match",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@TextButton
                        }

                        val request =
                            ChangePasswordRequest(

                                phone = phone,

                                current_password = currentPassword,

                                new_password = newPassword
                            )

                        RetrofitClient.api
                            .changePassword(request)
                            .enqueue(

                                object :
                                    Callback<Map<String, String>> {

                                    override fun onResponse(
                                        call: Call<Map<String, String>>,
                                        response: Response<Map<String, String>>
                                    ) {
                                        if (!response.isSuccessful) {

                                            Toast.makeText(
                                                context,
                                                "Current password is incorrect",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            return
                                        }
                                        if (response.isSuccessful) {

                                            Toast.makeText(
                                                context,
                                                "Password changed. Please login again.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            val prefs =
                                                context.getSharedPreferences(
                                                    "USER",
                                                    Context.MODE_PRIVATE
                                                )

                                            onPasswordChanged()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<Map<String, String>>,
                                        t: Throwable
                                    ) {

                                        Toast.makeText(
                                            context,
                                            t.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                    },
                    shape = RoundedCornerShape(14.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34495E)
                    )

                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                    },
                    shape = RoundedCornerShape(14.dp),

                    border = BorderStroke(
                        1.dp,
                        Color(0xFFD0D7DE)
                    )
                ) {

                    Text(
                        text = "Cancel",
                        color = Color(0xFF34495E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
    if (showEditProfileDialog) {

        AlertDialog(
            shape = RoundedCornerShape(28.dp),

            containerColor = Color.White,

            tonalElevation = 0.dp,
            onDismissRequest = {
                showEditProfileDialog = false
            },

            title = {

                Text(

                    text = "Edit Profile",

                    fontSize = 24.sp,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF2C3E50)
                )
            },
            text = {

                Column {

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = phone,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Phone Number") }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editCountry,
                        onValueChange = { editCountry = it },
                        label = { Text("Country") }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editState,
                        onValueChange = { editState = it },
                        label = { Text("State") }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("City") }
                    )
                }
            },
            confirmButton = {

                Button(
                    onClick = {

                        if (
                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(editEmail)
                                .matches()
                        ) {

                            Toast.makeText(
                                context,
                                "Enter a valid email address",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        val request = UpdateProfileRequest(
                            phone = phone,
                            name = editName,
                            email = editEmail,
                            country = editCountry,
                            state = editState,
                            city = editCity
                        )

                        RetrofitClient.api
                            .updateProfile(request)
                            .enqueue(object :
                                Callback<Map<String, String>> {

                                override fun onResponse(
                                    call: Call<Map<String, String>>,
                                    response: Response<Map<String, String>>
                                ) {

                                    if (!response.isSuccessful) {

                                        Toast.makeText(
                                            context,
                                            "Email already registered",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        return
                                    }

                                    if (response.isSuccessful) {

                                        val prefs =
                                            context.getSharedPreferences(
                                                "USER",
                                                Context.MODE_PRIVATE
                                            )
                                        prefs.edit()

                                            .putString("name", editName)

                                            .putString("email", editEmail)

                                            .putString("country", editCountry)

                                            .putString("state", editState)

                                            .putString("city", editCity)

                                            .apply()
                                        onProfileUpdated(

                                            editName,

                                            editEmail,

                                            editCountry,

                                            editState,

                                            editCity
                                        )
                                        Toast.makeText(
                                            context,
                                            "Profile Updated",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        showEditProfileDialog = false
                                    }
                                }
                                override fun onFailure(
                                    call: Call<Map<String, String>>,
                                    t: Throwable
                                ) {

                                    Toast.makeText(
                                        context,
                                        t.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            )
                    },
                    shape = RoundedCornerShape(14.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34495E)
                    )
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showEditProfileDialog = false
                    },
                    shape = RoundedCornerShape(14.dp),

                    border = BorderStroke(
                        1.dp,
                        Color(0xFFD0D7DE)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF34495E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}
@Composable
fun ProfileItem(
    label: String,
    value: String
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,

            fontSize = 11.sp,

            letterSpacing = 1.sp,

            color = Color.Gray
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = value,

            fontSize = 18.sp,

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )
    }
}
@Composable
fun PremiumBackButton(onBack: () -> Unit) {

    OutlinedButton(

        onClick = { onBack() },

        shape = RoundedCornerShape(16.dp),

        border = BorderStroke(
            1.dp,
            Color(0xFFD0D7DE)
        ),

        colors = ButtonDefaults.outlinedButtonColors(

            containerColor = Color(0xFFF8FAFC),

            contentColor = Color(0xFF2C3E50)
        )
    ) {

        Icon(

            imageVector = Icons.AutoMirrored.Filled.ArrowBack,

            contentDescription = null,

            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(6.dp))

        Text(

            text = stringResource(R.string.back),

            fontWeight = FontWeight.SemiBold
        )
    }
}
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var passwordVisible by remember {

        mutableStateOf(false)
    }
    val phoneErrorText = stringResource(R.string.phone_error)
    val passwordErrorText = stringResource(R.string.password_error)

    val loginText = stringResource(R.string.login)
    val createAccountText = stringResource(R.string.create_account)

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.loginpic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp))
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    Color.White.copy(alpha = 0.18f)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {

            Column {

                Text(

                    text = stringResource(R.string.welcome),

                    style =
                        MaterialTheme.typography.headlineLarge,

                    fontWeight = FontWeight.Bold,

                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 10) {
                            phone = it
                        }
                    },
                    label = { Text(stringResource(R.string.phone)) },
                    leadingIcon = {

                        Icon(

                            Icons.Default.Phone,

                            contentDescription = null,

                            tint = Color.White
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),

                    singleLine = true,

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedTextColor = Color.White,

                        unfocusedTextColor = Color.White,

                        focusedBorderColor = Color(0xFF81C784),

                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),

                        focusedLabelColor = Color.White,

                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),

                        cursorColor = Color.White
                    )

                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(

                    value = password,

                    onValueChange = {
                        password = it
                    },

                    label = {
                        Text(stringResource(R.string.password))
                    },

                    leadingIcon = {

                        Icon(

                            Icons.Default.Lock,

                            contentDescription = null,

                            tint = Color.White
                        )
                    },

                    trailingIcon = {

                        IconButton(

                            onClick = {

                                passwordVisible = !passwordVisible
                            }

                        ) {

                            Icon(

                                imageVector =

                                    if (passwordVisible)
                                        Icons.Default.Visibility

                                    else
                                        Icons.Default.VisibilityOff,

                                contentDescription = null,

                                tint = Color.White
                            )
                        }
                    },

                    visualTransformation =

                        if (passwordVisible)

                            VisualTransformation.None

                        else

                            PasswordVisualTransformation(),

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(16.dp),

                    singleLine = true,

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedTextColor = Color.White,

                        unfocusedTextColor = Color.White,

                        focusedBorderColor = Color(0xFF81C784),

                        unfocusedBorderColor =
                            Color.White.copy(alpha = 0.5f),

                        focusedLabelColor = Color.White,

                        unfocusedLabelColor =
                            Color.White.copy(alpha = 0.8f),

                        cursorColor = Color.White
                    )
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {

                        when {

                            phone.isBlank() ||
                                    password.isBlank() ->

                                error = "Please fill all fields."

                            phone.length != 10 ->

                                error = phoneErrorText

                            password.length < 6 ->

                                error = passwordErrorText

                            else -> {

                                error = ""

                                onLogin(
                                    phone,
                                    password
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),

                    colors = ButtonDefaults.buttonColors(

                        containerColor = Color(0xFF2E7D32)
                    ),
                ) {
                    Text(loginText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = createAccountText,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
@Composable
fun RegisterScreen(onBack: () -> Unit) {

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var passwordVisible by remember {

        mutableStateOf(false)
    }
    var error by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    val registerText = stringResource(R.string.register)
    val errorText = stringResource(R.string.fill_details_error)
    val successText = stringResource(R.string.registration_success)
    val passwordErrorText = stringResource(R.string.password_error)
    val locationText = stringResource(R.string.give_location_access)

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.loginpic),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                TextButton(onClick = onBack) {
                    Text("←")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            Color.White.copy(alpha = 0.18f)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {

                    Text(
                        text = registerText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.Person,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() } && it.length <= 10)
                                phone = it
                        },
                        label = { Text(stringResource(R.string.phone)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.Phone,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(

                        value = password,

                        onValueChange = {
                            password = it
                        },

                        label = {
                            Text(stringResource(R.string.set_password))
                        },

                        leadingIcon = {

                            Icon(

                                Icons.Default.Lock,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },

                        trailingIcon = {

                            IconButton(

                                onClick = {

                                    passwordVisible = !passwordVisible
                                }

                            ) {

                                Icon(

                                    imageVector =

                                        if (passwordVisible)
                                            Icons.Default.Visibility

                                        else
                                            Icons.Default.VisibilityOff,

                                    contentDescription = null,

                                    tint = Color.White
                                )
                            }
                        },

                        visualTransformation =

                            if (passwordVisible)

                                VisualTransformation.None

                            else

                                PasswordVisualTransformation(),

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text(stringResource(R.string.country)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.LocationOn,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text(stringResource(R.string.state)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.Map,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(stringResource(R.string.city)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.LocationCity,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = {

                            Icon(

                                Icons.Default.Email,

                                contentDescription = null,

                                tint = Color.White
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),

                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        colors = OutlinedTextFieldDefaults.colors(

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            focusedBorderColor = Color(0xFF81C784),

                            unfocusedBorderColor =
                                Color.White.copy(alpha = 0.5f),

                            focusedLabelColor = Color.White,

                            unfocusedLabelColor =
                                Color.White.copy(alpha = 0.8f),

                            cursorColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            Log.e("REGISTER", "BUTTON CLICKED")
                            if (
                                name.isBlank() ||
                                phone.isBlank() ||
                                email.isBlank() ||
                                password.isBlank() ||
                                country.isBlank() ||
                                state.isBlank() ||
                                city.isBlank()
                            ) {

                                error = "Please fill all fields."

                            } else if (phone.length != 10) {

                                error = "Phone number must be exactly 10 digits."

                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                                error = "Please enter a valid email address."

                            } else if (password.length < 6) {

                                error = "Password must be at least 6 characters."

                            } else {

                                error = ""

                                val request = RegisterRequest(
                                    name = name,
                                    phone = phone,
                                    password = password,
                                    country = country,
                                    state = state,
                                    city = city,
                                    email = email
                                )

                                RetrofitClient.api.register(request)
                                    .enqueue(object : Callback<Map<String, String>> {

                                        override fun onResponse(
                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {

                                            Log.e("REGISTER", "CODE: ${response.code()}")

                                            if (response.isSuccessful) {

                                                Toast.makeText(
                                                    context,
                                                    successText,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                onBack()

                                            } else {

                                                when (response.code()) {

                                                    400 -> {
                                                        error = "Phone number or email is already registered."
                                                    }

                                                    else -> {
                                                        error = "Registration failed. Please try again."
                                                    }
                                                }
                                            }
                                        }
                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {
                                            Log.e(
                                                "REGISTER",
                                                "FAILED: ${t.message}"
                                            )

                                            error = t.message ?: "Network error"
                                        }
                                    }
                                    )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(

                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Text(registerText)
                    }
                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = Color.Red)
                    }
                }
            }
        }
    }
}
@Composable
fun RoleScreen(
    onRentedMachines: () -> Unit,
    onRecentlyViewed: () -> Unit,
    onTake: () -> Unit,
    onGive: () -> Unit,
    onBack: () -> Unit,
    onMyMachines: () -> Unit
) {
    Box(

        modifier = Modifier
            .fillMaxWidth()
    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.SpaceEvenly
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.select_role),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Color.White.copy(alpha = 0.55f)
                    )
                    .shadow(

                        elevation = 10.dp,

                        shape = RoundedCornerShape(24.dp),

                        ambientColor = Color.Transparent,
                        spotColor = Color.Transparent
                    )
                    .border(
                        1.55.dp,
                        Color(0xFFC5CCD3),
                        RoundedCornerShape(24.dp)
                    )
                    .fillMaxWidth()
                    .clickable {
                        onTake()
                    },
                ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "🚜",
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.take_rent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.take_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Color.White.copy(alpha = 0.55f)
                    )
                    .shadow(

                        elevation = 10.dp,

                        shape = RoundedCornerShape(24.dp),

                        ambientColor = Color.Transparent,
                        spotColor = Color.Transparent
                    )
                    .border(
                        1.55.dp,
                        Color(0xFFC5CCD3),
                        RoundedCornerShape(24.dp)
                    )
                    .fillMaxWidth()
                    .clickable {
                        onGive()
                    },
                ) {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    Text(
                        text = "🏭",
                        fontSize = 34.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.give_rent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.give_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        1.3.dp,
                        Color(0xFFC5CCD3),
                        RoundedCornerShape(28.dp)
                    )
                    .background(
                        Color.White.copy(alpha = 0.15f)
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    Button(
                        modifier = Modifier.weight(1f),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(

                            containerColor = Color(0xFF34495E)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 8.5.dp,
                            vertical = 3.2.dp
                        ),

                        onClick = {
                            onMyMachines()
                        }
                    ) {

                        Text(
                            "🚜 My Machines",
                            fontSize = 12.sp,
                            color = Color.White
                        )

                    }

                    Button(
                        modifier = Modifier.weight(1f),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(

                            containerColor = Color(0xFF34495E)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 7.3.dp,
                            vertical = 3.2.dp
                        ),
                        onClick = {

                            onRecentlyViewed()
                        }

                    ) {

                        Text(
                            text = "🕓 Recent",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),

                        shape = RoundedCornerShape(16.dp),

                        colors = ButtonDefaults.buttonColors(

                            containerColor = Color(0xFF34495E)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 3.2.dp
                        ),

                        onClick = {

                            onRentedMachines()
                        }

                    ) {
                        Text(
                            "📦 Rentals",
                            fontSize = 12.sp,
                            color = Color.White
                        )

                    }
                }
            }
        }
    }
}
@Composable
fun MachinerySelectScreen(
    title: String,
    onTractorClick: () -> Unit,
    onHarvesterClick: () -> Unit,
    onSeedDrillClick: () -> Unit,
    onRotavatorClick: () -> Unit
) {
    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            MachineCard(
                emoji = "🚜",
                text = stringResource(R.string.tractor),
                onClick = onTractorClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            MachineCard(
                emoji = "🌾",
                text = stringResource(R.string.harvester),
                onClick = onHarvesterClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            MachineCard(
                emoji = "🌱",
                text = stringResource(R.string.seed_drill),
                onClick = onSeedDrillClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            MachineCard(
                emoji = "⚙️",
                text = stringResource(R.string.rotavator),
                onClick = onRotavatorClick
            )
        }
    }
}
data class Option(
    val label: String,
    val value: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeTractorForm(
    onNext: (String, String, String, String, String) -> Unit
) {
    var farmSize by remember { mutableStateOf("") }
    var usage by remember { mutableStateOf("") }
    var soil by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }
    var usageExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val fillError = stringResource(R.string.fill_all_fields)
    val usageOptions = listOf(
        Option(stringResource(R.string.ploughing), "Ploughing"),
        Option(stringResource(R.string.spraying), "Spraying"),
        Option(stringResource(R.string.seeding), "Seeding"),
        Option(stringResource(R.string.transport), "Transport")
    )
    val soilOptions = listOf(
        Option(stringResource(R.string.clay_soil), "Clay"),
        Option(stringResource(R.string.sandy_soil), "Sandy"),
        Option(stringResource(R.string.loamy_soil), "Loamy")
    )
    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )
    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.farm_details),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(

            value = farmSize,
            onValueChange = {

                if (it.all { c -> c.isDigit() }) {

                    farmSize = it
                }
            },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E)
            )
        )
        Spacer(Modifier.height(16.dp))

        DropdownField(
            value = usageOptions.find { it.value == usage }?.label ?: "",
            label = stringResource(R.string.select_usage),
            options = usageOptions.map { it.label },
            expanded = usageExpanded,
            onExpandChange = { usageExpanded = it },
            onSelected = { selectedLabel ->
                usage = usageOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))

        DropdownField(
            value = soilOptions.find { it.value == soil }?.label ?: "",
            label = stringResource(R.string.select_soil),
            options = soilOptions.map { it.label },
            expanded = soilExpanded,
            onExpandChange = { soilExpanded = it },
            onSelected = { selectedLabel ->
                soil = soilOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (
                    farmSize.isBlank() ||
                    usage.isBlank() ||
                    soil.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""
                    onNext(farmSize, usage, soil, budget, pricing)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {
            Text(stringResource(R.string.show_results),
                    fontWeight = FontWeight.SemiBold)
        }
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeHarvesterForm(
    onNext: (String, String, String, String) -> Unit
) {
    var farmSize by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }
    var cropExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val fillError = stringResource(R.string.fill_all_fields)
    val cropOptions = listOf(
        Option(stringResource(R.string.wheat), "Wheat"),
        Option(stringResource(R.string.rice), "Rice"),
        Option(stringResource(R.string.maize), "Maize")
    )
    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )
    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.farm_details),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))
        OutlinedTextField(

            value = farmSize,
            onValueChange = {

                if (it.all { c -> c.isDigit() }) {

                    farmSize = it
                }
            },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(16.dp))

        DropdownField(
            value = cropOptions.find { it.value == crop }?.label ?: "",
            label = stringResource(R.string.select_crop),
            options = cropOptions.map { it.label },
            expanded = cropExpanded,
            onExpandChange = { cropExpanded = it },
            onSelected = { selectedLabel ->
                crop = cropOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {

                if (

                    farmSize.isBlank() ||

                    crop.isBlank() ||

                    budget.isBlank() ||

                    pricing.isBlank()

                ) {

                    error = fillError

                } else {

                    error = ""

                    onNext(
                        farmSize,
                        crop,
                        budget,
                        pricing
                    )
                }
            },
            modifier = Modifier

                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeSeedDrillForm(
    onNext: (String, String, String, String) -> Unit
) {
    var farmSize by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }
    var cropExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val fillError = stringResource(R.string.fill_all_fields)
    val cropOptions = listOf(
        Option(stringResource(R.string.wheat), "Wheat"),
        Option(stringResource(R.string.rice), "Rice"),
        Option(stringResource(R.string.maize), "Maize"),
        Option(stringResource(R.string.soybean), "Soybean"),
        Option(stringResource(R.string.cotton), "Cotton")
    )
    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )
    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.farm_details),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = farmSize,
            onValueChange = {

                if (it.all { c -> c.isDigit() }) {

                    farmSize = it
                }
            },
            label = { Text(stringResource(R.string.farm_size_hectare)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )
        Spacer(Modifier.height(16.dp))

        DropdownField(
            value = cropOptions.find { it.value == crop }?.label ?: "",
            label = stringResource(R.string.select_crop),
            options = cropOptions.map { it.label },
            expanded = cropExpanded,
            onExpandChange = { cropExpanded = it },
            onSelected = { selectedLabel ->
                crop = cropOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (
                    farmSize.isBlank() ||
                    crop.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""
                    onNext(farmSize, crop, budget, pricing)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeRotavatorForm(
    onNext: (String, String, String, String) -> Unit
) {
    var hp by remember { mutableStateOf("") }
    var soil by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf("") }
    var soilExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val fillError = stringResource(R.string.fill_all_fields)
    val soilOptions = listOf(
        Option(stringResource(R.string.clay_soil), "Clay"),
        Option(stringResource(R.string.loamy_soil), "Loamy"),
        Option(stringResource(R.string.rocky_soil), "Rocky")
    )
    val budgetOptions = listOf(
        Option(stringResource(R.string.low), "Low"),
        Option(stringResource(R.string.medium), "Medium"),
        Option(stringResource(R.string.high), "High")
    )
    val pricingOptions = listOf(
        Option(stringResource(R.string.per_hour), "hour"),
        Option(stringResource(R.string.per_day), "day"),
        Option(stringResource(R.string.per_week), "week"),
        Option(stringResource(R.string.per_month), "month")
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            text = stringResource(R.string.farm_details),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = hp,
            onValueChange = {

                if (it.all { c -> c.isDigit() }) {

                    hp = it
                }
            },
            label = { Text(stringResource(R.string.tractor_hp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(16.dp))

        DropdownField(
            value = soilOptions.find { it.value == soil }?.label ?: "",
            label = stringResource(R.string.select_soil),
            options = soilOptions.map { it.label },
            expanded = soilExpanded,
            onExpandChange = { soilExpanded = it },
            onSelected = { selectedLabel ->
                soil = soilOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )

        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = budgetOptions.find { it.value == budget }?.label ?: "",
            label = stringResource(R.string.select_budget),
            options = budgetOptions.map { it.label },
            expanded = budgetExpanded,
            onExpandChange = { budgetExpanded = it },
            onSelected = { selectedLabel ->
                budget = budgetOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(16.dp))
        DropdownField(
            value = pricingOptions.find { it.value == pricing }?.label ?: "",
            label = stringResource(R.string.select_pricing_preference),
            options = pricingOptions.map { it.label },
            expanded = pricingExpanded,
            onExpandChange = { pricingExpanded = it },
            onSelected = { selectedLabel ->
                pricing = pricingOptions.find { it.label == selectedLabel }?.value ?: ""
            }
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (
                    hp.isBlank() ||
                    soil.isBlank() ||
                    budget.isBlank() ||
                    pricing.isBlank()
                ) {
                    error = fillError
                } else {
                    error = ""
                    onNext(hp, soil, budget, pricing)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {
            Text(stringResource(R.string.show_results))
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}
@Composable
fun GiveTractorForm() {
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var uploadedImageUrl by remember {
        mutableStateOf("")
    }
    var hpRange by remember { mutableStateOf("") }
    var tractorModel by remember { mutableStateOf("") }
    val context = LocalContext.current
    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val ownerEmail = prefs.getString("email", "") ?: ""
    var error by remember { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        selectedImageUri = uri
    }
    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(

        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(stringResource(R.string.tractor_requirements),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50))

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = hpRange,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) hpRange = it
            },
            label = { Text(stringResource(R.string.tractor_hp_range)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = tractorModel,
            onValueChange = { tractorModel = it },
            label = { Text(stringResource(R.string.tractor_model_name)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(

            stringResource(R.string.pricing_details),

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(12.dp))

        PriceFields(perHour, { perHour = it }, perDay, { perDay = it },
            perWeek, { perWeek = it }, perMonth, { perMonth = it })
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFFEAF1F6),

                contentColor = Color(0xFF2C3E50)
            )
        ) {
            Text("📷 Upload Machine Image")
        }
        selectedImageUri?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(onClick = {

            if (hpRange.isBlank() || tractorModel.isBlank()) {
                error = fillErrorText
                return@Button
            }
            if (

                perHour.isBlank() &&

                perDay.isBlank() &&

                perWeek.isBlank() &&

                perMonth.isBlank()

            ) {

                error = "Please enter at least one pricing option."

                return@Button
            }
            error = ""
            val prefs = context.getSharedPreferences(
                "USER",
                Context.MODE_PRIVATE
            )
            val ownerName = prefs.getString("name", "") ?: ""
            val ownerPhone = prefs.getString("phone", "") ?: ""
            val city = prefs.getString("city", "") ?: ""
            if (selectedImageUri != null) {

                val file = uriToFile(context, selectedImageUri!!)

                val requestFile = file.asRequestBody(
                    "image/*".toMediaTypeOrNull()
                )

                val body = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestFile
                )

                RetrofitClient.api.uploadImage(body)
                    .enqueue(object : Callback<ImageUploadResponse> {

                        override fun onResponse(
                            call: Call<ImageUploadResponse>,
                            response: Response<ImageUploadResponse>
                        ) {

                            if (response.isSuccessful) {

                                uploadedImageUrl =
                                    response.body()?.image_url ?: ""
                                val request = AddMachineRequest(
                                    type = "tractor",
                                    model_name = tractorModel,
                                    hp_range = hpRange.toInt(),
                                    cutting_width = null,
                                    working_width = null,
                                    row_count = null,
                                    price_per_hour = perHour.toIntOrNull(),
                                    price_per_day = perDay.toIntOrNull(),
                                    price_per_week = perWeek.toIntOrNull(),
                                    price_per_month = perMonth.toIntOrNull(),
                                    owner_name = ownerName,
                                    owner_phone = ownerPhone,
                                    location = city,
                                    owner_email = ownerEmail,
                                    image_url = uploadedImageUrl
                                )
                                RetrofitClient.api.addMachine(request)
                                    .enqueue(object : Callback<Map<String, String>> {

                                        override fun onResponse(
                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {

                                            if (response.isSuccessful) {

                                                Log.e("API", "✅ Machine Added")

                                                Toast.makeText(
                                                    context,
                                                    "🚜 Machine registered successfully!",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                hpRange = ""
                                                tractorModel = ""
                                                perHour = ""
                                                perDay = ""
                                                perWeek = ""
                                                perMonth = ""

                                                selectedImageUri = null

                                            } else {

                                                Log.e("API", "❌ Error: ${response.code()}")
                                            }
                                        }
                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {

                                            Log.e("API", "❌ FAILED: ${t.message}")
                                        }
                                    }
                                    )
                            }
                        }
                        override fun onFailure(
                            call: Call<ImageUploadResponse>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                context,
                                "Image upload failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            else {
                val request = AddMachineRequest(
                    type = "tractor",
                    model_name = tractorModel,
                    hp_range = hpRange.toInt(),
                    cutting_width = null,
                    working_width = null,
                    row_count = null,
                    price_per_hour = perHour.toIntOrNull(),
                    price_per_day = perDay.toIntOrNull(),
                    price_per_week = perWeek.toIntOrNull(),
                    price_per_month = perMonth.toIntOrNull(),
                    owner_name = ownerName,
                    owner_phone = ownerPhone,
                    location = city,
                    owner_email = ownerEmail,
                    image_url = null
                )
                RetrofitClient.api.addMachine(request)
                    .enqueue(object : Callback<Map<String, String>> {

                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {
                            if (response.isSuccessful) {

                                Toast.makeText(
                                    context,
                                    "🚜 Machine registered successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                hpRange = ""
                                tractorModel = ""

                                perHour = ""
                                perDay = ""
                                perWeek = ""
                                perMonth = ""

                                selectedImageUri = null
                            } else {

                                Log.e("API", "❌ Error: ${response.code()}")
                            }
                        }
                        override fun onFailure(
                            call: Call<Map<String, String>>,
                            t: Throwable
                        ) {

                            Log.e("API", "❌ FAILED: ${t.message}")
                        }
                    }
                    )
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )) {
            Text(stringResource(R.string.save))
        }
        if (error.isNotEmpty()) Text(error, color = Color.Red)
    }
}
fun uriToFile(context: Context, uri: Uri): File {

    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("upload", ".jpg", context.cacheDir)

    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}
@Composable
fun GiveHarvesterForm() {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var uploadedImageUrl by remember {
        mutableStateOf("")
    }
    var cuttingWidth by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }
    val fillErrorText = stringResource(R.string.fill_all_fields)
    Column(

        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.harvester_requirements),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = cuttingWidth,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    cuttingWidth = it
                }
            },
            label = {
                Text(stringResource(R.string.cutting_width_ft))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.harvester_model_name))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(

            stringResource(R.string.pricing_details),

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFFEAF1F6),

                contentColor = Color(0xFF2C3E50)
            )
        ) {
            Text("📷 Upload Machine Image")
        }

        selectedImageUri?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    cuttingWidth.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }
                if (

                    perHour.isBlank() &&

                    perDay.isBlank() &&

                    perWeek.isBlank() &&

                    perMonth.isBlank()

                ) {

                    error = "Please enter at least one pricing option."

                    return@Button
                }
                error = ""

                if (selectedImageUri != null) {

                    val file = uriToFile(context, selectedImageUri!!)

                    val requestFile = file.asRequestBody(
                        "image/*".toMediaTypeOrNull()
                    )

                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                    RetrofitClient.api.uploadImage(body)
                        .enqueue(object : Callback<ImageUploadResponse> {

                            override fun onResponse(
                                call: Call<ImageUploadResponse>,
                                response: Response<ImageUploadResponse>
                            ) {

                                if (response.isSuccessful) {

                                    uploadedImageUrl =
                                        response.body()?.image_url ?: ""

                                    val request = AddMachineRequest(
                                        type = "harvester",
                                        model_name = modelName,
                                        hp_range = null,
                                        cutting_width = cuttingWidth.toDouble(),
                                        working_width = null,
                                        row_count = null,
                                        price_per_hour = perHour.toIntOrNull(),
                                        price_per_day = perDay.toIntOrNull(),
                                        price_per_week = perWeek.toIntOrNull(),
                                        price_per_month = perMonth.toIntOrNull(),
                                        owner_name = ownerName,
                                        owner_phone = ownerPhone,
                                        owner_email = ownerEmail,
                                        location = city,
                                        image_url = uploadedImageUrl
                                    )
                                    RetrofitClient.api.addMachine(request)

                                        .enqueue(object : Callback<Map<String, String>> {

                                            override fun onResponse(
                                                call: Call<Map<String, String>>,
                                                response: Response<Map<String, String>>
                                            ) {

                                                if (response.isSuccessful) {

                                                    Toast.makeText(
                                                        context,
                                                        "🚜 Harvester registered successfully!",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    cuttingWidth = ""
                                                    modelName = ""

                                                    perHour = ""
                                                    perDay = ""
                                                    perWeek = ""
                                                    perMonth = ""

                                                    selectedImageUri = null
                                                }

                                                else {

                                                    Toast.makeText(
                                                        context,
                                                        "Failed to register harvester",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<Map<String, String>>,
                                                t: Throwable
                                            ) {

                                                Toast.makeText(
                                                    context,
                                                    "Failed: ${t.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        )
                                }
                            }
                            override fun onFailure(
                                call: Call<ImageUploadResponse>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Image upload failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

                }
                else {

                    val request = AddMachineRequest(
                        type = "harvester",
                        model_name = modelName,
                        hp_range = null,
                        cutting_width = cuttingWidth.toDouble(),
                        working_width = null,
                        row_count = null,
                        price_per_hour = perHour.toIntOrNull(),
                        price_per_day = perDay.toIntOrNull(),
                        price_per_week = perWeek.toIntOrNull(),
                        price_per_month = perMonth.toIntOrNull(),
                        owner_name = ownerName,
                        owner_phone = ownerPhone,
                        owner_email = ownerEmail,
                        location = city,
                        image_url = null
                    )
                    RetrofitClient.api.addMachine(request)
                        .enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {

                                if (response.isSuccessful) {

                                    Toast.makeText(
                                        context,
                                        "🚜 Harvester registered successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    cuttingWidth = ""
                                    modelName = ""

                                    perHour = ""
                                    perDay = ""
                                    perWeek = ""
                                    perMonth = ""

                                    selectedImageUri = null

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Failed to register harvester",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(
                                call: Call<Map<String, String>>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}
@Composable
fun GiveSeedDrillForm() {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var uploadedImageUrl by remember {
        mutableStateOf("")
    }
    var rowCount by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        selectedImageUri = uri
    }

    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(

        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {

        Text(
            stringResource(R.string.seed_drill_requirements),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = rowCount,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    rowCount = it
                }
            },
            label = {
                Text(stringResource(R.string.row_count))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.seed_drill_model_name))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(

            stringResource(R.string.pricing_details),

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFFEAF1F6),

                contentColor = Color(0xFF2C3E50)
            )
        ) {
            Text("📷 Upload Machine Image")
        }

        selectedImageUri?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    rowCount.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }
                if (

                    perHour.isBlank() &&

                    perDay.isBlank() &&

                    perWeek.isBlank() &&

                    perMonth.isBlank()

                ) {

                    error = "Please enter at least one pricing option."

                    return@Button
                }
                error = ""

                if (selectedImageUri != null) {

                    val file = uriToFile(context, selectedImageUri!!)

                    val requestFile = file.asRequestBody(
                        "image/*".toMediaTypeOrNull()
                    )

                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                    RetrofitClient.api.uploadImage(body)
                        .enqueue(object : Callback<ImageUploadResponse> {

                            override fun onResponse(
                                call: Call<ImageUploadResponse>,
                                response: Response<ImageUploadResponse>
                            ) {

                                if (response.isSuccessful) {

                                    uploadedImageUrl =
                                        response.body()?.image_url ?: ""

                                    val request = AddMachineRequest(
                                        type = "seed drill",
                                        model_name = modelName,
                                        hp_range = null,
                                        cutting_width = null,
                                        working_width = null,
                                        row_count = rowCount.toInt(),
                                        price_per_hour = perHour.toIntOrNull(),
                                        price_per_day = perDay.toIntOrNull(),
                                        price_per_week = perWeek.toIntOrNull(),
                                        price_per_month = perMonth.toIntOrNull(),
                                        owner_name = ownerName,
                                        owner_phone = ownerPhone,
                                        owner_email = ownerEmail,
                                        location = city,
                                        image_url = uploadedImageUrl
                                    )

                                    RetrofitClient.api.addMachine(request)

                                        .enqueue(object : Callback<Map<String, String>> {

                                            override fun onResponse(
                                                call: Call<Map<String, String>>,
                                                response: Response<Map<String, String>>
                                            ) {

                                                if (response.isSuccessful) {

                                                    Toast.makeText(
                                                        context,
                                                        "🌾 Seed Drill registered successfully!",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    rowCount = ""
                                                    modelName = ""

                                                    perHour = ""
                                                    perDay = ""
                                                    perWeek = ""
                                                    perMonth = ""

                                                    selectedImageUri = null
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<Map<String, String>>,
                                                t: Throwable
                                            ) {

                                                Toast.makeText(
                                                    context,
                                                    "Failed: ${t.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                }
                            }
                            override fun onFailure(
                                call: Call<ImageUploadResponse>,
                                t: Throwable
                            ) {
                                Toast.makeText(
                                    context,
                                    "Image upload failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                }
                else {

                    val request = AddMachineRequest(
                        type = "seed drill",
                        model_name = modelName,
                        hp_range = null,
                        cutting_width = null,
                        working_width = null,
                        row_count = rowCount.toInt(),
                        price_per_hour = perHour.toIntOrNull(),
                        price_per_day = perDay.toIntOrNull(),
                        price_per_week = perWeek.toIntOrNull(),
                        price_per_month = perMonth.toIntOrNull(),
                        owner_name = ownerName,
                        owner_phone = ownerPhone,
                        owner_email = ownerEmail,
                        location = city,
                        image_url = null
                    )
                    RetrofitClient.api.addMachine(request)

                        .enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {

                                if (response.isSuccessful) {

                                    Toast.makeText(
                                        context,
                                        "🌾 Seed Drill registered successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    rowCount = ""
                                    modelName = ""

                                    perHour = ""
                                    perDay = ""
                                    perWeek = ""
                                    perMonth = ""

                                    selectedImageUri = null

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Failed to register seed drill",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            override fun onFailure(
                                call: Call<Map<String, String>>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}
@Composable
fun GiveRotavatorForm() {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var uploadedImageUrl by remember {
        mutableStateOf("")
    }
    var workingWidth by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var perHour by remember { mutableStateOf("") }
    var perDay by remember { mutableStateOf("") }
    var perWeek by remember { mutableStateOf("") }
    var perMonth by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val ownerName = prefs.getString("name", "") ?: ""
    val ownerPhone = prefs.getString("phone", "") ?: ""
    val ownerEmail = prefs.getString("email", "") ?: ""
    val city = prefs.getString("city", "") ?: ""
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        selectedImageUri = uri
    }
    val fillErrorText = stringResource(R.string.fill_all_fields)

    Column(

        Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            stringResource(R.string.rotavator_requirements),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = workingWidth,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    workingWidth = it
                }
            },
            label = {
                Text(stringResource(R.string.working_width_m))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = modelName,
            onValueChange = {
                modelName = it
            },
            label = {
                Text(stringResource(R.string.rotavator_model_name))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        Spacer(Modifier.height(24.dp))

        Text(

            stringResource(R.string.pricing_details),

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(12.dp))

        PriceFields(
            perHour,
            { perHour = it },

            perDay,
            { perDay = it },

            perWeek,
            { perWeek = it },

            perMonth,
            { perMonth = it }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFFEAF1F6),

                contentColor = Color(0xFF2C3E50)
            )
        ) {
            Text("📷 Upload Machine Image")
        }

        selectedImageUri?.let {

            Spacer(Modifier.height(16.dp))

            AsyncImage(
                model = it,
                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(

            onClick = {

                if (
                    workingWidth.isBlank() ||
                    modelName.isBlank()
                ) {

                    error = fillErrorText
                    return@Button
                }
                if (

                    perHour.isBlank() &&

                    perDay.isBlank() &&

                    perWeek.isBlank() &&

                    perMonth.isBlank()

                ) {

                    error = "Please enter at least one pricing option."

                    return@Button
                }
                error = ""

                if (selectedImageUri != null) {

                    val file = uriToFile(context, selectedImageUri!!)

                    val requestFile = file.asRequestBody(
                        "image/*".toMediaTypeOrNull()
                    )

                    val body = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                    RetrofitClient.api.uploadImage(body)
                        .enqueue(object : Callback<ImageUploadResponse> {

                            override fun onResponse(
                                call: Call<ImageUploadResponse>,
                                response: Response<ImageUploadResponse>
                            ) {

                                if (response.isSuccessful) {

                                    uploadedImageUrl =
                                        response.body()?.image_url ?: ""

                                    val request = AddMachineRequest(
                                        type = "rotavator",
                                        model_name = modelName,
                                        hp_range = null,
                                        cutting_width = null,
                                        working_width = workingWidth.toDouble(),
                                        row_count = null,
                                        price_per_hour = perHour.toIntOrNull(),
                                        price_per_day = perDay.toIntOrNull(),
                                        price_per_week = perWeek.toIntOrNull(),
                                        price_per_month = perMonth.toIntOrNull(),
                                        owner_name = ownerName,
                                        owner_phone = ownerPhone,
                                        owner_email = ownerEmail,
                                        location = city,
                                        image_url = uploadedImageUrl
                                    )
                                    RetrofitClient.api.addMachine(request)

                                        .enqueue(object : Callback<Map<String, String>> {

                                            override fun onResponse(
                                                call: Call<Map<String, String>>,
                                                response: Response<Map<String, String>>
                                            ) {

                                                if (response.isSuccessful) {

                                                    Toast.makeText(
                                                        context,
                                                        "🚜 Rotavator registered successfully!",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    workingWidth = ""
                                                    modelName = ""

                                                    perHour = ""
                                                    perDay = ""
                                                    perWeek = ""
                                                    perMonth = ""

                                                    selectedImageUri = null
                                                }
                                            }
                                            override fun onFailure(
                                                call: Call<Map<String, String>>,
                                                t: Throwable
                                            ) {

                                                Toast.makeText(
                                                    context,
                                                    "Failed: ${t.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        )
                                }
                            }
                            override fun onFailure(
                                call: Call<ImageUploadResponse>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Image upload failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                }
                else {
                    val request = AddMachineRequest(
                        type = "rotavator",
                        model_name = modelName,
                        hp_range = null,
                        cutting_width = null,
                        working_width = workingWidth.toDouble(),
                        row_count = null,
                        price_per_hour = perHour.toIntOrNull(),
                        price_per_day = perDay.toIntOrNull(),
                        price_per_week = perWeek.toIntOrNull(),
                        price_per_month = perMonth.toIntOrNull(),
                        owner_name = ownerName,
                        owner_phone = ownerPhone,
                        owner_email = ownerEmail,
                        location = city,
                        image_url = null
                    )
                    RetrofitClient.api.addMachine(request)

                        .enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {

                                if (response.isSuccessful) {

                                    Toast.makeText(
                                        context,
                                        "🚜 Rotavator registered successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    workingWidth = ""
                                    modelName = ""

                                    perHour = ""
                                    perDay = ""
                                    perWeek = ""
                                    perMonth = ""

                                    selectedImageUri = null

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Failed to register rotavator",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            override fun onFailure(
                                call: Call<Map<String, String>>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(

                containerColor = Color(0xFF34495E)
            )
        ) {

            Text(stringResource(R.string.save))
        }

        if (error.isNotEmpty()) {

            Spacer(Modifier.height(16.dp))

            Text(
                error,
                color = Color.Red
            )
        }
    }
}
@Composable
fun PriceFields(
    perHour: String, onHour: (String) -> Unit,
    perDay: String, onDay: (String) -> Unit,
    perWeek: String, onWeek: (String) -> Unit,
    perMonth: String, onMonth: (String) -> Unit
) {

    fun onlyNumbers(input: String, update: (String) -> Unit) {
        if (input.all { it.isDigit() }) {
            update(input)
        }
    }
    OutlinedTextField(
        value = perHour,
        onValueChange = { onlyNumbers(it, onHour) },
        label = { Text(stringResource(R.string.price_per_hour)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = Color(0xFF34495E),

            unfocusedBorderColor = Color(0xFFD0D7DE),

            focusedLabelColor = Color(0xFF34495E),

            cursorColor = Color(0xFF34495E),

            focusedContainerColor = Color(0xFFF8FAFC),

            unfocusedContainerColor = Color(0xFFF8FAFC),

            focusedTextColor = Color(0xFF2C3E50),

            unfocusedTextColor = Color(0xFF2C3E50)
        )
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perDay,
        onValueChange = { onlyNumbers(it, onDay) },
        label = { Text(stringResource(R.string.price_per_day)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = Color(0xFF34495E),

            unfocusedBorderColor = Color(0xFFD0D7DE),

            focusedLabelColor = Color(0xFF34495E),

            cursorColor = Color(0xFF34495E),

            focusedContainerColor = Color(0xFFF8FAFC),

            unfocusedContainerColor = Color(0xFFF8FAFC),

            focusedTextColor = Color(0xFF2C3E50),

            unfocusedTextColor = Color(0xFF2C3E50)
        )
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perWeek,
        onValueChange = { onlyNumbers(it, onWeek) },
        label = { Text(stringResource(R.string.price_per_week)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = Color(0xFF34495E),

            unfocusedBorderColor = Color(0xFFD0D7DE),

            focusedLabelColor = Color(0xFF34495E),

            cursorColor = Color(0xFF34495E),

            focusedContainerColor = Color(0xFFF8FAFC),

            unfocusedContainerColor = Color(0xFFF8FAFC),

            focusedTextColor = Color(0xFF2C3E50),

            unfocusedTextColor = Color(0xFF2C3E50)
        )
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = perMonth,
        onValueChange = { onlyNumbers(it, onMonth) },
        label = { Text(stringResource(R.string.price_per_month)) },
        leadingIcon = { Text("₹") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),

        singleLine = true,

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = Color(0xFF34495E),

            unfocusedBorderColor = Color(0xFFD0D7DE),

            focusedLabelColor = Color(0xFF34495E),

            cursorColor = Color(0xFF34495E),

            focusedContainerColor = Color(0xFFF8FAFC),

            unfocusedContainerColor = Color(0xFFF8FAFC),

            focusedTextColor = Color(0xFF2C3E50),

            unfocusedTextColor = Color(0xFF2C3E50)
        )
    )
}
@Composable
fun MachineCard(
    emoji: String,
    text: String,
    onClick: () -> Unit
) {
    Card(

        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .shadow(

                elevation = 6.dp,

                shape = RoundedCornerShape(24.dp)
            )

            .border(

                1.3.dp,

                Color(0xFFD0D7DE),

                RoundedCornerShape(24.dp)
            )
            .background(
                Color(0xFFFDFDFD)
            )
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(

            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,

                style = MaterialTheme.typography.titleMedium,

                fontWeight = FontWeight.SemiBold,

                fontSize = 17.3.sp,

                color = Color(0xFF2C3E50),

                modifier = Modifier
                    .padding(top = 4.dp)
                    .offset(y = 6.4.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    value: String,
    label: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandChange(!expanded) }
    ) {

        OutlinedTextField(

            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(18.dp),

            singleLine = true,
            trailingIcon = {

                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor = Color(0xFF34495E),

                unfocusedBorderColor = Color(0xFFD0D7DE),

                focusedLabelColor = Color(0xFF34495E),

                cursorColor = Color(0xFF34495E),

                focusedContainerColor = Color(0xFFF8FAFC),

                unfocusedContainerColor = Color(0xFFF8FAFC),

                focusedTextColor = Color(0xFF2C3E50),

                unfocusedTextColor = Color(0xFF2C3E50)
            )
        )

        ExposedDropdownMenu(
            modifier = Modifier
                .background(Color.White),
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) }
        ) {
            options.forEach {

                DropdownMenuItem(
                    colors = MenuDefaults.itemColors(

                        textColor = Color(0xFF2C3E50)
                    ),
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        onExpandChange(false)
                    }
                )
            }
        }
    }
}
@Composable
fun RecommendationScreen(
    result: RecommendResponse?,
    machineType: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedMachine by remember {
        mutableStateOf<Machine?>(null)
    }
    var selectedImageUrl by remember {
        mutableStateOf<String?>(null)
    }
    var showDelistedDialog by remember {

        mutableStateOf(false)
    }

    var showRecommendationInfo by remember {
        mutableStateOf(false)
    }

    var requestedMachines by remember {

        mutableStateOf(setOf<Int>())
    }

        val prefsUser = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )

    val phone =
        prefsUser.getString("phone", "")
            ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        Text(

            text = "Recommendations",

            style = MaterialTheme.typography.headlineLarge,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(

            text = "AI-picked machines for your requirements",

            color = Color.Gray,

            fontSize = 14.sp
        )
        when {

            result == null -> {

                Text(
                    "Loading...",
                    color = Color.Gray
                )
            }

            result.machines.isEmpty() -> {

                Text(
                    "No machines found",
                    color = Color.Red
                )
            }

            else -> {

                Row(

                    verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(

                        text = "Best Match: ${result.prediction}",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.SemiBold,

                        color = Color(0xFF607D8B)
                    )
                    Log.e(
                        "PREDICTION_DEBUG",
                        result.prediction.toString()
                    )
                    IconButton(

                        onClick = {
                            showRecommendationInfo =
                                !showRecommendationInfo
                        },

                        modifier = Modifier.size(24.dp)
                    ) {

                        Icon(

                            imageVector = Icons.Default.Info,

                            contentDescription = null,

                            tint = Color.Gray
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showRecommendationInfo
                ) {

                    Surface(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),

                        shape = RoundedCornerShape(12.dp),

                        color = Color(0xFFF5F7FA)
                    ) {

                        Text(

                            text = when (machineType) {

                                "takeTractorForm" ->
                                    "The displayed range (${result.prediction}) represents the recommended HP (Horsepower) range calculated from your farm size, soil conditions and operational requirements."

                                "takeHarvesterForm" ->
                                    "The displayed value (${result.prediction}) represents the recommended cutting width calculated from your entered Farm details for harvesting."

                                "takeRotavatorForm" ->
                                    "The displayed value (${result.prediction}) represents the recommended working width calculated from your entered Farm details for cultivation."

                                "takeSeedDrillForm" ->
                                    "The displayed value (${result.prediction}) represents the recommended row count calculated from your entered Farm details for sowing."

                                else ->
                                    "The displayed value (${result.prediction}) was generated from your entered farm details."
                            },

                            modifier = Modifier.padding(14.dp),

                            color = Color(0xFF2C3E50),

                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                val hasRecommendedMachine =
                    result.machines.any {
                        it.recommended == true
                    }

                if (!hasRecommendedMachine) {

                    Surface(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),

                        shape = RoundedCornerShape(16.dp),

                        color = Color(0xFFFFF8E1)
                    ) {

                        Text(

                            text =
                                "No registered machines currently match the recommended ${result.prediction} specification. The machines below are the closest available alternatives.",

                            modifier = Modifier.padding(14.dp),

                            color = Color(0xFF6D4C41),

                            fontSize = 13.sp
                        )
                    }
                }

                result.machines
                    .sortedByDescending { it.recommended == true }
                    .forEach { machine ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)

                                .shadow(

                                    elevation = 6.dp,

                                    shape = RoundedCornerShape(24.dp)
                                )

                                .border(

                                    width = if (machine.recommended == true) 1.8.dp else 1.3.dp,

                                    color = if (machine.recommended == true)

                                        Color(0xFF90A4AE)

                                    else

                                        Color(0xFFD0D7DE),

                                    shape = RoundedCornerShape(24.dp)
                                )

                                .clickable {
                                    selectedMachine = machine
                                    val prefs =
                                        context.getSharedPreferences(
                                            "RECENT",
                                            Context.MODE_PRIVATE
                                        )

                                    val existing =

                                        prefs.getStringSet(
                                            "machines_$phone",
                                            emptySet()
                                        )?.toMutableSet()

                                            ?: mutableSetOf()

                                    val machineData =
                                        "${machine.id}|" +
                                                "${machine.model_name}|" +
                                                "${machine.owner_name}|" +
                                                "${machine.owner_phone}|" +
                                                "${machine.location}|" +
                                                "${machine.owner_email}|" +
                                                "${machine.price_per_hour}|" +
                                                "${machine.price_per_day}|" +
                                                "${machine.price_per_week}|" +
                                                "${machine.price_per_month}|" +
                                                "${machine.image_url}|" +
                                                "${machine.type}|" +
                                                "${machine.hp_range}|" +
                                                "${machine.cutting_width}|" +
                                                "${machine.working_width}|" +
                                                "${machine.row_count}"
                                    existing.removeAll {

                                        it.startsWith("${machine.id}|")
                                    }

                                    existing.add(machineData)
                                    prefs.edit()

                                        .putStringSet(
                                            "machines_$phone",
                                            existing
                                        )
                                        .apply()
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =

                                    if (machine.recommended == true)

                                        Color(0xFFF8FBFD)

                                    else

                                        Color.White
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {

                                if (machine.recommended == true) {

                                    Box(

                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color(0xFFE3F2FD))
                                            .padding(

                                                horizontal = 10.dp,

                                                vertical = 4.dp
                                            )
                                    ) {

                                        Text(

                                            text = "Recommended",

                                            color = Color(0xFF1565C0),

                                            fontSize = 12.sp,

                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                }

                                Text(
                                    text = machine.model_name ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    machine.hp_range?.let {

                                        InfoChip("HP", "$it")
                                    }

                                    machine.cutting_width?.let {

                                        InfoChip("Cut", "${it}ft")
                                    }

                                    machine.working_width?.let {

                                        InfoChip("Width", "${it}m")
                                    }

                                    machine.row_count?.let {

                                        InfoChip("Rows", "$it")
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    PriceItem("Hr", machine.price_per_hour)

                                    PriceItem("Day", machine.price_per_day)

                                    PriceItem("Week", machine.price_per_week)

                                    PriceItem("Month", machine.price_per_month)
                                }

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Tap to view owner details",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                machine.image_url?.let {

                                    Spacer(Modifier.height(12.dp))

                                    OutlinedButton(

                                        onClick = {

                                            selectedImageUrl = machine.image_url

                                            Log.e("IMAGE", machine.image_url ?: "NULL")
                                        },

                                        shape = RoundedCornerShape(14.dp),

                                        border = BorderStroke(

                                            1.dp,

                                            Color(0xFFD0D7DE)
                                        ),

                                        colors = ButtonDefaults.outlinedButtonColors(

                                            containerColor = Color(0xFFF8FAFC),

                                            contentColor = Color(0xFF2C3E50)
                                        )
                                    ) {
                                        Text(

                                            text = "View Image",

                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
    selectedMachine?.let { machine ->

        AlertDialog(
            shape = RoundedCornerShape(28.dp),

            containerColor = Color.White,
            onDismissRequest = {
                selectedMachine = null
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        selectedMachine = null
                    }
                ) {
                    Text("Close")
                }
            },
            title = {
                Text(

                    "Owner Details",

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF2C3E50)
                )
            },
            text = {

                Column {

                    OwnerInfoRow(

                        label = "Owner",

                        value = machine.owner_name ?: ""
                    )

                    Spacer(Modifier.height(14.dp))

                    OwnerInfoRow(

                        label = "Phone",

                        value = machine.owner_phone ?: ""
                    )

                    Spacer(Modifier.height(14.dp))

                    OwnerInfoRow(

                        label = "Email",

                        value = machine.owner_email ?: ""
                    )

                    Spacer(Modifier.height(14.dp))

                    OwnerInfoRow(

                        label = "Location",

                        value = machine.location ?: ""
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {

                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${machine.owner_phone}")
                                )
                                context.startActivity(intent)
                            }
                            ,
                            modifier = Modifier
                                .width(90.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFD0D7DE)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = Color(0xFF2C3E50)
                            )
                        ) {
                            Text("Call")
                        }
                        OutlinedButton(

                            onClick = {

                                val intent = Intent(Intent.ACTION_SEND).apply {

                                    type = "message/rfc822"

                                    putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(machine.owner_email)
                                    )

                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Regarding Machine Rental"
                                    )

                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
Hello ${machine.owner_name},

I am interested in renting your machine:

${machine.model_name}

Please share more details.

Thank you.
                """.trimIndent()
                                    )
                                }

                                context.startActivity(
                                    Intent.createChooser(intent, "Send Email")
                                )
                            }
                            ,
                            modifier = Modifier
                                .width(90.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFD0D7DE)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = Color(0xFF2C3E50)
                            )
                        ) {
                            Text("Email")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (machine.id !in requestedMachines) {
                        Button(
                            onClick = {
                                val prefs = context.getSharedPreferences(
                                    "USER",
                                    Context.MODE_PRIVATE
                                )
                                val requesterName =
                                    prefs.getString("name", "") ?: ""
                                val requesterPhone =
                                    prefs.getString("phone", "") ?: ""
                                val requesterEmail =
                                    prefs.getString("email", "") ?: ""
                                val requesterLocation =
                                    prefs.getString("city", "") ?: ""
                                val body = RequestMachineBody(
                                    machine_id = machine.id ?: 0,
                                    requester_name = requesterName,
                                    requester_phone = requesterPhone,
                                    owner_phone = machine.owner_phone ?: "",
                                    requester_email = requesterEmail,
                                    requester_location = requesterLocation
                                )

                                RetrofitClient.api.requestMachine(body)

                                    .enqueue(object : Callback<Map<String, String>> {

                                        override fun onResponse(
                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {

                                            if (response.isSuccessful) {

                                                val msg =

                                                    response.body()?.get("message")
                                                        ?: ""

                                                if (msg == "Machine delisted") {

                                                    showDelistedDialog = true

                                                } else {

                                                    requestedMachines =
                                                        requestedMachines + (machine.id ?: 0)

                                                    Toast.makeText(
                                                        context,
                                                        "🚜 Request sent, you will be notified soon",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {

                                            Toast.makeText(
                                                context,
                                                "Request failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    )
                            }
                           ,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF34495E)
                            )
                        ) {
                            Text(

                                "Request Machine",

                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (showDelistedDialog) {

                            AlertDialog(

                                onDismissRequest = {

                                    showDelistedDialog = false
                                },

                                confirmButton = {

                                    Button(

                                        onClick = {

                                            showDelistedDialog = false
                                        }

                                    ) {

                                        Text("OK")
                                    }
                                },

                                title = {

                                    Text("Machine Delisted")
                                },

                                text = {

                                    Text(
                                        "Sorry, the owner has delisted this machine."
                                    )
                                }
                            )
                        }

                    } else {

                        OutlinedButton(

                            onClick = {

                                val phone = context
                                    .getSharedPreferences(
                                        "USER",
                                        Context.MODE_PRIVATE
                                    )
                                    .getString("phone", "") ?: ""

                                RetrofitClient.api.undoRequest(

                                    machine.id ?: 0,
                                    phone

                                ).enqueue(
                                    object : Callback<Map<String, String>> {

                                        override fun onResponse(
                                            call: Call<Map<String, String>>,
                                            response: Response<Map<String, String>>
                                        ) {

                                            requestedMachines =
                                                requestedMachines - (machine.id ?: 0)

                                            Toast.makeText(
                                                context,
                                                "Request removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onFailure(
                                            call: Call<Map<String, String>>,
                                            t: Throwable
                                        ) {

                                            Toast.makeText(
                                                context,
                                                "Failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                           ,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFFD0D7DE)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF8FAFC),
                                contentColor = Color(0xFF2C3E50)
                            )
                        ) {
                            Text("Undo Request")
                        }
                    }
                }
            }
        )
    }
    selectedImageUrl?.let {

        AlertDialog(
            shape = RoundedCornerShape(28.dp),

            containerColor = Color.White,
            onDismissRequest = {
                selectedImageUrl = null
            },

            confirmButton = {

                Button(
                    shape = RoundedCornerShape(16.dp),

                    colors = ButtonDefaults.buttonColors(

                        containerColor = Color(0xFF34495E)
                    ),
                    onClick = {
                        selectedImageUrl = null
                    }
                ) {
                    Text(

                        "Done",

                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            title = {
                Text(

                    "Machine Preview",

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF2C3E50)
                )
            },

            text = {

                AsyncImage(

                    model =
                        AppConfig.BASE_URL + it.removePrefix("/"),

                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .border(
                            1.dp,
                            Color(0xFFDCE3EA),
                            RoundedCornerShape(22.dp)
                        )
                )
            }
        )
    }
}
@Composable
fun OwnerInfoRow(
    label: String,
    value: String
) {
    Column {
        Text(

            text = label,

            color = Color.Gray,

            fontSize = 12.sp
        )

        Spacer(Modifier.height(2.dp))

        Text(

            text = value,

            color = Color(0xFF2C3E50),

            fontWeight = FontWeight.Medium,

            fontSize = 16.sp
        )
    }
}
@Composable
fun InfoChip(
    title: String,
    value: String
) {
    Box(

        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F7FA))
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {

        Text(

            "$title: $value",

            fontSize = 12.sp,

            color = Color(0xFF2C3E50),

            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun PriceItem(
    label: String,
    value: Int?
) {

    Column(

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(

            label,

            fontSize = 11.sp,

            color = Color.Gray
        )

        Text(

            "₹${value ?: "-"}",

            fontWeight = FontWeight.SemiBold,

            color = Color(0xFF2C3E50)
        )
    }
}
@Composable
fun MyMachinesScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    var editHpRange by remember {
        mutableStateOf("")
    }
    var newImageUrl by remember {
        mutableStateOf("")
    }
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val imagePickerLauncher =

        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.GetContent()

        ) { uri ->

            if (uri != null) {

                selectedImageUri = uri

                val inputStream =
                    context.contentResolver.openInputStream(uri)

                val file = File(
                    context.cacheDir,
                    "upload_${System.currentTimeMillis()}.jpg"
                )

                file.outputStream().use { output ->

                    inputStream?.copyTo(output)
                }

                val requestFile =

                    file.asRequestBody("image/*".toMediaType())

                val body =

                    MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        requestFile
                    )

                RetrofitClient.api
                    .uploadImage(body)

                    .enqueue(

                        object : Callback<ImageUploadResponse> {

                            override fun onResponse(

                                call: Call<ImageUploadResponse>,

                                response: Response<ImageUploadResponse>
                            ) {

                                if (response.isSuccessful) {

                                    newImageUrl =
                                        response.body()?.image_url ?: ""

                                    Toast.makeText(
                                        context,
                                        "Image uploaded",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(

                                call: Call<ImageUploadResponse>,

                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Upload failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
            }
        }
    val phone = prefs.getString("phone", "") ?: ""
    var editCuttingWidth by remember { mutableStateOf("") }
    var editWorkingWidth by remember { mutableStateOf("") }
    var editRowCount by remember { mutableStateOf("") }
    var machines by remember {
        mutableStateOf<List<Machine>>(emptyList())
    }
    var selectedMachine by remember {
        mutableStateOf<Machine?>(null)
    }
    var editModelName by remember { mutableStateOf("") }
    var editPricePerHour by remember { mutableStateOf("") }
    var editPricePerDay by remember { mutableStateOf("") }
    var editPricePerWeek by remember { mutableStateOf("") }
    var editPricePerMonth by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {

        RetrofitClient.api.getMyMachines(phone)

            .enqueue(object : Callback<MyMachinesResponse> {

                override fun onResponse(
                    call: Call<MyMachinesResponse>,
                    response: Response<MyMachinesResponse>
                ) {

                    if (response.isSuccessful) {

                        machines =
                            response.body()?.machines ?: emptyList()
                    }
                }

                override fun onFailure(
                    call: Call<MyMachinesResponse>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        context,
                        "Failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(

            text = "My Machines",

            style = MaterialTheme.typography.headlineLarge,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        if (machines.isEmpty()) {

            Surface(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),

                shape = RoundedCornerShape(24.dp),

                color = Color(0xFFF8FAFC),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE3EA)
                )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 36.dp
                        ),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(

                        imageVector = Icons.Default.Agriculture,

                        contentDescription = null,

                        tint = Color(0xFF90A4AE),

                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(

                        text = "No Machines Added",

                        fontSize = 22.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50),

                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(

                        text = "Machines you add for rental will appear here and can be managed anytime.",

                        color = Color.Gray,

                        textAlign = TextAlign.Center,

                        lineHeight = 22.sp
                    )
                }
            }

        } else {

            machines.forEach { machine ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)

                        .shadow(

                            elevation = 6.dp,

                            shape = RoundedCornerShape(24.dp)
                        )

                        .border(

                            1.2.dp,

                            Color(0xFFDCE3EA),

                            RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(

                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = machine.model_name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            machine.type
                                ?.replace("_", " ")
                                ?.replaceFirstChar {
                                    it.uppercase()
                                }
                                ?: ""
                        )

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            machine.hp_range?.let {

                                InfoChip("HP", "$it")
                            }

                            machine.cutting_width?.let {

                                InfoChip("Cut", "${it}ft")
                            }

                            machine.working_width?.let {

                                InfoChip("Width", "${it}m")
                            }

                            machine.row_count?.let {

                                InfoChip("Rows", "$it")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            PriceItem("Hr", machine.price_per_hour)

                            PriceItem("Day", machine.price_per_day)

                            PriceItem("Week", machine.price_per_week)

                            PriceItem("Month", machine.price_per_month)
                        }
                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            shape = RoundedCornerShape(18.dp),

                            border = BorderStroke(

                                1.dp,

                                Color(0xFFD0D7DE)
                            ),

                            colors = ButtonDefaults.outlinedButtonColors(

                                containerColor = Color(0xFFF8FAFC),

                                contentColor = Color(0xFF2C3E50)
                            ),
                            onClick = {
                                selectedMachine = machine

                                editModelName =
                                    machine.model_name ?: ""

                                editPricePerHour =
                                    machine.price_per_hour?.toString() ?: ""

                                editPricePerDay =
                                    machine.price_per_day?.toString() ?: ""
                                editCuttingWidth =
                                    machine.cutting_width?.toString() ?: ""

                                editWorkingWidth =
                                    machine.working_width?.toString() ?: ""

                                editRowCount =
                                    machine.row_count?.toString() ?: ""
                                editPricePerWeek =
                                    machine.price_per_week?.toString() ?: ""
                                editHpRange =
                                    machine.hp_range?.toString() ?: ""
                                editPricePerMonth =
                                    machine.price_per_month?.toString() ?: ""
                            }
                        ) {
                            Text("Edit")
                        }
                        Spacer(Modifier.height(12.dp))

                        var showDeleteDialog by remember {

                            mutableStateOf(false)
                        }
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            shape = RoundedCornerShape(18.dp),

                            border = BorderStroke(

                                1.dp,

                                Color(0xFFFFCDD2)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(

                                containerColor = Color(0xFFFFF5F5),

                                contentColor = Color(0xFFD32F2F)
                            ),
                            onClick = {

                                showDeleteDialog = true
                            }
                        ) {

                            Text("Delist")
                        }
                        if (showDeleteDialog) {

                            AlertDialog(
                                shape = RoundedCornerShape(28.dp),

                                containerColor = Color.White,
                                onDismissRequest = {

                                    showDeleteDialog = false
                                },

                                title = {

                                    Text("Delist Machine")
                                },

                                text = {

                                    Text(
                                        "Are you sure you want to delist this machine?"
                                    )
                                },
                                confirmButton = {

                                    OutlinedButton(
                                        shape = RoundedCornerShape(16.dp),

                                        border = BorderStroke(

                                            1.dp,

                                            Color(0xFFD0D7DE)
                                        ),

                                        colors = ButtonDefaults.outlinedButtonColors(

                                            containerColor = Color(0xFFF8FAFC),

                                            contentColor = Color(0xFF2C3E50)
                                        ),
                                        onClick = {

                                            RetrofitClient.api.
                                            deleteMachine(machine.id ?: 0)

                                                .enqueue(

                                                    object :
                                                        Callback<Map<String, String>> {

                                                        override fun onResponse(

                                                            call: Call<Map<String, String>>,

                                                            response: Response<Map<String, String>>
                                                        ) {

                                                            Toast.makeText(
                                                                context,
                                                                "Machine delisted",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            machines =
                                                                machines.filter {

                                                                    it.id != machine.id
                                                                }

                                                            showDeleteDialog = false
                                                        }

                                                        override fun onFailure(

                                                            call: Call<Map<String, String>>,

                                                            t: Throwable
                                                        ) {

                                                            Toast.makeText(
                                                                context,
                                                                "Failed",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                            showDeleteDialog = false
                                                        }
                                                    })
                                        }

                                    ) {

                                        Text("Yes")
                                    }
                                },
                                dismissButton = {

                                    OutlinedButton(
                                        shape = RoundedCornerShape(16.dp),

                                        border = BorderStroke(

                                            1.dp,

                                            Color(0xFFD0D7DE)
                                        ),

                                        colors = ButtonDefaults.outlinedButtonColors(

                                            containerColor = Color(0xFFF8FAFC),

                                            contentColor = Color(0xFF2C3E50)
                                        ),
                                        onClick = {

                                            showDeleteDialog = false
                                        }

                                    ) {

                                        Text("No")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    selectedMachine?.let { machine ->

        AlertDialog(
            shape = RoundedCornerShape(28.dp),

            containerColor = Color.White,
            onDismissRequest = {
                selectedMachine = null
            },

            confirmButton = {

                OutlinedButton(
                    shape = RoundedCornerShape(16.dp),

                    border = BorderStroke(

                        1.dp,

                        Color(0xFFD0D7DE)
                    ),

                    colors = ButtonDefaults.outlinedButtonColors(

                        containerColor = Color(0xFFF8FAFC),

                        contentColor = Color(0xFF2C3E50)
                    ),
                    onClick = {
                        val request = AddMachineRequest(
                            type = machine.type ?: "",
                            model_name = editModelName,
                            hp_range = editHpRange.toIntOrNull(),
                            cutting_width = editCuttingWidth.toDoubleOrNull(),
                            working_width = editWorkingWidth.toDoubleOrNull(),
                            row_count = editRowCount.toIntOrNull(),
                            price_per_hour = editPricePerHour.toIntOrNull(),
                            price_per_day = editPricePerDay.toIntOrNull(),
                            price_per_week = editPricePerWeek.toIntOrNull(),
                            price_per_month = editPricePerMonth.toIntOrNull(),
                            image_url =
                                if (newImageUrl.isNotBlank())
                                    newImageUrl
                                else
                                    machine.image_url ?: "",
                            owner_name = "",
                            owner_phone = "",
                            owner_email = "",
                            location = machine.location ?: ""
                        )

                        RetrofitClient.api.updateMachine(
                            machine.id ?: 0,
                            request
                        ).enqueue(object : Callback<Map<String, String>> {

                            override fun onResponse(
                                call: Call<Map<String, String>>,
                                response: Response<Map<String, String>>
                            ) {

                                Toast.makeText(
                                    context,
                                    "Updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                machines = machines.map {

                                    if (it.id == machine.id) {
                                        it.copy(
                                            model_name = editModelName,
                                            hp_range = editHpRange.toIntOrNull(),
                                            cutting_width = editCuttingWidth.toDoubleOrNull(),
                                            working_width = editWorkingWidth.toDoubleOrNull(),
                                            row_count = editRowCount.toIntOrNull(),
                                            price_per_hour = editPricePerHour.toIntOrNull(),
                                            price_per_day = editPricePerDay.toIntOrNull(),
                                            price_per_week = editPricePerWeek.toIntOrNull(),
                                            price_per_month = editPricePerMonth.toIntOrNull(),
                                            image_url =
                                                if (newImageUrl.isNotBlank())
                                                    newImageUrl
                                                else
                                                    it.image_url
                                        )

                                    } else it
                                }
                                selectedMachine = null
                            }

                            override fun onFailure(
                                call: Call<Map<String, String>>,
                                t: Throwable
                            ) {

                                Toast.makeText(
                                    context,
                                    "Failed: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        )
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {

                OutlinedButton(
                    shape = RoundedCornerShape(16.dp),

                    border = BorderStroke(

                        1.dp,

                        Color(0xFFD0D7DE)
                    ),

                    colors = ButtonDefaults.outlinedButtonColors(

                        containerColor = Color(0xFFF8FAFC),

                        contentColor = Color(0xFF2C3E50)
                    ),
                    onClick = {
                        selectedMachine = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            title = {
                Text(

                    "Edit Machine",

                    style = MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color = Color(0xFF2C3E50)
                )
            },
            text = {

                Column(

                    modifier = Modifier
                        .verticalScroll(
                            rememberScrollState()
                        )

                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editModelName,

                        onValueChange = {
                            editModelName = it
                        },
                        label = {
                            Text("Model Name")
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    when (machine.type) {

                        "tractor" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),

                                shape = RoundedCornerShape(18.dp),

                                singleLine = true,

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedBorderColor = Color(0xFF34495E),

                                    unfocusedBorderColor = Color(0xFFD0D7DE),

                                    focusedLabelColor = Color(0xFF34495E),

                                    cursorColor = Color(0xFF34495E),

                                    focusedContainerColor = Color(0xFFF8FAFC),

                                    unfocusedContainerColor = Color(0xFFF8FAFC),

                                    focusedTextColor = Color(0xFF2C3E50),

                                    unfocusedTextColor = Color(0xFF2C3E50)
                                ),
                                value = editHpRange,

                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() }) {
                                        editHpRange = it
                                    }
                                },
                                label = {
                                    Text("HP")
                                }
                            )
                        }
                        "harvester" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),

                                shape = RoundedCornerShape(18.dp),

                                singleLine = true,

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedBorderColor = Color(0xFF34495E),

                                    unfocusedBorderColor = Color(0xFFD0D7DE),

                                    focusedLabelColor = Color(0xFF34495E),

                                    cursorColor = Color(0xFF34495E),

                                    focusedContainerColor = Color(0xFFF8FAFC),

                                    unfocusedContainerColor = Color(0xFFF8FAFC),

                                    focusedTextColor = Color(0xFF2C3E50),

                                    unfocusedTextColor = Color(0xFF2C3E50)
                                ),
                                value = editCuttingWidth,

                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() }) {
                                        editCuttingWidth = it
                                    }
                                },

                                label = {
                                    Text("Cutting Width")
                                }
                            )
                        }
                        "rotavator" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),

                                shape = RoundedCornerShape(18.dp),

                                singleLine = true,

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedBorderColor = Color(0xFF34495E),

                                    unfocusedBorderColor = Color(0xFFD0D7DE),

                                    focusedLabelColor = Color(0xFF34495E),

                                    cursorColor = Color(0xFF34495E),

                                    focusedContainerColor = Color(0xFFF8FAFC),

                                    unfocusedContainerColor = Color(0xFFF8FAFC),

                                    focusedTextColor = Color(0xFF2C3E50),

                                    unfocusedTextColor = Color(0xFF2C3E50)
                                ),
                                value = editWorkingWidth,

                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() }) {
                                        editWorkingWidth = it
                                    }
                                },

                                label = {
                                    Text("Working Width")
                                }
                            )
                        }
                        "seed drill" -> {

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),

                                shape = RoundedCornerShape(18.dp),

                                singleLine = true,

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedBorderColor = Color(0xFF34495E),

                                    unfocusedBorderColor = Color(0xFFD0D7DE),

                                    focusedLabelColor = Color(0xFF34495E),

                                    cursorColor = Color(0xFF34495E),

                                    focusedContainerColor = Color(0xFFF8FAFC),

                                    unfocusedContainerColor = Color(0xFFF8FAFC),

                                    focusedTextColor = Color(0xFF2C3E50),

                                    unfocusedTextColor = Color(0xFF2C3E50)
                                ),
                                value = editRowCount,

                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() }) {
                                        editRowCount = it
                                    }
                                },

                                label = {
                                    Text("Row Count")
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editPricePerHour,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                editPricePerHour = it
                            }
                        },
                        label = {
                            Text("Price Per Hour")
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editPricePerDay,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                editPricePerDay = it
                            }
                        },
                        label = {
                            Text("Price Per Day")
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editPricePerWeek,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                editPricePerWeek = it
                            }
                        },
                        label = {
                            Text("Price Per Week")
                        }
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(18.dp),

                        singleLine = true,

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedBorderColor = Color(0xFF34495E),

                            unfocusedBorderColor = Color(0xFFD0D7DE),

                            focusedLabelColor = Color(0xFF34495E),

                            cursorColor = Color(0xFF34495E),

                            focusedContainerColor = Color(0xFFF8FAFC),

                            unfocusedContainerColor = Color(0xFFF8FAFC),

                            focusedTextColor = Color(0xFF2C3E50),

                            unfocusedTextColor = Color(0xFF2C3E50)
                        ),
                        value = editPricePerMonth,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                editPricePerMonth = it
                            }
                        },
                        label = {
                            Text("Price Per Month")
                        }
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(

                        text = "Current Image",

                        style = MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.SemiBold,

                        color = Color(0xFF2C3E50)
                    )

                    Spacer(Modifier.height(8.dp))

                    if (
                        machine.image_url.isNullOrBlank()
                        && newImageUrl.isBlank()
                    ) {

                        Text(
                            text = "You have not provided any image.",
                            color = Color.Gray
                        )
                    } else {

                        AsyncImage(

                            model =

                                if (newImageUrl.isNotBlank())

                                    AppConfig.BASE_URL + newImageUrl.removePrefix("/")

                                else

                                    AppConfig.BASE_URL + machine.image_url?.removePrefix("/"),

                            contentDescription = null,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    1.dp,
                                    Color(0xFFDCE3EA),
                                    RoundedCornerShape(22.dp)
                                ),

                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),

                        shape = RoundedCornerShape(18.dp),

                        border = BorderStroke(

                            1.dp,

                            Color(0xFFD0D7DE)
                        ),

                        colors = ButtonDefaults.outlinedButtonColors(

                            containerColor = Color(0xFFF8FAFC),

                            contentColor = Color(0xFF2C3E50)
                        ),
                        onClick = {

                            imagePickerLauncher.launch("image/*")
                        }

                    ) {
                        Text("Upload New Image")
                    }
                }
            }
        )

    }
}
@Composable
fun NotificationsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    var selectedMyRequest by remember {
        mutableStateOf<MyRequest?>(null)
    }
    var showMyRequests by remember {

        mutableStateOf(false)
    }
    val phone =
        prefs.getString("phone", "") ?: ""
    var notifications by remember {
        mutableStateOf<List<MachineRequest>>(emptyList())
    }
    var myRequests by remember {
        mutableStateOf<List<MyRequest>>(emptyList())
    }
    LaunchedEffect(Unit) {

        RetrofitClient.api.getNotifications(phone)

            .enqueue(object : Callback<List<MachineRequest>> {

                override fun onResponse(
                    call: Call<List<MachineRequest>>,
                    response: Response<List<MachineRequest>>
                ) {

                    if (response.isSuccessful) {

                        notifications =
                            (response.body() ?: emptyList())
                                .reversed()
                    }
                }
                override fun onFailure(
                    call: Call<List<MachineRequest>>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        context,
                        "Failed to load notifications",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            )

        RetrofitClient.api.getMyRequests(phone)

            .enqueue(object : Callback<List<MyRequest>> {

                override fun onResponse(
                    call: Call<List<MyRequest>>,
                    response: Response<List<MyRequest>>
                ) {

                    if (response.isSuccessful) {

                        myRequests =
                            (response.body() ?: emptyList())
                        Log.e(
                            "MY_REQUESTS_DEBUG",
                            "Count = ${myRequests.size}"
                        )

                    }
                }

                override fun onFailure(
                    call: Call<List<MyRequest>>,
                    t: Throwable
                ) {

                }
            }
            )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(Modifier.height(8.dp))

        Text(

            text = "Notifications",

            style = MaterialTheme.typography.headlineLarge,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(

            onClick = {

                showMyRequests = !showMyRequests
            },

            shape = RoundedCornerShape(18.dp),

            border = BorderStroke(
                1.dp,
                Color(0xFFD0D7DE)
            ),

            colors = ButtonDefaults.outlinedButtonColors(

                containerColor = Color(0xFFF8FAFC),

                contentColor = Color(0xFF2C3E50)
            )
        ) {

            Text(

                if (showMyRequests)
                    "Back to Incoming Requests"
                else
                    "Your Requests",

                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))

        var selectedRequest by remember {
            mutableStateOf<MachineRequest?>(null)
        }

        selectedRequest?.let { request ->

            AlertDialog(
                shape = RoundedCornerShape(28.dp),

                containerColor = Color.White,
                onDismissRequest = {
                    selectedRequest = null
                },

                confirmButton = { },

                title = {

                    Text(

                        "Requester Details",

                        style = MaterialTheme.typography.titleLarge,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )
                },

                text = {

                    Column {

                        OwnerInfoRow(
                            label = "Name",
                            value = request.requesterName
                        )

                        OwnerInfoRow(
                            label = "Phone",
                            value = request.requesterPhone
                        )

                        OwnerInfoRow(
                            label = "Email",
                            value = request.requesterEmail
                        )

                        OwnerInfoRow(
                            label = "Location",
                            value = request.requesterLocation
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.Center
                        ) {

                            OutlinedButton(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                ),
                                onClick = {

                                    val intent = Intent(
                                        Intent.ACTION_DIAL
                                    )

                                    intent.data =
                                        Uri.parse(
                                            "tel:${request.requesterPhone}"
                                        )

                                    context.startActivity(intent)
                                }

                            ) {

                                Text("Call")
                            }
                            Spacer(Modifier.width(14.dp))
                            OutlinedButton(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                ),
                                onClick = {

                                    val intent = Intent(Intent.ACTION_SEND)

                                    intent.type = "message/rfc822"

                                    intent.putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(request.requesterEmail)
                                    )

                                    intent.putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Regarding Machine Request"
                                    )

                                    intent.putExtra(
                                        Intent.EXTRA_TEXT,

                                        "Hello ${request.requesterName},\n\n" +

                                                "I am contacting you regarding the machine request for ${request.machineName}.\n\n" +

                                                "Thank you."
                                    )

                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            "Send Email"
                                        )
                                    )
                                }

                            ) {
                                Text("Mail")
                            }
                        }
                    }
                }
            )
        }
        selectedMyRequest?.let { request ->

            AlertDialog(
                shape = RoundedCornerShape(28.dp),

                containerColor = Color.White,
                onDismissRequest = {
                    selectedMyRequest = null
                },

                confirmButton = { },

                title = {

                    Text(

                        "Owner Details",

                        style = MaterialTheme.typography.titleLarge,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )
                },

                text = {

                    Column {

                        OwnerInfoRow(
                            label = "Name",
                            value = request.ownerName
                        )

                        OwnerInfoRow(
                            label = "Phone",
                            value = request.ownerPhone
                        )

                        OwnerInfoRow(
                            label = "Email",
                            value = request.ownerEmail
                        )

                        OwnerInfoRow(
                            label = "Location",
                            value = request.location
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.Center
                        ) {

                            OutlinedButton(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                ),
                                onClick = {

                                    val intent = Intent(
                                        Intent.ACTION_DIAL
                                    )

                                    intent.data =
                                        Uri.parse(
                                            "tel:${request.ownerPhone}"
                                        )

                                    context.startActivity(intent)
                                }

                            ) {

                                Text("Call")
                            }
                            Spacer(Modifier.width(14.dp))
                            OutlinedButton(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                ),
                                onClick = {

                                    val intent = Intent(Intent.ACTION_SEND)

                                    intent.type = "message/rfc822"

                                    intent.putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(request.ownerEmail)
                                    )

                                    intent.putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Regarding Machine Request"
                                    )

                                    intent.putExtra(
                                        Intent.EXTRA_TEXT,

                                        "Hello ${request.ownerName},\n\n" +

                                                "I am contacting you regarding the machine request for ${request.machineName}.\n\n" +

                                                "Thank you."
                                    )
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            "Send Email"
                                        )
                                    )
                                }

                            ) {
                                Text("Mail")
                            }
                        }
                    }
                }
            )
        }
        if (!showMyRequests) {

            if (notifications.isEmpty()) {

                Surface(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),

                    shape = RoundedCornerShape(24.dp),

                    color = Color(0xFFF8FAFC),

                    border = BorderStroke(
                        1.dp,
                        Color(0xFFDCE3EA)
                    )
                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 36.dp
                            ),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(

                            imageVector = Icons.Default.NotificationsNone,

                            contentDescription = null,

                            tint = Color(0xFF90A4AE),

                            modifier = Modifier.size(42.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(

                            text = "No Incoming Requests",

                            fontSize = 22.sp,

                            fontWeight = FontWeight.Bold,

                            textAlign = TextAlign.Center,

                            color = Color(0xFF2C3E50)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(

                            text = "Rental requests from interested users will appear here.",

                            color = Color.Gray,

                            textAlign = TextAlign.Center,

                            lineHeight = 22.sp
                        )
                    }
                }

            } else {

                LazyColumn {

                    items(notifications) { request ->

                        var selectedDecision by remember {

                            mutableStateOf("")
                        }
                        var locked by remember {

                            mutableStateOf(false)
                        }
                        var countdown by remember {

                            mutableStateOf(10)
                        }
                        var timerStarted by remember {

                            mutableStateOf(false)
                        }
                        LaunchedEffect(timerStarted) {

                            if (timerStarted) {

                                while (countdown > 0) {

                                    delay(1000)

                                    countdown--
                                }

                                locked = true

                                if (selectedDecision.isNotEmpty()) {

                                    val body =
                                        UpdateStatusBody(
                                            status = selectedDecision
                                        )

                                    RetrofitClient.api
                                        .updateRequestStatus(
                                            request.id,
                                            body
                                        )

                                        .enqueue(
                                            object :
                                                Callback<Map<String, String>> {

                                                override fun onResponse(
                                                    call: Call<Map<String, String>>,
                                                    response: Response<Map<String, String>>
                                                ) {

                                                    Toast.makeText(
                                                        context,
                                                        "Final decision saved",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    notifications = notifications.map {

                                                        if (it.id == request.id)
                                                            it.copy(status = selectedDecision)
                                                        else it
                                                    }

                                                    myRequests = myRequests.map {

                                                        if (it.id == request.id)
                                                            it.copy(status = selectedDecision)
                                                        else it
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<Map<String, String>>,
                                                    t: Throwable
                                                ) {

                                                    Toast.makeText(
                                                        context,
                                                        "Failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                }
                            }
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable {
                                    selectedRequest = request
                                }
                                .shadow(
                                    6.dp,
                                    RoundedCornerShape(24.dp)
                                )

                                .border(
                                    1.2.dp,
                                    Color(0xFFDCE3EA),
                                    RoundedCornerShape(24.dp)
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {

                                val machineDelisted =
                                    request.machineName.isBlank()

                                if (machineDelisted) {

                                    Text(

                                        text = "Machine Delisted",

                                        style = MaterialTheme.typography.titleMedium,

                                        fontWeight = FontWeight.Bold,

                                        color = Color(0xFFC62828)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(

                                        text = "You have delisted this machine. It is no longer available for receiving rental requests and its pricing information is unavailable.",

                                        color = Color.Gray,

                                        lineHeight = 20.sp
                                    )

                                } else {

                                    Text(

                                        text = request.machineName,

                                        style = MaterialTheme.typography.titleMedium,

                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = "Requester: ${request.requesterName}",
                                        color = Color(0xFF2C3E50),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    val specText = when (request.type.lowercase()) {

                                        "tractor" ->
                                            "Tractor • ${request.hpRange ?: "-"} HP"

                                        "harvester" ->
                                            "Harvester • ${request.cuttingWidth ?: "-"} ft Cutting Width"

                                        "rotavator" ->
                                            "Rotavator • ${request.workingWidth ?: "-"} ft Working Width"

                                        "seed drill" ->
                                            "Seed Drill • ${request.rowCount ?: "-"} Rows"

                                        else -> ""
                                    }

                                    Text(
                                        text = specText,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Spacer(Modifier.height(8.dp))

                                    Row(

                                        modifier = Modifier.fillMaxWidth(),

                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        PriceItem("Hr", request.pricePerHour)

                                        PriceItem("Day", request.pricePerDay)

                                        PriceItem("Week", request.pricePerWeek)

                                        PriceItem("Month", request.pricePerMonth)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                val statusBackground = when (request.status.lowercase()) {

                                    "accepted" -> Color(0xFFE8F5E9)

                                    "rejected" -> Color(0xFFFFEBEE)

                                    else -> Color(0xFFFFF8E1)
                                }

                                val statusTextColor = when (request.status.lowercase()) {

                                    "accepted" -> Color(0xFF2E7D32)

                                    "rejected" -> Color(0xFFC62828)

                                    else -> Color(0xFFF9A825)
                                }

                                Box(

                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(statusBackground)
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                ) {

                                    Text(

                                        text = request.status.uppercase(),

                                        color = statusTextColor,

                                        fontSize = 12.sp,

                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                if (!machineDelisted) {

                                    Row(
                                        horizontalArrangement =
                                            Arrangement.spacedBy(12.dp)
                                    ) {

                                        OutlinedButton(
                                            modifier = Modifier.height(50.dp),

                                            shape = RoundedCornerShape(16.dp),

                                            border = BorderStroke(

                                                1.dp,

                                                if (selectedDecision == "accepted")
                                                    Color(0xFF81C784)
                                                else
                                                    Color(0xFFD0D7DE)
                                            ),

                                            colors = ButtonDefaults.outlinedButtonColors(

                                                containerColor =
                                                    if (selectedDecision == "accepted")
                                                        Color(0xFFE8F5E9)
                                                    else
                                                        Color(0xFFF8FAFC),

                                                contentColor = Color(0xFF2C3E50)
                                            ),
                                            enabled =

                                                !locked &&

                                                        request.status == "pending" &&

                                                        selectedDecision != "accepted",

                                            onClick = {

                                                if (!timerStarted) {

                                                    timerStarted = true
                                                }

                                                selectedDecision = "accepted"
                                            }

                                        ) {

                                            Text(

                                                "Accept",

                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        OutlinedButton(
                                            modifier = Modifier.height(50.dp),

                                            shape = RoundedCornerShape(16.dp),

                                            border = BorderStroke(

                                                1.dp,

                                                if (selectedDecision == "rejected")
                                                    Color(0xFFEF9A9A)
                                                else
                                                    Color(0xFFD0D7DE)
                                            ),

                                            colors = ButtonDefaults.outlinedButtonColors(

                                                containerColor =
                                                    if (selectedDecision == "rejected")
                                                        Color(0xFFFFEBEE)
                                                    else
                                                        Color(0xFFF8FAFC),

                                                contentColor = Color(0xFF2C3E50)
                                            ),
                                            enabled =

                                                !locked &&

                                                        request.status == "pending" &&

                                                        selectedDecision != "rejected",

                                            onClick = {

                                                if (!timerStarted) {

                                                    timerStarted = true
                                                }

                                                selectedDecision = "rejected"
                                            }

                                        ) {

                                            Text(

                                                "Reject",

                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Spacer(Modifier.height(12.dp))

                                    if (!machineDelisted && timerStarted && !locked) {

                                        Text(

                                            text =
                                                "⏳ You have ${countdown}s left to finalize your decision",

                                            color =
                                                if (countdown <= 5)
                                                    Color.Red
                                                else
                                                    Color.Gray,

                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    if (!machineDelisted && locked) {

                                        Text(

                                            text =
                                                "⌛ Time exhausted. Decision finalized.",

                                            color = Color.Red,

                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    if (machineDelisted) {

                                        Text(

                                            text = "This request can no longer be processed because the machine has been delisted.",

                                            color = Color.Gray,

                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(Modifier.height(8.dp))
                                    }

                                }
                                Text(
                                    text = "Tap to view requester details",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
            if (showMyRequests) {

                if (myRequests.isEmpty()) {

                    Surface(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        shape = RoundedCornerShape(24.dp),

                        color = Color(0xFFF8FAFC),

                        border = BorderStroke(
                            1.dp,
                            Color(0xFFDCE3EA)
                        )
                    ) {

                        Column(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = 36.dp
                                ),

                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(

                                imageVector = Icons.Outlined.Send,

                                contentDescription = null,

                                tint = Color(0xFF90A4AE),

                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(

                                text = "No Requests Sent",

                                fontSize = 22.sp,

                                fontWeight = FontWeight.Bold,

                                color = Color(0xFF2C3E50)
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(

                                text = "Machines you request for rental will be tracked here.",

                                color = Color.Gray,

                                textAlign = TextAlign.Center,

                                lineHeight = 22.sp
                            )
                        }
                    }

                } else {

                    LazyColumn {

                        items(myRequests) { request ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)

                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(24.dp)
                                    )

                                    .border(
                                        1.2.dp,
                                        Color(0xFFDCE3EA),
                                        RoundedCornerShape(24.dp)
                                    )

                                    .clickable {
                                        selectedMyRequest = request
                                    },

                                shape = RoundedCornerShape(24.dp),

                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    val machineDelisted =
                                        request.machineName.isBlank()
                                    if (machineDelisted) {

                                        Text(

                                            text = "Machine Delisted",

                                            style = MaterialTheme.typography.titleMedium,

                                            fontWeight = FontWeight.Bold,

                                            color = Color(0xFFC62828)
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Text(

                                            text = "This machine has been delisted by the owner. Machine specifications and pricing information are no longer available.",

                                            color = Color.Gray,

                                            lineHeight = 20.sp
                                        )

                                        Spacer(Modifier.height(8.dp))

                                    } else {

                                        Text(

                                            text = request.machineName,

                                            style = MaterialTheme.typography.titleMedium,

                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(Modifier.height(8.dp))
                                    }

                                    val statusBackground = when (request.status.lowercase()) {

                                        "accepted" -> Color(0xFFE8F5E9)

                                        "rejected" -> Color(0xFFFFEBEE)

                                        else -> Color(0xFFFFF8E1)
                                    }

                                    val statusTextColor = when (request.status.lowercase()) {

                                        "accepted" -> Color(0xFF2E7D32)

                                        "rejected" -> Color(0xFFC62828)

                                        else -> Color(0xFFF9A825)
                                    }

                                    Box(

                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(statusBackground)
                                            .padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            )
                                    ) {

                                        Text(

                                            text = request.status.uppercase(),

                                            color = statusTextColor,

                                            fontSize = 12.sp,

                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Owner: ${request.ownerName}",
                                        color = Color(0xFF2C3E50),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    val specText = when (request.type.lowercase()) {

                                        "tractor" ->
                                            "Tractor • ${request.hpRange ?: "-"} HP"

                                        "harvester" ->
                                            "Harvester • ${request.cuttingWidth ?: "-"} ft Cutting Width"

                                        "rotavator" ->
                                            "Rotavator • ${request.workingWidth ?: "-"} ft Working Width"

                                        "seed drill" ->
                                            "Seed Drill • ${request.rowCount ?: "-"} Rows"

                                        else -> ""
                                    }

                                    Text(
                                        text = specText,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Spacer(Modifier.height(8.dp))

                                    Row(

                                        modifier = Modifier.fillMaxWidth(),

                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        PriceItem("Hr", request.pricePerHour)

                                        PriceItem("Day", request.pricePerDay)

                                        PriceItem("Week", request.pricePerWeek)

                                        PriceItem("Month", request.pricePerMonth)
                                    }


                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text =
                                            "Tap to view owner details",
                                        color = Color.Gray,
                                        style =
                                            MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}
@Composable
fun RentedMachinesScreen() {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    val phone =
        prefs.getString("phone", "") ?: ""
    val requesterName =
        prefs.getString("name", "")
            ?: ""
    val requesterEmail =
        prefs.getString("email", "")
            ?: ""
    val requesterLocation =
        prefs.getString("location", "")
            ?: ""
    var rentedMachines by remember {

        mutableStateOf<List<MyRequest>>(emptyList())
    }
    LaunchedEffect(Unit) {

        RetrofitClient.api
            .getMyRequests(phone)

            .enqueue(
                object : Callback<List<MyRequest>> {

                    override fun onResponse(
                        call: Call<List<MyRequest>>,
                        response: Response<List<MyRequest>>
                    ) {

                        if (response.isSuccessful) {

                            rentedMachines =

                                response.body()

                                    ?.filter {

                                        it.status == "accepted"
                                    }
                                    ?: emptyList()
                        }
                    }

                    override fun onFailure(
                        call: Call<List<MyRequest>>,
                        t: Throwable
                    ) {

                        Toast.makeText(
                            context,
                            "Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(Modifier.height(18.dp))

        Text(

            text = "Rented Machines",

            style = MaterialTheme.typography.headlineLarge,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))

        var showDelistedDialog by remember {

            mutableStateOf(false)
        }
        if (rentedMachines.isEmpty()) {

            Surface(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),

                shape = RoundedCornerShape(24.dp),

                color = Color(0xFFF8FAFC),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE3EA)
                )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 36.dp
                        ),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(

                        imageVector = Icons.Outlined.History,

                        contentDescription = null,

                        tint = Color(0xFF90A4AE),

                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(

                        text = "No Rentals Yet",

                        fontSize = 22.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(

                        text = "Machines you rent will appear here for quick access and repeat bookings.",

                        color = Color.Gray,

                        textAlign = TextAlign.Center,

                        lineHeight = 22.sp
                    )
                }
            }

        } else {
            LazyColumn {

                items(rentedMachines) { machine ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)

                            .shadow(

                                elevation = 6.dp,

                                shape = RoundedCornerShape(24.dp)
                            )

                            .border(

                                1.2.dp,

                                Color(0xFFDCE3EA),

                                RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(

                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(20.dp)
                        ) {
                            val machineDelisted =

                                machine.machineName.isBlank()
                            if (machineDelisted) {

                                Text(

                                    text = "Machine Delisted",

                                    style = MaterialTheme.typography.titleMedium,

                                    fontWeight = FontWeight.Bold,

                                    color = Color(0xFFC62828)
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(

                                    text = "This machine has been delisted by the owner. Machine specifications and pricing information are no longer available.",

                                    color = Color.Gray,

                                    lineHeight = 20.sp
                                )

                            } else {

                                Text(
                                    text = machine.machineName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Owner: ${machine.ownerName}",
                                    color = Color(0xFF2C3E50),
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = "Location: ${machine.location}",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                val specText = when (machine.type.lowercase()) {

                                    "tractor" ->
                                        "Tractor • ${machine.hpRange ?: "-"} HP"

                                    "harvester" ->
                                        "Harvester • ${machine.cuttingWidth ?: "-"} ft Cutting Width"

                                    "rotavator" ->
                                        "Rotavator • ${machine.workingWidth ?: "-"} ft Working Width"

                                    "seed drill" ->
                                        "Seed Drill • ${machine.rowCount ?: "-"} Rows"

                                    else -> ""
                                }

                                Text(
                                    text = specText,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(Modifier.height(16.dp))

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    PriceItem("Hr", machine.pricePerHour)

                                    PriceItem("Day", machine.pricePerDay)

                                    PriceItem("Week", machine.pricePerWeek)

                                    PriceItem("Month", machine.pricePerMonth)
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            Box(

                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(

                                        horizontal = 10.dp,

                                        vertical = 4.dp
                                    )
                            ) {

                                Text(

                                    text = "Rental Confirmed",

                                    color = Color(0xFF2E7D32),

                                    fontSize = 12.sp,

                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(12.dp))

                            OutlinedButton(

                                onClick = {

                                    val body = RequestMachineBody(

                                        machine_id = machine.machineId,

                                        requester_name = requesterName,

                                        requester_phone = phone,

                                        requester_email = requesterEmail,

                                        requester_location = requesterLocation,

                                        owner_phone = machine.ownerPhone
                                    )

                                    RetrofitClient.api
                                        .requestMachine(body)

                                        .enqueue(

                                            object :
                                                Callback<Map<String, String>> {

                                                override fun onResponse(

                                                    call: Call<Map<String, String>>,

                                                    response: Response<Map<String, String>>
                                                ) {

                                                    val msg =

                                                        response.body()?.get("message")
                                                            ?: ""

                                                    if (msg == "Machine delisted") {

                                                        showDelistedDialog = true

                                                    } else {

                                                        Toast.makeText(
                                                            context,
                                                            "🔁 Request sent again",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onFailure(

                                                    call: Call<Map<String, String>>,

                                                    t: Throwable
                                                ) {

                                                    Toast.makeText(
                                                        context,
                                                        "Failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),

                                shape = RoundedCornerShape(18.dp),

                                border = BorderStroke(

                                    1.dp,

                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                )
                            ) {

                                Text(

                                    "Order Again",

                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (showDelistedDialog) {

                                AlertDialog(
                                    shape = RoundedCornerShape(28.dp),

                                    containerColor = Color.White,
                                    onDismissRequest = {

                                        showDelistedDialog = false
                                    },

                                    confirmButton = {

                                        OutlinedButton(
                                            shape = RoundedCornerShape(16.dp),

                                            border = BorderStroke(

                                                1.dp,

                                                Color(0xFFD0D7DE)
                                            ),

                                            colors = ButtonDefaults.outlinedButtonColors(

                                                containerColor = Color(0xFFF8FAFC),

                                                contentColor = Color(0xFF2C3E50)
                                            ),
                                            onClick = {

                                                showDelistedDialog = false
                                            }

                                        ) {

                                            Text(

                                                "OK",

                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },

                                    title = {

                                        Text(

                                            "Machine Unavailable",

                                            style = MaterialTheme.typography.titleLarge,

                                            fontWeight = FontWeight.Bold,

                                            color = Color(0xFF2C3E50)
                                        )
                                    },

                                    text = {

                                        Text(

                                            "This machine is no longer available for rental requests.",

                                            color = Color.Gray,

                                            lineHeight = 22.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun RecentlyViewedScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(
        "RECENT",
        Context.MODE_PRIVATE
    )
    val prefsUser = context.getSharedPreferences(
        "USER",
        Context.MODE_PRIVATE
    )
    var imageDialogUrl by remember {

        mutableStateOf<String?>(null)
    }
    val phone =
        prefsUser.getString("phone", "")
            ?: ""
    val requesterName =
        prefsUser.getString("name", "")
            ?: ""
    val requesterEmail =
        prefsUser.getString("email", "")
            ?: ""
    val requesterLocation =
        prefsUser.getString("location", "")
            ?: ""
    val machines =
        prefs.getStringSet(
            "machines_$phone",
            emptySet()
        )?.toList()

            ?: emptyList()
    var selectedMachine by remember {
        mutableStateOf<List<String>?>(null)
    }
    var showNoImageDialog by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        Text(

            text = "Recently Viewed",

            style = MaterialTheme.typography.headlineLarge,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2C3E50)
        )

        Spacer(Modifier.height(24.dp))
        if (machines.isEmpty()) {

            Surface(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),

                shape = RoundedCornerShape(24.dp),

                color = Color(0xFFF8FAFC),

                border = BorderStroke(
                    1.dp,
                    Color(0xFFDCE3EA)
                )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 36.dp
                        ),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(

                        imageVector = Icons.Outlined.History,

                        contentDescription = null,

                        tint = Color(0xFF90A4AE),

                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(

                        text = "No Activity Yet",

                        fontSize = 22.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(

                        text = "Machines you explore will be saved here for quick access later.",

                        color = Color.Gray,

                        textAlign = TextAlign.Center,

                        lineHeight = 22.sp
                    )
                }
            }

        } else {
            LazyColumn {

                items(machines.reversed()) { item ->

                    val parts = item.split("|")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)

                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp)
                            )

                            .border(
                                1.2.dp,
                                Color(0xFFDCE3EA),
                                RoundedCornerShape(24.dp)
                            )

                            .clickable {
                                selectedMachine = parts
                            },

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = parts.getOrNull(1) ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Owner: ${parts.getOrNull(2) ?: ""}",
                                color = Color(0xFF2C3E50),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))

                            val specText = when (parts.getOrNull(11)?.lowercase()) {

                                "tractor" ->
                                    "Tractor • ${parts.getOrNull(12) ?: "-"} HP"

                                "harvester" ->
                                    "Harvester • ${parts.getOrNull(13) ?: "-"} ft Cutting Width"

                                "rotavator" ->
                                    "Rotavator • ${parts.getOrNull(14) ?: "-"} ft Working Width"

                                "seed drill" ->
                                    "Seed Drill • ${parts.getOrNull(15) ?: "-"} Rows"

                                else -> ""
                            }

                            Text(
                                text = specText,
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                PriceItem("Hr", parts.getOrNull(6)?.toIntOrNull())

                                PriceItem("Day", parts.getOrNull(7)?.toIntOrNull())

                                PriceItem("Week", parts.getOrNull(8)?.toIntOrNull())

                                PriceItem("Month", parts.getOrNull(9)?.toIntOrNull())
                            }
                            Spacer(Modifier.height(12.dp))

                            OutlinedButton(

                                onClick = {

                                    val imagePath = parts.getOrNull(10)?.trim() ?: ""

                                    if (

                                        imagePath.isBlank() ||

                                        imagePath.equals("null", true) ||

                                        imagePath.equals("none", true)

                                    ) {

                                        showNoImageDialog = true

                                    } else {

                                        imageDialogUrl =
                                            AppConfig.BASE_URL + imagePath.removePrefix("/")
                                    }
                                },

                                modifier = Modifier.height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                )
                            ) {
                                Text("View Image")
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(

                                text =
                                    "Tap to view owner details",

                                color = Color.Gray,

                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        imageDialogUrl?.let { url ->

            AlertDialog(
                shape = RoundedCornerShape(28.dp),

                containerColor = Color.White,
                onDismissRequest = {

                    imageDialogUrl = null
                },

                confirmButton = {

                    OutlinedButton(

                        onClick = {

                            imageDialogUrl = null
                        },

                        modifier = Modifier
                            .height(50.dp),

                        shape = RoundedCornerShape(16.dp),

                        border = BorderStroke(
                            1.dp,
                            Color(0xFFD0D7DE)
                        ),

                        colors = ButtonDefaults.outlinedButtonColors(

                            containerColor = Color(0xFFF8FAFC),

                            contentColor = Color(0xFF2C3E50)
                        )
                    ) {

                        Text(
                            "Close",
                            fontWeight = FontWeight.Medium
                        )
                    }

                },
                title = {
                    Text(
                        "Machine Preview",

                        style = MaterialTheme.typography.titleLarge,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )
                },
                text = {
                    AsyncImage(

                        model = url,

                        contentDescription = null,

                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))

                            .border(

                                1.dp,

                                Color(0xFFDCE3EA),

                                RoundedCornerShape(22.dp)
                            )
                            .fillMaxWidth()
                            .height(250.dp),


                        contentScale = ContentScale.Crop
                    )
                }
            )
        }
        if (showNoImageDialog) {

            AlertDialog(

                onDismissRequest = {

                    showNoImageDialog = false
                },

                shape = RoundedCornerShape(28.dp),

                containerColor = Color.White,

                confirmButton = {

                    OutlinedButton(

                        onClick = {

                            showNoImageDialog = false
                        }

                    ) {

                        Text("OK")
                    }
                },

                title = {

                    Text("Image Unavailable")
                },

                text = {

                    Text(
                        "The owner has not uploaded an image for this machine."
                    )
                }
            )
        }
        var showDelistedDialog by remember {

            mutableStateOf(false)
        }
        selectedMachine?.let { machine ->

            AlertDialog(
                shape = RoundedCornerShape(28.dp),

                containerColor = Color.White,
                onDismissRequest = {

                    selectedMachine = null
                },

                confirmButton = { },

                title = {

                    Text(

                        "Owner Details",

                        style = MaterialTheme.typography.titleLarge,

                        fontWeight = FontWeight.Bold,

                        color = Color(0xFF2C3E50)
                    )
                },

                text = {

                    Column {

                        OwnerInfoRow(
                            label = "Owner",
                            value = machine.getOrNull(2) ?: ""
                        )

                        OwnerInfoRow(
                            label = "Phone",
                            value = machine.getOrNull(3) ?: ""
                        )

                        OwnerInfoRow(
                            label = "Email",
                            value = machine.getOrNull(5) ?: ""
                        )

                        OwnerInfoRow(
                            label = "Location",
                            value = machine.getOrNull(4) ?: ""
                        )

                        Spacer(Modifier.height(12.dp))


                        Spacer(Modifier.height(16.dp))

                        Row(

                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.Center
                        ) {

                            OutlinedButton(

                                onClick = {

                                    val intent = Intent(
                                        Intent.ACTION_DIAL
                                    )

                                    intent.data =

                                        Uri.parse(
                                            "tel:${machine.getOrNull(3) ?: ""}"
                                        )

                                    context.startActivity(intent)
                                },

                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp),

                                shape = RoundedCornerShape(16.dp),

                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFD0D7DE)
                                ),

                                colors = ButtonDefaults.outlinedButtonColors(

                                    containerColor = Color(0xFFF8FAFC),

                                    contentColor = Color(0xFF2C3E50)
                                )
                            ) {

                                Text(
                                    "Call",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            if (machine.size > 4) {

                                OutlinedButton(

                                    onClick = {

                                        val intent =
                                            Intent(Intent.ACTION_SEND)

                                        intent.type =
                                            "message/rfc822"

                                        intent.putExtra(

                                            Intent.EXTRA_EMAIL,

                                            arrayOf(
                                                machine.getOrNull(5) ?: ""
                                            )
                                        )

                                        intent.putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Regarding Machine Inquiry"
                                        )

                                        intent.putExtra(

                                            Intent.EXTRA_TEXT,

                                            "Hello ${machine.getOrNull(2) ?: ""},\n\n" +

                                                    "I am contacting you regarding your machine.\n\n" +

                                                    "Thank you."
                                        )

                                        context.startActivity(

                                            Intent.createChooser(
                                                intent,
                                                "Send Email"
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(50.dp),

                                    shape = RoundedCornerShape(16.dp),

                                    border = BorderStroke(
                                        1.dp,
                                        Color(0xFFD0D7DE)
                                    ),

                                    colors = ButtonDefaults.outlinedButtonColors(

                                        containerColor = Color(0xFFF8FAFC),

                                        contentColor = Color(0xFF2C3E50)
                                    )
                                ) {

                                    Text(
                                        "Email",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        if (showDelistedDialog) {

                            AlertDialog(

                                onDismissRequest = {

                                    showDelistedDialog = false
                                },
                                shape = RoundedCornerShape(28.dp),

                                containerColor = Color.White,
                                confirmButton = {

                                    OutlinedButton(

                                        onClick = {

                                            showDelistedDialog = false
                                        },

                                        shape = RoundedCornerShape(16.dp),

                                        border = BorderStroke(

                                            1.dp,

                                            Color(0xFFD0D7DE)
                                        ),

                                        colors = ButtonDefaults.outlinedButtonColors(

                                            containerColor = Color(0xFFF8FAFC),

                                            contentColor = Color(0xFF2C3E50)
                                        )
                                    ) {

                                        Text(

                                            "OK",

                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },

                                title = {

                                    Text(

                                        "Machine Unavailable",

                                        style = MaterialTheme.typography.titleLarge,

                                        fontWeight = FontWeight.Bold,

                                        color = Color(0xFF2C3E50)
                                    )
                                },

                                text = {

                                    Text(

                                        "This machine is no longer available for rental requests.",

                                        color = Color.Gray,

                                        lineHeight = 22.sp
                                    )
                                }
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(

                            onClick = {

                                val body = RequestMachineBody(

                                    machine_id =
                                        machine.getOrNull(0)?.toIntOrNull() ?: 0,

                                    requester_name = requesterName,

                                    requester_phone = phone,

                                    requester_email = requesterEmail,

                                    requester_location = requesterLocation,

                                    owner_phone =
                                        machine.getOrNull(3) ?: ""
                                )
                                RetrofitClient.api
                                    .requestMachine(body)

                                    .enqueue(

                                        object :
                                            Callback<Map<String, String>> {

                                            override fun onResponse(

                                                call: Call<Map<String, String>>,

                                                response: Response<Map<String, String>>
                                            ) {

                                                val msg =

                                                    response.body()?.get("message")
                                                        ?: ""

                                                if (msg.contains("delisted")) {

                                                    showDelistedDialog = true

                                                } else {

                                                    Toast.makeText(
                                                        context,
                                                        msg,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                            override fun onFailure(

                                                call: Call<Map<String, String>>,

                                                t: Throwable
                                            ) {

                                                Toast.makeText(
                                                    context,
                                                    "Failed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),

                            shape = RoundedCornerShape(18.dp),

                            border = BorderStroke(

                                1.dp,

                                Color(0xFFD0D7DE)
                            ),

                            colors = ButtonDefaults.outlinedButtonColors(

                                containerColor = Color(0xFFF8FAFC),

                                contentColor = Color(0xFF2C3E50)
                            )
                        ) {
                            Text(

                                "Request",

                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        }
    }
}


