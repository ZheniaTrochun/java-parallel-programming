package com.yevhenii.service.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class FileUtils {

    public static Optional<String> readPart(File file, int offset, int length) {
        try {
            int finalLength = (int) Math.min(offset + length, file.length()) - offset;
            FileChannel fileChannel = FileChannel.open(file.toPath());
            ByteBuffer buff = ByteBuffer.allocate(finalLength);
            fileChannel.read(buff, offset);

            return Optional.of(new String(buff.array()).trim());
        } catch (IOException e) {
            e.printStackTrace();

            return Optional.empty();
        }
    }
}
