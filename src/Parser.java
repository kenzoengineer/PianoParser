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

public class Parser {
    static final int READ_HEIGHT = 310;
    static final int KEY_WIDTH = 12;
    static final int IMAGE_WIDTH = 640;
    static final int KEYS = 54;
    static final int FRAMES = 200; //3911 is the max frames

    public static Color[] parseImage(File file) throws Exception{
        Color[] temp = new Color[KEYS];
        BufferedImage img = ImageIO.read(file);
        for (int i = 0; i < KEYS; i++) {
            int pixel = img.getRGB(i * KEY_WIDTH + 1,READ_HEIGHT);
            Color c = new Color(pixel, false);
            temp[i] = c;
           //System.out.println(i + " RGB: " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
        }
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

        Color[] originalColors = parseImage(new File("frame0.png"));
        String[] letters = {"Ab", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G",};
        NoteContainer nc = new NoteContainer(KEYS);
        String str = "0000";
        for (int x = 0; x < FRAMES; x++) {
            System.out.print(df.format((x*1.0/(FRAMES - 1))*100) + "%\r");
            str = countUp(str);
            Color[] frameColors = parseImage(new File("frames\\" + str + ".jpg"));

            for (int i = 0; i < originalColors.length; i++) {
                Color og = originalColors[i];
                Color test = frameColors[i];
                double red = og.getRed() / (test.getRed() * 1.0);
                double green = og.getGreen() / (test.getGreen() * 1.0);
                double blue = og.getBlue() / (test.getBlue() * 1.0);
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
        BufferedImage output = new BufferedImage(FRAMES,KEYS, BufferedImage.TYPE_INT_RGB);
        for (int f = 0; f < FRAMES; f++) {
            for (int n = 0; n < KEYS; n++) {
                if (nc.getAt(n).getAt(f)) {
                    output.setRGB(f,n,Color.BLACK.getRGB());
                } else {
                    output.setRGB(f,n,Color.WHITE.getRGB());
                }
            }
        }
        ImageIO.write(output,"png", new File("output.png"));
        System.out.println("Image written");

        System.out.println("Initializing midi writer");

        Sequence s = new Sequence(javax.sound.midi.Sequence.PPQ,24);
        Track t = s.createTrack();
        byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
        SysexMessage sm = new SysexMessage();
        sm.setMessage(b, 6);
        MidiEvent me = new MidiEvent(sm,(long)0);
        t.add(me);
        MetaMessage mt = new MetaMessage();
        byte[] bt = {0x02, (byte)0x00, 0x00};
        mt.setMessage(0x51 ,bt, 3);
        me = new MidiEvent(mt,(long)0);
        t.add(me);
        mt = new MetaMessage();
        String TrackName = new String("midifile track");
        mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
        me = new MidiEvent(mt,(long)0);
        t.add(me);
        ShortMessage mm = new ShortMessage();
        mm.setMessage(0xB0, 0x7D,0x00);
        me = new MidiEvent(mm,(long)0);
        t.add(me);
        mm = new ShortMessage();
        mm.setMessage(0xB0, 0x7F,0x00);
        me = new MidiEvent(mm,(long)0);
        t.add(me);
        mm = new ShortMessage();
        mm.setMessage(0xC0, 0x00, 0x00);
        me = new MidiEvent(mm,(long)0);
        t.add(me);
        System.out.println("Midi writer initialized");
        for (int f = 1; f < FRAMES; f++) {
            System.out.print(df.format((f*1.0/(FRAMES - 1))*100) + "%\r");
            for (int n = 0; n < KEYS; n++) {
                if (nc.getAt(n).getAt(f)) {
                    if (!nc.getAt(n).getAt(f-1)) { //START NOTE IF ITS [f,t]
                        mm = new ShortMessage();
                        mm.setMessage(0x90,n+31,0x60);
                        me = new MidiEvent(mm,(long)1+f*10);
                        t.add(me);
                    }
                } else {
                    if (nc.getAt(n).getAt(f-1)) { //END NOTE IF ITS [t,f]
                        mm = new ShortMessage();
                        mm.setMessage(0x80,n+31,0x40);
                        me = new MidiEvent(mm,(long)1+f*10);
                        t.add(me);
                    }
                }
            }
            Thread.sleep(40);
        }
        System.out.println("Writer finished, exporting...");
        mt = new MetaMessage();
        byte[] bet = {}; // empty array
        mt.setMessage(0x2F,bet,0);
        me = new MidiEvent(mt, (long)140);
        t.add(me);
        File fi = new File("midifile.mid");
        MidiSystem.write(s,1,fi);
        System.out.println("Midi created");

        System.out.println("Initializing Midi");
        Synthesizer midiSynth = MidiSystem.getSynthesizer();
        midiSynth.open();
        Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
        MidiChannel[] mc = midiSynth.getChannels();
        midiSynth.loadInstrument(instr[0]);

        for (int f = 0; f < FRAMES; f++) {
            System.out.print(df.format((f*1.0/(FRAMES - 1))*100) + "%\r");
            for (int n = 0; n < KEYS; n++) {
                if (nc.getAt(n).getAt(f)) {
                    if (f >= 1 && !nc.getAt(n).getAt(f - 1)) mc[0].noteOn(n + 31, 100);
                } else {
                    mc[0].noteOff(n + 31);
                }
            }
            Thread.sleep(40);
        }
    }
}
