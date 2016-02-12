#!/bin/sh

SRC_JAVA=./src
BUILD=$(pwd)/target
LIB=$(pwd)/target/dependency
CLASSES=$BUILD/classes:$BUILD/generated-code/resources
BIN=$(pwd)/bin
RETVAL=0
prog="build.sh"

###################################################################

build() {
    mvn install -Dmaven.test.skip=true
    mvn dependency:copy-dependencies
    mkdir -p bin

echo "
#!/bin/sh
BUILD=$BUILD
LIB=$LIB
" > ./bin/Matchmaker.sh

echo '

for i in $(ls $LIB |grep ".jar"); do
        CLASSES=$CLASSES:$LIB/$i
done

for i in $(ls $BUILD |grep ".jar"); do
        CLASSES=$CLASSES:$BUILD/$i
done



if [ "$1" = "" ];
then
    echo
    echo "#########################################"
    echo "#             Matchmaker.sh             #"
    echo "#########################################"
    echo
    echo "$ Matchmaker.sh <properties_file>"
    echo
    exit 1
fi

echo
echo "Matchmaker server started..."
echo "Listening for incoming messages..."
echo

CP=:$CLASSPATH:$CLASSES:.
java -classpath $CP edu.indiana.d2i.sead.matchmaker.service.ServiceLauncher $1
' >> ./bin/Matchmaker.sh
chmod 755 ./bin/Matchmaker.sh




	return $RETVAL
}
###################################################################
clean(){
	rm -rf $BIN
	mvn clean
	return $RETVAL
}
###################################################################
case "$1" in
  clean)
        clean
        ;;
  *)
        #clean
        build
        RETVAL=$?
        ;;
esac

exit $RETVAL

