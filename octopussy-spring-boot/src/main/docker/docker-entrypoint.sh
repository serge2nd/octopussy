#!/bin/sh
set -exf

set -u && mkdir -p "$EXT_DIR" && set +u

[ -n "$JDBC_DRIVER_URLS" ] && for url in $JDBC_DRIVER_URLS; do
    filepath="${EXT_DIR%/}/${url##*/}"
    [ -s "$filepath" ] || (wget -O "$filepath" "$url" || rm -f "$filepath")
done

# shellcheck disable=SC2154
java -Dloader.path="$EXT_DIR" -Dloader.debug=true -jar "${dist.file}"
