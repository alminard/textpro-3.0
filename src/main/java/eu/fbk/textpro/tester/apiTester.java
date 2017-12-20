package eu.fbk.textpro.tester;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;

import eu.fbk.textpro.api.TextProGate;
import eu.fbk.textpro.api.TextProGate.ChunkProType;
import eu.fbk.textpro.api.TextProGate.KXType;
import eu.fbk.textpro.api.TextProGate.LemmaProType;
import eu.fbk.textpro.api.TextProGate.MorphoProType;
import eu.fbk.textpro.api.TextProGate.TagProType;
import eu.fbk.textpro.api.TextProGate.TokenProType;
import eu.fbk.textpro.api.TextProGate.EntityProType;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
//import eu.fbk.textpro.api.TextProGate.TimeProType;
//import eu.fbk.textpro.api.TextProGate.SentiProType;
import eu.fbk.textpro.wrapper.TextProPipeLine;


public class apiTester {


    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, JAXBException, IOException, CloneNotSupportedException {
        TextProGate textpro = new TextProGate();
        textpro.setTextProPath(TEXTPROVARIABLES.getTEXTPROPATH()+ File.separator+"textpro.sh");
        // textpro.getChunkPro().deactivateAll(ChunkProType.class);
        // textpro.getChunkPro().deactive(ChunkProType.chunk.name());
        textpro.setLanguage("eng");
        textpro.overwriteOutput();
        textpro.activeVerboseMood();
        //textpro.activeHtmlCleaner();
        textpro.setOutputFolder("test/output/");
        textpro.setOutputFileName("trento_wiki_en.api.txt.txp");
        textpro.setInputFile("test/input/trento_wiki_en.txt");
        textpro.getTokenPro().active(TokenProType.token.name());
        textpro.getTokenPro().active(TokenProType.tokenid.name());
        textpro.getTokenPro().active(TokenProType.tokennorm.name());
        textpro.getTokenPro().active(TokenProType.tokenstart.name());
        textpro.getTokenPro().active(TokenProType.tokenend.name());
        textpro.getTokenPro().active(TokenProType.tokentype.name());
        textpro.getTagPro().active(TagProType.pos.name());
        textpro.getMorphoPro().active(MorphoProType.full_morpho.name());
        textpro.getLemmaPro().active(LemmaProType.comp_morpho.name());
        textpro.getLemmaPro().active(LemmaProType.lemma.name());
        textpro.getChunkPro().active(ChunkProType.chunk.name());
        //textpro.getTimePro().active(EntityProType.entity.name());
        //textpro.getTimePro().active(TimeProType.timex.name());
        textpro.getKX().active(KXType.keywords.name());
        //textpro.getSentiPro().active(SentiProType.sentiment.name());
        //textpro.disableTokenizer();
        //textpro.disableSentenceSplitter();
        //textpro.activeHtmlCleaner();
        //textpro.getTagPro().activateAll(TagProType.class);

        //textpro.getTokenPro().activateAll(TokenProType.class);
        //textpro.getChunkPro().activateAll(ChunkProType.class);
        //textpro.getTokenPro().deactive(TokenProType.token.name());
        //textpro.getTokenizer().active(TokenProType.tokenid.name());
        //textpro.run();
        textpro.runTextPro();
    }

}
