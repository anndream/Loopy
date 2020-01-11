/*
 * Copyright (C) 2018 The Android Open Source Project
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


#include <oboe/Oboe.h>

#include "StorageDataSource.h"
#include <fstream>

//#if !defined(USE_FFMPEG)
//#error USE_FFMPEG should be defined in app.gradle
//#endif

//#if USE_FFMPEG==1
//#include "FFMpegExtractor.h"
//#else
#include "NDKExtractor.h"
#include "Constants.h"
#include "logging.h"
#include "StorageDataSource.h"
#include "FFMpegExtractor.h"
#include <fstream>

//#endif


constexpr int kMaxCompressionRatio{12};


int getFileSize(const char *fileName) {
    std::ifstream file(fileName, std::ifstream::in | std::ifstream::binary);

    if (!file.is_open()) {
        return -1;
    }

    file.seekg(0, std::ios::end);
    int fileSize = static_cast<int>(file.tellg()); // casting long to int - let's see if that works
    file.close();

    return fileSize;
}

StorageDataSource *StorageDataSource::newFromCompressedAsset(
        const char *filename,
        const AudioProperties targetProperties) {
    FILE *file;
    file = fopen(filename, "r");

    std::ifstream in;
    in.open(filename);
    if (!in) { /* report an error */ }
    std::string str;
    in >> str;
    const char *filedata = str.c_str();

//    if (!file) {
//        LOGE("Failed to open file %s", filename);
//        return nullptr;
//    }

    off_t fileSize = getFileSize(filename);
    LOGD("Opened %s, size %ld", filename, fileSize);

    // Allocate memory to store the decompressed audio. We don't know the exact
    // size of the decoded data until after decoding so we make an assumption about the
    // maximum compression ratio and the decoded sample format (float for FFmpeg, int16 for NDK).
//#if USE_FFMPEG==true
    const long maximumDataSizeInBytes = kMaxCompressionRatio * fileSize * sizeof(float);
    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int64_t bytesDecoded = FFMpegExtractor::decode2(const_cast<char *>(filename), decodedData, targetProperties);
    auto numSamples = bytesDecoded / sizeof(float);
//#else
//    const long maximumDataSizeInBytes = kMaxCompressionRatio * fileSize * sizeof(int16_t);
//    auto decodedData = new uint8_t[maximumDataSizeInBytes];
//
//    int64_t bytesDecoded = NDKExtractor::decode(filename, decodedData, targetProperties);
//    auto numSamples = bytesDecoded / sizeof(int16_t);
//#endif

    // Now we know the exact number of samples we can create a float array to hold the audio data
    auto outputBuffer = std::make_unique<float[]>(numSamples);

//#if USE_FFMPEG==1
    memcpy(outputBuffer.get(), decodedData, (size_t)bytesDecoded);
////#else
//    // The NDK decoder can only decode to int16, we need to convert to floats
//    oboe::convertPcm16ToFloat(
//            reinterpret_cast<int16_t *>(decodedData),
//            outputBuffer.get(),
//            bytesDecoded / sizeof(int16_t));
////#endif

    delete[] decodedData;

    return new StorageDataSource(std::move(outputBuffer),
                                numSamples,
                                targetProperties);
}

