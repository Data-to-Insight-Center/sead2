#
# Java 7 & Tomcat 7 & Dockerfile
#

# Build (or rebuild) using
# docker build -t d2i/mm-docker-tomcat .

# pull base image.
FROM ubuntu:latest

# maintainer details
MAINTAINER D2I "d2i@indiana.edu"

WORKDIR /opt

# update source  
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe"> /etc/apt/sources.list  
RUN apt-get update \
  && apt-get install -y wget zip \
  && apt-get -y install curl git \
  && rm -rf /var/lib/apt/lists/* 
  
# Install JDK 7  
RUN cd /tmp &&  curl -L 'http://download.oracle.com/otn-pub/java/jdk/7u65-b17/jdk-7u65-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie; gpw_e24=Dockerfile' | tar -xz  
RUN mkdir -p /usr/lib/jvm  
RUN mv /tmp/jdk1.7.0_65/ /usr/lib/jvm/java-7-oracle/  
  
# Set Oracle JDK 7 as default Java  
RUN update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-7-oracle/bin/java 300     
RUN update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/java-7-oracle/bin/javac 300     
  
ENV JAVA_HOME /usr/lib/jvm/java-7-oracle/
  
# Install tomcat7  
RUN cd /tmp && curl -L 'http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.8/bin/apache-tomcat-7.0.8.tar.gz' | tar -xz  
RUN mv /tmp/apache-tomcat-7.0.8/ /opt/tomcat7/  
  
ENV CATALINA_HOME /opt/tomcat7  
ENV PATH $PATH:$CATALINA_HOME/bin
  
ADD tomcat7.sh /etc/init.d/tomcat7  
RUN chmod 755 /etc/init.d/tomcat7

#copy config file and change the configuration settings
RUN mkdir -p -m 755 /con-data/config/
WORKDIR /con-data/config
ADD config/sead-pdt.war /con-data/config/
ADD config/sead-mm.war /con-data/config/
RUN mkdir -p -m 755 /con-data/config/sead-mm-conf/
ADD config/sead-mm-conf/ /con-data/config/sead-mm-conf/
RUN mkdir -p -m 755 /con-data/config/sead-pdt-conf/
ADD config/sead-pdt-conf/ /con-data/config/sead-pdt-conf/

RUN cd /con-data/config/
RUN unzip sead-mm.war -d sead-mm
RUN unzip sead-pdt.war -d sead-pdt

RUN cp sead-mm-conf/default.properties sead-mm/WEB-INF/classes/org/sead/matchmaker/default.properties
RUN cp sead-pdt-conf/default.properties sead-pdt/WEB-INF/classes/org/seadpdt/util/default.properties

EXPOSE 8080

RUN cp -r sead-mm sead-pdt /opt/tomcat7/webapps/

ENTRYPOINT service tomcat7 start && tail -f /opt/tomcat7/logs/catalina.out