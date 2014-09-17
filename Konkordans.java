import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.ArrayList;
public class Konkordans{

    public static void main (String [] args)
    {
        Kattio io = new Kattio(System.in, System.out);
        
        long startTime = System.currentTimeMillis();
        try {
            RandomAccessFile RAFindex = new RandomAccessFile("/Users/andredanielsson/Documents/index", "r");
            RandomAccessFile RAFut = new RandomAccessFile("/Users/andredanielsson/Documents/adk/ut", "r");
            RandomAccessFile RAFkorpus = new RandomAccessFile("/Users/andredanielsson/Documents/adk/korpus", "r");

            if(args.length == 1) {
                byte[] word1 = getWord(args[0]);
                int ut2pos = 0;
                ArrayList<String> word = new ArrayList<String>();
                ArrayList<Integer> index = new ArrayList<Integer>();
                String line;

                int indexpos = hashFunction(word1)*4-3; // Innehåller positionen för det sökta ordet i 'index'
                RAFindex.seek((long)indexpos);
                int ut1pos = RAFindex.readInt(); // Innehåller positionen för de tre första bokstäverna ur args[0] i filen 'ut'
                RAFindex.seek((long)indexpos); // Ställer tillbaka positionen så att ingen nästkommande bokstavskomb. skippas.

                if(ut1pos == 0 && indexpos != 3597) { // Om ej någon position är 0 och positionen inte är för 'a'
                    io.println(args[0] + " finns inte i listan");
                    RAFindex.close();
                    RAFut.close();
                    RAFkorpus.close();
                    io.close();
                    return;
                }

                // Letar igenom 'index' efter nästkommande bokstavskombination
                while(RAFindex.skipBytes(4) == 4 && indexpos != 107993) {
                    ut2pos = RAFindex.readInt(); // Innehåller positionen till det nästkommande ordet i filen 'ut'
                    if(ut2pos > 0) {
                        break;
                    }
                    RAFindex.seek(RAFindex.getFilePointer()-4); // Går tillbaka en int för att inte skippa några bytes
                }

                // FLYTTA SPLITTANDET TILL FORLOOPEN
                RAFut.seek((long)ut1pos);// Positionen för första ordet i filen 'ut'
                while ((line = RAFut.readLine()) != null) {
                    Pattern pattern = Pattern.compile(" ");
                    String[] split = pattern.split(line); // [0] ord, [1] position
                    word.add(split[0]);
                    index.add(Integer.valueOf(split[1]));
                    if(RAFut.getFilePointer() == (long)ut2pos) // När man läst allt från ut1pos till ut2pos
                        break;
                }
                
                int first = word.indexOf(args[0]);
                if(first == -1) {
                    io.println(args[0] + " finns inte i listan");
                    RAFindex.close();
                    RAFut.close();
                    RAFkorpus.close();
                    io.close();
                    return;
                }
                int last = word.lastIndexOf(args[0]);
                /*  */
                /*  */
                /*  */
                /* //DEBUG */
                /* io.println("first: " + first + " last: " + last); */
                /*  */
                /*  */
                byte[] buff = new byte[60 + word.get(first).length()];
                int counter = 0;
                for (int i = first; i <= last && counter < 25; i++) {
                    /* io.println(word.get(i) + " index: " + index.get(i));//DEBUG */
                    if(index.get(i) > 30)
                        RAFkorpus.seek(index.get(i).longValue()-30);
                    else
                        RAFkorpus.seek(0);
                    RAFkorpus.read(buff,0, 60 + word.get(first).length());
                    io.println(removeLineBreak(new String(buff, "ISO-8859-1")));

                    counter++;
                    if(counter == 24){
                        io.print("Vill du fortsätta?(j/n):");
                        String tempString = io.getWord();
                        if(tempString.equals("j"))
                            counter = 0;
                    }
                }

                /* //DEBUG */
                /* RAFut.seek((long)ut1pos); */
                /* String s1 = RAFut.readLine(); */
                /* RAFut.seek((long)ut2pos); */
                /* String s2 = RAFut.readLine(); */
                /* io.println("s1: " + s1 + " s2: " + s2); */
                /*  */
                /* io.println("args[0] = " + args[0] + " ut1pos = " + ut1pos + " ut2pos = " + ut2pos); */

            }

            /* // DEBUG */
            /* int index = 0; */
            /* String sTest = null; */
            /* RAFindex.seek(3597); */
            /* while(RAFindex.skipBytes(4) == 4) { */
            /*     index = RAFindex.readInt(); */
            /*     if(index > 0){ */
            /*         RAFut.seek((long)index); */
            /*         sTest = RAFut.readLine(); */
            /*         io.println("sTest: " + sTest + "index: " + index); */
            /*     } */
            /* } */
            /*  */
            /* byte[] word = getWord("stä"); */
            /*  */
            /* int pos = hashFunction(word) * 4 - 3; */
            /*  */
            /* RAFindex.seek((long)pos); */
            /*  */
            /* System.out.println("RAFindex.getFilePointer: " + RAFindex.getFilePointer()); */
            /*  */
            /* int val = RAFindex.readInt(); */
            /*  */
            /* System.out.println("val: " + val); */
            /*  */
            /* RAFut.seek((long)val); */
            /* String s = RAFut.readLine(); */
            /*  */
            /* System.out.println("Content: " + s); */


            RAFindex.close();
            RAFut.close();
            RAFkorpus.close();
        }
       catch(IOException e) {
           e.printStackTrace();
       }
        // Tar fram programmets körtid
        long stopTime = System.currentTimeMillis();
        long totalTime = stopTime-startTime;
        System.out.println("Runtime: " + totalTime + " ms, " + (totalTime/1000) + " s.");

       io.close();
    }
    
    /* 
     * hashFunction tar in en array av typ char och genererar med
     * latmannahashning en position för en viss bokstavskombination
     */
    private static int hashFunction( byte[] b ){
        if( b[0] == ' ' )
            return 0;

        if( b[1] == ' ' )
            return getCharValue(b[0])*900;

        if( b[2] == ' ' )
            return getCharValue(b[0])*900 + getCharValue(b[1])*30;

        else
            return getCharValue(b[0])*900 + getCharValue(b[1])*30 + getCharValue(b[2]);
    }

    /*
     * Tar in en byte b och returnerar ett värde för bokstaven
     * ex: a -> 1
     *     b -> 2
     *     ...
     */
    private static int getCharValue( byte b ){
        int i = new Byte(b).intValue(); 

        switch(i){
            case -28:
                return 27;
            case -27:
                return 28;
            case -10:
                return 29;
            default:
                return (1 + b-0x61);
        }
    }

    private static String removeLineBreak(String s) {
        return s.replaceAll("\r\n", " ");
    }
    
    /*
     * Tar in en string, returnerar en chararray av de en/två/tre första bokstäverna
     * LETA EFTER EVENTUELLA FEL
     */
    private static byte[] getWord(String s){
        byte[] returnArray = new byte[3];
        try {
            int len = s.length();
            byte[] input = s.getBytes("ISO-8859-1");
            switch(len){
                case 1:
                    returnArray[0] = input[0];
                    returnArray[1] = ' ';
                    returnArray[2] = ' ';
                    break;
                case 2:
                    returnArray[0] = input[0];
                    returnArray[1] = input[1];
                    returnArray[2] = ' ';
                    break;
                default:
                    returnArray[0] = input[0];
                    returnArray[1] = input[1];
                    returnArray[2] = input[2];
                    break;
            }

        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return returnArray;

    }

}
