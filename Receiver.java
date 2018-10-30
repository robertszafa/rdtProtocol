/*************************************
 * Filename:  Receiver.java
 * Names:
 * Student-IDs:
 * Date:
 *************************************/
import java.util.Random;
import java.io.UnsupportedEncodingException;

public class Receiver extends NetworkHost

{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to application layer. Only call this in the
     *       Receiver class.
     *  double getTime()
     *       Returns the current time of the simulator.  Might be useful for
     *       debugging.
     *  String getReceivedData()
     *       Returns a String with all data delivered to receiving process.
     *       Might be useful for debugging. You should only call this in the
     *       Sender class.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from application layer
     *    Constructor:
     *      Message(String inputData):
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */

    // Add any necessary class variables here. They can hold
    // state information for the receiver.
    private int expectedSeqNum;

    // Also add any necessary methods (e.g. checksum of a String)
    private boolean isCorrupted(Packet packet) {
        int checkSum = generateChecksum(packet.getSeqnum(), packet.getAcknum(),
                                            getNumOfBytes(packet.getPayload()));
        if (packet.getChecksum() == checkSum) {
            return false;
        }
        return true;
    }

    private boolean isOutOfOrder(Packet p) {
        if (p.getSeqnum() != expectedSeqNum) {
            return true;
        }
        return false;
    }

    private int generateChecksum(int seqNum, int ackNum, int bytesSent) {
        return seqNum + ackNum + bytesSent;
    }

    private int getNumOfBytes(final String message) {
        int msgValue = 0;
        char[] msgChars = message.toCharArray();

        for (char c : msgChars) {
            msgValue += Character.getNumericValue(c);
        }

        return msgValue;
    }

    // This is the constructor.  Don't touch!
    public Receiver(int entityName,
                       EventList events,
                       double pLoss,
                       double pCorrupt,
                       int trace,
                       Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }


    // This routine will be called whenever a packet from the sender
    // (i.e. as a result of a udtSend() being done by a Sender procedure)
    // arrives at the receiver. Argument "packet" is the (possibly corrupted)
    // packet sent from the sender.
    protected void Input(Packet packet) {
        // System.out.println("RECEIVER RECEIVED: " + packet);

        if (!isCorrupted(packet) && !isOutOfOrder(packet)) {
            deliverData(packet.getPayload());

            Packet ack = new Packet(expectedSeqNum, expectedSeqNum,
                        generateChecksum(expectedSeqNum, expectedSeqNum, 0));
            udtSend(ack);
            expectedSeqNum++;
            // System.out.println("RECEIVER SENT: " + ack);
        }
        // packet corrupted or out of order, send ACK for highest rcvd seqNum
        else {
            expectedSeqNum--;
            Packet ackForLastSeq = new Packet(expectedSeqNum, expectedSeqNum,
                        generateChecksum(expectedSeqNum, expectedSeqNum, 0));
            udtSend(ackForLastSeq);
            expectedSeqNum++;
            // System.out.println("CORRUPTED - RECEIVER SENT: " + ackForLastSeq);
        }
    }



    // This routine will be called once, before any of your other receiver-side
    // routines are called. It should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the receiver).
    protected void Init() {
        expectedSeqNum = 0;
    }

}
