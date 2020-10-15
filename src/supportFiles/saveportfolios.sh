#!/bin/sh

IN1="$1"
if [ $# -eq 0 ]
  then 
	echo "Enter the filename (today's Date)!! Exiting..."
	exit
fi
echo "Saving portfolio files to directory: $IN1"
read -p "Is this OK?" -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
	echo
	echo "mounting Lexar.."
	diskutil mount /Volumes/Lexar/
	echo "creating directory: "$IN1
	mkdir /Volumes/Lexar/supportFiles/$IN1
	echo "copying files over.."
	cp portfolio.* /Volumes/Lexar/supportFiles/$IN1/
	echo "done. UnMounting Lexar.."
	diskutil unmount /Volumes/Lexar/
	echo "done."
	exit
fi
echo
echo "exiting without writing..."
