#
# make a zip archive from the source
#
./clean.sh
rm ~/KindletConcepts.zip
cd ..
zip -r ~/KindletConcepts KindletConcepts/ -x */.* */.settings/* *DS_Store

