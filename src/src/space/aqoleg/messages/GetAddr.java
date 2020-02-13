// this message requests Addr message
// no payload
package space.aqoleg.messages;

public class GetAddr {

    private GetAddr() {
    }

    /**
     * @return byte array with GetAddr message
     */
    public static byte[] toByteArray() {
        return Message.toByteArray("getaddr", new byte[0]);
    }
}