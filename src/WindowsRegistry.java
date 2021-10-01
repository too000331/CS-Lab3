import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;

public class WindowsRegistry
{
    public static void importSilently(String regFilePath) throws IOException,
            InterruptedException
    {
        if (!new File(regFilePath).exists())
        {
            throw new FileNotFoundException();
        }

        Process importer = Runtime.getRuntime().exec("reg import " + regFilePath);

        importer.waitFor();
    }

    public static void overwriteValue(String keyPath, String keyName, String keyType,
                                      String keyValue) throws IOException, InterruptedException
    {
        Process overwriter = Runtime.getRuntime().exec(
                "reg add " + keyPath + " /t " + keyType + " /v \"" + keyName + "\" /d "
                        + keyValue + " /f");

        int retVal = overwriter.waitFor();
        if(retVal != 0) {
            String readLine;
            StringBuffer errBuffer = new StringBuffer();
            try(BufferedReader errRdr = new BufferedReader(new InputStreamReader(overwriter.getErrorStream()))) {
                while ((readLine = errRdr.readLine()) != null)
                {
                    errBuffer.append(readLine);
                }
            }
            throw new IOException(errBuffer.toString());
        }
    }

    public static String getValue(String keyPath, String keyName, String keyType)
            throws IOException, InterruptedException
    {
        Process keyReader = Runtime.getRuntime().exec(
                "reg query \"" + keyPath + "\" /v \"" + keyName + "\"");

        BufferedReader outputReader;
        String readLine;
        StringBuffer outputBuffer = new StringBuffer();

        outputReader = new BufferedReader(new InputStreamReader(
                keyReader.getInputStream()));

        while ((readLine = outputReader.readLine()) != null)
        {
            outputBuffer.append(readLine);
        }

        String[] outputComponents = outputBuffer.toString().split("    ");

        keyReader.waitFor();

        return formatValue(outputComponents[outputComponents.length - 1], keyType);
    }

    public static String formatValue(String value, String type) {
        if(value == null || value.isEmpty()) {
            return "";
        }
        switch(type) {
            case "REG_SZ":
                return value;
            case "REG_DWORD" :
                try {
                    Integer n = Integer.decode(value);
                    return n.toString(); // Integer.valueOf(n.intValueExact()).toString();
                }catch(NumberFormatException ex) {
                    return "NaN";
                }
        }
        return value;
    }
}