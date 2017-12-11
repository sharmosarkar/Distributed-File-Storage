#!/usr/bin/env python

# For python version interop #
from __future__ import with_statement

# For logging #
import logging
# For OS, System and errors
import os
import sys
import errno
# For finding module name #
import inspect
# For JSON Handling #
import json
# For getting current username & it's uid, gid
from pwd import getpwnam
import getpass
# Import fusepy #
from fuse import FUSE, FuseOSError, Operations
# Import py4j JavaGateway for communications with JAVA #
from py4j.java_gateway import JavaGateway, GatewayParameters

class DropBox(Operations):
    # ============================= #
    # Constructor for Class Dropbox #
    # ============================= #
    def __init__(self, root, app=None):
        self.root = root
        self.app = app
        self.cache = {}
        self.default_dir_permission = 755
        self.default_dir_type = 40000
        self.default_file_permission = 644
        self.default_file_type = 100000
        self.local_file_path = os.path.join(self.root, "dump")
        self.local_file_path += "/"
        self.invalidate_cache_flag = 0
        if not os.path.exists(self.local_file_path):
            os.makedirs(self.local_file_path)
        self.init_create_mnt_dir()

    # ==================================== #
    # Create mount point entry in database #
    # ==================================== #
    def init_create_mnt_dir(self):
        filename, pathname = self.get_file_and_path_name(self.root)
        st1 = self.getFstat((filename, pathname))
        if st1 == {}:
            dir_mode = os.stat(self.root).st_mode
            username = getpass.getuser()
            uid = str(getpwnam(username).pw_uid)
            gid = str(getpwnam(username).pw_gid)
            is_dir = True
            # Logging #        
            self.log_func_name()
            logging.info("\npath = %s,type=%s\nmode = %s,type=%s,\nuid=%s,gid=%s\nis_dir=%s" % (
                 str(self.root), type(self.root), str(dir_mode), type(dir_mode), str(uid), str(gid), str(is_dir)))
            stat = json.loads(self.app.createDir(filename, pathname, dir_mode, uid, gid, is_dir))
            self.cache_update((filename, pathname), stat)
        else:
            logging.info("Mount point already exists")
    
    # ============== #
    # cache function #
    # ============== #
    def is_valid_fstat(self, st):
        return all(value == 0 for value in st.values())

    def cache_lookup(self, key):
        #print '===============CACHE LOOKUP================'
        #print self.cache
        #print '===============CACHE LOOKUP================'
        if key in self.cache:
            return self.cache[key]
        else:
            return None

    def cache_update(self, key, val):
        #print '===============CACHE UPDATE================'
        self.cache[key] = val
        #print self.cache
        #print '===============CACHE UPDATE================'

    def getFstat(self, key):
        if self.invalidate_cache_flag == 0:
            fStat = self.cache_lookup(key)
        else:
            fStat = json.loads(self.app.getFstat(key[0], key[1]))
            print 'CLOUD !!!!!'
        if fStat is not None:
            return fStat
        fStat = json.loads(self.app.getFstat(key[0], key[1]))
        if not self.is_valid_fstat(fStat):
            self.cache_update(key, fStat)
        else:
            fStat = {}
        return fStat

    def isdir(self, path):
        filename,pathname = self.get_file_and_path_name(path)
        fStat = self.getFstat((filename, pathname))
        if fStat[u'st_isdir'] == 1:
            return True
        return False

    def get_file_and_path_name(self, mnt_path):
        if mnt_path[-1] == "/":
            index = 2
        else:
            index = 1
        print "**********mount path********", mnt_path
        splitted = mnt_path.split("/")
        filename = splitted[-index]
        pathname = ("/".join(splitted[:-index]))+"/"
        print "*******Filename, Pathname*******", filename, type(filename), pathname
        if ".hidden" in filename:
            self.invalidate_cache_flag = 1
            print "CACHE INVALIDATED"
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
        mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
             str(mnt_path), type(mnt_path), str(mode), type(mode)))
        # TODO : 
        # Access check with mode. 
        # if not os.access(mnt_path, mode):
        #     raise FuseOSError(errno.EACCES)

    def chmod(self, path, mode):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s" % (
            str(mnt_path), type(mnt_path), str(mode), type(mode)))
        return os.chmod(local_file_name, mode)

    def chown(self, path, uid, gid):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nuid = %i,type=%s\ngid = %i,type=%s" % (
             str(mnt_path), type(mnt_path), uid, type(uid), gid, type(gid)))
        return os.chown(local_file_name, uid, gid)

    def getattr(self, path, fh=None):
        logging.info("\ngetattr")
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        if filename.startswith(".goutputstream"):
            local_file_name = os.path.join(self.local_file_path, filename)
            st1 = os.lstat(local_file_name)
            st = dict((key, getattr(st1, key)) for key in ('st_atime', 'st_ctime',
                'st_gid', 'st_mode', 'st_mtime', 'st_nlink', 'st_size', 'st_uid'))
        else:
            # Calling AWS Java Server #
            st1 = self.getFstat((filename, pathname))
            if st1 == {}:
                raise FuseOSError(errno.ENOENT)
            st = {}
            for key, val in st1.iteritems():
                if key in [u'st_gid', u'st_mode', u'st_nlink', u'st_size', u'st_uid']:
                    st[key] = st1[key]
                if key in [u'st_atime', u'st_ctime',  u'st_mtime']:
                    st[key] = (st1[key] * 1.0) / 1000
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nstat = %s,type=%s" % (
             str(mnt_path), type(mnt_path), str(st), type(st)))       
        return st

    def readdir(self, path, fh):
        mnt_path = self._get_mnt_path(path)
        print "Mount Path:", mnt_path
        if mnt_path[-1] != "/":
            pathname = mnt_path + "/"
        else:
            pathname = mnt_path
        print "Pathname:", pathname
        dirents = ['.', '..']
        dir_ent = self.app.readDir(pathname)
        # if os.path.isdir(mnt_path):
        dirents.extend(dir_ent)
        print "Directory entry received:", dirents
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\ndirents = %s,type=%s" % (
            str(mnt_path), type(mnt_path), str(dirents), type(dirents)))
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
        mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s\ndev = %s,type=%s" % (
             str(mnt_path), type(mnt_path), str(mode), type(mode), str(dev), type(dev)))
        return os.mknod(mnt_path, mode, dev)

    def rmdir(self, path):
        print "*****************RMDIR*************"
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        dir_ent = self.app.readDir(mnt_path + "/")
        print mnt_path, filename, pathname
        print dir_ent, type(dir_ent)
        if dir_ent:
            raise FuseOSError(errno.ENOTEMPTY)
        # Logging #
        self.log_func_name()        
        logging.info("\npath = %s,type=%s" % (str(mnt_path), type(mnt_path)))
        self.app.deleteDir(filename, pathname)
        return

    def mkdir(self, path, mode):
        mnt_path = self._get_mnt_path(path)
        # Conversion of -> 40755(octal) to 16879(decimal)
        dir_mode = int(str(self.default_dir_type + self.default_dir_permission), 8)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        username = getpass.getuser()
        uid = str(getpwnam(username).pw_uid)
        gid = str(getpwnam(username).pw_gid)
        is_dir = True
        # Logging #        
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s,\nuid=%s,gid=%s\nis_dir=%s" % (
             str(mnt_path), type(mnt_path), str(dir_mode), type(dir_mode), str(uid), str(gid), str(is_dir)))
        stat = json.loads(self.app.createDir(filename, pathname, dir_mode, uid, gid, is_dir))
        self.cache_update((filename, pathname), stat)
        return 

    def statfs(self, path):
        mnt_path = self._get_mnt_path(path)
        stv = os.statvfs(mnt_path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nstat = %s,type=%s" % (
             str(mnt_path), type(mnt_path), str(stv), type(stv))) 
        stat = dict((key, getattr(stv, key)) for key in ('f_bavail',
            'f_bfree',
            'f_blocks',
            'f_bsize',
            'f_favail',
            'f_ffree',
            'f_files',
            'f_flag',
            'f_frsize',
            'f_namemax'))
        stat['f_bsize'] = 10240000
        return stat

    def unlink(self, path):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (str(mnt_path), type(mnt_path)))
        # return os.unlink(mnt_path)
        if filename.startswith(".goutputstream"):
            local_file_name = os.path.join(self.local_file_path, filename)
            os.unlink(local_file_name)
            return None
        else:
            if not self.isdir(mnt_path):
                self.app.deleteFile(filename, pathname)
                return None
            else:
                raise FuseOSError(errno.EISDIR)

    def symlink(self, name, target):
        mnt_name = self._get_mnt_path(name)
        logging.info("\nname = %s,type=%s\ntarget = %s,type=%s"(
             str(mnt_name), type(mnt_name), str(target), type(target)))
        return os.symlink(target, mnt_name)

    def rename(self, old, new):
        mnt_path_old = self._get_mnt_path(old)
        mnt_path_new = self._get_mnt_path(new)
        filename, pathname = self.get_file_and_path_name(mnt_path_old)
        local_file_name_old = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name_old = local_file_name_old + "_#_" + pathname.replace("/","_")
        filename, pathname = self.get_file_and_path_name(mnt_path_new)
        local_file_name_new = os.path.join(self.local_file_path, filename)
        local_file_name_new = local_file_name_new + "_#_" + pathname.replace("/","_")
        print "RENAME :", local_file_name_old, local_file_name_new
        # Logging #
        self.log_func_name()
        logging.info("\nold = %s,type=%s\nnew = %s,type=%s" % (
            str(local_file_name_old), type(local_file_name_old), str(local_file_name_new), type(local_file_name_new)))
        ret = os.rename(local_file_name_old, local_file_name_new)
        username = getpass.getuser()
        uid = str(getpwnam(username).pw_uid)
        gid = str(getpwnam(username).pw_gid)
        is_dir = False
        size = os.stat(local_file_name_new).st_size
        file_mode = os.stat(local_file_name_new).st_mode
        stat = json.loads(self.app.updateFile(filename,
                pathname,
                self.local_file_path,
                size,
                file_mode, uid, gid, is_dir))
        print "Stat after write: ", stat
        self.cache_update((filename, pathname), stat)
        return ret;

    def link(self, target, name):
        mnt_name = self._get_mnt_path(name)
        mnt_target = self._get_mnt_path(target)
        # Logging #
        self.log_func_name()
        logging.info("\nsource = %s,type=%s\nlink_target = %s,type=%s" % (
             str(mnt_name), type(mnt_name), str(mnt_target), type(mnt_target)))
        return os.link(mnt_name, mnt_target)

    def utimens(self, path, times=None):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        # Logging #     
        self.log_func_name()
        logging.info("\npath = %s,type=%s\ntimes = %s,type=%s, \nlocal_file_path=%s" % (
             str(mnt_path), type(mnt_path),str(times), type(times), str(self.local_file_path)))
        return os.utime(local_file_name, times)

    # =============================================================================================== #
    #                                           File functions                                        #
    # =============================================================================================== #
    def open(self, path, flags):
        logging.info("\ncreate file")
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
            stat = json.loads(self.app.readFile(filename, pathname, self.local_file_path))
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nflags = %s,type=%s,\nlocal_file_name=%s,\nlocal_file_path=%s" % (
             str(mnt_path), type(mnt_path), str(flags), type(flags), str(local_file_name), str(self.local_file_path)))
        return os.open(local_file_name, flags)

    def create(self, path, mode, fi=None):
        logging.info("\ncreate file")
        mnt_path = self._get_mnt_path(path)
        # Conversion of -> 100644(octal) to 33279(decimal)
        file_mode = int(str(self.default_file_type + self.default_file_permission), 8)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        ret = os.open(local_file_name, os.O_WRONLY | os.O_CREAT, mode) 
        username = getpass.getuser()
        uid = str(getpwnam(username).pw_uid)
        gid = str(getpwnam(username).pw_gid)
        is_dir = False
        size = os.stat(local_file_name).st_size
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nmode = %s,type=%s,\nlocal_file_name=%s,\nlocal_file_path=%s" % (
             str(mnt_path), type(mnt_path), str(mode), type(mode), str(local_file_name), str(self.local_file_path)))
        if not filename.startswith(".goutputstream"):
            stat = json.loads(self.app.createFile(filename,
                pathname,
                self.local_file_path,
                size,
                file_mode, uid, gid, is_dir))
            self.cache_update((filename, pathname), stat)
        return ret

    def read(self, path, length, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nlength = %s,type=%s\noff = %i,type=%s" % (
             str(mnt_path), type(mnt_path), str(length), type(length), offset, type(offset)))
        return os.read(fh, length)

    def write(self, path, buf, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        mnt_path = self._get_mnt_path(path)
        # Conversion of -> 100644(octal) to 33279(decimal)
        file_mode = int(str(self.default_file_type + self.default_file_permission), 8)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        print "Write:",local_file_name
        print "Write:", buf
        ret = os.write(fh, buf)
        username = getpass.getuser()
        uid = str(getpwnam(username).pw_uid)
        gid = str(getpwnam(username).pw_gid)
        is_dir = False
        size = os.stat(local_file_name).st_size
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\noffset = %s,type=%s,\nlocal_file_name=%s,\nlocal_file_path=%s" % (
             str(mnt_path), type(mnt_path), str(offset), type(offset), str(local_file_name), str(self.local_file_path)))
        if not filename.startswith(".goutputstream"):
            stat = json.loads(self.app.updateFile(filename,
                pathname,
                self.local_file_path,
                size,
                file_mode, uid, gid, is_dir))
            print "Stat after write: ", stat
            self.cache_update((filename, pathname), stat)
        return ret

    def truncate(self, path, length, fh=None):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        local_file_name = os.path.join(self.local_file_path, filename)
        if not filename.startswith(".goutputstream"):
            local_file_name = local_file_name + "_#_" + pathname.replace("/","_")
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s\nlength = %s,type=%s" % (
             str(mnt_path), type(mnt_path), str(length), type(length)))
        with open(local_file_name, 'r+') as f:
            f.truncate(length)

    def flush(self, path, fh):
        mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(mnt_path), type(mnt_path)))
        return os.fsync(fh)

    def release(self, path, fh):
        mnt_path = self._get_mnt_path(path)
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(mnt_path), type(mnt_path)))
        ret = os.close(fh)
        #os.unlink(local_file_name)
        # if self.invalidate_cache_flag == 1:
        #     self.invalidate_cache_flag = 0
        return ret

    def fsync(self, path, fdatasync, fh):
        mnt_path = self._get_mnt_path(path)
        filename, pathname = self.get_file_and_path_name(mnt_path)
        print "Fsync:", filename
        if filename.startswith(".goutputstream"):
            path = os.path.join(self.local_file_path, filename)
            print "Fsync:", path
        # Logging #
        self.log_func_name()
        logging.info("\npath = %s,type=%s" % (
             str(mnt_path), type(mnt_path)))
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
    # ====================================================== #
    # Initialise py4j connection with the Java ICFSConnector #
    # ====================================================== #
    gateway = JavaGateway(gateway_parameters=GatewayParameters(address='127.0.1.1', port=25333))
    app = gateway.entry_point
    # ========================================= #
    # Initialise FUSE API and enable fuse mount #
    # ========================================= #
    FUSE(DropBox(root, app), mntpoint, nothreads=True, foreground=True)

if __name__ == '__main__':
    main(sys.argv[2], sys.argv[1])