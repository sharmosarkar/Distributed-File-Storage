import pymysql
import pandas as pd
import sys
import json

host = "distributedfilesystemmetadata.cnomz21blikj.us-west-2.rds.amazonaws.com"
port = 3306
dbname = "DistributedFileSystem"
user = "admin123"
password = "admin123"

try:
    conn = pymysql.connect(host, user=user, port=port, passwd=password, db=dbname,
                           cursorclass=pymysql.cursors.DictCursor)
except pymysql.err.OperationalError:
    print 'Could not connect to Remote Database\n'


def read_fstat_destination(file_name, file_path, owner_id):
    query = 'SELECT f.inode, f.file_name, f.path, f.protection, f.owner_id, f.group_id, f.size, ' \
            'f.last_access_time, f.creation_time, f.modification_time, f.last_access_user, ' \
            'd.device_file_name, d.device_sequence, d.device_size, d.device_path, d.device_id, ' \
            'p.protection_bits ' \
            'FROM DistributedFileSystem.Fstat f INNER JOIN DistributedFileSystem.Destination d ' \
            'INNER JOIN DistributedFileSystem.Permission p ' \
            'WHERE f.inode=d.inode AND f.protection=p.protection AND ' \
            'f.file_name="{0}" AND f.path="{1}" AND f.owner_id={2}' \
        .format(file_name, file_path, owner_id)
    # print query
    fstat_data = pd.read_sql(query, con=conn).to_json(orient='records', date_format='iso')
    # fstat_data_json = fstat_data.to_json(orient='records', date_format='iso')
    print fstat_data


def store_file_filestat(file_fstat_data):
    file_name = file_fstat_data[0]
    path = file_fstat_data[1]
    protection = file_fstat_data[2]
    owner_id = file_fstat_data[3]
    group_id = file_fstat_data[4]
    acc_time = file_fstat_data[5]
    mod_time = file_fstat_data[6]
    creation_time = file_fstat_data[7]
    size = file_fstat_data[8]
    current_user = file_fstat_data[9]

    with conn.cursor() as cursor:
        # search if the file_name, path , owner exists in the Fstat table
        try:
            query = 'SELECT * FROM DistributedFileSystem.Fstat ' \
                    'WHERE file_name="{0}" AND path="{1}" AND owner_id="{2}"' \
                .format(file_name, path, owner_id)
            fstat_data = pd.read_sql(query, con=conn)

            if len(fstat_data) == 0:
                # Create a new record
                sql = "INSERT INTO `Fstat`" \
                      "(`file_name`, `path`, `protection`, `owner_id`, `group_id`, `size`, " \
                      "`last_access_time`, `creation_time`, `modification_time`, `last_access_user`)" \
                      "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
                cursor.execute(sql, (file_name, path, protection, owner_id, group_id, size,
                                     acc_time, creation_time, mod_time, current_user))
                conn.commit()
                print "Successful Operation :: Created New record on Fstat Table"
            else:
                inode_no = str(fstat_data["inode"][0])
                sql = "Update `Fstat` set " \
                      "`protection` = %s, `size` = %s, `last_access_time` = %s, `modification_time`= %s, " \
                      "`last_access_user` = %s" \
                      "WHERE `inode` = %s "
                cursor.execute(sql, (protection, size, acc_time, mod_time, current_user, inode_no))
                conn.commit()
                print "Successful Operation :: Updated record on Fstat Table"
        except Exception:
            print "Failed Operation"


if __name__ == '__main__':
    # for arg in sys.argv:
    #     print arg
    args = sys.argv
    if args[1] == 'read_file_fstat':
        # python AWS_Conn.py <function> <file_name> <file_path> <owner_id>
        # python AWS_Conn.py read_file_fstat file_name, file_path, user_id
        read_fstat_destination(args[2], args[3], args[4])
    # read_fstat_destination('output.txt', '/', 1000)
    elif args[1] == 'store_file_fstat':
        store_file_filestat(args[2:])
