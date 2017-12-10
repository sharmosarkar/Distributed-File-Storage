#!/usr/bin/env python

# For python version interop #
from __future__ import with_statement

import os
import sys
import errno
# For logging #
import logging
# For finding module name #
import inspect
import json
import ast

# Import fusepy #
from fuse import FUSE, FuseOSError, Operations
from py4j.java_gateway import JavaGateway, GatewayParameters

class DropBox(Operations):

    # ============================= #
    # Constructor for Class Dropbox #
    # ============================= #
    def __init__(self, root, app=None):
        self.root = root
        self.app = app
        self.cache = {}

    ## cache function
    def cache_lookup(self, key):
        print '()()()()()()()()   CACHE LOOKUP ()()()()()()()()'
        print self.cache
        print '()()()()()()()()   CACHE LOOKUP ()()()()()()()()'
        if key in self.cache:
            return self.cache[key]
        else:
            return None

    def cache_update(self, key, val):
        print '()()()()()()()()   CACHE UPDATE ()()()()()()()()'
        self.cache[key] = val
        print self.cache
        print '()()()()()()()()   CACHE UPDATE ()()()()()()()()'

    def getFstat(self, key):
        fStat = self.cache_lookup(key)
        if fStat is not None:
            return fStat
        fStat = self.app.getFstat(key[0], key[1])
        self.cache_update(key, fStat)
        return fStat

    def isdir(self, path):
        filename,pathname = self.get_file_and_path_name(path)
        fStat = json.loads(self.getFstat((filename, pathname)))
        if fStat[u'st_isdir'] == 1:
            return True
        return False

    def get_file_and_path_name(self, get_mnt_path):
        if get_mnt_path[-1] == "/":
            index = 2
        else:
            index = 1
        print "**********mount path********", get_mnt_path
        splitted = get_mnt_path.split("/")
        filename = splitted[-index]
        pathname = ("/".join(splitted[:-index]))+"/"
        print "*******Filename, Pathname*******", filename, pathname
        return filename, pathname

    # =============================================================================================== #
    #                                       Helper Functions                                          #
    # =============================================================================================== #

    # ===================== #
    # Get mount point path  #
    # ===================== #
    def _get_mnt_path(self, partial):
        if partial.startswith("/"):
            partial = partial[1:]
        path = os.path.join(self.root, partial)
        return path

    # ========================================================== #
    # Log Function name, filename and line number for infoging  #
    # ========================================================== #     
    def log_func_name(self):
        func = inspect.currentframe().f_back.f_code
        logging.info("\n%s in %s:%i" % (func.co_name, func.co_filename, func.co_firstlineno))

    # =============================================================================================== #
    #                                      File System functions                                      #
    # =============================================================================================== #
    def access(self, path, mode):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #

        # Algorithm : #
        # 1. get fstat of the file #
        # 2.     Compare mode with fstat.st_mode #

        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(mode), type(mode)))
        # if not os.access(get_mnt_path, mode):
        #     raise FuseOSError(errno.EACCES)

    def chmod(self, path, mode):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        
        # Algorithm : #
        # 1. get fstat of the file #
        # 2. Compare mode with fstat.st_mode #
        #     if yes updatefile/dir with the new mode in the cloud metadata table #

        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
            str(get_mnt_path), type(get_mnt_path), str(mode), type(mode)))
        return os.chmod(get_mnt_path, mode)

    def chown(self, path, uid, gid):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #

        # Algorithm : #
        # 1. get fstat of the file #
        # 2. Compare uid,gid and if the user has permissions or not #
        #     if yes updatefile/dir with the uid,gid in the cloud metadata table ##
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nuid = %i,type=%s\ngid = %i,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), uid, type(uid), gid, type(gid)))
        return os.chown(get_mnt_path, uid, gid)

    def getattr(self, path, fh=None):
        # if fh == None:
        #     print "fh None"
        #     print path
        # else:
        #     print "fh not None"
        #     print "path :" + path
        #     print "fh : " + fh

        get_mnt_path = self._get_mnt_path(path)
        #st = os.lstat(get_mnt_path)
        #print st
        #print type(st)
        filename, pathname = self.get_file_and_path_name(get_mnt_path)

        # Calling AWS Java Server #
        ##############st1 = json.loads(self.app.getFstat(filename, pathname))
        st1 = json.loads(self.getFstat((filename, pathname)))
        print "************mystat************", st1, type(st1)
        # st2 = ast.literal_eval(json.dumps(st1))
        # print st2
        # print type(st2)
        st3 = {}
        for key, val in st1.iteritems():
            if key in [u'st_atime', u'st_ctime', u'st_gid', u'st_mode', u'st_mtime', u'st_nlink', u'st_size', u'st_uid'] :
                st3[key] = st1[key]
        print '*****************************************************************'
        print st3
        print type(st3)
        print '*****************************************************************'

        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nstat = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(st3), type(st3)))
    
        # temp = dict((key, getattr(st, key)) for key in ('st_atime',
        #     'st_ctime',
        #     'st_gid',
        #     'st_mode',
        #     'st_mtime',
        #     'st_nlink',
        #     'st_size',
        #     'st_uid'))

        # print "--------------------------------------------------"
        # print temp
        # print type(temp)
        # print "--------------------------------------------------"

        # return temp
        return st3


    def readdir(self, path, fh):

        get_mnt_path = self._get_mnt_path(path)

        # Algorithm #
        # 1. Call metadata readDir & GetDirents #
        # 2. Append Dirents #

        print "====================================="
        print "====================================="
        print "Mount Path:", get_mnt_path
        print "====================================="
        print "====================================="

        if get_mnt_path[-1] != "/":
            pathname = get_mnt_path + "/"
        else:
            pathname = get_mnt_path

        print "====================================="
        print "====================================="
        print "Pathname:", pathname
        print "====================================="
        print "====================================="

        dirents = ['.', '..']
        dir_ent = self.app.readDir(pathname)
        print "Directory entry received:", dir_ent

        # if os.path.isdir(get_mnt_path):
        if self.isdir(get_mnt_path):
            dirents.extend(dir_ent[2:])

        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\ndirents = %s,type=%s" % (
            str(get_mnt_path), type(get_mnt_path), str(dirents), type(dirents)))
        for r in dirents:
            yield r

    def readlink(self, path):
        pathname = os.readlink(self._get_mnt_path(path))
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (str(pathname), type(pathname)))
        if pathname.startswith("/"):
            # Path name is absolute, sanitize it.
            return os.path.relpath(pathname, self.root)
        else:
            return pathname

    def mknod(self, path, mode, dev):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s\ndev = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(mode), type(mode), str(dev), type(dev)))
        return os.mknod(get_mnt_path, mode, dev)

    def rmdir(self, path):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()        
        logging.info("\npath = %s,type=%s" % (str(get_mnt_path), type(get_mnt_path)))
        return os.rmdir(get_mnt_path)

    def mkdir(self, path, mode):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #        
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(mode), type(mode)))
        return os.mkdir(get_mnt_path, mode)

    def statfs(self, path):
        get_mnt_path = self._get_mnt_path(path)
        stv = os.statvfs(get_mnt_path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nstat = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(stv), type(stv))) 
        return dict((key, getattr(stv, key)) for key in ('f_bavail',
            'f_bfree',
            'f_blocks',
            'f_bsize',
            'f_favail',
            'f_ffree',
            'f_files',
            'f_flag',
            'f_frsize',
            'f_namemax'))

    def unlink(self, path):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (str(get_mnt_path), type(get_mnt_path)))
        return os.unlink(get_mnt_path)

    def symlink(self, name, target):
        get_mnt_name = self._get_mnt_path(name)
        logging.info("\nname = %s,type=%s\ntarget = %s,type=%s"(
             str(get_mnt_name), type(get_mnt_name), str(target), type(target)))
        return os.symlink(target, get_mnt_name)

    def rename(self, old, new):
        get_mnt_path_old = self._get_mnt_path(old)
        get_mnt_path_new = self._get_mnt_path(new)
        # Logg,type=%sg #
        self.log_func_name()
        logging.info("\nold = %s,type=%s\nnew = %s,type=%s" % (
            str(get_mnt_path_old), type(get_mnt_path_old), str(get_mnt_path_new), type(get_mnt_path_new)))
        return os.rename(get_mnt_path_old, get_mnt_path_new)

    def link(self, target, name):
        get_mnt_name = self._get_mnt_path(name)
        get_mnt_target = self._get_mnt_path(target)
        # Logging #
        self.log_func_name()
        logging.info("\nsource = %s,type=%s\nlink_target = %s,type=%s" % (
             str(get_mnt_name), type(get_mnt_name), str(get_mnt_target), type(get_mnt_target)))
        return os.link(get_mnt_name, get_mnt_target)

    def utimens(self, path, times=None):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #     
        self.log_func_name()
        logging.info("\npath = %s,type=%s\ntimes = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path),str(times), type(times)))
        return os.utime(get_mnt_path, times)

    # =============================================================================================== #
    #                                           File functions                                        #
    # =============================================================================================== #

    def open(self, path, flags):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nflags = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(flags), type(flags)))
        return os.open(get_mnt_path, flags)

    def create(self, path, mode, fi=None):
        get_mnt_path = self._get_mnt_path(path)

        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(mode), type(mode)))

        ret = os.open(get_mnt_path, os.O_WRONLY | os.O_CREAT, mode)
        return ret;

    def read(self, path, length, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nlength = %s,type=%s\noff = %i,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(length), type(length), offset, type(offset)))
        return os.read(fh, length)

    def write(self, path, buf, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nbuf = %s,type=%s\noff = %i,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(length), type(length), offset, type(offset)))
        return os.write(fh, buf)

    def truncate(self, path, length, fh=None):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nlength = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path), str(length), type(length)))
        with open(get_mnt_path, 'r+') as f:
            f.truncate(length)

    def flush(self, path, fh):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path)))
        return os.fsync(fh)

    def release(self, path, fh):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(get_mnt_path), type(get_mnt_path)))
        return os.close(fh)

    def fsync(self, path, fdatasync, fh):
        get_mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(get_mnt_path, type(get_mnt_path))))
        return self.flush(path, fh)

def main(mntpoint, root):
    # ======================================== #
    # Initialise logging library for CloudFUSE #
    # ======================================== #
    logging.basicConfig(format='%(asctime)s %(levelname)-8s %(message)s',
        filename='/var/log/cloudfuse.log',
        filemode='w',
        level=logging.INFO,
        datefmt='%Y-%m-%d %H:%M:%S')
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address='127.0.1.1', port=25333))
    app = gateway.entry_point
    # ========================================= #
    # Initialise FUSE API and enable fuse mount #
    # ========================================= #
    FUSE(DropBox(root, app), mntpoint, nothreads=True, foreground=True)

if __name__ == '__main__':
    main(sys.argv[2], sys.argv[1])