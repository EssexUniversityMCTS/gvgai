from ClientComm import ClientComm


class PythonClient:
    """
     * Python Client given an agent name (SampleRandomAgent by default)
    """

    def __init__(self, args):
        if len(args) == 1:
            agentName = args[0]
        else:
            agentName = "SampleRandomAgent"
        ccomm = ClientComm(agentName)
        ccomm.startComm()
