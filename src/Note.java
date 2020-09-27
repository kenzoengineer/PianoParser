import java.util.ArrayList;

public class Note {
    public ArrayList<Boolean> pressed = new ArrayList<>();;
    public Note() {
        pressed.add(false);
    }

    public void add(boolean b) {
        pressed.add(b);
    }

    public ArrayList<Boolean> get() {
        return pressed;
    }

    public boolean getAt(int i) {
        return pressed.get(i);
    }

    @Override
    public String toString() {
        return pressed.toString();
    }
}
