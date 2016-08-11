this=`dirname "$0"`
HOME=`cd "$this";pwd`

start_script=$HOME"/mongo_monitor.sh"
log_file=$HOME"/mongo_cron_log.txt"

echo "Log file location is "$log_file""

chmod +x $start_script

#write out current crontab
crontab -l > mongocron

#echo new cron into cron file
echo "00 */3 * * * "$start_script" > "$log_file" 2>&1" >> mongocron

#install new cron file
crontab mongocron

rm mongocron
