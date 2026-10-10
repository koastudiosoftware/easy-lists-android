package com.easylists.presentation.ui.listdetails

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.easylists.presentation.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PhotoActions(
    val takePhoto: () -> Unit,
    val pickPhoto: () -> Unit,
)


//region rememberPhotoActions
@Composable
fun rememberPhotoActions(onPhotoChosen: (String) -> Unit): PhotoActions {
    val context = LocalContext.current
    val currentOnPhotoChosen by rememberUpdatedState(onPhotoChosen)

    // survives the activity being recreated while the camera is open
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // uri is null when the picker is cancelled
        if (uri != null) currentOnPhotoChosen(uri.toString())
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        if (saved && uri != null) currentOnPhotoChosen(uri.toString())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            // TODO explain that the camera permission was denied (and maybe disable the camera icon)
        }
    }

    return remember(pickerLauncher, permissionLauncher) {
        PhotoActions(
            takePhoto = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            pickPhoto = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )
    }
}
//endregion


//region createCameraUri
// empty image file in the app cache that the camera app writes into
private fun createCameraUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File.createTempFile("camera_${timeStamp}_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.LIBRARY_PACKAGE_NAME}.provider",
        file
    )
}
//endregion
