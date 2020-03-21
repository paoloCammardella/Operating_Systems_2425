echo "Start wait device"
while ! (adb shell getprop sys.boot_completed)
do
  echo "Non si e' avviato..."
  sleep 3
done
sleep 10
echo "device avviato e pronto."
#echo "Sblocco il device"
#adb shell input keyevent 82
#echo "Disabling animations."
#adb shell settings put global window_animation_scale 0.0
#adb shell settings put global transition_animation_scale 0.0
#adb shell settings put global animator_duration_scale 0.0
