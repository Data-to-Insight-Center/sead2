this=`dirname "$0"`
HOME=`cd "$this";pwd`

start_script=$HOME"/monitor.sh"
log_file=$HOME"/cron_log.txt"
out_file=$HOME"/out.txt"

echo "Log file location is "$log_file""

chmod +x $start_script

#write out current crontab
crontab -l > textitcron

#echo new cron into cron file
echo "00 23 * * * "$start_script" "$out_file" > "$log_file" 2>&1" >> textitcron

#install new cron file
crontab textitcron

rm textitcron
