package com.allsoftdroid.common.base.store.audioPlayer

import com.allsoftdroid.common.base.extension.AudioPlayListItem
import com.allsoftdroid.common.base.extension.AudioPlayerEventState

sealed class AudioPlayerEvent

//Action Event for the Audio Player
data class Next(val result:AudioPlayerEventState) : AudioPlayerEvent()
data class Previous(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class Play(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class Pause(val result: AudioPlayerEventState) : AudioPlayerEvent()
data class EmptyEvent(val default: AudioPlayerEventState):AudioPlayerEvent()

//Details or information event for the player and UI
data class PlaySelectedTrack(val trackList : List<AudioPlayListItem>,val bookId:String,val bookName:String, val position:Int) : AudioPlayerEvent()
data class TrackDetails(val trackTitle:String,val bookId: String, val position: Int):AudioPlayerEvent()

//Alert to UI for next screen Action event
object OpenMainPlayerEvent : AudioPlayerEvent()
object OpenMiniPlayerEvent : AudioPlayerEvent()

//Player State event
data class AudioPlayerPlayingState(val isReady:Boolean) : AudioPlayerEvent()