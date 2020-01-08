# MPDroid
An Android Application RiskEvaluation Framework Basedon Minimum Permission Set Identiﬁcation
# In MPDroid
We train our model through benign apps and malignant apps, and then use our model to recommend permissions for other apps.
# data 
The Data set that the code needs is in the data folder,malicious app,benign app,and training dataset.

File path:MPDroid\Data
# Prerequisites
1.Import the MPDroid\AppRiskEvaluation\analyzedata.sql into your local MySQL.

2.Change the database password in MPDroid\AppRiskEvaluation\Second_Step\common.py(sql_password='').We need a password to connect to your database.

3.Debug and install the Python libraries you need in your code
# Run
We call all the steps and functions in MPDroid\AppRiskEvaluation\Second_Step\main.py,so you just run it.
