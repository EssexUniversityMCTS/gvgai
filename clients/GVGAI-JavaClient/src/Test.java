/**
 * Created by dperez on 18/03/2015.
 */
public class Test
{

    // MOCKS DATA PASSED TO THE CLIENT
    public static void mockInit(ClientComm cc, boolean isTraining)
    {
        String line = "INIT " + isTraining;
        cc.processCommandLine(line);
        cc.processLine(line);
        
        line = "Game#1.0#0#NO_WINNER#false#665#798#133#";
        cc.processLine(line);
        
        line = "Actions#ACTION_LEFT,ACTION_RIGHT,ACTION_DOWN,ACTION_UP,ACTION_NIL#";
        cc.processLine(line);
        
        line = "Avatar#266.0#133.0#1.0#ACTION_NIL#1,2;3,4#";
        cc.processLine(line);
        
        line = "s0#111111,101011,100001,110001,111111#";
        cc.processLine(line);
        
        line = "s1#000000,000000,010000,000000,000000#";
        cc.processLine(line);
        
        line = "s2#000000,000000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "s3#000000,000100,001110,001110,000000#";
        cc.processLine(line);
        
        line = "s4#000000,000000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "s5#000000,000000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "s6#000000,000000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "s7#000000,000000,000010,000000,000000#";
        cc.processLine(line);
        
        line = "s8#000000,010000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "s9#000000,000000,000100,000100,000000#";
        cc.processLine(line);
        
        line = "s10#000000,000000,000000,000000,000000#";
        cc.processLine(line);
        
        line = "INIT-END 998";
        cc.processLine(line);
    }

    // MOCKS DATA PASSED TO THE CLIENT
    public static void mockAct(ClientComm cc, int gameTick)
    {
        String line = "ACT";
        cc.processCommandLine(line);

        line = "Game#1.0#" + gameTick + "#NO_WINNER#false#";
        cc.processLine(line);

        line = "Avatar#266.0#133.0#1.0#ACTION_NIL#1,2;3,4#";
        cc.processLine(line);

        line = "s0#111111,101011,100001,110001,111111#";
        cc.processLine(line);

        line = "s1#000000,000000,010000,000000,000000#";
        cc.processLine(line);

        line = "s2#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s3#000000,000100,001110,001110,000000#";
        cc.processLine(line);

        line = "s4#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s5#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s6#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s7#000000,000000,000010,000000,000000#";
        cc.processLine(line);

        line = "s8#000000,010000,000000,000000,000000#";
        cc.processLine(line);

        line = "s9#000000,000000,000100,000100,000000#";
        cc.processLine(line);

        line = "s10#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "ACT-END 38";
        cc.processLine(line);
    }


    public static void mockEnd(ClientComm cc, int gameTick)
    {
        String line = "ENDGAME";
        cc.processCommandLine(line);

        line = "Game#1.0#" + gameTick + "#NO_WINNER#true#";
        cc.processLine(line);

        line = "Avatar#266.0#133.0#1.0#0##";
        cc.processLine(line);

        line = "Avatar#266.0#133.0#1.0#ACTION_NIL#1,2;3,4#";
        cc.processLine(line);

        line = "s0#111111,101011,100001,110001,111111#";
        cc.processLine(line);

        line = "s1#000000,000000,010000,000000,000000#";
        cc.processLine(line);

        line = "s2#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s3#000000,000100,001110,001110,000000#";
        cc.processLine(line);

        line = "s4#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s5#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s6#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "s7#000000,000000,000010,000000,000000#";
        cc.processLine(line);

        line = "s8#000000,010000,000000,000000,000000#";
        cc.processLine(line);

        line = "s9#000000,000000,000100,000100,000000#";
        cc.processLine(line);

        line = "s10#000000,000000,000000,000000,000000#";
        cc.processLine(line);

        line = "ENDGAME-END 38";
        cc.processLine(line);
    }
    
    public static void main(String[] args)
    {
        ClientComm ccomm = new ClientComm();

        mockInit(ccomm, true);

        ccomm.game.printToFile(0);
        ccomm.avatar.printToFile(0);

        mockAct(ccomm, 1);
        mockAct(ccomm, 2);
        mockAct(ccomm, 3);
        mockAct(ccomm, 4);

        mockEnd(ccomm, 500);


        mockInit(ccomm, false);

        ccomm.game.printToFile(1);
        ccomm.avatar.printToFile(1);

        mockAct(ccomm, 1);
        mockAct(ccomm, 2);
        mockAct(ccomm, 3);
        mockAct(ccomm, 4);

        mockEnd(ccomm, 500);
    }
}
