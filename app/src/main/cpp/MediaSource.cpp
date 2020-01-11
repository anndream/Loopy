//
// Created by Michael on 2020-01-11.
//

#ifndef LOOPY_MEDIASOURCE_CPP
#define LOOPY_MEDIASOURCE_CPP

extern "C" {
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
}

#include <cstdint>
#include <android/asset_manager.h>
#include <Constants.h>
#include <fstream>
#include "logging.h"

// wrapper class for file stream
class  MediaSource {
public:
    MediaSource(const std::string filePath) {
        const char *x = filePath.c_str();

        LOGD("Opened %s", x);
        source.open(filePath, std::ios::in | std::ios::binary);
    }
    ~MediaSource() {
        source.close();
    }
    int read(uint8_t *buffer, int buf_size) {
        // read data to buffer
        source.read((char *)buffer, buf_size);
        // return how many bytes were read
        return source.gcount();
    }
    int64_t seek(int64_t offset, int whence) {
        if (whence == AVSEEK_SIZE) {
            // FFmpeg needs file size.
            int oldPos = source.tellg();
            source.seekg(0,std::ios::end);
            int64_t length = source.tellg();
            // seek to old pos
            source.seekg(oldPos);
            return length;
        } else if (whence == SEEK_SET) {
            // set pos to offset
            source.seekg(offset);
        } else if (whence == SEEK_CUR) {
            // add offset to pos
            source.seekg(offset, std::ios::cur);
        } else {
            // do not support other flags, return -1
            return -1;
        }
        // return current pos
        return source.tellg();
    }
private:
    std::ifstream source;
};
#endif //LOOPY_MEDIASOURCE_CPP
