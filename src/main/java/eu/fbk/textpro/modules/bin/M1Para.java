package eu.fbk.textpro.modules.bin;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

// 
// Decompiled by Procyon v0.5.30
// 

public class M1Para
{
    public static String ENCODING;
    private static int PREFIXES;
    private static int SUFFIXES;
    
    public static void main(final String[] array) throws Exception {
        final M1Para m1Para = new M1Para();
        Reader reader = null;
        Reader reader2 = null;
        try {
            if (array.length == 1 && (array[0].equals("-v") || array[0].equals("--version"))) {
                System.out.println("M1Para version 1.5.0, 2012");
                System.exit(0);
            }
            else if (array.length == 1 && (array[0].equals("-h") || array[0].equals("--help"))) {
                System.out.println("usage: \njava M1-para [file_name] [-m morpho_file]");
                System.out.println("cat file_name | java M1-para [-m morpho_file]");
                System.exit(0);
            }
            else if (array.length == 0) {
                reader = new InputStreamReader(System.in, M1Para.ENCODING);
            }
            else if (array.length == 1) {
                reader = new InputStreamReader(new FileInputStream(array[0]), M1Para.ENCODING);
            }
            else if (array.length == 2 && array[0].equals("-m")) {
                reader = new InputStreamReader(System.in, M1Para.ENCODING);
                reader2 = new InputStreamReader(new FileInputStream(array[1]), M1Para.ENCODING);
            }
            else if ((array.length == 3 && array[0].equals("-m")) || array[1].equals("-m")) {
                String s;
                String s2;
                if (array[0].equals("-m")) {
                    s = array[2];
                    s2 = array[1];
                }
                else {
                    s = array[0];
                    s2 = array[2];
                }
                reader = new InputStreamReader(new FileInputStream(s), M1Para.ENCODING);
                reader2 = new InputStreamReader(new FileInputStream(s2), M1Para.ENCODING);
            }
            else {
                System.out.println("usage: \njava M1-para [file_name] [-m morpho_file]");
                System.out.println("cat file_name | java M1-para [-m morpho_file]");
                System.exit(1);
            }
            m1Para.readFile(reader, reader2);
        }
        finally {
            reader.close();
            if (reader2 != null) {
                reader2.close();
            }
        }
    }
    
    private void readFile(final Reader reader, final Reader reader2) throws Exception {
        final PrintStream printStream = new PrintStream(System.out, true, M1Para.ENCODING);
        try {
            Morpho morpho = null;
            if (reader2 != null) {
                morpho = new Morpho(reader2);
            }
            final Lexer lexer = new Lexer(reader);
            while (true) {
                final Ortho yylex = lexer.yylex();
                if (yylex == null) {
                    break;
                }
                switch (yylex) {
                    case LOWERCASE: {
                        final String normalizedToken = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken, "LOW");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken));
                        continue;
                    }
                    case UPPERCASE: {
                        final String normalizedToken2 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken2, "UPP");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken2));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken2));
                        continue;
                    }
                    case CAPITALIZED: {
                        final String normalizedToken3 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken3, "CAP");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken3));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken3));
                        continue;
                    }
                    case MIXEDCASE: {
                        final String normalizedToken4 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken4, "MIX");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken4));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken4));
                        continue;
                    }
                    case JLETTER: {
                        final String lowerCase = lexer.orthoin.toLowerCase();
                        printStream.printf("%s %s %s", lexer.orthoin, lowerCase, "JLE");
                        printStream.printf(" %s", this.getPrefixes(lowerCase));
                        printStream.printf(" %s", this.getSuffixes(lowerCase));
                        continue;
                    }
                    case JLETTERDIGIT: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin.toLowerCase(), "JLD");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case LETTER: {
                        final String lowerCase2 = lexer.orthoin.toLowerCase();
                        printStream.printf("%s %s %s", lexer.orthoin, lowerCase2, "LET");
                        printStream.printf(" %s", this.getPrefixes(lowerCase2));
                        printStream.printf(" %s", this.getSuffixes(lowerCase2));
                        continue;
                    }
                    case DIGIT: {
                        printStream.printf("%s %s %s", lexer.orthoin, "_NUM_", "DIG");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case END_OF_LINE: {
                        if (morpho != null) {
                            printStream.printf(" %s", this.readMorphoLine(morpho));
                        }
                        printStream.printf("%n", new Object[0]);
                        continue;
                    }
                    case END_OF_SENTENCE: {
                        if (morpho != null) {
                            this.readMorphoLine(morpho);
                        }
                        printStream.printf("%s", lexer.orthoin, "");
                        continue;
                    }
                    case SINGLE: {
                        final String[] split = lexer.orthoin.split("_#_");
                        printStream.printf("%s %s %s", split[0], split[0], this.mapUnicodeCategory(split[1]));
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case OTHER: {
                        final String normalizedToken5 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken5, "OTH");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken5));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken5));
                        continue;
                    }
                    case ABBR: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "ABB_");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case PUNCT: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "PUN");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case POS: {
                        printStream.printf("%s", lexer.orthoin);
                        continue;
                    }
                    default: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "DEF");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                }
            }
            printStream.close();
            if (reader2 != null && !this.readMorphoLine(morpho).equals("EOF")) {
                System.err.println("M1Para Error: the two input files are out of line!");
                System.exit(1);
            }
        }
        finally {
            printStream.close();
        }
    }
       
    public String readFileString(final Reader reader, final Reader reader2) throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final PrintStream printStream = new PrintStream(baos, true, M1Para.ENCODING);
        try {
            Morpho morpho = null;
            if (reader2 != null) {
                morpho = new Morpho(reader2);
            }
            final Lexer lexer = new Lexer(reader);
            while (true) {
                final Ortho yylex = lexer.yylex();
                if (yylex == null) {
                    break;
                }
                switch (yylex) {
                    case LOWERCASE: {
                        final String normalizedToken = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken, "LOW");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken));
                        continue;
                    }
                    case UPPERCASE: {
                        final String normalizedToken2 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken2, "UPP");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken2));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken2));
                        continue;
                    }
                    case CAPITALIZED: {
                        final String normalizedToken3 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken3, "CAP");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken3));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken3));
                        continue;
                    }
                    case MIXEDCASE: {
                        final String normalizedToken4 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken4, "MIX");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken4));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken4));
                        continue;
                    }
                    case JLETTER: {
                        final String lowerCase = lexer.orthoin.toLowerCase();
                        printStream.printf("%s %s %s", lexer.orthoin, lowerCase, "JLE");
                        printStream.printf(" %s", this.getPrefixes(lowerCase));
                        printStream.printf(" %s", this.getSuffixes(lowerCase));
                        continue;
                    }
                    case JLETTERDIGIT: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin.toLowerCase(), "JLD");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case LETTER: {
                        final String lowerCase2 = lexer.orthoin.toLowerCase();
                        printStream.printf("%s %s %s", lexer.orthoin, lowerCase2, "LET");
                        printStream.printf(" %s", this.getPrefixes(lowerCase2));
                        printStream.printf(" %s", this.getSuffixes(lowerCase2));
                        continue;
                    }
                    case DIGIT: {
                        printStream.printf("%s %s %s", lexer.orthoin, "_NUM_", "DIG");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case END_OF_LINE: {
                        if (morpho != null) {
                            printStream.printf(" %s", this.readMorphoLine(morpho));
                        }
                        printStream.printf("%n", new Object[0]);
                        continue;
                    }
                    case END_OF_SENTENCE: {
                        if (morpho != null) {
                            this.readMorphoLine(morpho);
                        }
                        printStream.printf("%s", lexer.orthoin, "");
                        continue;
                    }
                    case SINGLE: {
                        final String[] split = lexer.orthoin.split("_#_");
                        printStream.printf("%s %s %s", split[0], split[0], this.mapUnicodeCategory(split[1]));
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case OTHER: {
                        final String normalizedToken5 = this.getNormalizedToken(lexer.orthoin);
                        printStream.printf("%s %s %s", lexer.orthoin, normalizedToken5, "OTH");
                        printStream.printf(" %s", this.getPrefixes(normalizedToken5));
                        printStream.printf(" %s", this.getSuffixes(normalizedToken5));
                        continue;
                    }
                    case ABBR: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "ABB_");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case PUNCT: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "PUN");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                    case POS: {
                        printStream.printf("%s", lexer.orthoin);
                        continue;
                    }
                    default: {
                        printStream.printf("%s %s %s", lexer.orthoin, lexer.orthoin, "DEF");
                        printStream.printf(" %s", this.getPrefixes(""));
                        printStream.printf(" %s", this.getSuffixes(""));
                        continue;
                    }
                }
            }


            printStream.close();
            if (reader2 != null && !this.readMorphoLine(morpho).equals("EOF")) {
                System.err.println("M1Para Error: the two input files are out of line!");
                System.exit(1);
            }
        }
        finally {
            printStream.close();
        }
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        return content;
    }
       
   
    private String readMorphoLine(final Morpho morpho) throws Exception {
        String s = "";
        final Morphology yylex = morpho.yylex();
        if (yylex == null) {
            return "EOF";
        }
        switch (yylex) {
            case END_OF_LINE: {
                s = this.getMorphoString(morpho.morphoin);
                break;
            }
            case END_OF_SENTENCE: {
                s = this.getMorphoString(morpho.morphoin);
                break;
            }
        }
        return s;
    }
    
    private String getMorphoString(final int[] array) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(array[i]);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    private String getPrefixes(final String s) {
        final StringBuffer sb = new StringBuffer();
        final int length = s.length();
        for (int i = 2; i <= M1Para.PREFIXES; ++i) {
            sb.append((i <= length) ? s.substring(0, i) : "_");
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    private String getSuffixes(final String s) {
        final StringBuffer sb = new StringBuffer();
        final int length = s.length();
        for (int i = 2; i <= M1Para.SUFFIXES; ++i) {
            sb.append((i <= length) ? s.substring(length - i, length) : "_");
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    
    private String getNormalizedToken(final String s) {
        return s.toLowerCase().replaceAll("(\\p{Pf}|\\p{Pi})", "'");
    }
    
    private String mapUnicodeCategory(final String s) {
        String s2 = null;
        switch (Integer.parseInt(s)) {
            case 2: {
                s2 = "24";
                break;
            }
            case 4: {
                s2 = "24";
                break;
            }
            case 7: {
                s2 = "24";
                break;
            }
            case 9: {
                s2 = "24";
                break;
            }
            case 10: {
                s2 = "24";
                break;
            }
            case 11: {
                s2 = "24";
                break;
            }
            case 23: {
                s2 = "24";
                break;
            }
            case 20: {
                s2 = "24";
                break;
            }
            case 30: {
                s2 = "24";
                break;
            }
            case 29: {
                s2 = "24";
                break;
            }
            case 27: {
                s2 = "24";
                break;
            }
            case 25: {
                s2 = "24";
                break;
            }
            case 28: {
                s2 = "24";
                break;
            }
            default: {
                s2 = s;
                break;
            }
        }
        return s2;
    }
    
    static {
        M1Para.ENCODING = "UTF-8";
        M1Para.PREFIXES = 4;
        M1Para.SUFFIXES = 4;
    }
}