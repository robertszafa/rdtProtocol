/*************************************
 * Filename:  Sender.java
 * Names: Robert Szafarczyk, Thepnathi Chindalaksanaloet
 * Student-IDs: 201307211, 201123978
 * Date: 30/10/18
 *************************************/
import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;

public class Sender extends NetworkHost {
    /*
     * Predefined Constant (static member variables):
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
     *  Message: Used to encapsulate the message coming from application layer
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
    // state information for the sender.

    /** NESTED CLASSES **/
    /* This class encapsulates the sender window.
     * The window is represented by a Queue of packets and has a MAX_SIZE of 8.
    */
    private static class OutWindow {
        private final int MAX_SIZE = 8;
        private int size;
        private Queue<Packet> window;

        public OutWindow() {
            window = new LinkedList<>();
            size = 0;
        }

        public Queue<Packet> getWindow() {
        	return window;
        }

        public void add(Packet p) {
            if (size < MAX_SIZE) {
                window.add(p);
                size++;
            }
        }

        public void remove() {
            if (size > 0) {
                window.remove();
                size--;
            }
        }

        public Packet peek() {
            if (size > 0) {
                return window.peek();
            }

            return null;
        }

        public Packet poll() {
            if (size > 0) {
                size--;
                return window.poll();
            }

            return null;
        }

        public int getSize() {
        	return size;
        }

        public boolean isFull() {
            return size == MAX_SIZE;
        }

        public boolean isEmpty() {
            return size == 0;
        }

    }

    /* This class encapsulates the sender message buffer.
     * The buffer holds messages that come from the application layer.
     * The buffer is represented by a Queue of MAX_SIZE 50.
    */
    private static class MessageBuffer {
        private final int MAX_SIZE = 50;
        private int size;
        private Queue<Message> buffer;

        public MessageBuffer() {
            buffer = new LinkedList<>();
            size = 0;
        }

        public void add(Message m) {
            if (size < MAX_SIZE) {
                buffer.add(m);
                size++;
            }
        }

        public void remove() {
            if (size > 0) {
                buffer.remove();
                size--;
            }
        }

        public Message peek() {
            if (size > 0) {
                return buffer.peek();
            }

            return null;
        }

        public Message poll() {
            if (size > 0) {
                size--;
                return buffer.poll();
            }

            return null;
        }

        public int getSize() {
        	return size;
        }

        public boolean isFull() {
            return size == MAX_SIZE;
        }

        public boolean isEmpty() {
            return size == 0;
        }
    }

    /** GLOBAL VARIABLES **/
    // value for increment = RTT * 2
    private final double TIMER_INCREMENT = 40;
    // buffer to hold the messages from application layer
    private static MessageBuffer msgBuffer = new MessageBuffer();
    // sender window
    private static OutWindow window = new OutWindow();
    private int baseSeqNum;
    private int nextSeqNum;

    // Also add any necessary methods (e.g. checksum of a String)

    // returns the sum of the integer value of the characters in message
    private int getIntValue(final String message) {
        int msgValue = 0;
        char[] msgChars = message.toCharArray();

        for (char c : msgChars) {
            msgValue += Character.getNumericValue(c);
        }

        return msgValue;
    }

    // returns the calculated checksum
    private int generateChecksum(int seqNum, int ackNum, int bytesSent) {
        return seqNum + ackNum + bytesSent;
    }

    // returns true if the checksum field in the packet is not equal
    // to the calculated checksum
    private boolean isCorrupted(Packet packet) {
        int checkSum = generateChecksum(packet.getSeqnum(), packet.getAcknum(),
                                            getIntValue(packet.getPayload()));
        if (packet.getChecksum() == checkSum) {
            return false;
        }
        return true;
    }

    // returns true if the acknowledged packet is in sender window
    private boolean isInWindow(Packet p) {
        if (baseSeqNum <= p.getAcknum()) {
            return true;
        }
        return false;
    }


    // This is the constructor.  Don't touch!
    public Sender(int entityName,
                       EventList events,
                       double pLoss,
                       double pCorrupt,
                       int trace,
                       Random random)
    {
        super(entityName, events, pLoss, pCorrupt, trace, random);
    }

    // This routine will be called whenever the application layer at the sender
    // has a message to  send.  The job of your protocol is to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving application layer.
    protected void Output(Message message) {
        if (!msgBuffer.isFull()) {
            msgBuffer.add(message);
        } else {
            System.out.println("Warning: Senders message buffer is full!");
            System.out.println("Exiting program...");
            System.exit(1);
            return;
        }

        if (!window.isFull()) {
            // extract message from buffer and construct the packet
            Message nextMsg = msgBuffer.poll();
            int nextBytesSent = getIntValue(nextMsg.getData());
            int nextCheckSum = generateChecksum(nextSeqNum, nextSeqNum,
                                                            nextBytesSent);
            Packet p = new Packet(nextSeqNum, nextSeqNum, nextCheckSum,
                                                        nextMsg.getData());
            window.add(p);
            udtSend(p);
            if (baseSeqNum == nextSeqNum) {
                // keep timer only for the baseSeqNum packet
                startTimer(TIMER_INCREMENT);
            }
            nextSeqNum++;
        }
    }

    // This routine will be called whenever a packet sent from the receiver
    // (i.e. as a result of udtSend() being done by a receiver procedure)
    // arrives at the sender.  "packet" is the (possibly corrupted) packet
    // sent from the receiver.
    protected void Input(Packet packet) {
        if (isCorrupted(packet) || !isInWindow(packet)) {
            return; // do nothing
        }

        // baseSeqNum packet was ACKed, remove that packet from window
        window.remove();
        baseSeqNum = packet.getAcknum() + 1;

        if (baseSeqNum == nextSeqNum) {
            // no more pkts in window
            stopTimer();
        }
        else {
            // start timer for next in-flight packet
            startTimer(TIMER_INCREMENT);
        }

        // add msg from buffer in place of the ACKed packet
        if (!msgBuffer.isEmpty()) {
            Message nextMsg = msgBuffer.poll();
            int nextBytesSent = getIntValue(nextMsg.getData());
            int nextCheckSum = generateChecksum(nextSeqNum, nextSeqNum,
                                                            nextBytesSent);
            Packet p = new Packet(nextSeqNum, nextSeqNum, nextCheckSum,
                                                        nextMsg.getData());
            window.add(p);
            udtSend(p);
            nextSeqNum++;
        }
    }

    // This routine will be called when the senders's timer expires (thus
    // generating a timer interrupt). You'll probably want to use this routine
    // to control the retransmission of packets. See startTimer() and
    // stopTimer(), above, for how the timer is started and stopped.
    protected void TimerInterrupt() {
        // retransmit all packets in window from baseSeqNum up to nextSeqNum
        startTimer(TIMER_INCREMENT);
        for (Packet p : window.getWindow()) {
            if (p == null) {
                break;
            }
            udtSend(p);
        }
    }

    // This routine will be called once, before any of your other sender-side
    // routines are called. It should be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of the sender).
    protected void Init() {
        baseSeqNum = 1;
        nextSeqNum = 1;
    }

}
