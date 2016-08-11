#!/bin/sh
# check mongod
if [ ! -f /var/run/mongodb/mongod.pid ] || ! ps -p `cat /var/run/mongodb/mongod.pid` > /dev/null ; then
  echo "Mongo main database is down"
  echo "Mongo main database is down" | mail -s "SEAD-VA Production Instance - MongoDB Alert" charmadu@umail.iu.edu
fi
# check mongod_raw
if [ ! -f /var/run/mongodb/mongod_ore.pid ] || ! ps -p `cat /var/run/mongodb/mongod_ore.pid` > /dev/null ; then
  echo "Mongo ORE database is down"
  echo "Mongo ORE database is down" | mail -s "SEAD-VA Production Instance - MongoDB Alert" charmadu@umail.iu.edu
fi
