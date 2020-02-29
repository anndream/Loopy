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
class MediaSource {
public:

    MediaSource() {
    }

    ~MediaSource() {
        source.close();
    }

    void open(const std::string &filePath) {
        const char *x = filePath.c_str();
        LOGD("Opened %s", x);
        source.open(filePath, std::ios::in | std::ios::binary);
    }

    int read(uint8_t *buffer, int buf_size) {
        // read data to buffer
        source.read((char *) buffer, buf_size);
        // return how many bytes were read
        int count = source.gcount();
        LOGD("read...buf_size: %s, gcount: %s", (char *)buf_size, (char *)count);
        return count;
    }

    int64_t seek(int64_t offset, int whence) {
        LOGD("seek...offset: %s, whence: %s, filesize: %s", (char *) offset, (char *) whence, (char *)AVSEEK_SIZE);
        if (whence == AVSEEK_SIZE) {
            // FFmpeg needs file size.
            int oldPos = source.tellg();
            source.seekg(0, std::ios::end);
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
        return 0;
    }

//    int read(void *opaque, uint8_t *buf, int buf_size) {
//
//        auto asset = (AAsset *) opaque;
//        int bytesRead = AAsset_read(asset, buf, (size_t)buf_size);
//        return bytesRead;
//    }

//    int64_t seek(void *opaque, int64_t offset, int whence){
//
//        auto asset = (AAsset*)opaque;
//
//        // See https://www.ffmpeg.org/doxygen/3.0/avio_8h.html#a427ff2a881637b47ee7d7f9e368be63f
//        if (whence == AVSEEK_SIZE) return AAsset_getLength(asset);
//        if (AAsset_seek(asset, offset, whence) == -1){
//            return -1;
//        } else {
//            return 0;
//        }
//    }

private:
    std::ifstream source;
};

#endif //LOOPY_MEDIASOURCE_CPP
