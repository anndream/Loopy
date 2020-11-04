//
// Created by michael on 01.11.20.
//

#ifndef LOOPY_CONVERTER_H
#define LOOPY_CONVERTER_H


class Converter {
public:

    explicit  Converter();
    bool setFolder(const char *folderName);
    bool convertFolder();

private:
    const char *mFolder;
    bool doConversion(std::string name);
    std::string wav = ".wav";
    std::string mp3 = ".mp3";
    std::string ogg = ".ogg";

    inline bool endsWith(std::string const & value, std::string const & ending)
    {
        if (ending.size() > value.size()) return false;
        return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
    }
};

#endif //LOOPY_CONVERTER_H
