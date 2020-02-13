package space.aqoleg.network;

import space.aqoleg.utils.Converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Data {
    private static final Data data = new Data();

    private Data() {
    }

    /**
     * @param address String with address
     * @return instance of the Balance for this address or null
     */
    public static Balance getBalance(String address) {
        try {
            URL url = new URL("https://blockchain.info/balance?active=" + address);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            do {
                input = reader.readLine();
                if (input == null) {
                    reader.close();
                    input = builder.toString();
                    break;
                }
                builder.append(input);
            } while (true);
            // {"address":{"final_balance":0,"n_tx":0,"total_received":0}}
            String value = parse(input, "final_balance");
            String txN = parse(input, "n_tx");
            return data.new Balance(txN, value);
        } catch (IOException | IndexOutOfBoundsException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    /**
     * @param address String with address
     * @return ArrayList of the UnspentOutputs for this address or null
     */
    public static ArrayList<UnspentOutput> getUnspentOutputs(String address) {
        try {
            URL url = new URL("https://blockchain.info/unspent?active=" + address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            if (connection.getResponseCode() != 200) {
                connection.disconnect();
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuilder builder = new StringBuilder();
            do {
                input = reader.readLine();
                if (input == null) {
                    reader.close();
                    input = builder.toString();
                    break;
                }
                builder.append(input);
            } while (true);
            connection.disconnect();
            // {"unspent_outputs":[{"tx_hash":"1", "tx_hash_big_endian":"1", "tx_output_n":1,
            // "script":"1", "value":1, "value_hex":"1", "confirmations":1, "tx_index":1}, ...]}
            ArrayList<UnspentOutput> out = new ArrayList<>();
            do {
                if (!input.contains("tx")) {
                    return out;
                }
                String transactionHash = parse(input, "tx_hash_big_endian");
                String outIndex = parse(input, "tx_output_n");
                String scriptPubKey = parse(input, "script");
                String value = parse(input, "value");
                input = input.substring(input.indexOf("tx_index") + 12);
                out.add(data.new UnspentOutput(transactionHash, outIndex, scriptPubKey, value));
            } while (true);
        } catch (IOException | IndexOutOfBoundsException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    private static String parse(String input, String key) {
        int startIndex = input.indexOf(key) + key.length();
        do {
            startIndex++;
            char c = input.charAt(startIndex);
            if (c != ':' && c != ' ') {
                break;
            }
        } while (true);
        boolean isString = input.charAt(startIndex) == '"';
        if (isString) {
            startIndex++;
        }
        int stopIndex = startIndex;
        if (isString) {
            do {
                stopIndex++;
                char c = input.charAt(stopIndex);
                if (c == '"') {
                    break;
                } else if (c == '\\') {
                    stopIndex++;
                }
            } while (true);
        } else {
            do {
                stopIndex++;
                char c = input.charAt(stopIndex);
                if (c == '+' || c == '-' || c == 'e' || c == 'E' || c == '.') {
                    stopIndex++;
                } else if (!Character.isDigit(c)) {
                    break;
                }
            } while (true);
        }
        return input.substring(startIndex, stopIndex);
    }

    public class Balance {
        public final int txN; // number of the transactions
        public final BigDecimal value; // btc

        private Balance(String txN, String value) {
            this.txN = Integer.parseInt(txN);
            this.value = new BigDecimal(value).movePointLeft(8);
        }
    }

    public class UnspentOutput {
        public final byte[] transactionHash; // little endian, as in the explorers
        public final int outIndex;
        public final byte[] scriptPubKey;
        public final BigDecimal value; // btc

        private UnspentOutput(String transactionHash, String outIndex, String scriptPubKey, String value) {
            this.transactionHash = Converter.hexToBytes(transactionHash);
            this.outIndex = Integer.parseInt(outIndex);
            this.scriptPubKey = Converter.hexToBytes(scriptPubKey);
            this.value = new BigDecimal(value).movePointLeft(8);
        }
    }
}