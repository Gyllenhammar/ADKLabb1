import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;
public class Process {

    public static void main (String [] args)
    {
        RandomAccessFile RAFin;
        RandomAccessFile RAFout;
        RandomAccessFile RAFfreq;

        long startTime = System.currentTimeMillis();

        try {
            RAFin = new RandomAccessFile( "/Users/andredanielsson/Documents/adk/ut", "r" );
            File indexFile = new File( "/Users/andredanielsson/Documents/adklab1/index" );
            File freqFile = new File("/Users/andredanielsson/Documents/adklab1/freq");

            // Om det inte finns en fil för index, skapa en ny fil.
            if (indexFile.exists() == false)
                indexFile.createNewFile();
            RAFout = new RandomAccessFile(indexFile, "rw");

            // Om det inte finns en fil för freq, skapa en ny fil.
            if (freqFile.exists() == false)
                freqFile.createNewFile();
            RAFfreq = new RandomAccessFile(freqFile, "rw");

            int tempdata; // Innehåller den genererade positionen från hashFunction
            int temppos = 0; // Innehåller den nuvarande positionen i filen "ut"
            String line = null; // Innehåller en rad text från filen "ut"
            byte[] compare = null; // Innehåller de senaste en/två/tre bokstavskombinationer
            Pattern pattern = Pattern.compile(" "); // Används för att dela raderna
            long freqpos = 0; // Anger nästa position att skriva på i frekvensfilen
            int wordCounter = 0; // Håller reda på hur ofta av ett visst ord som förekommer

            String counterString = RAFin.readLine(); // 
            String[] counterSplit = pattern.split(counterString);
            counterString = counterSplit[0];

            String tempString = null; // Används för att skriva en sträng med ett visst format till RAFfreq
            RAFin.seek(0); // Återgår till position 0
            
            // Loopa igenom alla rader i ut-filen
            // Tar ut hashat ORD och POSITION samt skriver dessa
            // i indexfilen
            
            while((line = RAFin.readLine()) != null) {

                String[] split = pattern.split(line);

                // För att undvika att överskriva positioner jämför man
                // de en/två/tre första bokstäverna
                byte[] tempCompare = getWord(split[0]);

                // Skriver i filen som innehåller ett ords frekvens
                if(containsWord(counterString, line) == true)
                    wordCounter++;
                else {
                    tempString = counterString + " " + wordCounter + "\n";
                    RAFfreq.seek(freqpos);
                    RAFfreq.write(tempString.getBytes("ISO-8859-1"));
                    wordCounter = 1;
                    counterString = split[0];
                    freqpos = RAFfreq.getFilePointer();

                    // Om EOF, skriv det sista ordet
                    if(RAFin.getFilePointer() >= RAFin.length()){
                        tempString = counterString + " " + wordCounter + "\n";
                        RAFfreq.seek(freqpos);
                        RAFfreq.write(tempString.getBytes("ISO-8859-1"));
                        wordCounter = 1;
                        counterString = split[0];
                        freqpos = RAFfreq.getFilePointer();
                    }

                }

                // Sriver orden till den binärlagrade filen
                if(Arrays.equals(compare, tempCompare) == false) {
                    compare = tempCompare; // Uppdatera compare

                    tempdata = extractData(line); // Ta ut vilken position man ska skriva data i RAFout
                    RAFout.seek((long)tempdata);
                    RAFout.writeInt(temppos); // Skriver positionen för ett hashat ord
                }
                temppos = (int)RAFin.getFilePointer();
            }

            RAFin.close();
            RAFout.close();
            RAFfreq.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }



        // Tar fram programmets körtid
        long stopTime = System.currentTimeMillis();
        long totalTime = stopTime-startTime;
        System.out.println("Runtime: " + totalTime + " ms, " + (totalTime/1000) + " s.");

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
    
    /*
     * Tar in en string, returnerar en chararray av de en/två/tre första bokstäverna
     */
    private static byte[] getWord(String s){
        byte[] returnArray = new byte[3];
        try {
            int len = s.length();
            byte[] input = s.getBytes("ISO-8859-1");

            // Skapar ser till att ett visst format skapas
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

    /*
     * Tar in en string s med format "ORD POSITION"
     * och returnerar dess hashvärde
     */
    private static int extractData(String s){
        int returnValue = 0;
        Pattern pattern = Pattern.compile(" ");
        String[] split = pattern.split(s); // [0] - Ordet, [1] positionen

        // Ta ut de en/två/tre första bokstäverna
        byte[] word = getWord(split[0]);

        // Får ut hashfunktionens värde baserat på de en/två/tre första bokstäverna
        returnValue = hashFunction(word);

        // Skapar ett avstånd 4 bytes så att inget skrivs över
        // (då vi skriver med int)
        if (returnValue > 0)
            returnValue = returnValue * 4 - 3;

        return returnValue;
    }

    /*
     * Tar in två ord och jämför orden fram till ett mellanslag
     * Är de samma ord returneras true annars false
     */
    private static boolean containsWord(String s1, String s2){
        if (s1 == null || s2 == null) {
            return false;
        }
        s1 += " ";

        for (int i = 0; i < s1.length(); i++) {
            if(s1.charAt(i) != s2.charAt(i))
                return false;
        }

        return true;
    }

}
