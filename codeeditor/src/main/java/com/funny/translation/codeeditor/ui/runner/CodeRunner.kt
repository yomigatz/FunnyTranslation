@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.codeeditor.ui.runner

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget

private const val TAG = "CodeRunner"

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ComposeCodeRunner(
    navController: NavController,
    activityCodeViewModel: ActivityCodeViewModel
){
    val viewModel : CodeRunnerViewModel = viewModel()
    val verticalScrollState = rememberScrollState()
    DisposableEffect(key1 = TAG){
        Debug.addTarget(DefaultDebugTarget)
        onDispose {
            Debug.removeTarget(DefaultDebugTarget)
        }
    }
    Scaffold(
        topBar = { CodeRunnerTopBar(
            backAction = {
                navController.navigateUp()
            }
        ) }
    ) {
        SelectionContainer(modifier = Modifier.padding(it)) {
            CodeRunnerText(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(verticalScrollState),
                viewModel = viewModel,
                activityCodeViewModel = activityCodeViewModel
            )
        }
    }
}

@Composable
fun CodeRunnerTopBar(
    backAction : ()->Unit
){
    SmallTopAppBar(
        title = {
            Text(text = stringResource(id = R.string.code_run))
        },
        navigationIcon = {
            IconButton(onClick = backAction) {
                Icon(Icons.Filled.ArrowBack,"Back")
            }
        }
    )
}

@Composable
fun CodeRunnerText(
    modifier: Modifier,
    viewModel: CodeRunnerViewModel,
    activityCodeViewModel: ActivityCodeViewModel
) {
    val code = activityCodeViewModel.codeState.value
    LaunchedEffect(key1 = code){
        //Log.d(TAG, "CodeRunnerText: $code")
        viewModel.initJs(activityCodeViewModel,code.toString())
    }
    val output = viewModel.outputDebug.observeAsState("")
    Text(
        output.value,
        modifier = modifier,
    )
}