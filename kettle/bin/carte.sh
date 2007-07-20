#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=${KETTLE_HOME}
if [ -z "$KETTLE_HOME" ]; then
  BASEDIR=$(dirname $0)
fi
CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine-3.0.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find $BASEDIR/libext -type f -name "*.jar"` `find $BASEDIR/libext -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 256m to higher values in case you run out of memory.  **
# ******************************************************************

OPT="-Xmx256m -cp $CLASSPATH -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD"

# ***************
# ** Run...    **
# ***************

java $OPT org.pentaho.di.www.Carte "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
