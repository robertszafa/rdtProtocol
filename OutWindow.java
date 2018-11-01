/* This class encapsulates the sender window.
 * The window is represented by a Queue of packets and has a MAX_SIZE of 8.
*/
import java.util.Queue;
import java.util.LinkedList;

public class OutWindow {
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
