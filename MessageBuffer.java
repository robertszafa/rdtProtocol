import java.util.Queue;
import java.util.LinkedList;

public class MessageBuffer {
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
        return size > 0;
    }

}
