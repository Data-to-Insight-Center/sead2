SEAD MatchMaker Docker Implementation
=====================================

Seda-Matchmaker folder structure
---------------------------------
	--docker
		  --config
				 --config-files(sead-mm-conf/default.properties, sead-pdt-conf/default.properties)
				 --war files(sead-mm.war, sead-pdt.war)
		  --Dockerfile
		  --README.md
		  --tomcat7.sh

Steps to build and run MongoDB(The services require two running instances of mongoDB):
-------------------------------

Move to root of the docker directory and execute following commands.

cd docker
(open the docker quick terminal on this path)

(Its downloading mongodb from public docker hub and open two instances)
docker run -t -d --name mongodb1 -p 27017:27017 -v /data/docker_db1:/data/db mongo mongod --rest --httpinterface --smallfiles
docker run -t -d --name mongodb2 -p 27018:27017 -v /data/docker_db2:/data/configdb mongo mongod --rest --httpinterface --smallfiles

You can check weather two db instancces up and running
http://{DOCKER_IP}:27017/
http://{DOCKER_IP}:27018/

There is a config folder inside the docker folder. It contains the war files and configuration files. So based on your local settings feel free to edit the configuration file and then after bulid the docker file for an matchmaker app. (If config files and war files are not available in the config folder then build the project and copy those files and place it in the below path)

../docker/config/sead-mm-conf/default.properties
../docker/config/sead-pdt-conf/default.properties
../docker/config/sead-pdt.war
../docker/config/sead-mm.war

Build, deploy and run project on Tomcat:
----------------------------------------

docker build -t d2i/mm-docker-tomcat .
docker run -d --name sead-mm --link mongodb1:mongodb1 --link mongodb2:mongodb2 -p 8080:8080 d2i/mm-docker-tomcat

Now the API should be accessible through the following URL.

http://{DOCKER_IP}:8080/sead-mm/ro/matchingrepositories/rules

Once you done with your works and restart your machine. In another time if you want to see the api. You need to do the following.

Start the terminal with docker

docker start mongodb1(name of the the first mongodb instance) or docker restart mongodb1
docker start mongodb2(name of the the first mongodb instance) or docker restart mongodb2
(dont remove the above db containers then u will loose the data)

docker start sead-mm(name of the webapp) or docker restart sead-mm