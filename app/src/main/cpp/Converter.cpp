//
// Created by michael on 01.11.20.
//

#include <string>
#include <iostream>
#include <iostream>
#include <dirent.h>
#include <cinttypes>
#include <media/NdkMediaExtractor.h>
#include <fstream>
#include <oboe/Oboe.h>
//#include <filesystem>
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
    if ((dir = opendir (mFolder)) != NULL) {
        LOGD("Dir exists");
        /* print all the files and directories within directory */
        while ((ent = readdir (dir)) != NULL) {
           char* c = ent->d_name;
            LOGD ("%s\n", c);
            doConversion(std::string(c));
        }
        closedir (dir);
    } else {
        /* could not open directory */
        LOGE("Dir does not exist");
        return EXIT_FAILURE;
    }
    return false;
}

bool Converter::doConversion(std::string name) {
    bool isValid = endsWith(name, wav) or endsWith(name, mp3) or endsWith(name,ogg);
    if (!isValid) {
        LOGD("Omitted from conversion: %s", name.c_str());
        return false;
    }

    std::string fullPath = std::string(mFolder) + name;
    LOGD("My string: %s", fullPath.c_str());

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
    } else {
        LOGD("Opened %s", fullPath.c_str());

    }
    stream.seekg(0, std::ios::end);
    long size = stream.tellg();
    LOGD("size %ld", size);
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
    LOGD("OutputBuffer: %zu\n", sizeof(outputBuffer));
    LOGD("Number of Samples: %i", numSamples);
    // The NDK decoder can only decode to int16, we need to convert to floats
    oboe::convertPcm16ToFloat(
            reinterpret_cast<int16_t *>(decodedData),
            outputBuffer.get(),
            bytesDecoded / sizeof(int16_t));


    return true;
}



