//
// Created by michael on 01.11.20.
//

#include <string>
#include <iostream>
#include <iostream>
#include <dirent.h>
#include <media/NdkMediaExtractor.h>
//#include <filesystem>
#include "Converter.h"
#include "utils/logging.h"

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

    return true;
}



