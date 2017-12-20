package eu.fbk.textpro.modules.cleanpro;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: macbook
 * Date: 4-mag-2008
 * Time: 17.11.01
 */
public class StopWord {
    static String[] ITALIAN = {"gg","km","pt","n","n.","b","d","d.","r","s","s.","z","euro","l","l.","lit","lire","di","d","a","ad","da","in","con","coi","col","su","per","tra","fra","fin","e","ed","o","la","l","i","il","Il","le","lo","un","uno","una","un","si","s","ai","agli","allo","alle","al","all","agli","alla","alle","del","dei","dell","della","delle","dello","degli","dal","dallo","dalla","dalle","dagli","dall","dai","nel","nella","nelle","nello","nell","nei","negli","sui","sul","sulle","sulla","sullo","sugli","sull","che","chi","cui","io","tu","egli","esso","essa","lei","lui","noi","voi","essi","esse","mi","m","me","ti","te","ci","ce","c","li","gli","vi","ve","v","ne","n","mio","mia","mie","miei","tuo","tua","tue","tuoi","suo","sua","suoi","sue","nostro","nostri","nostra","nostre","vostro","vostra","vostri","vostre","loro","cio","ci�","quest","questo","questa","questi","queste","quel","quell","quello","quelli","quegli","quei","quella","quelle","qualche","quanto","quanta","quanti","quante","quale","quali","qual","qualsiasi","qualcosa","qualcuno","ciascun","ciascuno","ciascuna","tale","tali","nessun","nessuno","alcun","alcuno","alcuna","alcuni","alcune","cos","cosa","cose","proprio","propria","propri","proprie","stesso","stessi","stessa","stesse","ogni","ognuno","sono","sei","�","�","�","e","era","ero","eri","eravamo","eravate","erano","siamo","siete","sar�","sara","saranno","sarei","saresti","sarebbe","saremmo","sareste","sarebbero","essere","essersi","essendo","fossi","fosse","fu","furono","ho","hai","ha","abbiamo","avete","hanno","avr�","avranno","abbia","abbiano","avere","aver","avra","aveva","avevano","avrebbe","avendo","deve","devo","devi","dev","dobbiamo","devono","doveva","dovevano","sto","stai","sta","stiamo","stanno","stato","stata","stati","state","viene","vengono","verr�","verra","fa","posso","puoi","pu�","puo","possiamo","potete","possono","potr�","potrai","potr�","potremo","potrete","potranno","possa","possano","poter","diventa","diventano","diventiamo","diventi","diventino","diventassero","voglio","vuoi","vuol","vuole","vogliono","vorrei","vorrebbe","voluto","volendo","voler","ma","pi�","piu","meno","non","n�","se","mai","dopo","come","circa","verso","oltre","senza","senz","solo","entro","rispetto","ossia","quando","gi�","gia","grazie","anzi","anziche","anzitutto","oppure","mentre","sia","almeno","quindi","fino","ne","anche","pur","anch","ancora","cosi","cos�","dove","perche","perch�","nonch�","contro","attraverso","poi","insieme","presso","allora","dunque","davanti","appena","troppo","qui","quasi","invece","pero","per�","sotto","sempre","forse","infatti","ormai","ora","adesso","particolarmente","abbastanza","soprattutto","comunque","piuttosto","subito","po","nuovo","nuovi","nuova","nuove","grande","grandi","vari","varie","diversi","diverse","varie","molto","molta","molti","molte","moltissimo","moltissima","moltissimi","moltissime","parecchio","parecchia","parecchi","parecchie","poco","poca","pochi","poche","tanto","tanta","tanti","tante","tutto","tutte","tutti","tutta","tutt","intero","intera","interi","intere","altro","altra","altre","altri","possibile","possibili","necessario","necessari","scorso","scorsa","scorse","scorsi","ultimo","ultima","ultimi","ultime","prossimo","prossima","prossime","prossimi","numeroso","numerosa","numerosi","numerose","gennaio","febbraio","marzo","aprile","maggio","giugno","luglio","agosto","settembre","ottobre","novembre","dicembre","�f","st","miliardo","miliardi","milioni","chilometri","persone","tuttavia","finora"};
    static String[] ENGLISH = {"t","a","about","above","across","after","against","all","along","alongside","also","although","always","am","amid","amidst","among","amongst","an","and","any","anybody","anyone","anything","anywhere","apropos","are","aren","aren't","around","as","at","atop","be","because","been","before","behind","being","below","beneath","beside","besides","between","beyond","both","but","by","can","can't","cannot","cause","cos","could","couldn't","coz","dare","daren","daren't","despite","did","didn","didn't","do","does","doesn","doesn't","doing","don","don't","done","dr","during","each","either","else","even","every","everybody","everyone","everything","everywhere","except","few","first","firstly","for","from","go","going","had","hadn","hadn't","has","hasn","hasn't","have","haven","haven't","having","he","he'd","he'll","ll","he's","her","here","hers","herself","him","himself","his","how","however","i","d","m","ve","i'd","i'll","i'm","i've","if","in","inside","into","is","isn","isn't","it","it'd","it'll","it's","its","itself","just","last","less","like","make","man","many","may","maybe","mayn","mayn't","me","men","might","mine","minus","more","most","mr","mrs","much","must","mustn","mustn't","my","myself","needn","needn't","neither","never","nevertheless","no","no-one","nobody","none","nonetheless","noone","nor","not","nothing","notwithstanding","now","of","off","often","on","one","only","or","other","ought","oughtn","oughtn't","our","ours","ourselves","out","outside","over","part","per","perhaps","plus","possibly","rather","s","said","say","shall","shan't","she","she'd","she'll","she's","should","shouldn","shouldn't","since","so","some","somebody","someone","someplace","something","sometime","sometimes","somewhere","than","that","that'd","that'll","that's","the","thee","their","theirs","them","themselves","then","there","there'd","there'll","there's","there've","therefore","therewith","these","they","re","they'd","they'll","they're","they've","thine","this","those","thou","though","through","throughout","thus","thy","till","to","too","toward","towards","under","underneath","until","up","upon","us","usual","usually","very","via","was","wasn","wasn't","we","we'd","week","we'll","we're","well","were","what","whatsoever","what'd","what'll","what's","what've","whatever","when","whenever","where","wherever","whether","which","whichever","while","whilst","who","whom","whose","why","will","with","within","without","woman","women","won","won't","would","wouldn","wouldn't","ye","yeah","yes","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves","a","about","above","abst","abst","accordance","accordance","according","across","act","actually","added","adj","adopted","after","afterwards","again","against","all","almost","alone","along","already","also","although","always","am","among","amongst","an","and","announce","another","any","anyhow","anyone","anything","anywhere","are","aren","aren't","arent","around","as","at","auth","available","b","be","became","because","become","becomes","becoming","been","before","beforehand","begin","beginning","behind","being","below","beside","besides","between","beyond","billion","both","but","by","c","ca","can","can't","cannot","cant","caption","co","co.","contains","could","couldn't","couldnt","d","date","did","didn't","didnt","do","does","doesn't","doesnt","don't","dont","down","during","e","each","ed","eg","eight","eighty","either","else","elsewhere","end","ending","enough","etc","even","ever","every","everyone","everything","everywhere","except","f","far","few","fifty","first","five","fix","for","former","formerly","forty","found","four","from","further","g","get","go","got","h","had","has","hasn't","hasnt","have","haven't","havent","he","he'd","he'll","he's","hed","hell","hence","her","here","here's","hereafter","hereby","herein","heres","hereupon","hers","herself","hes","hid","him","himself","his","home","hop","how","however","hundred","i","i'd","i'll","i'm","i've","id","ie","if","ill","im","in","inc","inc.","include","includes","indeed","index","information","instead","internet","into","is","isn't","isnt","it","it's","its","itself","ive","j","just","k","keys","l","last","later","latter","latterly","least","less","let","let's","lets","like","likely","line","links","ll","ltd","m","made","make","makes","many","may","maybe","me","meantime","meanwhile","might","million","miss","more","moreover","most","mostly","mr","mrs","much","must","my","myself","n","na","namely","near","neither","never","nevertheless","new","next","nine","ninety","no","nobody","none","nonetheless","noone","nor","not","nothing","now","nowhere","o","of","off","often","oh","omitted","on","once","one","one's","ones","only","onto","or","ord","other","others","otherwise","our","ours","ourselves","out","over","overall","own","p","page","pages","part","per","perhaps","pp","proud","put","q","r","ran","rather","re","recent","recently","ref","refs","related","research","run","s","same","say","search","sec","section","seem","seemed","seeming","seems","server","seven","seventy","several","she","she'd","she'll","she's","shed","shell","shes","should","shouldn't","shouldnt","since","six","sixty","so","some","somehow","someone","something","sometime","sometimes","somewhere","still","stop","such","t","taking","ten","than","that","that'll","that's","that've","thatll","thats","thatve","the","their","them","themselves","then","thence","there","there'd","there'll","there're","there's","there've","thereafter","thereby","thered","therefore","therein","therell","therere","theres","thereupon","thereve","these","they","they'd","they'll","they're","they've","theyd","theyll","theyre","theyve","thirty","this","those","though","thousand","three","through","throughout","thru","thus","til","tip","to","together","too","toward","towards","trillion","try","twenty","two","u","under","unless","unlike","unlikely","until","unto","up","upon","ups","us","used","using","v","ve","very","via","vol","vols","vs","w","was","wasn't","wasnt","way","we","we'd","we'll","we're","we've","web","wed","well","were","weren't","werent","weve","what","what'll","what's","what've","whatever","whatll","whats","whatve","when","whence","whenever","where","where's","whereafter","whereas","whereby","wherein","wheres","whereupon","wherever","whether","which","while","whim","whither","who","who'd","who'll","who's","whod","whoever","whole","wholl","whom","whomever","whos","whose","why","will","with","within","without","won't","wont","words","world","would","wouldn't","wouldnt","www","x","y","yes","yet","you","you'd","you'll","you're","you've","youd","youll","your","youre","yours","yourself","yourselves","youve","z"};
    static String[] GERMAN = {"Ab","Aber","Als","Am","An","Auch","Auf","Aus","Bei","Bereich","Da","Daher","Das","Der","Deshalb","Die","Dies","Diese","Dieser","Ein","Eine","Er","Es","Fall","F�r","Herr","Ich","Ihnen","Ihre","Im","In","Mit","Nur","Sehr","Sie","So","Und","Von","Vor","Was","Wenn","Wie","Wir","ab","aber","alle","allem","allen","aller","alles","als","also","am","an","andere","anderen","auch","auf","aus","bedeutet","bei","beiden","beim","bereits","besonders","besteht","bin","bis","bzw.","da","dabei","daf�r","daher","damit","dann","daran","darauf","darf","darin","darum","dar�ber","daruber","das","dass","davon","dazu","da�","dem","den","denen","denn","der","deren","des","deshalb","dessen","deutlich","die","dies","diese","diesem","diesen","dieser","dieses","doch","dort","durch","d�rfen","eigenen","ein","eine","einem","einen","einer","eines","einige","einigen","einmal","er","erst","ersten","es","etwa","etwas","f�r","fur","ganz","geben","gegen","gegen�ber","gegenuber","gehen","geht","gemeinsame","genau","gerade","gesagt","gibt","glaube","habe","haben","halten","hat","hatte","her","heute","hier","hin","hinaus","hoffe","h�tte","ich","ihm","ihn","ihnen","ihr","ihre","ihrem","ihren","ihrer","im","immer","in","indem","innerhalb","insbesondere","ist","ja","jedoch","jetzt","kann","kaum","kein","keine","keinen","klar","kommen","k�nnen","konnen","k�nnte","konnte","k�nnten","konnten","letzten","liegt","m","machen","man","mehr","meine","meinen","meiner","meines","meist","mich","mir","mit","muss","mu�","m�chte","mochte","m�glich","moglich","m�gliche","m�glichen","m�ssen","mussen","nach","nat�rlich","naturlich","neue","neuen","nicht","nichts","noch","nun","nur","n�mlich","namlich","ob","oder","oft","ohne","sagen","schon","schwach","sehr","sei","sein","seine","seinen","seiner","seit","selbst","sich","sicher","sie","sind","so","sogar","solche","soll","sollen","sollte","sollten","sondern","sowie","sowohl","stehen","steht","stellen","stellt","tun","um","und","uns","unser","unsere","unseren","unserer","unter","unterst�tzen","viel","viele","vielen","vielleicht","vom","von","vor","war","waren","warm","was","wegen","weht","weil","weiter","weitere","weiterhin","welche","weniger","wenn","werde","werden","wichtig","wichtige","wie","wieder","will","wir","wird","wirklich","wissen","wo","wollen","worden","wurde","wurden","w�hrend","w�re","ware","w�rde","wurde","w�rden","wurden","zu","zum","zur","zur�ck","zuruck","zwar","zwei","zwischen","�ber","�ber","uber"};
    Vector stopwords = new Vector();

    public StopWord () {
        init();
    }
    
    public StopWord(String lang) {
        if (lang != null) {
            if (lang.startsWith("ita")) {
                for (int i=0;i<ITALIAN.length;i++) {
                    stopwords.add(ITALIAN[i]);
                }
            } else if (lang.startsWith("eng")) {
                for (int i=0;i<ENGLISH.length;i++) {
                    stopwords.add(ENGLISH[i]);
                }
            } else if (lang.startsWith("ger")) {
                for (int i=0;i<GERMAN.length;i++) {
                    stopwords.add(GERMAN[i]);
                }
            }
        }

        if (stopwords.size() == 0) {
            init();
        }
    }

    private void init() {
        for (int i=0;i<ITALIAN.length;i++) {
            stopwords.add(ITALIAN[i]);
        }
        for (int i=0;i<ENGLISH.length;i++) {
            stopwords.add(ENGLISH[i]);
        }
        for (int i=0;i<GERMAN.length;i++) {
            stopwords.add(GERMAN[i]);
        }
    }
    //Arrays.binarySearch(vec, 35);
    public boolean isStopWord (String stop) {
        if (stopwords.contains(stop.toLowerCase()))
            return true;
        return false;
    }

    public int getStopNumber (String str, boolean withpunc) {
        return listStopWord(str, withpunc).size();
    }

    private Vector listStopWord (String str, boolean withpunc) {
        Vector stops = new Vector();
        str = str.replaceAll("[^\\d|\\w|\\s]"," # ");
        String[] tokens = str.split("\\s");
        for (int i=0; i<tokens.length; i++) {
            if (isStopWord(tokens[i]) || tokens[i].matches("^\\d+$")) {
                stops.add(tokens[i]);
            } else {
                if (withpunc && tokens[i].equals("#")) {
                    stops.add(tokens[i]);
                }
            }
        }

        return stops;
    }

    public String getStopWord (String str, boolean withpunc) {
        Vector stop = listStopWord(str, withpunc);
        String result = "";
        for (int i=0; i<stop.size(); i++) {
            result += " " + stop.get(i);
        }
        return result.trim();
    }

    public static void main(String[] args) throws Exception {
        StopWord sw = new StopWord("ita");
        String line = "Good idea Dan. He is the best one!A nice site www.google.it/. Oggi è il 12";
        System.err.println("LINE: " + line +
                "\nNum of stopwords (with punc.): " + sw.getStopNumber(line,true) + " (" + sw.getStopWord(line,true) + ")"
                + "\nNum of stopwords (without punc.): " + sw.getStopNumber(line,false) + " (" + sw.getStopWord(line,false) + ")"

        );
    }
}