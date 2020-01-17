# MPDroid
The source code of MPDroid, an Android application risk evaluation tool which
based on description minimum permission set identiﬁcation.
# The principal modules in MPDroid
1)data
The data set involved in the MPDroid is in the data folder, including malicious app set, benign app set etc.
File path:MPDroid\Data

2)Principal code modules 
The tool includes 4 principal modules, which are described as follows:
1) Over-declared Permission Identification(corresponds to Over_declared_Per_Identify.jar.jar): It mainly identifies the over-declared permissions in the app, and it includes the benign and malicious app data sets etc involved in the paper. The output of this module is the initial description-minimum permission set.
2) Support-Filtering(corresponds to SupportFiltering.jar): On the basis of module 1), MPDroid filters the low-support permissions in the app based on the functionality point-permission set identification. The output of this module is the final description-minimum permission set.
3) Risk Calculation(corresponds to RiskCalculation.jar):An algorithm for calculating the app risk. The output is the risk value of app.
4) MinPermissionIdentification: Based on the final description-minimum permission set obtained in module 2), MPDroid identifies the minimum permission of the test app, and further calculates the risk permission, risk value, etc. of the test app based on the algorithm of module 3) .
# Prerequisites
1.Import the MPDroid\Data\analyzedata.sql into your local MySQL.

2.Change the database password in MPDroid\MinPermissionIdentification\common.py(sql_password=local mysql password''). MPDroid need a password to connect to your database.

3.Install and debug the Python libraries which you need in your code.

4.In mpdroid\MinPermissionIdentification\ReData.py we back up the database. This requires configuring the environment variables, which means matching mysqldump to the path

5.If you want to evaluate the new app data, first replace the first 20% of the mpdroid\ MinPermissionIdentification\ data\ topic_test.txt file with the new app data, and then import the corresponding app data into the normal_test_permission table of the database
# Run
cmd cd D:\MPDroid\MinPermissionIdentification ->

D:\MPDroid\MinPermissionIdentification >python main.py
