//
// Created by michael on 01.11.20.
//

#include <string>
#include <iostream>
#include <iostream>
#include <dirent.h>
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
            LOGD ("%s\n", ent->d_name);
        }
        closedir (dir);
    } else {
        /* could not open directory */
        perror ("Dir does not exist");
        return EXIT_FAILURE;
    }
    return false;
}
