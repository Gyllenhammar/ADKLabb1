import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;
public class Process {

    public static void main (String [] args)
    {
        RandomAccessFile RAFin;
        RandomAccessFile RAFout;

        try {
            RAFin = new RandomAccessFile( "/Users/andredanielsson/Documents/adk/ut", "r" );
            File indexFile = new File( "/Users/andredanielsson/Dropbox/AlgoDataKomp/Lab1/index" );

            // Om det inte finns en fil för index, skapa en ny fil.
            if (indexFile.exists() == false)
                indexFile.createNewFile();
            RAFout = new RandomAccessFile(indexFile, "rw");

            int[] tempdata;
            String line = null;
            
            // Loopa igenom alla rader i ut-filen
            // Tar ut hashat ORD och POSITION samt skriver dessa
            // i indexfilen
            /* while((line = RAFin.readLine()) != null) {  */
            for (int i = 0; i < 120000; i++) {
                line = RAFin.readLine();
                tempdata = extractData(line);
                RAFout.seek((long)tempdata[0]);
                RAFout.writeInt(tempdata[1]);
            }

            /* int[] data = new int[2]; */
            /*  */
            /* data = extractData("testord 278901"); */
            /* // Tar sig till positionen i filen */
            /* // X * 4 - 3 */
            /* RAFout.seek((long)data[0]); */
            /* System.out.println("pos: " + RAFout.getFilePointer()); */
            /* RAFout.writeInt(data[1]); */
            /*  */
            /* // DEBUG */
            /* String bla = new String(getWord("aasdasd "), "ISO-8859-1"); */
            /* System.out.println(bla + "."); */
            /*  */
            /* System.out.println("hashfun for aa: " + hashFunction(new String("ab ").getBytes("ISO-8859-1"))); */
            /*  */
            /* System.out.println("d - a = " + ('d'-'a' + 1)); */
            /* System.out.println("e: " + getCharValue('å')); */

            RAFin.close();
            RAFout.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

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
            case 0xE4:
                return 27;
            case 0xE5:
                return 28;
            case 0xF6:
                return 29;
            default:
                return (1 + b-0x61);
        }
    }
    
    /*
     * Tar in en string, returnerar en chararray av de en/två/tre första bokstäverna
     * LETA EFTER EVENTUELLA FEL
     */
    private static byte[] getWord(String s){
        int len = s.length();
        byte[] input = s.getBytes();
        byte[] returnArray = new byte[3];
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

        return returnArray;
    }

    /*
     * Tar in en string s med format "ORD POSITION"
     * och returnerar
     * [0]: hashvärde
     * [1]: position i texten
     */
    private static int[] extractData(String s){
        int[] returnArray = new int[2];
        Pattern pattern = Pattern.compile(" ");
        String[] split = pattern.split(s); // [0] - Ordet, [1] positionen

        // Ta ut de en/två/tre första bokstäverna
        byte[] word = getWord(new StringBuilder(split[0]).append(' ').toString());

        // positionen konverteras från string till int
        returnArray[1] = Integer.parseInt(split[1]);

        // Avstånd 4 bytes mellan int
        if (returnArray[0] > 0)
            returnArray[0] = returnArray[0] * 4 - 3;

        return returnArray;
    }

}
