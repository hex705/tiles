#!/bin/sh
rsync -e ssh --verbose --delete --progress --size-only --stats --compress --recursive *.cgi deadpixel@heimdallr.dreamhost.com:deadpixel.ca/tiles
