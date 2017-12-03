# Distributed-File-Storage

To Checkout Remote Database::
1) Download and Install https://dev.mysql.com/downloads/workbench/
2) Login Credentials
	host = "distributedfilesystemmetadata.cnomz21blikj.us-west-2.rds.amazonaws.com"
	port = 3306
	dbname = "DistributedFileSystem"
	user = "admin123"
	password = "admin123"
	NOTE ::: Click "Ignore" if pormted for Empty Schema Warning
	PLEASE DO NOT ENTER ANY DATA DIRECTLY INTO THE REMOTE DATABASE
	MAKE SURE TO LOGOUT OF THE TOOL AS SOON AS YOU ARE DONE (this would prevent the throttling on out Free-Tier AWS RDS Database)
	
To Checkout the Python Code ::
1) Download and Install Python https://www.python.org/downloads/
2) Verify your Python installation by
	$python -version
	$python which
3) run the Setup Script
	$sh setup.sh
4) run the AWS_Conn.py script
	Example :: (get the file stats)
	$python AWS_Conn.py read_file_fstat output.txt / 1000
5) Edit the appropiate parameters on the Test_Database.java file
6) Comment or Uncomment the appropiate methods from main()
6) Compiling and Running Test_Database.java should return appropiate messages 
