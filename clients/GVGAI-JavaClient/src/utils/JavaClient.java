package utils; /**
 * Created by Daniel on 04.03.2017.
 */

/**
 *  -----  DO NOT MODIFY THIS CLASS -----
 */

import utils.ClientComm;

/**
 * Class to generate and run the client's side of the communication protocol.
 */
public class JavaClient
{
    public static void main(String[] args)
    {
        //I should receive as argument the agent to use.
        String agentName = "";
        if(args.length == 1)
        {
            agentName = args[0];
        }

        ClientComm ccomm = new ClientComm(agentName);
        ccomm.startComm();
    }
}
