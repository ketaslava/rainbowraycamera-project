package com.ktvincco.rainbowraycamera.ui


import android.app.Activity
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.ktvincco.rainbowraycamera.R
import com.ktvincco.rainbowraycamera.presentation.ModelData
import com.ktvincco.rainbowraycamera.presentation.UiEventHandler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import androidx.compose.animation.fadeOut
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.ui.components.GeneralComponents


class Gallery (
    private var mainActivity: Activity,
    private var modelData: ModelData,
    private var uiEventHandler: UiEventHandler,
    private val uiScale: Float,
    private val screenResolution: Pair<Int, Int>,
    private var generalComponents: GeneralComponents
) {


    // Navigation bar for switch media and close gallery
    @Composable
    private fun NavigationBar() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 18)
                .background(Color(0xE51A1A1A))
        ) {

            val isBackButtonActive =
                modelData.isBackButtonActive.collectAsState().value
            var backButtonAlpha = 1F
            if (!isBackButtonActive) { backButtonAlpha = 0.33F }
            generalComponents.ImageIcon1(
                R.drawable.icon_arrow,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .rotate(180F)
                    .alpha(backButtonAlpha)
                    .clickable { uiEventHandler.backButtonClicked() }
            )

            generalComponents.ImageIcon1(
                R.drawable.icon_delete,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.galleryDeleteButtonClicked() }
            )
            generalComponents.ImageIcon1(
                R.drawable.icon_cross,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.closeButtonClicked() }
            )
            generalComponents.ImageIcon1(
                R.drawable.icon_share,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .clickable { uiEventHandler.galleryShareButtonClicked() }
            )

            val isNextButtonActive =
                modelData.isNextButtonActive.collectAsState().value
            var nextButtonAlpha = 1F
            if (!isNextButtonActive) { nextButtonAlpha = 0.33F }
            generalComponents.ImageIcon1(
                R.drawable.icon_arrow,
                modifier = Modifier
                    .width(uiScale.dp * 9)
                    .height(uiScale.dp * 9)
                    .alpha(nextButtonAlpha)
                    .clickable { uiEventHandler.nextButtonClicked() }
            )

        }
    }


    @Composable
    private fun RotateImageButtons() {
        // Rotate image buttons
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(uiScale.dp * 5)
            ) {
                generalComponents.ImageIcon1(
                    R.drawable.icon_rotate90_left,
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .alpha(0.5F)
                        .clickable { uiEventHandler.galleryImageRotateButtonClicked(-90) }
                )
                Spacer(modifier = Modifier.width(uiScale.dp * 5))
                generalComponents.ImageIcon1(
                    R.drawable.icon_rotate180_left,
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .alpha(0.5F)
                        .clickable { uiEventHandler.galleryImageRotateButtonClicked(180) }
                )
                Spacer(modifier = Modifier.width(uiScale.dp * 5))
                generalComponents.ImageIcon1(
                    R.drawable.icon_rotate90_right,
                    modifier = Modifier
                        .width(uiScale.dp * 9)
                        .height(uiScale.dp * 9)
                        .alpha(0.5F)
                        .clickable { uiEventHandler.galleryImageRotateButtonClicked(90) }
                )
            }
        }
    }


    @Composable
    private fun VideoView(exoPlayer: ExoPlayer, context: Activity) {
        DisposableEffect(
            AndroidView(factory = {
                PlayerView(context).apply {
                    hideController()
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            })
        ) {
            onDispose { exoPlayer.release() }
        }
    }


    @Composable
    private fun VideoViewer(videoPath: String) {

        // Variables

        val context = mainActivity
        var isPlayingVideoNow by remember { mutableStateOf(false) }
        var lastSeekPosition by remember { mutableStateOf(0.0F) }

        // ExoPlayer

        // Builder
        val exoPlayer = remember {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                        context,
                        defaultDataSourceFactory
                    )
                    val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(videoPath))

                    setMediaSource(source)
                    prepare()
                }
        }

        // Event listener
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isPlayingVideoNow = isPlaying
            }
        }
        exoPlayer.addListener(listener)

        // Settings
        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF

        // Video player UI

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0x991A1A1A))
        ) {

            // View media
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){
                VideoView(exoPlayer, mainActivity)
            }

            // View media controls
            Column (
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(uiScale.dp * 18)
                ) {
                    if (!isPlayingVideoNow) {
                        generalComponents.ImageIcon1(
                            R.drawable.icon_play,
                            modifier = Modifier
                                .width(uiScale.dp * 9)
                                .height(uiScale.dp * 9)
                                .clickable {
                                    if (exoPlayer.currentPosition < exoPlayer.duration * 0.95F) {
                                        exoPlayer.play()
                                    } else {
                                        exoPlayer.seekTo(0)
                                        exoPlayer.play()
                                    }
                                }
                        )
                    } else {
                        generalComponents.ImageIcon1(
                            R.drawable.icon_pause,
                            modifier = Modifier
                                .width(uiScale.dp * 9)
                                .height(uiScale.dp * 9)
                                .clickable { exoPlayer.pause() }
                        )
                    }
                }

                // Slide to map video
                generalComponents.Slider1 (0.0F, { newPosition ->
                    // On start call remove
                    if (newPosition == lastSeekPosition) { return@Slider1 }
                    // Seek to new position
                    exoPlayer.seekTo((newPosition * exoPlayer.duration).toLong())
                    lastSeekPosition = newPosition
                }, isShowValue = false)

                // Bottom space for navigation bar
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(uiScale.dp * 18)
                )
            }
        }
    }


    @Composable
    private fun InProcessingCover() {
        // View media
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x58000000))
        ){
            generalComponents.TextMain(text = "In processing...\nmay take a few mins")
        }
    }


    @Composable
    private fun ImageViewer(image: Bitmap) {

        // Variables
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset(0f, 0f)) }

        // Limits
        if (scale < 1F) { scale = 1F }
        if (scale > 3F) { scale = 3F }
        if (offset.x > uiScale * 200 * scale) {
            offset = Offset(uiScale * 200 * scale, offset.y) }
        if (offset.x < uiScale * -200 * scale) {
            offset = Offset(uiScale * -200 * scale, offset.y) }
        if (offset.y > uiScale * 200 * scale) {
            offset = Offset(offset.x, uiScale * 200 * scale) }
        if (offset.y < uiScale * -200 * scale) {
            offset = Offset(offset.x, uiScale * -200 * scale) }

        // View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x991A1A1A))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset = if (scale > 1) {
                            Offset(offset.x + pan.x * zoom, offset.y + pan.y * zoom)
                        } else {
                            Offset(0f, 0f)
                        }
                    }
                }
        ) {
            // View media
            SubcomposeLayout { constraints ->
                val placeable = subcompose(Unit) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = maxOf(1f, minOf(scale, 3f)),
                                scaleY = maxOf(1f, minOf(scale, 3f)),
                                translationX = offset.x,
                                translationY = offset.y,
                            )
                    ) {
                        val imageBitmap = image.asImageBitmap()
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }[0].measure(constraints)

                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeRelative(0, 0)
                }
            }
        }
    }


    @Composable
    private fun EmptyGallery() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0x991A1A1A))
        ) {

            // Text
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){
                generalComponents.TextMain(text = "Gallery is empty")
            }
        }
    }


    @Composable
    private fun RawImageScreen() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0x991A1A1A))
        ) {

            // Text
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ){
                generalComponents.TextMain(text = "RAW image")
            }
        }
    }


    // Top bar with counter of available saves
    @Composable
    private fun AvailableSavesCountBar() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(uiScale.dp * 12)
                .background(Color(0xE51A1A1A))
                .clickable { uiEventHandler.onSavesCountBarButtonClicked() }
        ) {

            // Show saves count
            val mediaSavesCount = modelData.currentSavesCountForSavesCountBar.collectAsState().value
            generalComponents.TextMain(text = "$mediaSavesCount Saves")

            // Show get more saves button
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(uiScale.dp * 9)
                    .width(uiScale.dp * 32)
                    .background(Color(0xE54B4B4B))
            ) {
                generalComponents.TextMain(text = "Get More")
            }
        }
    }


    @Composable
    fun GetMoreSavesPopup() {
        Column (
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCD000000))
                .clickable { uiEventHandler.onGetMoreSavesPopupButtonClicked("Cancel") }
        ) {
            Column (
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(uiScale.dp * 80)
                    .height(uiScale.dp * 80)
                    .background(Color(0xCD202020))
                    .padding(all = uiScale.dp * 5)
            ) {
                val freeSavesRewardByWatchAd = AppSettings().getFreeSavesRewardByWatchAd()

                generalComponents.TextMain(
                    text = "If You Like our app and want to save more files, " +
                            "You can Watch AD and get $freeSavesRewardByWatchAd free saves, " +
                            "Or You can make one time payment for the " +
                            "Full Version of the RainbowRayCamera")

                Column (
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(uiScale.dp * 9)
                            .background(Color(0xE54B4B4B))
                            .clickable { uiEventHandler
                                .onGetMoreSavesPopupButtonClicked("WatchAd") }
                    ) {
                        generalComponents.TextMain(text = "Watch AD")
                    }
                    Spacer(modifier = Modifier.height(uiScale.dp * 5))
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(uiScale.dp * 9)
                            .background(Color(0xE54B4B4B))
                            .clickable { uiEventHandler
                                .onGetMoreSavesPopupButtonClicked("BuyFullVersion") }
                    ) {
                        val fullVersionPrice =
                            modelData.getMoreSavesPopupFullVersionPrice.collectAsState().value
                        generalComponents.TextMain(text = "Buy Full Version ($fullVersionPrice)")
                    }
                }
            }
        }
    }


    @Composable
    private fun SaveToDeviceButton() {

        // Calculate position
        val videoToView = modelData.galleryViewVideo.collectAsState().value
        val advancedBottomPadding = if (videoToView != null) { uiScale.dp * 12 } else { 0.dp }

        // Button
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = uiScale.dp * 5)
                    .padding(bottom = advancedBottomPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(uiScale.dp * 9).background(Color(0x80000000))
                        .padding(end = uiScale.dp * 5)
                        .clickable { uiEventHandler.onSaveButtonClicked() }
                ) {
                    generalComponents.ImageIcon1(
                        R.drawable.icon_save,
                        modifier = Modifier
                            .width(uiScale.dp * 9)
                            .height(uiScale.dp * 9)
                    )
                    Spacer(modifier = Modifier.width(uiScale.dp * 5))
                    generalComponents.TextMain(text = "SAVE")
                }
            }
        }
    }


    @Composable
    fun GalleryPage() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // Media views

            val imageToView = modelData.galleryViewImage.collectAsState().value
            if (imageToView != null) {
                ImageViewer(imageToView)
            }

            val videoToView = modelData.galleryViewVideo.collectAsState().value
            if (videoToView != null) {
                VideoViewer(videoToView)
            }

            val isGalleryEmpty = modelData.isGalleryEmpty.collectAsState().value
            if (isGalleryEmpty) {
                EmptyGallery()
            }

            val isShowRawImageScreen =
                modelData.isGalleryShowRawImageScreenAsView.collectAsState().value
            if (isShowRawImageScreen) {
                RawImageScreen()
            }

            // In precessing cover
            val isEnableGalleryInProcessingCover =
                modelData.isEnableGalleryInProcessingCover.collectAsState().value
            if (isEnableGalleryInProcessingCover) {
                InProcessingCover()
            }

            // Monetization bar
            if (modelData.isEnableAvailableSavesCountBar.collectAsState().value) {
                AvailableSavesCountBar()
            }
            AnimatedVisibility(
                visible = modelData.isEnableGetMoreSavesPopup.collectAsState().value,
                enter = fadeIn(animationSpec = tween(333)),
                exit = fadeOut(animationSpec = snap())
            ) {
                GetMoreSavesPopup()
            }

            // Controls
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box (
                    modifier = Modifier.fillMaxWidth().height(uiScale.dp * 50)
                ) {
                    if (imageToView != null) { RotateImageButtons() }
                    if (modelData.isEnableSaveButton.collectAsState().value) {
                    SaveToDeviceButton() }
                }
                NavigationBar()
            }
        }
    }
}