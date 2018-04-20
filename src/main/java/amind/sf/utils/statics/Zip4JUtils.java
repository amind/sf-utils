package amind.sf.utils.statics;

import java.io.File;
import java.util.UUID;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 *
 * @author acdc
 */
public class Zip4JUtils {

    public static File generateTempFileZip() {

        File temp;

        String tmpFileDir = System.getProperty("java.io.tmpdir");

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        
        String tmpFileName = tmpFileDir + "/tmpf" + randomUUIDString + ".zip";

        temp = new File(tmpFileName);

        return temp;

    }

    public static File compress(String inputDir) throws ZipException {

        File compressedFile = generateTempFileZip();

        ZipFile zipFile = new ZipFile(compressedFile);

        ZipParameters parameters = new ZipParameters();

        // COMP_DEFLATE is for compression
        // COMp_STORE no compression
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // DEFLATE_LEVEL_ULTRA = maximum compression
        // DEFLATE_LEVEL_MAXIMUM
        // DEFLATE_LEVEL_NORMAL = normal compression
        // DEFLATE_LEVEL_FAST
        // DEFLATE_LEVEL_FASTEST = fastest compression
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

        File dir = new File(inputDir);
        File[] files = dir.listFiles();

        for (File f : files) {
            // file compressed
            zipFile.addFile(f, parameters);
        }

        /*long uncompressedSize = inputFileH.length();
            File outputFileH = new File(compressedFile);
            long comrpessedSize = outputFileH.length();

            //System.out.println("Size "+uncompressedSize+" vs "+comrpessedSize);
            double ratio = (double) comrpessedSize / (double) uncompressedSize;
            System.out.println("File compressed with compression ratio : " + ratio);*/
        return compressedFile;

    }

}
