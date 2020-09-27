import javax.sound.midi.*;
import javax.imageio.ImageIO;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;

public class Parser {
    static final int READ_HEIGHT = 30;//310
    static final int LOW_READ_HEIGHT = 77;
    static final int KEYS = 88;//54
    static final double KEY_WIDTH = 1920.0/KEYS;//12
    static final int FRAMES = 836; //3911 is the max frames
    static final int OFFSET = 20;//31
    static final int[] WHITE = {1,3,4,6,8,9,11,13,15,16,18,20,21,23,25,27,28,30,32,33,35,37,39,40,42,44,45,47,49,51,52,54,56,57,59,61,63,64,66,68,69,71,73,75,76,78,80,81,83,85,87,88};
    static final int[] BLACK = {0,1,1,2,2,2,3,3,4,4,4,5,5,6,6,6,7,7,8,8,8,9,9,10,10,10,11,11,12,12,12,13,13,14,14,14};

    static int binarySearch(int arr[], int l, int r, int x) {
        if (r >= l) {
            int mid = l + (r - l) / 2;
            if (arr[mid] == x)
                return mid;
            if (arr[mid] > x)
                return binarySearch(arr, l, mid - 1, x);
            return binarySearch(arr, mid + 1, r, x);
        }
        return -1;
    }

    public static Color[] parseImage(File file) throws Exception{
        Color[] temp = new Color[KEYS];
        BufferedImage img = ImageIO.read(file);
        int wCount = 0;
        int bCount = 0;
        for (int i = 0; i < KEYS; i++) {
            int pixel;
            if (binarySearch(WHITE,0,WHITE.length-1,i+1) != -1) {
                pixel = img.getRGB(wCount * 37 + 17,LOW_READ_HEIGHT);
                img.setRGB(wCount * 37 + 17,LOW_READ_HEIGHT,Color.RED.getRGB());
                wCount++;
            } else {
                pixel = img.getRGB((bCount+BLACK[bCount]) * 37 + 37,READ_HEIGHT);
                img.setRGB((bCount+BLACK[bCount]) * 37 + 37,READ_HEIGHT,Color.RED.getRGB());
                bCount++;
            }
            Color c = new Color(pixel, false);
            temp[i] = c;
           //System.out.println(i + " RGB: " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
        }
        if (file.equals(new File("eine klein.jpg"))) ImageIO.write(img,"png", new File("checker.png"));
        return temp;
    }

    public static String countUp(String s) {
        int num = Integer.parseInt(s);
        num++;
        String output = Integer.toString(num);
        while (output.length() < 4) {
            output = "0" + output;
        }
        return output;
    }
    public static void main(String[] args) throws Exception {
        DecimalFormat df = new DecimalFormat("0.0");

        Color[] originalColors = parseImage(new File("eine klein.jpg")); //frame0.png
        String[] letters = {"Ab", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G",};
        NoteContainer nc = new NoteContainer(KEYS);
        String str = "0000";
        for (int x = 0; x < FRAMES; x++) {
            System.out.print(df.format((x * 1.0 / (FRAMES - 1)) * 100) + "%\r");
            str = countUp(str);
            Color[] frameColors = parseImage(new File("einklein\\" + str + ".jpg"));

            for (int i = 0; i < originalColors.length; i++) {
                Color og = originalColors[i];
                Color test = frameColors[i];
                double red = og.getRed() < test.getRed() ?  (test.getRed() * 1.0) / og.getRed() : og.getRed() / (test.getRed() * 1.0);
                double green = og.getGreen() < test.getGreen() ?  (test.getGreen() * 1.0) / og.getGreen() : og.getGreen() / (test.getGreen() * 1.0);
                double blue = og.getBlue() < test.getBlue() ?  (test.getBlue() * 1.0) / og.getBlue() : og.getBlue() / (test.getBlue() * 1.0);
                if (str.equals("0434") && i == 33) {
                    System.out.println(red + green + blue);
                    System.out.println("og:" + og.getRed() + " " + og.getGreen() + " " + og.getBlue());
                }
                if (red + green + blue > 4) {
                    //System.out.println(notes[i % 12] + (i / 12 + 2) + ": " + df.format(red) + " " + df.format(green) + " " + df.format(blue) + "-------------" + df.format(red + green + blue));
                    nc.getAt(i).add(true);
                } else {
                    nc.getAt(i).add(false);
                }
            }
        }
        System.out.println("Processing Done");

        System.out.println("Starting image creation");
        BufferedImage output = new BufferedImage(FRAMES, KEYS, BufferedImage.TYPE_INT_RGB);
        for (int f = 0; f < FRAMES; f++) {
            for (int n = 0; n < KEYS; n++) {
                if (nc.getAt(n).getAt(f)) {
                    output.setRGB(f, n, Color.BLACK.getRGB());
                } else {
                    output.setRGB(f, n, Color.WHITE.getRGB());
                }
            }
        }
        ImageIO.write(output, "png", new File("output.png"));
        System.out.println("Image written");

        System.out.println("Initializing midi writer");

        Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ, 24);
        Track t = s.createTrack();
        byte[] b = {(byte) 0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte) 0xF7};
        SysexMessage sm = new SysexMessage();
        sm.setMessage(b, 6);
        MidiEvent me = new MidiEvent(sm, (long) 0);
        t.add(me);
        MetaMessage mt = new MetaMessage();
        byte[] bt = {0x02, (byte) 0x00, 0x00};
        mt.setMessage(0x51, bt, 3);
        me = new MidiEvent(mt, (long) 0);
        t.add(me);
        mt = new MetaMessage();
        String TrackName = new String("midifile track");
        mt.setMessage(0x03, TrackName.getBytes(), TrackName.length());
        me = new MidiEvent(mt, (long) 0);
        t.add(me);
        ShortMessage mm = new ShortMessage();
        mm.setMessage(0xB0, 0x7D, 0x00);
        me = new MidiEvent(mm, (long) 0);
        t.add(me);
        mm = new ShortMessage();
        mm.setMessage(0xB0, 0x7F, 0x00);
        me = new MidiEvent(mm, (long) 0);
        t.add(me);
        mm = new ShortMessage();
        mm.setMessage(0xC0, 0x00, 0x00);
        me = new MidiEvent(mm, (long) 0);
        t.add(me);
        System.out.println("Midi writer initialized");
        for (int f = 1; f < FRAMES; f++) {
            System.out.print(df.format((f * 1.0 / (FRAMES - 1)) * 100) + "%\r");
            for (int n = 0; n < KEYS; n++) {
                if (nc.getAt(n).getAt(f)) {
                    if (!nc.getAt(n).getAt(f - 1)) { //START NOTE IF ITS [f,t]
                        mm = new ShortMessage();
                        mm.setMessage(0x90, n + OFFSET, 0x60);
                        me = new MidiEvent(mm, (long) 1 + f * 5);
                        t.add(me);
                    }
                } else {
                    if (nc.getAt(n).getAt(f - 1)) { //END NOTE IF ITS [t,f]
                        mm = new ShortMessage();
                        mm.setMessage(0x80, n + OFFSET, 0x40);
                        me = new MidiEvent(mm, (long) 1 + f * 5);
                        t.add(me);
                    }
                }
            }
            Thread.sleep(40);
        }
        System.out.println("Writer finished, exporting...");
        mt = new MetaMessage();
        byte[] bet = {}; // empty array
        mt.setMessage(0x2F, bet, 0);
        me = new MidiEvent(mt, (long) 140);
        t.add(me);
        File fi = new File("midifile.mid");
        MidiSystem.write(s, 1, fi);
        System.out.println("Midi created");
        Scanner sc = new Scanner(System.in);
        System.out.println("Play? (y/n)");
        String in = sc.next();
        if (in.equals("y")){
            System.out.println("Initializing Midi");
            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();
            Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
            MidiChannel[] mc = midiSynth.getChannels();
            midiSynth.loadInstrument(instr[0]);

            for (int f = 0; f < FRAMES; f++) {
                System.out.print(df.format((f * 1.0 / (FRAMES - 1)) * 100) + "%\r");
                for (int n = 0; n < KEYS; n++) {
                    if (nc.getAt(n).getAt(f)) {
                        if (f >= 1 && !nc.getAt(n).getAt(f - 1)) mc[0].noteOn(n + OFFSET, 100);
                    } else {
                        mc[0].noteOff(n + 31);
                    }
                }
                Thread.sleep(40);
            }
        }
    }
}
