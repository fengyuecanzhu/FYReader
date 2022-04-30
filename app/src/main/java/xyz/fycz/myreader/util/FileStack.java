/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.util;

import java.io.File;
import java.util.List;

/**
 * Created by newbiechen on 17-5-28.
 */

public class FileStack {

    private Node node = null;
    private int count = 0;

    public void push(FileSnapshot fileSnapshot){
        if (fileSnapshot == null) return;
        Node fileNode = new Node();
        fileNode.fileSnapshot = fileSnapshot;
        fileNode.next = node;
        node = fileNode;
        ++count;
    }

    public FileSnapshot pop(){
        Node fileNode = node;
        if (fileNode == null) return null;
        FileSnapshot fileSnapshot = fileNode.fileSnapshot;
        node = fileNode.next;
        --count;
        return fileSnapshot;
    }

    public int getSize(){
        return count;
    }

    //节点
    public class Node {
        FileSnapshot fileSnapshot;
        Node next;
    }

    //文件快照
    public static class FileSnapshot{
        public String filePath;
        public List<File> files;
        public int scrollOffset;
    }
}
