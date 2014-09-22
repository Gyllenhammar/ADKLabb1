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
            RandomAccessFile RAFindex = new RandomAccessFile("/Users/andredanielsson/Documents/adklab1/index", "r");
            RandomAccessFile RAFut = new RandomAccessFile("/Users/andredanielsson/Documents/adk/ut", "r");
            RandomAccessFile RAFkorpus = new RandomAccessFile("/Users/andredanielsson/Documents/adk/korpus", "r");
            RandomAccessFile RAFfreq = new RandomAccessFile("/Users/andredanielsson/Documents/adklab1/freq", "r");

            if(args.length == 1) {
                byte[] word1 = getWord(args[0]);
                int ut2pos = 0;
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
                long stopTime = System.currentTimeMillis();
                long totalTime = stopTime-startTime;

                System.out.println("Tid efter att hitta nästa bokstavskomb: " + totalTime);

                /*
                // Ta ut orden från filen
                RAFut.seek((long)ut1pos);// Positionen för första ordet i filen 'ut'
                while ((line = RAFut.readLine()) != null) {
                    wordWithIndex.add(line);
                    if(RAFut.getFilePointer() == (long)ut2pos) // När man läst allt från ut1pos till ut2pos
                        break;
                }
                stopTime = System.currentTimeMillis();
                totalTime = stopTime-startTime;

                io.println("Tid att ta ut orden från filen: " + totalTime);*/

                /*//DEBUG
                for (int i = 0; i < wordWithIndex.size(); i++) {
                    io.println(wordWithIndex.get(i));
                }*/

                System.out.println("ut1pos: " + ut1pos + " ut2pos: " + ut2pos);
                // SKRIV I BINARYSEARCHLAST OM UT2POS ÄR == 0
                long firstoccurrence = binarySearchFirst(RAFut, args[0], (long)ut1pos, (long)ut2pos);
                long lastoccurrence = binarySearchLast(RAFut, args[0], (long)ut1pos, (long)ut2pos);
                if(firstoccurrence >= 0){
                    RAFut.seek(firstoccurrence);
                    io.println("ut: " + RAFut.readLine());
                }
                if(lastoccurrence >= 0){
                    RAFut.seek(lastoccurrence);
                    io.println("ut: " + RAFut.readLine());
                }
                else {
                    io.println("lastoccurrence hittas inte");
                }
                System.out.println();

                long freqPos = binarySearchFirst(RAFfreq, args[0], 0, RAFfreq.length());
                RAFfreq.seek(freqPos);
                System.out.println("Antal förekomster: " + RAFfreq.readLine());
                
                
                long iteratingOffset = firstoccurrence;
                System.out.println("firstoccurrence: " + firstoccurrence + " lastoccurrence: " + lastoccurrence);
                RAFut.seek(firstoccurrence);
                byte[] buff = new byte[80];
                int counter = 0;
                while (iteratingOffset <= lastoccurrence) {
                    String s = RAFut.readLine();
                    Pattern pattern = Pattern.compile(" ");
                    String[] split = pattern.split(s); // [0] ord, [1] position
                    if(iteratingOffset > 30)
                        RAFkorpus.seek(Long.parseLong(split[1])-30);
                    else
                        RAFkorpus.seek(0);

                    RAFkorpus.read(buff,0, 60 + split[0].length());
                    System.out.println(removeLineBreak(new String(buff, "ISO-8859-1")));
                    iteratingOffset = RAFut.getFilePointer();

                    counter++;
                    if(counter == 24){
                        System.out.println( System.currentTimeMillis() - startTime + " millisekunder har gått");
                        System.out.print("Vill du skriva ut resten?(j/n):");
                        String tempString = io.getWord();
                        if(tempString.equals("j"))
                            counter = 25;
                        else
                            break;
                    }
                }
                

                /*
                // Hitta första indexet
                int first = -1;
                for (int i = 0; i < wordWithIndex.size() && first == -1; i++) { 
                    if(containsWord(args[0], wordWithIndex.get(i))) // kolla om orden är samma
                        first = i;
                }
                stopTime = System.currentTimeMillis();
                totalTime = stopTime-startTime;

                io.println("Tid att hitta första indexet: " + totalTime + " index: " + index + " first: " + first + " size " + wordWithIndex.size());
                
                if(first == -1) {
                    io.println(args[0] + " finns inte i listan");
                    RAFindex.close();
                    RAFut.close();
                    RAFkorpus.close();
                    io.close();
                    return;
                }*/
                // Hitta sista indexet

                /* int last = word.lastIndexOf(args[0]); */
                /*  */
                /*  */
                /*  */
                /* //DEBUG */
                /* io.println("first: " + first + " last: " + last); */
                /*  */
                /*  */
                /* byte[] buff = new byte[60 + word.get(first).length()]; */
                /* int counter = 0; */
                /*  */
                /* // Skriva ut 25 förekomster */
                /* for (int i = first; i <= last && counter < 25; i++) { */
                    /* io.println(word.get(i) + " index: " + index.get(i));//DEBUG */
                /*     Pattern pattern = Pattern.compile(" "); */
                /*     String[] split = pattern.split(line); // [0] ord, [1] position */
                /*     word.add(split[0]); */
                /*     index.add(Integer.valueOf(split[1])); */
                /*     if(index.get(i) > 30) */
                /*         RAFkorpus.seek(index.get(i).longValue()-30); */
                /*     else */
                /*         RAFkorpus.seek(0); */
                /*     RAFkorpus.read(buff,0, 60 + word.get(first).length()); */
                /*     io.println(removeLineBreak(new String(buff, "ISO-8859-1"))); */
                /*  */
                /*     counter++; */
                /*     if(counter == 24){ */
                /*         io.print("Vill du fortsätta?(j/n):"); */
                /*         String tempString = io.getWord(); */
                /*         if(tempString.equals("j")) */
                /*             counter = 0; */
                /*     } */
                /* } */

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
            RAFfreq.close();
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
    
    private static boolean containsWord(String s1, String s2){
        s1 += " ";

        for (int i = 0; i < s1.length(); i++) {
            if(s1.charAt(i) != s2.charAt(i))
                return false;
        }

        return true;
    }


    private static long binarySearchFirst(RandomAccessFile raf, String target, long lowestOffset, long highestOffset) throws IOException{
        raf.seek(lowestOffset);
        String line = raf.readLine();

        if (containsWord(target,line) == true) {
            return lowestOffset;
        }

        long low = lowestOffset;
        long high = highestOffset;
        long p = -1;


        while (low < high) {

            long mid = (low + high) >>> 1;
            p = mid;

            // Går byte för byte bakåt tills man hittat en linebreak
            while (p >= lowestOffset) {
                raf.seek(p);
                char c = (char) raf.readByte();
                if(c == '\n')
                    break;
                p--;
            }
            if(p < lowestOffset)
                raf.seek(lowestOffset);

            line = raf.readLine();

            if( (line == null) || line.compareTo(target) < 0)
                low = mid + 1;
            else
                high = mid;
        }

        p = low;
        while (p >= lowestOffset){
            raf.seek(p);
            if(((char) raf.readByte()) == '\n')
                break;
            p--;
        }
        if(p < lowestOffset)
            raf.seek(lowestOffset);

        while(true){
            long returnValue = raf.getFilePointer();
            line = raf.readLine();
            if(line == null || containsWord(target, line) == false)
                break;
            return returnValue;
        }

        // Nothing found
        return -1;

    }
    
    private static long binarySearchLast(RandomAccessFile raf, String target, long lowestOffset, long highestOffset) throws IOException{
        
        long off = highestOffset-2;
        // Går byte för byte bakåt för att hitta en linebreak
        while (off >= lowestOffset){
            raf.seek(off);
            if(((char) raf.readByte()) == '\n')
                break;
            off--;
        }
        if(off < lowestOffset){
            raf.seek(lowestOffset);
        }
        else{
            raf.seek(highestOffset);
 
       }
        String line = raf.readLine();
        /*
        if (containsWord(target,line) == true) {
            return highestOffset;
        }*/

        long low = lowestOffset;
        long mid = 0;
        long high = highestOffset;
        long p = -1;


        while (low < high) {

            mid = (low + high) >>> 1;
            p = mid;

            // Går byte för byte bakåt tills man hittat en linebreak
            while (p >= lowestOffset) {
                raf.seek(p);
                char c = (char) raf.readByte();
                if(c == '\n')
                    break;
                p--;
            }
            if(p < lowestOffset)
                raf.seek(lowestOffset);

            line = raf.readLine();

            // Om line inte kunnat läsa till newline eller om line ligger före target
            if( (line == null) || line.compareTo(target) < 0 || containsWord(target, line) == true ){
                low = mid + 1;
            }
            else
                high = mid;
        }

        p = mid;
        // Går byte för byte bakåt för att hitta en linebreak
        while (p >= lowestOffset){
            raf.seek(p);
            if(((char) raf.readByte()) == '\n')
                break;
            p--;
        }
        if(p < lowestOffset)
            raf.seek(lowestOffset);

        while(true){
            long returnValue = raf.getFilePointer();
            line = raf.readLine();
            if(line == null || containsWord(target, line) == false)
                break;
            return returnValue;
        }

        // Nothing found
        return -1;
    }



}
