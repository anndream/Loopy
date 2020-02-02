//
// Created by Michael on 2020-02-01.
//

#include "FFMpegTest.h"



AVPacket packet;
AVCodecContext *codecContext = NULL;
AVFormatContext *formatContext;
AVFrame *frame = NULL;
SwrContext *swrContext = NULL;

int audio_stream_index = -1;
int ret;
uint8_t *localBuffer;

int out_sample_rate = 48000;

int FFMpegTest::createFFmpeg(const char *filename)
{
    int ret;
    AVCodec *dec;

    frame = av_frame_alloc();
    av_register_all();

    avformat_open_input(&formatContext, filename, NULL, NULL);
//    avformat_find_stream_info(formatContext, NULL);

    // select the audio stream
    audio_stream_index = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &dec, 0);

    // create decoding context
    codecContext = avcodec_alloc_context3(dec);
    avcodec_parameters_to_context(codecContext, formatContext->streams[audio_stream_index]->codecpar);

    // init the audio decoder
    avcodec_open2(codecContext, dec, NULL);
    swrContext = swr_alloc();

    // we assume here that the audio file is a 44100 Hz stereo audio file
    swr_alloc_set_opts(swrContext, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_FLT, out_sample_rate,
                       codecContext->channel_layout, codecContext->sample_fmt, codecContext->sample_rate, 0,
                       NULL);
    swr_init(swrContext);

    return 0;
}

void FFMpegTest::seek(long seekPosition)
{
    av_seek_frame(formatContext, -1, seekPosition, 0);
}

void FFMpegTest::getPcmFloat(float *buffer)
{
    // init :
    int i, ch, dataSize;
    bool extraxted = false;
    float sample = 0;

    // extract :
    while (!extraxted && av_read_frame(formatContext, &packet) >= 0)
    {
        if (packet.stream_index == audio_stream_index)
        {
            // send the packet with the compressed data to the decoder
            ret = avcodec_send_packet(codecContext, &packet);

            // read all the output frames (in general there may be any number of them
            while (ret >= 0)
            {
                ret = avcodec_receive_frame(codecContext, frame);
                if (ret == AVERROR(EAGAIN))
                {
                    LOGW("AVERROR(EAGAIN)\n");
                    break;
                }
                else if (ret == AVERROR_EOF)
                {
                    LOGW("AVERROR_EOF\n");
                    break;
                }

                dataSize = av_get_bytes_per_sample(codecContext->sample_fmt);

                // resampling:
                int out_samples = (int) av_rescale_rnd(
                        swr_get_delay(swrContext, codecContext->sample_rate) + frame->nb_samples,
                        out_sample_rate,
                        codecContext->sample_rate,
                        AV_ROUND_UP);
                av_samples_alloc(&localBuffer, NULL, 2, out_samples, AV_SAMPLE_FMT_FLT, 0);
                int numberSampleOutputPerChannel = swr_convert(swrContext,
                                                               &localBuffer, out_samples,
                                                               (const uint8_t **) frame->extended_data, frame->nb_samples);

//                // position in microseconds:
//                long position = 1000000 * (packet.pts * ((float) stream->time_base.num / stream->time_base.den));
//                LOGD("position = %li", position);

                // copy from localBuffer to buffer:
                int a = 0;
                for (i = 0; i < numberSampleOutputPerChannel; i++)
                {
                    for (ch = 0; ch < codecContext->channels; ch++)
                    {
                        memcpy(&sample, &localBuffer[(codecContext->channels * i + ch) * dataSize], dataSize);
                        buffer[a] = sample;
                        a++;
                    }
                }

                av_freep(&localBuffer);

                // exit extract:
                extraxted = true;
            }
        }
    }
}

void FFMpegTest::releaseFFmpeg()
{
    packet.data = NULL;
    packet.size = 0;
    avcodec_free_context(&codecContext);
    swr_free(&swrContext);
}

