## Explanations:
The code includes:
- MyWatchFace (default activity as a WatchFace)
- AccelerometerService (main service - running sensor manager in this case)
- MyReceiver (a BroadcastReceiver - called when Alarm/Timer is triggered)
- RestartService (to restart the main service when it crashed/stopped)

**MyWatchFace**
Most of the code here is just programming how the watch would look like, when to update the minutes, etc. The important parts are under a few lines of code in  'onCreate' and 'startServices' to start the main service (AccelerometerService).

**AccelerometerService**
The AccelerometerService is programmed to start both a sensor manager and alarm manager (look under 'onStartCommand'). The AlarmManager is triggered every 10 seconds - calling **MyReceiver** every time it triggered - this should work with the Android 'doze' mode as well. *The AccelerometerService also streams sensor data to a PC via socket - ignore that part if you don't use it.*

**RestartService**
The RestartService is there to start the main service (in this case, AccelerometerService) if it crashed or stopped. RestartServices is initialized in the AndroidManifest.   
