package com.example.youssefhossam.graphsvisualisationapp;

/**
 * Created by Youssef Hossam on 07/10/2017.
 */
public class AnamolyHandler extends Thread{

    boolean isSpeechMode;
    Boolean isVoiceActivityDone,isMatabPressed,isHofraPressed,isTakserPressed,isGhlatPressed,isHarakaPressed;
    Integer userVoiceReply;
    AnamolyHandler(boolean isSpeechMode, Boolean isVoiceActivityDone, Boolean isMatabPressed,Boolean isHofraPressed,Boolean isTakserPressed,Boolean isGhlatPressed, Boolean isHarakaPressed,Integer userVoiceReply)
    {
        this.isSpeechMode=isSpeechMode;
        this.isVoiceActivityDone=isVoiceActivityDone;
        this.isMatabPressed=isMatabPressed;
        this.isHofraPressed=isHofraPressed;
        this.isTakserPressed=isTakserPressed;
        this.isGhlatPressed=isGhlatPressed;
        this.isHarakaPressed=isHarakaPressed;
        this.userVoiceReply=userVoiceReply;
    }

    @Override
    public void run()
    {
        if(isSpeechMode)
            waitForSpeech();
        else
            waitForButtonPress();


    }

    void waitForSpeech()
    {
        while(!isVoiceActivityDone);
        processAnamoly(userVoiceReply);

    }

    void waitForButtonPress()
    {
        while (!(isMatabPressed || isHofraPressed || isGhlatPressed || isTakserPressed || isHarakaPressed)) ;

        if (isMatabPressed) {
            processAnamoly(MainActivity.MATAB);
        } else if (isHofraPressed) {
            processAnamoly(MainActivity.HOFRA);
        } else if (isTakserPressed) {
            processAnamoly(MainActivity.TAKSER);
        } else if (isGhlatPressed) {
            processAnamoly(MainActivity.GHLAT);
        } else {
            processAnamoly(MainActivity.HARAKA);
        }
        isMatabPressed=isHarakaPressed=isTakserPressed=isGhlatPressed=isHofraPressed=false;
    }

    void processAnamoly(int userChoice)
    {

    }

}
