import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    //fdf9:da92:1f1e:10:e988:7574:cae1:f826
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println(help());
            System.exit(1);
        }
        String hostname = args[0], configPath = args[1],
                prefixFromWeb = calcPrefix(getAddressFromWeb()),
                prefixFormConfig = calcPrefix(getAddressFromConfig(hostname,configPath));

        if(Objects.equals(prefixFromWeb,prefixFormConfig)) {
            System.exit(0);
        }

        setPrefix(prefixFromWeb,prefixFormConfig,configPath);
    }

    public static String calcPrefix(String address) {
        for (int i = 0; i < 4; i++) {
            address = address.substring(0,address.lastIndexOf(":"));
        }
        return address;
    }
    public static void setPrefix(String prefixFromWeb, String prefixFormConfig,
                                 String configPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(configPath)
        );

        StringBuilder config = new StringBuilder();
        String configLine;

        while ((configLine = bufferedReader.readLine()) != null) {
            config.append(configLine.replace(prefixFormConfig,prefixFromWeb)).append("\n");
        }
        bufferedReader.close();

        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(configPath)
        );

        bufferedWriter.write(config.toString());
        bufferedWriter.close();
    }
    public static String getAddressFromConfig(String hostname, String configPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(configPath)
        );

        String configLine;

        while ((configLine = bufferedReader.readLine()) != null) {
            if(configLine.contains(hostname) && configLine.contains("IN")) {
                configLine = configLine.replace(" ","");
                configLine = configLine.substring(configLine.indexOf("A") + 4);

                bufferedReader.close();
                return configLine;
            }
        }
        bufferedReader.close();
        return null;
    }
    public static String getAddressFromWeb() throws IOException {
        URL currentIP = new URL("https://6.myip.is/");
        Scanner sc = new Scanner(currentIP.openStream());
        StringBuilder stringBuilder = new StringBuilder();

        while (sc.hasNext()) {
            stringBuilder.append("\n").append(sc.next());
        }

        int start = stringBuilder.indexOf(":") + 2;
        int end = stringBuilder.indexOf(",") - 1;

        return  stringBuilder.substring(start,end);
    }

    public static String help() {
        return """
                provide a hostname to check against and the path to your bind zone-file, where your Records are located.\s
                -> prefix-swapper hostname path/to/config

                It makes sense to add the programm into a cronjob =)""";
    }
}