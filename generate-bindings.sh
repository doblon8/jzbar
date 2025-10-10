export JEXTRACT_HOME=/tmp/jextract-22

${JEXTRACT_HOME}/bin/jextract \
      --output src/main/java \
      --target-package io.github.doblon8.jzbar.bindings \
      --header-class-name zbar \
      --library /usr/lib/x86_64-linux-gnu/libzbar.so \
      --include-dir /usr/include/zbar \
      /usr/include/zbar.h
