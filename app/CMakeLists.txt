cmake_minimum_required(VERSION 3.10)

include_directories(src/main/cpp/)
# TODO this needs to not have to be done by hand!
set(FFMPEG_DIR /Volumes/Work/repos/misc/ffmpeg/build/${ANDROID_ABI})

add_library(native-lib

        SHARED

        src/main/cpp/native-lib.cpp
        src/main/cpp/AudioEngine.cpp
        src/main/cpp/AudioCallback.cpp
        src/main/cpp/DataSource.h
        src/main/cpp/StorageDataSource.cpp
        src/main/cpp/LockFreeQueue.h
        src/main/cpp/utils/logging.h
        src/main/cpp/NDKExtractor.cpp
        src/main/cpp/Constants.h
        src/main/cpp/OpenGLFunctions.cpp
        src/main/cpp/Player.cpp
        src/main/cpp/Mixer.h
        src/main/cpp/IRenderableAudio.h
        src/main/cpp/utils/UtilityFunctions.cpp
        src/main/cpp/ObserverChain.h
        src/main/cpp/ObserverChain.cpp
        src/main/cpp/FFMpegExtractor.h
        src/main/cpp/FFMpegExtractor.cpp
        )

set (TARGET_LIBS log android oboe GLESv2)

add_definitions(-DUSE_FFMPEG=0)
#target_sources( native-lib PRIVATE src/main/cpp/audio/NDKExtractor.cpp )
set (TARGET_LIBS ${TARGET_LIBS} mediandk)


#set(CMAKE_CXX_STANDARD 14)

# Set the path to the Oboe directory.
set(OBOE_DIR /volumes/work/repos/android/oboe)

# Add the Oboe library as a subdirectory.
add_subdirectory(${OBOE_DIR} ./oboe)

# Specify the path to the Oboe header files.
include_directories(${OBOE_DIR}/include ${FFMPEG_DIR}/include)

# FFMpeg

add_library( avformat SHARED IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavformat.so)
add_library( avutil SHARED IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavutil.so)
add_library( avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libavcodec.so)
add_library( swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${FFMPEG_DIR}/lib/libswresample.so)

# end FFMpeg

target_link_libraries( native-lib ${TARGET_LIBS} avformat avutil avcodec swresample)
#target_compile_options(native-lib
#        PRIVATE -std=c++14 -Wall -Werror "$<$<CONFIG:RELEASE>:-Ofast>")
