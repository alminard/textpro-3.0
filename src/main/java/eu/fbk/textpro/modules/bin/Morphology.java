package eu.fbk.textpro.modules.bin;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
enum Morphology {
    VERB_INDICATIVE_PRES,
    VERB_INDICATIVE_PAST,
    VERB_INFINITE_PRES,
    VERB_INFINITE_PAST,
    VERB_PARTICIPLE_PRES,
    VERB_PARTICIPLE_PAST,
    VERB_GERUND_PRES,
    VERB_GERUND_PAST,
    NOUN_SING,
    NOUN_PLUR,
    NOUN_NEUT,
    PRON,
    ADJ,
    PUNC,
    ADV,
    ART,
    CONJ,
    PREP,
    INTER,
    PROPER_NOUN,
    NUMBER,
    END_OF_SENTENCE,
    END_OF_LINE,
    ERROR,
    TOKEN;
    

    private Morphology() {
    }
}