import javax.sound.midi.*;

public class MidiTest{

    public static void main(String[] args) throws Exception{
        Synthesizer midiSynth = MidiSystem.getSynthesizer();
        midiSynth.open();
        Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
        MidiChannel[] mChannels = midiSynth.getChannels();
        midiSynth.loadInstrument(instr[0]);

        mChannels[0].noteOn(60, 100);//On channel 0, play note number 60 with velocity 100
        mChannels[0].noteOn(64, 100);
        mChannels[0].noteOn(67, 100);
        Thread.sleep(2000);
        mChannels[0].noteOff(60);
        mChannels[0].noteOff(64);
        mChannels[0].noteOff(67);
        mChannels[0].noteOff(60);
        mChannels[0].noteOff(64);
        mChannels[0].noteOff(67);
    }

}    