from ClientComm import ClientComm


class PythonClient:
    """
     * Python Client given an agent name (SampleRandomAgent by default)
    """

    def __init__(self, args):
        if len(args) == 1:
            agentName = args[0]
        else:
            agentName = "Agent"

        print("start python client")
        ccomm = ClientComm(agentName)
        ccomm.startComm()

if __name__ == "__main__":
    lc = PythonClient("Agent")
