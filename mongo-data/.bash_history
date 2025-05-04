apt update && apt install -y mongodb-database-tools
mongodump --uri="mongodb://localhost:27017" --out="C:/Users/HP-G9/Desktop/dump"
docker cp C:/Users/HP-G9/Desktop/dump mongo:/dump
exit
