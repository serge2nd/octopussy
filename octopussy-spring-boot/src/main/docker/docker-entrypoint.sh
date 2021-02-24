#!/bin/sh
set -exf

ext0_dir=/extra0
ext1_dir=/extra1
mkdir -p "$ext0_dir" "$ext1_dir"

[ -n "$JDBC_DRIVER_URLS" ] && for url in $JDBC_DRIVER_URLS; do
    filepath="$ext1_dir/${url##*/}"
    [ -s "$filepath" ] || (wget -O "$filepath" "$url" || rm -f "$filepath")
done

# shellcheck disable=SC2154
java $JVM_OPTS -Dloader.path="$ext0_dir,$ext1_dir" -Dloader.debug=true -jar "${dist.file}"
