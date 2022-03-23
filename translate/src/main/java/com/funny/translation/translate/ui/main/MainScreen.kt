package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.trans.*
import com.funny.translation.translate.*
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.bean.RoundCornerConfig
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.ClipBoardUtil
import com.google.accompanist.flowlayout.FlowRow
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

private const val TAG = "MainScreen"
private interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun MainScreen() {
    val vm : MainViewModel = viewModel()

    val updateSelectedEngine = object : UpdateSelectedEngine {
        override fun add(engine: TranslationEngine) {
            vm.addSelectedEngines(engine)
        }

        override fun remove(engine: TranslationEngine) {
            vm.removeSelectedEngine(engine)
        }
    }

    val bindEngines by vm.bindEnginesFlow.collectAsState(emptyList())
    val jsEngines by vm.jsEnginesFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarState.current

    val showSnackbar : (String) -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(it)
        }
    }

//    SideEffect {
//        Log.d(TAG, "MainScreen: text:$translateText sourceId:$source targetId:$target")
//    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth > 720.dp) { // 横屏
            val scrollState = rememberScrollState()
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                EngineSelect(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .padding(8.dp)
                        .verticalScroll(scrollState)
                    ,
                    bindEngines, jsEngines,
                    updateSelectedEngine
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .width(2.dp)
                        .background(MaterialTheme.colors.surface)
                )
                Column(
                    Modifier
                        .fillMaxHeight()
                        .padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TranslatePart(
                        vm = vm,
                        showSnackbar = showSnackbar,
                        modifier = Modifier
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .fillMaxWidth(0.610f)
                    )
                }
            }
        }else{
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var expandEngineSelect by remember {
                    mutableStateOf(false)
                }
                val swipeableState = rememberSwipeableState(initialValue = ExpandState.CLOSE)

                AnimatedVisibility(visible = swipeableState.currentValue == ExpandState.OPEN || expandEngineSelect) {
                    EngineSelect(
                        modifier = Modifier.padding(8.dp),
                        bindEngines, jsEngines,
                        updateSelectedEngine
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .background(MaterialTheme.colors.secondary)
                    .clickable { expandEngineSelect = !expandEngineSelect }
                )
                Spacer(modifier = Modifier.height(12.dp))

                TranslatePart(
                    vm = vm,
                    showSnackbar = showSnackbar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 接收其他应用传来的参数并开始翻译
//    val translateTextState = remember {
//        mutableStateOf(activityVM.tempTransConfig.sourceString)
//    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val activityVM : ActivityViewModel = LocalActivityVM.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    DisposableEffect(key1 = lifecycleOwner){
        val observer = LifecycleEventObserver { _, event ->
//            Log.d(TAG, "MainScreen: event: $event")
            if (event == Lifecycle.Event.ON_RESUME) {
//                Log.d(TAG, "MainScreen: activityVM:${activityVM.hashCode()}")
                val text = activityVM.tempTransConfig.sourceString?.trim() ?: ""
//                Log.d(TAG, "MainScreen: get_text:$text")
                if(text!=""){
                    vm.translateText.value = text
                    if (activityVM.tempTransConfig.sourceLanguage != null) {
                        vm.sourceLanguage.value = activityVM.tempTransConfig.sourceLanguage
                    }
                    if (activityVM.tempTransConfig.targetLanguage != null) {
                        vm.targetLanguage.value = activityVM.tempTransConfig.targetLanguage
                    }
                    vm.translate()
                    activityVM.tempTransConfig.clear()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                softwareKeyboardController?.hide()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

//    LaunchedEffect(key1 = translateTextState){
//        delay(800)
//        val text = activityVM.tempTransConfig.sourceString?.trim() ?: ""
//        Log.d(TAG, "MainScreen: get_text:$text")
//        if(text!=""){
//            vm.translateText.value = text
//            if (activityVM.tempTransConfig.sourceLanguage != null) {
//                vm.sourceLanguage.value = activityVM.tempTransConfig.sourceLanguage
//            }
//            if (activityVM.tempTransConfig.targetLanguage != null) {
//                vm.targetLanguage.value = activityVM.tempTransConfig.targetLanguage
//            }
//            vm.translate()
//        }
//    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TranslatePart(
    vm: MainViewModel,
    showSnackbar: (String) -> Unit,
    modifier : Modifier
) {
    val transText by vm.translateText.observeAsState("")
    val sourceLanguage by vm.sourceLanguage.observeAsState()
    val targetLanguage by vm.targetLanguage.observeAsState()

    val resultList by vm.resultList.observeAsState()
    val translateProgress by vm.progress.observeAsState()

    val softKeyboardController = LocalSoftwareKeyboardController.current
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        LanguageSelect(
            language = sourceLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.sourceLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_SOURCE_LANGUAGE, it.id)
            }
        )
        ExchangeButton {
            Log.d(TAG, "MainScreen: clicked")
            val temp = sourceLanguage
            vm.sourceLanguage.value = targetLanguage
            vm.targetLanguage.value = temp

            DataSaverUtils.saveData(Consts.KEY_SOURCE_LANGUAGE, vm.sourceLanguage.value!!.id)
            DataSaverUtils.saveData(Consts.KEY_TARGET_LANGUAGE, vm.targetLanguage.value!!.id)
        }
        LanguageSelect(
            language = targetLanguage!!,
            languages = allLanguages,
            updateLanguage = {
                vm.targetLanguage.value = it
                DataSaverUtils.saveData(Consts.KEY_TARGET_LANGUAGE, it.id)
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    InputText(text = transText, updateText = { vm.translateText.value = it })
    Spacer(modifier = Modifier.height(12.dp))
    TranslateButton(translateProgress!!.toInt()) {
        val selectedEngines = vm.selectedEngines
        if (selectedEngines.isEmpty()) {
            showSnackbar(FunnyApplication.resources.getString(R.string.snack_no_engine_selected))
            return@TranslateButton
        }
        val selectedSize = selectedEngines.size
        if (selectedSize > Consts.MAX_SELECT_ENGINES){
            showSnackbar(FunnyApplication.resources.getString(R.string.message_out_of_max_engine_limit).format(Consts.MAX_SELECT_ENGINES, selectedSize))
            return@TranslateButton
        }
        if(!vm.isTranslating()) {
            vm.translate()
            softKeyboardController?.hide()
        }
        else{
            vm.cancel()
            showSnackbar(FunnyApplication.resources.getString(R.string.message_stop_translate))
        }
    }
    Spacer(modifier = Modifier.height(18.dp))
    TranslationList(resultList!!, showSnackbar)
}

@ExperimentalAnimationApi
@Composable
private fun EngineSelect(
    modifier: Modifier,
    bindEngines: List<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    updateSelectEngine: UpdateSelectedEngine
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(id = R.string.bind_engine),
            fontWeight = W600
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            mainAxisSpacing = 12.dp,
            crossAxisSpacing = 8.dp
        ) {
            bindEngines.forEachIndexed { index, task ->
                SelectableChip(initialSelect = task.selected, text = task.name) {
                    if(!task.selected){ // 选中了
                        updateSelectEngine.add(task)
                    }else updateSelectEngine.remove(task)
                    bindEngines[index].selected = !task.selected
                    DataSaverUtils.saveData(task.selectKey, task.selected)
                }
            }
        }

        if (jsEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.plugin_engine),
                fontWeight = W600
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                mainAxisSpacing = 12.dp,
                crossAxisSpacing = 8.dp
            ) {
                jsEngines.forEachIndexed { index, task ->
                    //临时出来的解决措施，因为ArrayList单个值更新不会触发LiveData的更新。更新自己
                    SelectableChip(initialSelect = task.selected, text = task.name) {
                        if(!task.selected){ // 选中了
                            updateSelectEngine.add(task)
                        }else updateSelectEngine.remove(task)

                        jsEngines[index].selected = !task.selected
                        DataSaverUtils.saveData(task.selectKey, task.selected)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelect(
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    RoundCornerButton(text = language.displayText, onClick = {
        expanded = true
    }) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }) {
                    Text(it.displayText)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TranslationList(
    resultList: List<TranslationResult>,
    showSnackbar: (String) -> Unit
) {
    val size = resultList.size
    LazyColumn(
        modifier = Modifier ,
        verticalArrangement = spacedBy(4.dp)
    ) {
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { index, result ->
//            Log.d(TAG, "TranslationList: $result")
            TranslationItem(
                modifier = Modifier.animateItemPlacement() ,
                result = result, roundCornerConfig = when (index) {
                    0 -> if (size == 1) RoundCornerConfig.All else RoundCornerConfig.Top
                    size - 1 -> RoundCornerConfig.Bottom
                    else -> RoundCornerConfig.None
                }, showSnackbar = showSnackbar
            )
        }
    }
}

@Composable
fun TranslateButton(
    progress: Int = 100,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                val p = if (progress == 0) 100 else progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth(p / 100f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary)
                )
                Text(
                    text = stringResource(id = R.string.translate),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 22.sp
                )
            }
        }
    }
}


@Composable
fun TranslationItem(
    modifier: Modifier,
    result: TranslationResult,
    roundCornerConfig: RoundCornerConfig,
    showSnackbar: (String) -> Unit
) {
    val cornerSize = 16.dp
    val shape = when (roundCornerConfig) {
        is RoundCornerConfig.Top -> RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
        is RoundCornerConfig.Bottom -> RoundedCornerShape(
            bottomEnd = cornerSize,
            bottomStart = cornerSize
        )
        is RoundCornerConfig.All -> RoundedCornerShape(cornerSize)
        is RoundCornerConfig.None -> RectangleShape
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = shape)
            .padding(12.dp)
            .animateContentSize()

    ) {
        var expandDetail by remember {
            mutableStateOf(false)
        }
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = result.engineName, color = MaterialColors.Grey600, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val fontSize = when (result.basicResult.trans.length) {
                in 0..25 -> 24
                in 26..50 -> 20
                in 50..70 -> 18
                in 70..90 -> 16
                else -> 14
            }
            Text(
                text = result.basicResult.trans,
                color = MaterialTheme.colors.onSurface,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                        ClipBoardUtil.copy(FunnyApplication.ctx, result.basicResult.trans)
                        showSnackbar(FunnyApplication.resources.getString(R.string.snack_finish_copy))
                    }, modifier = Modifier
//                        .then(Modifier.size(36.dp))
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.secondary)
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_copy_content),
                        contentDescription = stringResource(id = R.string.copy_content),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        AudioPlayer.play(
                            result.basicResult.trans.trim(), result.targetLanguage!!
                        ) {
                            showSnackbar(FunnyApplication.resources.getString(R.string.snack_speak_error))
                        }
                    }, modifier = Modifier
//                        .then(Modifier.size(36.dp))
                        .clip(CircleShape)
                        .size(48.dp)
                        .background(MaterialTheme.colors.secondary)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                AudioPlayer.pause()
                            })
                        }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_speak),
                        contentDescription = stringResource(id = R.string.speak),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (!result.detailText.isNullOrEmpty()){
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        ExpandMoreButton {
                            expandDetail = !expandDetail
                        }
                    }
                }
            }
            if(expandDetail){
                MarkdownText(markdown = result.detailText!!, Modifier.padding(4.dp))
            }
        }
    }
}

enum class ExpandState{
    OPEN,
    CLOSE
}