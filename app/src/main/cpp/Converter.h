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
};

#endif //LOOPY_CONVERTER_H
