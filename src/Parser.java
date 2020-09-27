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
    static final int FRAMES = 3911; //3911 is the max frames

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
        String s = "0000";
        for (int x = 0; x < FRAMES; x++) {
            System.out.print(df.format((x*1.0/(FRAMES - 1))*100) + "%\r");
            s = countUp(s);
            Color[] frameColors = parseImage(new File("frames\\" + s + ".jpg"));

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
        System.out.println("Image written");
        ImageIO.write(output,"png", new File("output.png"));

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
