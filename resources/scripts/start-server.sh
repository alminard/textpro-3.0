#!/bin/sh

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink 
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. $DIR/conf/config.properties 

LOCALCLASSPATH=$TEXTPROHOME
for JAR in `ls $TEXTPROHOME/lib/*.jar` ; do
        LOCALCLASSPATH=$LOCALCLASSPATH:$JAR;
done

$JAVA_HOME/bin/java -Dfile.encoding=UTF8 $TEXTPROMEM_server -cp "$LOCALCLASSPATH" eu.fbk.textpro.server.TCPServer $DIR/conf/config.properties
