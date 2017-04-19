import com.google.gson.Gson;
import ontology.Types;

/**
 * Created by Daniel on 11.04.2017.
 */
public class ServerCommTest {

    public static void mockInit(ClientComm cc, boolean isTraining) {
        SerializableStateObservation sso = new SerializableStateObservation();
        sso.gameState = SerializableStateObservation.State.INIT_STATE;
        Gson gson = new Gson();

        String json = gson.toJson(sso);

        cc.commState = cc.processCommandLine(json);
        cc.processLine(json);

        if(cc.commState == ClientComm.COMM_STATE.INIT_END)
        {
            //We can work on some initialization stuff here.
            System.out.println("init done");

        }
    }

    public static void mockAct(ClientComm cc, boolean isTraining) {
        SerializableStateObservation sso = new SerializableStateObservation();
        sso.gameState = SerializableStateObservation.State.ACT_STATE;
        Gson gson = new Gson();

        String json = gson.toJson(sso);

        cc.commState = cc.processCommandLine(json);
        cc.processLine(json);

        if(cc.commState == ClientComm.COMM_STATE.ACT_END)
        {
            //This is the place to think and return what action to take.
            String rndAction = Types.ACTIONS.ACTION_NIL.toString();
            System.out.println(rndAction);

        }
    }

    public static void main(String[] args)
    {
        ClientComm ccomm = new ClientComm();

        mockInit(ccomm, true);
        mockAct(ccomm,true);
    }
}
