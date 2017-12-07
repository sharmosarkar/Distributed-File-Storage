#!/usr/bin/env python

#from __future__ import with_statement

import os
import sys
import errno

from fuse import FUSE, FuseOSError, Operations

class DropBox(Operations):
    def __init__(self, root):
        self.root = root

    # ===================== #
    # Get mount point path  #
    # ===================== #
    def _get_mnt_path(self, partial):
        if partial.startswith("/"):
            partial = partial[1:]
        path = os.path.join(self.root, partial)
        return path

    # ================== #
    # Filesystem methods #
    # ================== #

    def access(self, path, mode):
        get_mnt_path = self._get_mnt_path(path)
        if not os.access(get_mnt_path, mode):
            raise FuseOSError(errno.EACCES)

    def chmod(self, path, mode):
        get_mnt_path = self._get_mnt_path(path)
        return os.chmod(get_mnt_path, mode)

    def chown(self, path, uid, gid):
        get_mnt_path = self._get_mnt_path(path)
        return os.chown(get_mnt_path, uid, gid)

    def getattr(self, path, fh=None):
        get_mnt_path = self._get_mnt_path(path)
        st = os.lstat(get_mnt_path)
        print st
        return dict((key, getattr(st, key)) for key in ('st_atime', 'st_ctime',
                     'st_gid', 'st_mode', 'st_mtime', 'st_nlink', 'st_size', 'st_uid'))

    def readdir(self, path, fh):
        get_mnt_path = self._get_mnt_path(path)

        dirents = ['.', '..']
        if os.path.isdir(get_mnt_path):
            dirents.extend(os.listdir(get_mnt_path))
        for r in dirents:
            yield r

    def readlink(self, path):
        pathname = os.readlink(self._get_mnt_path(path))
        if pathname.startswith("/"):
            # Path name is absolute, sanitize it.
            return os.path.relpath(pathname, self.root)
        else:
            return pathname

    def mknod(self, path, mode, dev):
        return os.mknod(self._get_mnt_path(path), mode, dev)

    def rmdir(self, path):
        get_mnt_path = self._get_mnt_path(path)
        return os.rmdir(get_mnt_path)

    def mkdir(self, path, mode):
        return os.mkdir(self._get_mnt_path(path), mode)

    def statfs(self, path):
        get_mnt_path = self._get_mnt_path(path)
        stv = os.statvfs(get_mnt_path)
        print stv
        return dict((key, getattr(stv, key)) for key in ('f_bavail', 'f_bfree',
            'f_blocks', 'f_bsize', 'f_favail', 'f_ffree', 'f_files', 'f_flag',
            'f_frsize', 'f_namemax'))

    def unlink(self, path):
        return os.unlink(self._get_mnt_path(path))

    def symlink(self, name, target):
        return os.symlink(target, self._get_mnt_path(name))

    def rename(self, old, new):
        return os.rename(self._get_mnt_path(old), self._get_mnt_path(new))

    def link(self, target, name):
        return os.link(self._get_mnt_path(name), self._get_mnt_path(target))

    def utimens(self, path, times=None):
        return os.utime(self._get_mnt_path(path), times)

    # File methods
    # ============

    def open(self, path, flags):
        get_mnt_path = self._get_mnt_path(path)
        print path, type(path)
        print flags, type(flags)
        return os.open(get_mnt_path, flags)

    def create(self, path, mode, fi=None):
        get_mnt_path = self._get_mnt_path(path)
        return os.open(get_mnt_path, os.O_WRONLY | os.O_CREAT, mode)

    def read(self, path, length, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        return os.read(fh, length)

    def write(self, path, buf, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        return os.write(fh, buf)

    def truncate(self, path, length, fh=None):
        get_mnt_path = self._get_mnt_path(path)
        with open(get_mnt_path, 'r+') as f:
            f.truncate(length)

    def flush(self, path, fh):
        return os.fsync(fh)

    def release(self, path, fh):
        return os.close(fh)

    def fsync(self, path, fdatasync, fh):
        return self.flush(path, fh)


def main(mntpoint, root):
    FUSE(DropBox(root), mntpoint, nothreads=True, foreground=True)

if __name__ == '__main__':
    main(sys.argv[2], sys.argv[1])
