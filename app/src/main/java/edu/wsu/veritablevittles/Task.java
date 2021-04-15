package edu.wsu.veritablevittles;

// Import packages
import java.util.TimerTask;

//--------------------------------------------------------------------
// class Task
//--------------------------------------------------------------------
public class Task extends TimerTask
{

    //----------------------------------------------------------------
    // run
    //----------------------------------------------------------------
    public void run()
    {

        // Test if message sent
        if (ActMain.timerTaskHandler.sendEmptyMessage(0))
            System.out.println("[Task] Timer task  " +
                    "message sent to main thread.");
        else
            System.out.println("[Task] Error: " +
                    "timer task message NOT sent to main thread.");

    }

}
