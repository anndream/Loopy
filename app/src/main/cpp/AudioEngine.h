/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef RHYTHMGAME_GAME_H
#define RHYTHMGAME_GAME_H

#include <future>

#include <android/asset_manager.h>
#include <oboe/Oboe.h>

#include "Mixer.h"

#include "Player.h"
#include "StorageDataSource.h"
#include "OpenGLFunctions.h"
#include "LockFreeQueue.h"
#include "utils/UtilityFunctions.h"
#include "Constants.h"
#include <media/NdkMediaExtractor.h>


using namespace oboe;

enum class AudioEngineState {
    Loading,
    Playing,
    FailedToLoad
};

class AudioEngine : public AudioStreamCallback {
public:
    explicit AudioEngine(AMediaExtractor&);
    void start();
    void stop();
    void setFileName(const char * fileName);

    // Inherited from oboe::AudioStreamCallback
    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;
    void onErrorAfterClose(AudioStream *oboeStream, Result error) override;

private:
    AMediaExtractor& mExtraxtor;
    AudioStream *mAudioStream { nullptr };
    std::unique_ptr<Player> mClap;
    std::unique_ptr<Player> mBackingTrack;
    Mixer mMixer;
    std::unique_ptr<float[]> mConversionBuffer { nullptr }; // For float->int16 conversion

    LockFreeQueue<int64_t, kMaxQueueItems> mClapEvents;
    std::atomic<int64_t> mCurrentFrame { 0 };
    std::atomic<int64_t> mSongPositionMs { 0 };
    LockFreeQueue<int64_t, kMaxQueueItems> mClapWindows;
    LockFreeQueue<TapResult, kMaxQueueItems> mUiEvents;
    std::atomic<int64_t> mLastUpdateTime { 0 };
    std::atomic<AudioEngineState> mAudioEngineState { AudioEngineState::Loading };
    std::future<void> mLoadingResult;

    void load();
    TapResult getTapResult(int64_t tapTimeInMillis, int64_t tapWindowInMillis);
    bool openStream();
    bool setupAudioSources();
    void scheduleSongEvents();

};


#endif //RHYTHMGAME_GAME_H
