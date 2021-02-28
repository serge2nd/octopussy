#!/bin/sh
set -exf

# Directories to extend the app classpath
ext0_dir=/extra0 # possible to mount RO
ext1_dir=/extra1 # possible to mount RW ($EXT_URLS will be downloaded here)
mkdir -p "$ext0_dir" "$ext1_dir"

# Download extensions if specified but absent
[ -n "$EXT_URLS" ] && for url in $EXT_URLS; do
    filepath="$ext1_dir/${url##*/}"
    [ -s "$filepath" ] || (wget -O "$filepath" "$url" || rm -f "$filepath")
done

# shellcheck disable=SC2154
java $JVM_OPTS -Dloader.path="$ext0_dir,$ext1_dir" -Dloader.debug=true -jar "${dist.file}"
