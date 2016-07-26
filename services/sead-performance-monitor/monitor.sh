#!/bin/sh

echo "Running SEAD service performance testing.."

out_file=$1
url="https://seadva.d2i.indiana.edu/sead-c3pr/api/repositories"

iterations=5

STARTTIME=`date +%s%N`

for ((i=0;i<iterations;i++))
    do
        curl -si $url > $out_file
        content="$(curl -si "$url" | grep HTTP/1.1 | tail -1 | awk {'print $2'})"
        if [ ! -z $content ] && [ $content -eq 200 ]
        then
                echo "Valid URL"
        else
                echo “Invalid URL”
                echo  "Error occurred while querying $url : $(cat $out_file)" | tr -d \\r  | mail -s "SEAD Service Response Failure Alert" charmadu@umail.iu.edu
                exit 1
        fi
    done

ENDTIME=`date +%s%N`
elapsed=$((($ENDTIME - $STARTTIME)/(2*$iterations*1000000)))
echo "Time elapsed : " $elapsed

#200 is the pre-determinde upper limit of the time that takes to query $url from seadva-test instance
if [ "$elapsed" -gt 200 ]
then
        echo  "Response time delay("$elapsed"ms) when querying $url : $(cat $out_file)" | tr -d \\r  | mail -s "SEAD Service Response Time Alert" charmadu@umail.iu.edu
fi
