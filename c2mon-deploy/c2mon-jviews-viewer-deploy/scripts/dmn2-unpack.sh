#!/bin/sh

#########################################################################
#                                                                       #
# This script is executed by build server after the mvn build task      #
# is finished. This script is executed remotely (ssh) on the remote web # 
# server host and its job is to unpack the product (tar.gz) into the    #
# destination folder                                                    #
#                                                                       #
#########################################################################

# folder where tim2-jviews-viewer should be unpacked
DEST_FOLDER=~/public_html/dmn2-jviews-viewer
PREV_FOLDER=${DEST_FOLDER}-prev

#expected location of tar.gz file
TAR_FILE=/tmp/c2mon-jviews-viewer-dmn2-dmn2.tar.gz

#check if dest. folder exists. if yes- delete it
if [ -d ${DEST_FOLDER} ]; then 
  echo "copying ${DEST_FOLDER} to ${PREV_FOLDER}"
  if [ -d ${PREV_FOLDER} ]; then
    rm -rf ${PREV_FOLDER}
  fi 
  cp -R ${DEST_FOLDER} ${PREV_FOLDER}
  rm -rf ${DEST_FOLDER}
fi

#recreate dest. folder
mkdir ${DEST_FOLDER}

echo "unpacking file ${TAR_FILE} to ${DEST_FOLDER}"
#unpack tarbo
tar -xf ${TAR_FILE} -C ${DEST_FOLDER}

#delete tarbo
rm -rf ${TAR_FILE}