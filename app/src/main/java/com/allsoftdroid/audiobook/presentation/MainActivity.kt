package com.allsoftdroid.audiobook.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.audiobook.di.AppModule
import com.allsoftdroid.audiobook.domain.model.LastPlayedTrack
import com.allsoftdroid.audiobook.feature_downloader.domain.IDownloaderCore
import com.allsoftdroid.audiobook.feature_downloader.presentation.DownloadManagementActivity
import com.allsoftdroid.audiobook.feature_mini_player.presentation.MiniPlayerFragment
import com.allsoftdroid.audiobook.presentation.viewModel.MainActivityViewModel
import com.allsoftdroid.audiobook.utility.MovableFrameLayout
import com.allsoftdroid.audiobook.utility.OnSwipeTouchListener
import com.allsoftdroid.common.base.activity.BaseActivity
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ConnectionLiveData
import com.allsoftdroid.common.base.store.audioPlayer.*
import com.allsoftdroid.common.base.store.downloader.DownloadEvent
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.store.downloader.DownloadNothing
import com.allsoftdroid.common.base.store.userAction.*
import com.allsoftdroid.common.base.utils.StoragePermissionHandler
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import it.sephiroth.android.library.xtooltip.Tooltip
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class MainActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_main

    companion object{
        const val MINI_PLAYER_TAG = "MiniPlayer"
    }

    private val mainActivityViewModel : MainActivityViewModel by viewModel{
        parametersOf(Bundle(), "vm_main")
    }
    private val connectionListener: ConnectionLiveData by inject{parametersOf(this)}
    private val downloadEventStore:DownloadEventStore by inject()
    private val downloader: IDownloaderCore by inject{parametersOf(this)}
    private val userActionEventStore:UserActionEventStore by inject()
    private val disposables = CompositeDisposable()


    private val snackBar by lazy {
        val sb = Snackbar.make(findViewById(R.id.navHostFragment), getString(R.string.offline_message), Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
        sb.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        sb
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppModule.injectFeature()

        mainActivityViewModel.lastPlayed.observe(this, Observer {event ->
            event.getContentIfNotHandled()?.let {
                Timber.d("Last played : $it")

                val dialog = alertDialog(it)
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }
        })
    }


    /**
     * Show Dialog for continuing last played book on App start.
     * @param lastPlayedTrack It holds data related to @[LastPlayedTrack]
     * @return @[AlertDialog] object
     */
    private fun alertDialog(lastPlayedTrack: LastPlayedTrack):AlertDialog{
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.resume_book_header,lastPlayedTrack.bookName))
        builder.setMessage(getString(R.string.resume_book_message,lastPlayedTrack.title))

        builder.setPositiveButton(getString(R.string.btn_listen_label)) { _, _ ->
            Toast.makeText(this,getString(R.string.playing_label),Toast.LENGTH_SHORT).show()
            //Navigate to display page
            val bundle = bundleOf(
                "bookId" to lastPlayedTrack.bookId,
                "title" to lastPlayedTrack.title,
                "bookName" to lastPlayedTrack.bookName,
                "trackNumber" to lastPlayedTrack.position)

            connectionListener.value?.let { connectedEvent->
                if(connectedEvent.peekContent()){
                    findNavController(this,R.id.navHostFragment)
                        .navigate(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle)
                    mainActivityViewModel.clearSharedPref()
                }else{
                    Toast.makeText(this,getString(R.string.network_connect_message),Toast.LENGTH_SHORT).show()
                }
            }
        }

        builder.setNegativeButton(getString(R.string.btn_dismiss_label)){
            _,_ ->
            mainActivityViewModel.clearSharedPref()
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()

        mainActivityViewModel.bindAudioService()

        connectionListener.observe(this, Observer {isConnectedEvent ->
            isConnectedEvent.getContentIfNotHandled()?.let {isConnected ->
                showNetworkMessage(isConnected)
                if(isConnected){
                    try{
                        mainActivityViewModel.playIfAnyTrack()
                    }catch (exception:Exception){
                        Timber.e("Error occurred when resuming: ${exception.printStackTrace()}")
                    }
                }
            }
        })

        Timber.d("Main Activity  start")
        mainActivityViewModel.showPlayer.observe(this, Observer {
            it.let { shouldShow ->
                Timber.d("Player state event received from view model:shouldShow->$shouldShow")
                miniPlayerViewState(shouldShow)
            }
        })

        mainActivityViewModel.playerEvent.observeForever {
            it.getContentIfNotHandled()?.let {audioPlayerEvent ->
                Timber.d("Event is new and is being handled")
                performAction(audioPlayerEvent)
            }
        }

        disposables.add(downloadEventStore.observe()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                handleDownloadEvent(it)
            })

        disposables.add(userActionEventStore.observe()
            .subscribe{
                it.getContentIfNotHandled()?.let {action ->
                    when(action){
                        is OpenDownloadUI -> {
                            navigateToDownloadManagementActivity()
                        }

                        is OpenLicensesUI -> {
                            navigateToLicensesActivity()
                        }

                        is OpenMainPlayerUI ->{
                            mainActivityViewModel.playerStatus(showPlayer = false)
                            navigateToMainPlayerScreen()
                        }

                        is OpenMiniPlayerUI ->{
                            Timber.d("mini player opening event received")
                            mainActivityViewModel.playerStatus(showPlayer = true)
                        }
                    }
                }
            })
    }

    /**
     * Handler function for @[DownloadEvent]. It checks whether storage permission is granted or not.
     * if it is not granted, it request to the user else forward the request to download Manager for further handling.
     */
    private fun handleDownloadEvent(event: Event<DownloadEvent>) {

        event.getContentIfNotHandled()?.let {

            if( it is DownloadNothing) return

            if(!StoragePermissionHandler.isPermissionGranted(this)){
                StoragePermissionHandler.requestPermission(this)
            }else{
                downloader.handleDownloadEvent(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(StoragePermissionHandler.isRequestGrantedFor(requestCode,grantResults)){
            Toast.makeText(this,getString(R.string.permission_granted_message),Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,getString(R.string.permission_failed_message),Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDownloadManagementActivity() {

        val parent = findViewById<View>(R.id.navHostFragment)
        val options = ActivityOptionsCompat.makeClipRevealAnimation(parent,parent.width,0,parent.width/2,parent.height/2)

        val intent = Intent(this,
            DownloadManagementActivity::class.java)
        startActivity(intent,options.toBundle())
    }

    private fun navigateToLicensesActivity(){

        val parent = findViewById<View>(R.id.navHostFragment)
        val options = ActivityOptionsCompat.makeClipRevealAnimation(parent,parent.width,0,parent.width/2,parent.height/2)

        OssLicensesMenuActivity.setActivityTitle(getString(R.string.license_screen_title))
        startActivity(Intent(this,OssLicensesMenuActivity::class.java),options.toBundle())
    }

    /**
     * This function handles visibility and loading of mini player. Based on @[shouldShow] parameter, it decides whether mini player,
     * should be visible or not. It also loads the fragment into the view if it should show.
     */
    private fun miniPlayerViewState(shouldShow: Boolean) {
        if(shouldShow){

            val fragment = supportFragmentManager.findFragmentByTag(MINI_PLAYER_TAG)

            if(fragment == null){
                supportFragmentManager.beginTransaction()
                    .add(R.id.miniPlayerContainer,MiniPlayerFragment(),MINI_PLAYER_TAG)
                    .commit()
            }else{
                supportFragmentManager.beginTransaction()
                    .show(fragment)
                    .commit()
            }

            val containerView = findViewById<MovableFrameLayout>(R.id.miniPlayerContainer)
            containerView.apply {
                visibility = View.VISIBLE
                setOnTouchListener(object : OnSwipeTouchListener(context) {

                    override fun onSwipeTop() {
                        super.onSwipeTop()
                        Timber.d("Event sent for opening main player event")
                        userActionEventStore.publish(Event(OpenMainPlayerUI(this::class.java.simpleName)))
                    }
                })
            }.post {
                if(!mainActivityViewModel.isToolTipShown()) showToolTipForMiniPlayer()
            }

            findViewById<View>(R.id.navHostFragment).apply {

                val layout = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,CoordinatorLayout.LayoutParams.MATCH_PARENT)
                layout.bottomMargin = resources.getDimension(R.dimen.mini_player_height).toInt()

                layoutParams = layout
            }



        }else{
            val fragment = supportFragmentManager.findFragmentByTag(MINI_PLAYER_TAG)

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .hide(it)
                    .commit()
            }

            findViewById<MovableFrameLayout>(R.id.miniPlayerContainer).visibility = View.GONE

            findViewById<View>(R.id.navHostFragment).apply {

                val layout = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,CoordinatorLayout.LayoutParams.MATCH_PARENT)
                layout.bottomMargin = 0
                layoutParams = layout
            }
        }
    }

    /**
     * This function handles all the @[AudioPlayerEvent] related event on @[AudioPlayerEventStore]
     */
    private fun performAction(event: AudioPlayerEvent){
        when(event){
            is Next -> {
                mainActivityViewModel.nextTrack(event)
            }

            is Previous -> {
                mainActivityViewModel.previousTrack()
            }

            is Play -> {
                mainActivityViewModel.resumeOrPlayTrack()
            }

            is Pause -> {
                mainActivityViewModel.pauseTrack()
            }

            is PlaySelectedTrack -> {

                mainActivityViewModel.apply {
                    playSelectedTrack(event)
                    playerStatus(true)
                }
            }

            is Rewind -> {
                mainActivityViewModel.apply {
                    rewindTrack()
                }
            }

            is Forward ->{
                mainActivityViewModel.apply {
                    forwardTrack()
                }
            }

            is Finished -> {
                mainActivityViewModel.playerStatus(showPlayer = false)
            }

            else -> {
                Timber.d("Unknown event received")
                Timber.d("Unknown Event has message of type TrackDetails: ${event is TrackDetails}")
                Timber.d("Unknown Event has message of type Initial: ${event is EmptyEvent}")
            }
        }
    }

    /**
     * This function handles the network status related visibility. If network is not available, it
     * display snackBar to let the user know of the network connecton issue
     */
    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            snackBar.show()
        } else {
            snackBar.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioService()
        disposables.dispose()
        downloader.Destroy()
    }

    private fun stopAudioService(){
        try{
            mainActivityViewModel.unBoundAudioService()
        }catch (exception: Exception){
            Timber.d(exception)
        }
    }

    /**
     * This function handles opening of Main Player and prepare necessary param data required to start the Main player
     */
    private fun navigateToMainPlayerScreen(){
        val controller = findNavController(R.id.navHostFragment)
        val playingTrackDetails = mainActivityViewModel.getPlayingTrack()

        playingTrackDetails?.let {trackDetails ->
            val bundle = bundleOf(
                "bookId" to trackDetails.bookIdentifier,
                "bookTitle" to trackDetails.bookTitle,
                "trackName" to trackDetails.trackName,
                "chapterIndex" to trackDetails.chapterIndex,
                "totalChapter" to trackDetails.totalChapter,
                "isPlaying" to trackDetails.isPlaying)

            controller.currentDestination?.let {
                when(it.id){
                    R.id.AudioBookDetailsFragment ->{
                        controller.navigate(R.id.action_AudioBookDetailsFragment_to_MainPlayerFragment,bundle)
                    }

                    R.id.AudioBookListFragment ->{
                        controller.navigate(R.id.action_AudioBookListFragment_to_MainPlayerFragment,bundle)
                    }

                    R.id.MyBooksFragment -> {
                        controller.navigate(R.id.action_MyBooksFragment_to_MainPlayerFragment,bundle)
                    }
                    
                    R.id.ListenLaterFragment -> {
                        controller.navigate(R.id.action_ListenLaterFragment_to_MainPlayerFragment,bundle)
                    }

                    R.id.SettingsFragment -> {
                        controller.navigate(R.id.action_SettingsFragment_to_MainPlayerFragment,bundle)
                    }

                    else -> {
                        Timber.d("Operation not allowed")
                        Toast.makeText(this,getString(R.string.player_navigation_error),Toast.LENGTH_SHORT).show()
                    }
                }

                mainActivityViewModel.setToolTipShown(shouldSkip = true)
            }
        }

    }

    private fun showToolTipForMiniPlayer(){
        val containerView:View = findViewById<MovableFrameLayout>(R.id.miniPlayerContainer)

        val metrics = resources.displayMetrics
        val gravity = Tooltip.Gravity.TOP

        var tooltip:Tooltip? = Tooltip.Builder(containerView.context)
            .anchor(containerView,100,70,false)
            .text(getString(R.string.tooltip_open_player_message))
            .maxWidth(metrics.widthPixels / 2)
            .arrow(false)
            .floatingAnimation(Tooltip.Animation.DEFAULT)
            .showDuration(3000)
            .overlay(true)
            .create()

        tooltip
            ?.doOnHidden {
                tooltip = null
            }
            ?.show(containerView.rootView, gravity, true)
    }
}
