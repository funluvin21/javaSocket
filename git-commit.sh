#! /bin/bash

if [ $# -lt 1 ] ; then
	echo 'need write comment behind git-commit.sh '
	exit 1
fi

MY_ARGUMENT=`echo $1 | tr '[A-Z]' '[a-z]'`;

#echo MY_ARGUMENT : ${MY_ARGUMENT}

git commit -m "${MY_ARGUMENT}"



