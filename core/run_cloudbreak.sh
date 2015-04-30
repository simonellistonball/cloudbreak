#!/bin/bash

main() {
      pushd `dirname $0` > /dev/null
      SCRIPTPATH=`pwd`
      cd ..
      GRADLE_PATH=`pwd`
      popd > /dev/null

      if [ "$CB_DEBUG_MODE" == "debug" ]; then
          params="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
          java $params -jar $SCRIPTPATH/build/libs/cloudbreak-*.jar
      elif [ "$CB_DEBUG_MODE" == "hotswap" ]; then
          $GRADLE_PATH/gradlew -p core bootRun
      else
          java -jar $SCRIPTPATH/build/libs/cloudbreak-*.jar
      fi
}

[[ "$0" == "$BASH_SOURCE" ]] && main "$@"
