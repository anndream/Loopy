//
// Created by Michael on 2020-02-01.
//

#ifndef LOOPY_FFMPEGTEST_H
#define LOOPY_FFMPEGTEST_H

extern "C" {
#include <libavformat/avformat.h>
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
}

#include <cstdint>
#include <android/asset_manager.h>
#include <Constants.h>
#include <fstream>
#include "MediaSource.cpp"

class FFMpegTest {

public:

    FFMpegTest();

    int createFFmpeg(const char *filename);

    void seek(long seekPosition);

    void getPcmFloat(float *buffer);

    void releaseFFmpeg();
};


#endif //LOOPY_FFMPEGTEST_H
