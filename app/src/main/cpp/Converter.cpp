//
// Created by michael on 01.11.20.
//

#include <string>
#include <iostream>
#include <set>
#include <dirent.h>
#include <cinttypes>
#include <media/NdkMediaExtractor.h>
#include <fstream>
#include <oboe/Oboe.h>
#include "Converter.h"
#include "utils/logging.h"
#include "NDKExtractor.h"
#include <utils/AudioFile.h>

namespace fs = std::__fs::filesystem;

Converter::Converter() = default;

bool Converter::setDestinationFolder(const char *folderName) {
    mFolder = folderName;
    LOGD("Destination folder set: %s", mFolder);
    return true;
}

bool Converter::convertFolder() {
    LOGD("converting folder: %s", mFolder);
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir(mFolder)) != nullptr) {
        LOGD("Dir exists");

        std::set<std::string> allFileNames;
        std::set<std::string> excludedFileNames;
        while ((ent = readdir(dir)) != nullptr) {
            std::string name = std::string(ent->d_name);

            bool isConverted = endsWith(name, pcm);
            if (isConverted) {
                // Add names of converted files to the excluded list
                name.erase(name.length() - pcm.length());
                excludedFileNames.insert(name);
            } else {
                bool isValid = endsWith(name, wav) or endsWith(name, mp3) or endsWith(name, ogg);
                if (isValid) {
                    allFileNames.insert(name);
                }
            }

        }
        closedir(dir);
        for (const auto &name: excludedFileNames) {
            LOGD("Excluded name: %s", name.c_str());
        }

        for (const auto &name : allFileNames) {
            LOGD("Name: %s", name.c_str());
            // TODO take back in
//            if (excludedFileNames.find(name) == excludedFileNames.end()) {
//                LOGD("Not yet converted");

            std::string fullPath = std::string(mFolder) + name;
            LOGD ("Starting conversion for: %s\n", fullPath.c_str());
            doConversion(std::string(fullPath), std::string(name));
//            }

        }
    } else {
        /* could not open directory */
        LOGE("Dir does not exist");
        return EXIT_FAILURE;
    }
    return false;
}

bool Converter::doConversion(const std::string &fullPath, const std::string &name) {

    AMediaExtractor *extractor = AMediaExtractor_new();
    if (extractor == nullptr) {
        LOGE("Could not obtain AMediaExtractor");
        return false;
        return false;
    }
    media_status_t amresult = AMediaExtractor_setDataSource(extractor, fullPath.c_str());
    if (amresult != AMEDIA_OK) {
        LOGE("Error setting extractor data source, err %d", amresult);
        return false;
    } else {
        LOGD("amresult ok");
    }

    std::ifstream stream;
    stream.open(fullPath, std::ifstream::in | std::ifstream::binary);

    if (!stream.is_open()) {
        LOGE("Opening stream failed! %s", fullPath.c_str());
        return false;
    }
    stream.seekg(0, std::ios::end);
    long size = stream.tellg();
    stream.close();

    constexpr int kMaxCompressionRatio{12};
    const long maximumDataSizeInBytes =
            kMaxCompressionRatio * (size) * sizeof(int16_t);
    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int32_t bitRate = NDKExtractor::getBitRate(*extractor);
    int numChannels = NDKExtractor::getChannelCount(*extractor);
    int64_t bytesDecoded = NDKExtractor::decode(*extractor, decodedData);
    auto numSamples = bytesDecoded / sizeof(int16_t);

    auto outputBuffer = std::make_unique<float[]>(numSamples);
    LOGD("Bytes decoded: %" PRId64 "\n", bytesDecoded);
    LOGD("Number of Samples: %i", numSamples);
    LOGD("Bit rate: %i", bitRate);
    LOGD("Channels: %i", numChannels);
    // The NDK decoder can only decode to int16, we need to convert to floats
    oboe::convertPcm16ToFloat(
            reinterpret_cast<int16_t *>(decodedData),
            outputBuffer.get(),
            bytesDecoded / sizeof(int16_t));

    AudioFile<float> audioFile;
    AudioFile<float>::AudioBuffer audioBuffer;
    audioBuffer.resize(2);
    audioBuffer[0].resize(numSamples);
    audioBuffer[1].resize(numSamples);
    audioFile.setSampleRate(96000);



    bool left = true;
    for (int i = 0; i < numSamples; i++) {
        float x = outputBuffer[i];
        if (left) {
            audioBuffer[0][i] = x;
        } else {
            audioBuffer[1][i] = x;
        }
        left = !left;
    }

    if (audioFile.setAudioBuffer(audioBuffer)) {
        LOGD("Setting buffer succeeded");
    } else {
        LOGD("setting buffer failed");
        return false;
    }


    std::string outputName = std::string(mFolder) + "/" + name + ".wav";
    LOGD("outputName: %s", outputName.c_str());

    LOGD("Audio File, sample rate: %i", audioFile.getSampleRate());
    LOGD("Audio File, length: %f", audioFile.getLengthInSeconds());
    LOGD("Audio File, bit depth: %i", audioFile.getBitDepth());
    LOGD("Audio File, samples per channel: %i", audioFile.getNumSamplesPerChannel());
    LOGD("Audio File, channels: %i", audioFile.getNumChannels());

    return audioFile.save(outputName);
}

bool Converter::convertSingleFile(const char *filePath, const char *fileName) {
    LOGD("Converting single file");
    return doConversion(std::string(filePath), std::string(fileName));
}




