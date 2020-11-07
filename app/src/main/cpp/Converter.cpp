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

namespace fs = std::__fs::filesystem;

Converter::Converter() {}

bool Converter::setFolder(const char *folderName) {
    mFolder = folderName;
    return true;
}

bool Converter::convertFolder() {
    LOGD("converting folder: %s", mFolder);
    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir(mFolder)) != NULL) {
        LOGD("Dir exists");
        /* print all the files and directories within directory */

        std::set<std::string> allFileNames;
        std::set<std::string> excludedFileNames;
        while ((ent = readdir(dir)) != NULL) {
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
        for (auto name: excludedFileNames) {
            LOGD("Excluded name: %s", name.c_str());
        }

        for (auto name : allFileNames) {
            LOGD("Name: %s", name.c_str());
            if (excludedFileNames.find(name) == excludedFileNames.end()) {
                LOGD("Not yet converted");

                std::string fullPath = std::string(mFolder) + name;
                LOGD ("Starting conversion for: %s\n", fullPath.c_str());
                doConversion(std::string(fullPath));
            }

        }
    } else {
        /* could not open directory */
        LOGE("Dir does not exist");
        return EXIT_FAILURE;
    }
    return false;
}

bool Converter::doConversion(std::string fullPath) {


    AMediaExtractor *extractor = AMediaExtractor_new();
    if (extractor == nullptr) {
        LOGE("Could not obtain AMediaExtractor");
        return false;
    }
    media_status_t amresult = AMediaExtractor_setDataSource(extractor, fullPath.c_str());
    if (amresult != AMEDIA_OK) {
        LOGE("Error setting extractor data source, err %d", amresult);
    } else {
        LOGD("amresult ok");
    }

    std::ifstream stream;
    stream.open(fullPath, std::ifstream::in | std::ifstream::binary);

    if (!stream.is_open()) {
        LOGE("Opening stream failed! %s", fullPath.c_str());
    }
    stream.seekg(0, std::ios::end);
    long size = stream.tellg();
    stream.close();

    constexpr int kMaxCompressionRatio{12};
    const long maximumDataSizeInBytes =
            kMaxCompressionRatio * (size) * sizeof(int16_t);
    auto decodedData = new uint8_t[maximumDataSizeInBytes];

    int32_t rate = NDKExtractor::getSampleRate(*extractor);
    int32_t bitRate = NDKExtractor::getBitRate(*extractor);
    int32_t *inputSampleRate = &rate;

    int64_t bytesDecoded = NDKExtractor::decode(*extractor, decodedData);
    auto numSamples = bytesDecoded / sizeof(int16_t);

    auto outputBuffer = std::make_unique<float[]>(numSamples);
    LOGD("Bytes decoded: %" PRId64 "\n", bytesDecoded);
    LOGD("Number of Samples: %i", numSamples);
    // The NDK decoder can only decode to int16, we need to convert to floats
    oboe::convertPcm16ToFloat(
            reinterpret_cast<int16_t *>(decodedData),
            outputBuffer.get(),
            bytesDecoded / sizeof(int16_t));

    std::string outputSuffix = ".pcm";
    std::string outputName = std::string(fullPath) + outputSuffix;
    LOGD("outputName: %s", outputName.c_str());
    std::ofstream outfile(outputName.c_str(), std::ios::out | std::ios::binary);
    outfile.write(reinterpret_cast<const char *>(&decodedData), sizeof decodedData);
    return true;
}



