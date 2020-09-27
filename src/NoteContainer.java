import java.util.ArrayList;

public class NoteContainer {
    public ArrayList<Note> notes = new ArrayList<>();
    public NoteContainer(int c) {
        for (int i = 0; i < c; i++) {
            notes.add(new Note());
        }
    }

    public ArrayList<Note> get() {
        return notes;
    }

    public Note getAt(int i) {
        return notes.get(i);
    }

    @Override
    public String toString() {
        return notes.toString();
    }
}
