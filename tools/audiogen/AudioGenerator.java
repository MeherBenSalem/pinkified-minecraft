import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Synthesises Pinkified's original audio as WAV files:
 *   sugarplum_lullaby.wav - a gentle music-box waltz in F major for the main menu
 *   click.wav / chime.wav  - soft UI sounds
 *
 *   java tools/audiogen/AudioGenerator.java build/audio
 *   ffmpeg -i build/audio/sugarplum_lullaby.wav -c:a libvorbis -q:a 4 .../sounds/music/sugarplum_lullaby.ogg
 */
public final class AudioGenerator {
    static final int RATE = 44100;

    // ------------------------------------------------------------------ score

    static final double BPM = 76;
    static final double BEAT = 60.0 / BPM;
    static final double BAR = BEAT * 3;

    /** Melody, one bar per entry: note:beats tokens, "r" for rests. */
    static final String[] MELODY = {
            // A
            "A4:1.5 Bb4:0.5 C5:1", "D5:2 C5:1", "Bb4:1.5 A4:0.5 G4:1", "G4:2 C5:1",
            "A4:1.5 Bb4:0.5 C5:1", "E5:2 D5:1", "D5:1 C5:1 Bb4:1", "A4:3",
            // A'
            "A4:1 C5:0.5 F5:0.5 E5:0.5 D5:0.5", "D5:1.5 E5:0.5 F5:1", "D5:1 Bb4:1 G4:1", "C5:1.5 D5:0.5 E5:1",
            "F5:1 C5:1 A4:1", "E5:1 C5:1 A4:1", "Bb4:1 D5:1 E5:1", "F5:3",
            // B
            "F5:1 D5:1 Bb4:1", "E5:1.5 D5:0.5 C5:1", "C5:1 A4:1 E5:1", "D5:3",
            "Bb4:1 D5:1 G5:1", "F5:1 E5:1 C5:1", "A5:1.5 G5:0.5 F5:1", "E5:1 C5:1 G4:1",
            // A''
            "A4:1 C5:0.5 F5:0.5 E5:0.5 D5:0.5", "D5:1.5 E5:0.5 F5:1", "D5:1 Bb4:1 G4:1", "C5:1.5 D5:0.5 E5:1",
            "F5:1 C5:1 A4:1", "E5:1 C5:1 A4:1", "Bb4:1 D5:1 E5:1", "F5:1.5 A5:0.5 C6:1", "F5:3",
    };

    static final String[] CHORDS = {
            "F", "Dm", "Bb", "C", "F", "Am", "C7", "F",
            "F", "Dm", "Bb", "C", "F", "Am", "C7", "F",
            "Bb", "C", "Am", "Dm", "Gm", "C", "F", "C7",
            "F", "Dm", "Bb", "C", "F", "Am", "C7", "F", "F",
    };

    static int[] chord(String name) {
        return switch (name) {
            case "F" -> new int[]{53, 57, 60};
            case "Dm" -> new int[]{50, 53, 57};
            case "Bb" -> new int[]{46, 50, 53};
            case "C" -> new int[]{48, 52, 55};
            case "C7" -> new int[]{48, 52, 55, 58};
            case "Am" -> new int[]{45, 48, 52};
            case "Gm" -> new int[]{43, 46, 50};
            default -> throw new IllegalArgumentException(name);
        };
    }

    static int midi(String n) {
        String[] names = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};
        int octave = n.charAt(n.length() - 1) - '0';
        String pc = n.substring(0, n.length() - 1);
        for (int i = 0; i < names.length; i++) if (names[i].equals(pc)) return 12 * (octave + 1) + i;
        throw new IllegalArgumentException(n);
    }

    static double freq(int midi) {
        return 440.0 * Math.pow(2, (midi - 69) / 12.0);
    }

    // ------------------------------------------------------------------ voices

    record Note(double start, double dur, int midi, double vel, int voice, double pan) {
    }

    static double musicBox(double f, double t) {
        if (t < 0) return 0;
        double atk = t < 0.004 ? t / 0.004 : 1;
        double tau = 1.25 * Math.pow(440.0 / f, 0.35);
        double s = Math.sin(2 * Math.PI * f * t) * Math.exp(-t / tau)
                + 0.36 * Math.sin(2 * Math.PI * 2 * f * t) * Math.exp(-t * 3.2)
                + 0.10 * Math.sin(2 * Math.PI * 3.01 * f * t) * Math.exp(-t * 6)
                + 0.07 * Math.sin(2 * Math.PI * 5.43 * f * t) * Math.exp(-t * 11);
        return s * atk;
    }

    static double celesta(double f, double t) {
        if (t < 0) return 0;
        double atk = t < 0.008 ? t / 0.008 : 1;
        return atk * (Math.sin(2 * Math.PI * f * t) + 0.22 * Math.sin(2 * Math.PI * 2 * f * t) * Math.exp(-t * 4)) * Math.exp(-t / 0.9);
    }

    static double bass(double f, double t) {
        if (t < 0) return 0;
        double atk = t < 0.012 ? t / 0.012 : 1;
        return atk * (Math.sin(2 * Math.PI * f * t) + 0.15 * Math.sin(2 * Math.PI * 2 * f * t)) * Math.exp(-t / 1.7);
    }

    static double pad(double f, double t, double dur) {
        if (t < 0 || t > dur + 1.2) return 0;
        double env = Math.min(1, t / 0.7) * (t > dur ? Math.max(0, 1 - (t - dur) / 1.2) : 1);
        return env * (Math.sin(2 * Math.PI * f * t) + Math.sin(2 * Math.PI * f * 1.003 * t) * 0.8
                + 0.25 * Math.sin(2 * Math.PI * 2 * f * t)) / 2.05;
    }

    static double voice(Note n, double t) {
        double f = freq(n.midi);
        return switch (n.voice) {
            case 0 -> musicBox(f, t);
            case 1 -> celesta(f, t);
            case 2 -> bass(f, t);
            case 3 -> pad(f, t, n.dur);
            default -> musicBox(f, t) * 0.8;
        };
    }

    static double tailOf(Note n) {
        return switch (n.voice) {
            case 3 -> n.dur + 1.3;
            case 2 -> 6;
            default -> 5;
        };
    }

    // ------------------------------------------------------------------ rendering

    static float[][] render(List<Note> notes, double seconds) {
        int len = (int) (seconds * RATE);
        float[][] out = new float[2][len];
        for (Note n : notes) {
            int s0 = (int) (n.start * RATE);
            int s1 = Math.min(len, (int) ((n.start + tailOf(n)) * RATE));
            double l = Math.cos(n.pan * Math.PI / 2), r = Math.sin(n.pan * Math.PI / 2);
            for (int i = s0; i < s1; i++) {
                double v = voice(n, (i - s0) / (double) RATE) * n.vel;
                out[0][i] += (float) (v * l);
                out[1][i] += (float) (v * r);
            }
        }
        return out;
    }

    /** A small Freeverb-style reverb. */
    static float[][] reverb(float[][] in, double wet, double room) {
        int[] combs = {1557, 1617, 1491, 1422, 1277, 1356};
        int[] alls = {556, 441, 341, 225};
        float[][] out = new float[2][in[0].length];
        for (int ch = 0; ch < 2; ch++) {
            int spread = ch * 23;
            double[][] cb = new double[combs.length][];
            int[] ci = new int[combs.length];
            double[] filt = new double[combs.length];
            for (int k = 0; k < combs.length; k++) cb[k] = new double[combs[k] + spread];
            double[][] ab = new double[alls.length][];
            int[] ai = new int[alls.length];
            for (int k = 0; k < alls.length; k++) ab[k] = new double[alls[k] + spread];
            for (int i = 0; i < in[ch].length; i++) {
                double x = in[ch][i] * 0.015;
                double acc = 0;
                for (int k = 0; k < combs.length; k++) {
                    double y = cb[k][ci[k]];
                    filt[k] = y * 0.75 + filt[k] * 0.25;
                    cb[k][ci[k]] = x + filt[k] * room;
                    ci[k] = (ci[k] + 1) % cb[k].length;
                    acc += y;
                }
                for (int k = 0; k < alls.length; k++) {
                    double b = ab[k][ai[k]];
                    double y = -acc + b;
                    ab[k][ai[k]] = acc + b * 0.5;
                    ai[k] = (ai[k] + 1) % ab[k].length;
                    acc = y;
                }
                out[ch][i] = (float) (in[ch][i] * (1 - wet * 0.5) + acc * wet * 3);
            }
        }
        return out;
    }

    static void normalize(float[][] s, double peakDb) {
        double peak = 1e-9;
        for (float[] ch : s) for (float v : ch) peak = Math.max(peak, Math.abs(v));
        double gain = Math.pow(10, peakDb / 20) / peak;
        for (float[] ch : s) for (int i = 0; i < ch.length; i++) ch[i] *= gain;
    }

    static void fade(float[][] s, double inSec, double outSec) {
        int len = s[0].length;
        int fi = (int) (inSec * RATE), fo = (int) (outSec * RATE);
        for (float[] ch : s) {
            for (int i = 0; i < fi && i < len; i++) ch[i] *= i / (float) fi;
            for (int i = 0; i < fo && i < len; i++) ch[len - 1 - i] *= i / (float) fo;
        }
    }

    static void writeWav(float[][] s, File f) throws IOException {
        f.getParentFile().mkdirs();
        int channels = s.length, len = s[0].length;
        try (DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            int dataLen = len * channels * 2;
            o.writeBytes("RIFF");
            o.writeInt(Integer.reverseBytes(36 + dataLen));
            o.writeBytes("WAVEfmt ");
            o.writeInt(Integer.reverseBytes(16));
            o.writeShort(Short.reverseBytes((short) 1));
            o.writeShort(Short.reverseBytes((short) channels));
            o.writeInt(Integer.reverseBytes(RATE));
            o.writeInt(Integer.reverseBytes(RATE * channels * 2));
            o.writeShort(Short.reverseBytes((short) (channels * 2)));
            o.writeShort(Short.reverseBytes((short) 16));
            o.writeBytes("data");
            o.writeInt(Integer.reverseBytes(dataLen));
            for (int i = 0; i < len; i++)
                for (float[] ch : s) {
                    int v = (int) Math.round(Math.max(-1, Math.min(1, ch[i])) * 32767);
                    o.writeShort(Short.reverseBytes((short) v));
                }
        }
        System.out.println("  wrote " + f.getName());
    }

    // ------------------------------------------------------------------ pieces

    static void lullaby(File dir) throws IOException {
        List<Note> notes = new ArrayList<>();
        Random rnd = new Random(1234);
        double lead = 0.6;
        for (int bar = 0; bar < MELODY.length; bar++) {
            double t0 = lead + bar * BAR;
            double beat = 0;
            for (String tok : MELODY[bar].split(" ")) {
                String[] p = tok.split(":");
                double dur = Double.parseDouble(p[1]);
                if (!p[0].equals("r")) {
                    double human = (rnd.nextDouble() - 0.5) * 0.012;
                    double vel = 0.34 + (beat == 0 ? 0.06 : 0) + rnd.nextDouble() * 0.04;
                    notes.add(new Note(t0 + beat * BEAT + human, dur * BEAT, midi(p[0]), vel, 0, 0.58));
                }
                beat += dur;
            }
            int[] ch = chord(CHORDS[bar]);
            // waltz accompaniment: bass on one, chord tones on two and three
            notes.add(new Note(t0, BEAT, ch[0] - 12, 0.30, 2, 0.45));
            for (int b = 1; b <= 2; b++) {
                for (int k = 1; k < Math.min(3, ch.length); k++) {
                    notes.add(new Note(t0 + b * BEAT + k * 0.012, BEAT, ch[k] + 12, 0.075, 1, 0.38));
                }
            }
            // warm pad underneath
            for (int k = 0; k < Math.min(3, ch.length); k++) notes.add(new Note(t0, BAR, ch[k] + 12, 0.035, 3, k == 1 ? 0.3 : 0.7));
            // twinkles in the second half
            if (bar >= 16 && bar < MELODY.length - 1 && rnd.nextDouble() < 0.7) {
                int tw = ch[rnd.nextInt(Math.min(3, ch.length))] + 36;
                notes.add(new Note(t0 + (1.5 + rnd.nextInt(3) * 0.5) * BEAT, BEAT, tw, 0.07, 4, rnd.nextDouble()));
            }
        }
        double seconds = lead + MELODY.length * BAR + 3.5;
        float[][] mix = render(notes, seconds);
        mix = reverb(mix, 0.32, 0.8);
        normalize(mix, -1.5);
        fade(mix, 0.02, 3.0);
        writeWav(mix, new File(dir, "sugarplum_lullaby.wav"));
    }

    static void click(File dir) throws IOException {
        double seconds = 0.32;
        int len = (int) (seconds * RATE);
        float[][] s = new float[1][len];
        double f = freq(midi("C6"));
        Random rnd = new Random(5);
        for (int i = 0; i < len; i++) {
            double t = i / (double) RATE;
            double v = musicBox(f, t) * Math.exp(-t * 9) * 0.8;
            v += 0.35 * Math.sin(2 * Math.PI * f * 1.5 * t) * Math.exp(-t * 22);
            if (t < 0.004) v += (rnd.nextDouble() - 0.5) * 0.25 * (1 - t / 0.004);
            s[0][i] = (float) v;
        }
        normalize(s, -3);
        fade(s, 0.0, 0.03);
        writeWav(s, new File(dir, "click.wav"));
    }

    static void chime(File dir) throws IOException {
        double seconds = 0.9;
        int len = (int) (seconds * RATE);
        float[][] s = new float[1][len];
        int[] seq = {midi("G5"), midi("D6"), midi("G6")};
        for (int i = 0; i < len; i++) {
            double t = i / (double) RATE;
            double v = 0;
            for (int k = 0; k < seq.length; k++) {
                double tk = t - k * 0.055;
                v += musicBox(freq(seq[k]), tk) * Math.exp(-Math.max(0, tk) * 3.5) * (1 - k * 0.2);
            }
            s[0][i] = (float) v;
        }
        normalize(s, -4);
        fade(s, 0.0, 0.1);
        writeWav(s, new File(dir, "chime.wav"));
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(args.length > 0 ? args[0] : "build/audio");
        System.out.println("Pinkified audio generator -> " + dir.getAbsolutePath());
        lullaby(dir);
        click(dir);
        chime(dir);
    }
}
