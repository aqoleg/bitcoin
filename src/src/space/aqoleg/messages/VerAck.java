// reply to Version message
// no payload
package space.aqoleg.messages;

public class VerAck {
    public static final String command = "verack";

    private VerAck() {
    }

    /**
     * @return byte array with VerAck message
     */
    public static byte[] toByteArray() {
        return Message.toByteArray(command, new byte[0]);
    }
}