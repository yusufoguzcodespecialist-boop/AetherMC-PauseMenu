#!/bin/sh
# Gradle wrapper launcher. Run with: sh ./gradlew <task>
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if [ -n "${JAVA_HOME:-}" ]; then
    JAVACMD="${JAVA_HOME}/bin/java"
else
    JAVACMD=java
fi

CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVACMD" ${JAVA_OPTS:-} ${GRADLE_OPTS:-} -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
