#!/usr/bin/env bash

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})
SRC_DIR=$(cd $SCRIPT_DIR/../src; pwd)

echo "SRC_DIR=$SRC_DIR"

cd $SRC_DIR/main/java

echo -n '  => Compiling proto for java... '
protoc --java_out=. \
 com/ctrip/ferriswheel/proto/workbook.proto \
 com/ctrip/ferriswheel/proto/action.proto \
 --descriptor_set_out=../resources/ferriswheel-proto.desc --include_imports \
&& echo 'Done!' || (echo 'Failed!'; exit 1)

cd $SCRIPT_DIR
echo 'All done!'
