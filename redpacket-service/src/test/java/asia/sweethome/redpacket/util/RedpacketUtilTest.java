package asia.sweethome.redpacket.util;

import org.junit.jupiter.api.Test;

class RedpacketUtilTest {

    @Test
    void testSplitRedpacket() {
        System.out.println(
                RedpacketUtil.splitRedpacket(
                        10, 2
                )
        );
    }

}