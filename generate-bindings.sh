# Configure these paths according to your environment
export JEXTRACT_HOME=/tmp/jextract-22
export INCLUDE_DIR=/usr/include
export LIB_PATH=/usr/lib/x86_64-linux-gnu/libzbar.so

${JEXTRACT_HOME}/bin/jextract \
      --output src/main/java \
      --target-package io.github.doblon8.jzbar.bindings \
      --header-class-name zbar \
      --library ${LIB_PATH} \
      --include-dir ${INCLUDE_DIR}/zbar \
      ${INCLUDE_DIR}/zbar.h
