import dropbox
import os
import sys
from dropbox.files import WriteMode
from dropbox.exceptions import ApiError, AuthError
import urllib2

DATA_DIR = 'Data'

LOCALFILE = os.path.join(DATA_DIR, 'xyz.txt')
BACKUPPATH = '/xyz.txt' # Keep the forward slash before destination filename
LOCAL_DOWNLOAD_FILE = os.path.join(DATA_DIR, 'output.txt')
REMOTE_FILE = 'output.txt'

# Uploads contents of LOCALFILE to Dropbox
def upload_file():
    with open(LOCALFILE, 'rb') as f:
        # We use WriteMode=overwrite to make sure that the settings in the file
        # are changed on upload
        print("Uploading " + LOCALFILE + " to Dropbox as " + BACKUPPATH + "...")
        try:
            dbx.files_upload(f.read(), BACKUPPATH, mode=WriteMode('overwrite'))
        except ApiError as err:
            # This checks for the specific error where a user doesn't have enough Dropbox space quota to upload this file
            if (err.error.is_path() and
                    err.error.get_path().error.is_insufficient_space()):
                sys.exit("ERROR: Cannot back up; insufficient space.")
            elif err.user_message_text:
                print(err.user_message_text)
                sys.exit()
            else:
                print(err)
                sys.exit()
    print 'Upload Complete !!!'


def download_file():
    print 'Downloading : ', REMOTE_FILE
    metadata, f = dbx.files_download('/' + REMOTE_FILE)
    print 'MetaData : \n', metadata
    print 'File Content : \n', f
    out = open(LOCAL_DOWNLOAD_FILE, 'wb')
    out.write(f.content)
    out.close()
    print 'Download Complete'

access_token = 'Fu8JUVLZcVAAAAAAAAACc6eiK3w9618gKRlXOfsvfVT-bWQ_L1HNcn9mfMsktv1B'
dbx = dropbox.Dropbox(access_token)
user_account = dbx.users_get_current_account()
print 'User Account \n', user_account

user_id = user_account.account_id
print 'USer Id : \n', user_id

space_info = dbx.users_get_space_usage()
print 'Storage : \n', space_info

# get directory contents
# print 'Files in Application'
for entry in dbx.files_list_folder('').entries:
    print entry

# upload a file
upload_file()

# download a file
download_file()
