package eu.fbk.textpro.modules.bin;

public class M1Para1 {
    static final /* synthetic */ int[] SwitchMapOrtho;
    static final /* synthetic */ int[] SwitchMapMorphology;

    static {
        SwitchMapMorphology = new int[Morphology.values().length];
        try {
            M1Para1.SwitchMapMorphology[Morphology.END_OF_LINE.ordinal()] = 1;
        }
        catch (NoSuchFieldError var0) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapMorphology[Morphology.END_OF_SENTENCE.ordinal()] = 2;
        }
        catch (NoSuchFieldError var0_1) {
            // empty catch block
        }
        SwitchMapOrtho = new int[Ortho.values().length];
        try {
            M1Para1.SwitchMapOrtho[Ortho.LOWERCASE.ordinal()] = 1;
        }
        catch (NoSuchFieldError var0_2) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.UPPERCASE.ordinal()] = 2;
        }
        catch (NoSuchFieldError var0_3) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.CAPITALIZED.ordinal()] = 3;
        }
        catch (NoSuchFieldError var0_4) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.MIXEDCASE.ordinal()] = 4;
        }
        catch (NoSuchFieldError var0_5) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.JLETTER.ordinal()] = 5;
        }
        catch (NoSuchFieldError var0_6) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.JLETTERDIGIT.ordinal()] = 6;
        }
        catch (NoSuchFieldError var0_7) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.LETTER.ordinal()] = 7;
        }
        catch (NoSuchFieldError var0_8) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.DIGIT.ordinal()] = 8;
        }
        catch (NoSuchFieldError var0_9) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.END_OF_LINE.ordinal()] = 9;
        }
        catch (NoSuchFieldError var0_10) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.END_OF_SENTENCE.ordinal()] = 10;
        }
        catch (NoSuchFieldError var0_11) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.SINGLE.ordinal()] = 11;
        }
        catch (NoSuchFieldError var0_12) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.OTHER.ordinal()] = 12;
        }
        catch (NoSuchFieldError var0_13) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.ABBR.ordinal()] = 13;
        }
        catch (NoSuchFieldError var0_14) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.PUNCT.ordinal()] = 14;
        }
        catch (NoSuchFieldError var0_15) {
            // empty catch block
        }
        try {
            M1Para1.SwitchMapOrtho[Ortho.POS.ordinal()] = 15;
        }
        catch (NoSuchFieldError var0_16) {
            // empty catch block
        }
    }
}