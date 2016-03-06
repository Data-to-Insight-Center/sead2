#!/bin/sh
ROOT=$(pwd)

cd scripts
BUILD=$(pwd)
CLASSES=$(pwd)
SRC=$ROOT/src/main/java
RESOURCES=$ROOT/src/main/resources/edu/indiana/d2i/sead/matchmaker/

echo
echo "#########################################"
echo "#               codegen.sh              #"
echo "#########################################"
echo
echo "Generating java source code"
echo

curl -O http://central.maven.org/maven2/org/jsonschema2pojo/jsonschema2pojo-core/0.4.21/jsonschema2pojo-core-0.4.21.jar
curl -O http://central.maven.org/maven2/com/sun/codemodel/codemodel/2.6/codemodel-2.6.jar
curl -O http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.7.2/jackson-databind-2.7.2.jar
curl -O http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.7.2/jackson-core-2.7.2.jar
curl -O http://central.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.7.2/jackson-annotations-2.7.2.jar
curl -O http://central.maven.org/maven2/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar
curl -O http://central.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar

for i in $(ls $BUILD |grep ".jar"); do
        CLASSES=$CLASSES:$BUILD/$i
done

CP=:$CLASSPATH:$CLASSES:.
javac -classpath $CP -d $BUILD ../src/main/java/edu/indiana/d2i/sead/matchmaker/core/POJOFactory.java
java -classpath $CP edu.indiana.d2i.sead.matchmaker.core.POJOFactory $SRC $RESOURCES

rm *.jar
rm -rf edu

echo
echo "#########################################"
echo "#      POJO Successfully Generated      #"
echo "#########################################"
echo
echo