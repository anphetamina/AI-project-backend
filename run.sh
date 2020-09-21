#!/bin/bash

# Run Mysql
/usr/bin/mysqld_safe --timezone=${DATE_TIMEZONE}&
sleep 5

# Setup db data
mysql -uroot -e "CREATE DATABASE teams;"
mysql -uroot -e "CREATE USER 'backend'@'localhost' IDENTIFIED BY 'password';"
mysql -uroot -e "GRANT ALL PRIVILEGES ON *.* TO 'backend'@'localhost';"
mysql -uroot -e "FLUSH PRIVILEGES;"

java -jar /app.jar
