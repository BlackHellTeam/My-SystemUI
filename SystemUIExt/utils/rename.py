#!/usr/bin/env python
from os import rename, listdir, remove
import sys
from shutil import copy
action = sys.argv[1]
dir_path = "/home/jdsong/161/mtk6592_B2B_V5.1/vendor/lewa/apps/LewaSystemUIExt/library/res/layout"
temp_path = "/home/jdsong/161/mtk6592_B2B_V5.1/vendor/lewa/apps/LewaSystemUIExt/library/tempres/layout"
badprefix = "fake_"
if action == "makefile":
    fnames =  listdir(dir_path)
    from_path = dir_path
    to_path = temp_path
elif action == "eclipse":
    fnames = listdir(temp_path)
    from_path = temp_path
    to_path = dir_path
fnames = listdir(from_path)
print fnames
for fname in fnames:
    if fname.startswith(badprefix):
        print fname
        copy(from_path + "/"  + fname, to_path + "/" + fname)
        remove (from_path + "/" + fname)
