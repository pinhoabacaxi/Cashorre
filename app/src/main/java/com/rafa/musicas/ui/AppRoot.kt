@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(store: PlaylistStore) {
    val nav = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = Intent(context, PlaybackService::class.java)
        context.startForegroundService(intent)
    }

    Scaffold(
        bottomBar = { PlayerMiniBar() }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "playlists",
            modifier = Modifier.padding(padding)
        ) {
            composable("playlists") {
                PlaylistsScreen(store) { playlist ->
                    nav.navigate("playlist/$playlist")
                }
            }

            composable("search") {
                SearchAndImportScreen(store)
            }

            composable(
                "playlist/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) {
                val name = it.arguments?.getString("name") ?: ""
                PlaylistDetailScreen(name, store) {
                    nav.popBackStack()
                }
            }
        }
    }
}
