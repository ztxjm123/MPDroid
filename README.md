# MPDroid
An Android Application RiskEvaluation Framework Basedon Minimum Permission Set Identiﬁcation
# In MPDroid
We train our model through benign apps and malignant apps, and then use our model to recommend permissions for other apps.
# File declaration
MPDroid\AppRiskEvaluation\topicNumGet is the source code of topicNumGet.jar in MPDroid\AppRiskEvaluation\MinPermissionRecognition.
# data 
The Data set that the code needs is in the data folder,malicious app,benign app,and training dataset.

File path:MPDroid\Data
# Prerequisites
1.Import the MPDroid\Data\analyzedata.sql into your local MySQL.

2.Change the database password in MPDroid\AppRiskEvaluation\MinPermissionRecognition\common.py(sql_password='').We need a password to connect to your database.

3.Debug and install the Python libraries you need in your code
# Run
D:MPDroid\AppRiskEvaluation\MinPermissionRecognition>python main.py

