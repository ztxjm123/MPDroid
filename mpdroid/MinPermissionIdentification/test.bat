@echo off

echo 执行数据库备份
echo 输出为 %1
mysqldump -u root -p123456 analyzedata permissions permissionlog  > %1.sql
echo 备份执行完毕