# MPDroid
The source code of MPDroid, an Android application risk evaluation tool which
based on description minimum permission set identiﬁcation.
# The principal modules in MPDroid
1)data
The data set involved in the MPDroid is in the data folder, including malicious app set, benign app set etc.
File path:MPDroid\Data

2)Principal code modules 
The tool includes 4 principal modules, which are described as follows:
1.	Over-declared Permission Identification (Over_declared_Per_Identify.jar). This module identifies the over-declared permissions of an app, and includes the benign and malicious app data sets involved in the paper. Its output is the initial description-minimum permission set.
2.	Support-Filtering (SupportFiltering.jar). This module filters the low-support permissions in the app. Its output is the final description-minimum permission set.
3.	Risk Calculation (RiskCalculation.jar). This module calculates the app risk. Its output is the risky permissions and risk values.
4.	MinPermissionIdentification (main module). This module identifies the minimum permission of the app, and further identifies the risk permissions and calculate risk values.

# Prerequisites
1.Import the MPDroid\Data\analyzedata.sql into your local MySQL.

2.Change the database password in MPDroid\MinPermissionIdentification\common.py(sql_password=local mysql password''). MPDroid need a password to connect to your database.

3.Install and debug the Python libraries which you need in your code.

4.In mpdroid\MinPermissionIdentification\ReData.py we back up the database. This requires configuring the environment variables, which means matching mysqldump to the path

# Run
cmd cd D:\MPDroid\MinPermissionIdentification ->

D:\MPDroid\MinPermissionIdentification >python main.py
