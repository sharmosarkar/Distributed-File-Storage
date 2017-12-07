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

# Import fusepy #
from fuse import FUSE, FuseOSError, Operations

class DropBox(Operations):
	# ============================= #
	# Constructor for Class Dropbox #
	# ============================= #
	def __init__(self, root):
		self.root = root

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
	# Log Function name, filename and line number for debugging  #
	# ========================================================== #     
	def log_func_name(self):
		func = inspect.currentframe().f_back.f_code
		logging.debug("\n%s in %s:%i" % (func.co_name, func.co_filename, func.co_firstlineno))

	# =============================================================================================== #
	#                                      File System functions                                      #
	# =============================================================================================== #
	def access(self, path, mode):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nmode = %s" % (
			 str(get_mnt_path), str(mode)))
		if not os.access(get_mnt_path, mode):
			raise FuseOSError(errno.EACCES)

	def chmod(self, path, mode):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nmode = %s" % (
			str(get_mnt_path), str(mode)))
		return os.chmod(get_mnt_path, mode)

	def chown(self, path, uid, gid):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nuid = %i\ngid = %i" % (
			 str(get_mnt_path), uid, gid))
		return os.chown(get_mnt_path, uid, gid)

	def getattr(self, path, fh=None):
		get_mnt_path = self._get_mnt_path(path)
		st = os.lstat(get_mnt_path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nstat = %s" % (
			 str(get_mnt_path), str(st)))
		return dict((key, getattr(st, key)) for key in ('st_atime',
			'st_ctime',
			'st_gid',
			'st_mode',
			'st_mtime',
			'st_nlink',
			'st_size',
			'st_uid'))

	def readdir(self, path, fh):
		get_mnt_path = self._get_mnt_path(path)
		dirents = ['.', '..']
		if os.path.isdir(get_mnt_path):
			dirents.extend(os.listdir(get_mnt_path))
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\ndirents = %s" % (
			str(get_mnt_path), str(dirents)))
		for r in dirents:
			yield r

	def readlink(self, path):
		pathname = os.readlink(self._get_mnt_path(path))
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s" % (str(pathname)))
		if pathname.startswith("/"):
			# Path name is absolute, sanitize it.
			return os.path.relpath(pathname, self.root)
		else:
			return pathname

	def mknod(self, path, mode, dev):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nmode = %s\ndev = %s" % (
			 str(get_mnt_path), str(mode), str(dev)))
		return os.mknod(get_mnt_path, mode, dev)

	def rmdir(self, path):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()        
		logging.debug("\npath = %s" % (str(get_mnt_path)))
		return os.rmdir(get_mnt_path)

	def mkdir(self, path, mode):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #        
		self.log_func_name()
		logging.debug("\npath = %s\nmode = %s" % (
			 str(get_mnt_path), str(mode)))
		return os.mkdir(get_mnt_path, mode)

	def statfs(self, path):
		get_mnt_path = self._get_mnt_path(path)
		stv = os.statvfs(get_mnt_path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nstat = %s" % (
			 str(get_mnt_path), str(stv))) 
		return dict((key, getattr(stv, key)) for key in ('f_bavail', 'f_bfree',
			'f_blocks', 'f_bsize', 'f_favail', 'f_ffree', 'f_files', 'f_flag',
			'f_frsize', 'f_namemax'))

	def unlink(self, path):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s" % (str(get_mnt_path)))
		return os.unlink(get_mnt_path)

	def symlink(self, name, target):
		get_mnt_name = self._get_mnt_path(name)
		# Logging #
		self.log_func_name()
		logging.debug("\nname = %s\ntarget = %s"(
			 str(get_mnt_name), str(target)))
		return os.symlink(target, get_mnt_name)

	def rename(self, old, new):
		get_mnt_path_old = self._get_mnt_path(old)
		get_mnt_path_new = self._get_mnt_path(new)
		# Logging #
		self.log_func_name()
		logging.debug("\nold = %s\nnew = %s" % (
			str(get_mnt_path_old), str(get_mnt_path_new)))
		return os.rename(get_mnt_path_old, get_mnt_path_new)

	def link(self, target, name):
		get_mnt_name = self._get_mnt_path(name)
		get_mnt_target = self._get_mnt_path(target)
		# Logging #
		self.log_func_name()
		logging.debug("\nsource = %s\nlink_target = %s" % (
			 str(get_mnt_name), str(get_mnt_target)))
		return os.link(get_mnt_name, get_mnt_target)

	def utimens(self, path, times=None):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #    	
		self.log_func_name()
		logging.debug("\npath = %s\ntimes = %s" % (
			 str(get_mnt_path), str(times)))
		return os.utime(get_mnt_path, times)

	# =============================================================================================== #
	#                                           File functions                                        #
	# =============================================================================================== #

	def open(self, path, flags):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nflags = %s" % (
			 str(get_mnt_path), str(flags)))
		return os.open(get_mnt_path, flags)

	def create(self, path, mode, fi=None):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nmode = %s" % (
			 str(get_mnt_path), str(mode)))
		return os.open(get_mnt_path, os.O_WRONLY | os.O_CREAT, mode)

	def read(self, path, length, offset, fh):
		os.lseek(fh, offset, os.SEEK_SET)
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nlength = %s\noff = %i" % (
			 str(get_mnt_path), str(length), offset))
		return os.read(fh, length)

	def write(self, path, buf, offset, fh):
		os.lseek(fh, offset, os.SEEK_SET)
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nbuf = %s\noff = %i" % (
			 str(get_mnt_path), str(buf), offset))
		return os.write(fh, buf)

	def truncate(self, path, length, fh=None):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s\nlength = %s" % (
			 str(get_mnt_path), str(length)))
		with open(get_mnt_path, 'r+') as f:
			f.truncate(length)

	def flush(self, path, fh):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s" % (
			 str(get_mnt_path)))
		return os.fsync(fh)

	def release(self, path, fh):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s" % (
			 str(get_mnt_path)))
		return os.close(fh)

	def fsync(self, path, fdatasync, fh):
		get_mnt_path = self._get_mnt_path(path)
		# Logging #
		self.log_func_name()
		logging.debug("\npath = %s" % (
			 str(get_mnt_path)))
		return self.flush(path, fh)

def main(mntpoint, root):
	# ======================================== #
	# Initialise logging library for CloudFUSE #
	# ======================================== #
	logging.basicConfig(filename='/var/log/cloudfuse.log', filemode='w',level=logging.DEBUG)
	# ========================================= #
	# Initialise FUSE API and enable fuse mount #
	# ========================================= #
	FUSE(DropBox(root), mntpoint, nothreads=True, foreground=True)

if __name__ == '__main__':
	main(sys.argv[2], sys.argv[1])