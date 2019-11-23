package com.allsoftdroid.audiobook.services.audio

import com.allsoftdroid.audiobook.services.audio.NotificationUtils.Companion.sendNotification
import android.app.*
import android.content.Intent
import android.os.IBinder
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.PlayingState
import com.allsoftdroid.common.base.store.*
import timber.log.Timber


class AudioService : Service(){

    companion object CONSTANT{
        const val ACTION_PREVIOUS = 0
        const val PREVIOUS="previous"

        const val ACTION_PLAY_PAUSE=1
        const val PLAY="play"
        const val PAUSE="pause"

        const val ACTION_NEXT = 2
        const val NEXT="next"
    }

    private val audioServiceBinder by lazy {
        AudioServiceBinder(
            application
        )
    }

    private val eventStore : AudioPlayerEventStore by lazy {
        AudioPlayerEventBus.getEventBusInstance()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return audioServiceBinder
    }

    override fun onCreate() {
        super.onCreate()
        audioServiceBinder.trackTitle.observeForever {
            it?.let {
                buildNotification()
            }
        }

        audioServiceBinder.nextTrackEvent.observeForever {
            it.getContentIfNotHandled()?.let {nextEvent ->
                Timber.d("Received next event from AudioService binder")
                if(nextEvent){
                    Timber.d("Sending next Event")
                    eventStore.publish(Event(Next( result = PlayingState(
                        playingItemIndex = audioServiceBinder.getCurrentAudioPosition()+1,
                        action_need = false
                    ))))
                }
            }
        }
    }



    private fun buildNotification() {
        sendNotification(
            trackTitle = audioServiceBinder.getCurrentTrackTitle(),
            bookId = audioServiceBinder.getBookId(),
            bookName = audioServiceBinder.getBookId(),
            applicationContext = applicationContext,
            service = this,
            isAudioPlaying = audioServiceBinder.isPlaying(),
            currentAudioPos = audioServiceBinder.getCurrentAudioPosition())
    }

    override fun onUnbind(intent: Intent?): Boolean {
        audioServiceBinder.onUnbind()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}