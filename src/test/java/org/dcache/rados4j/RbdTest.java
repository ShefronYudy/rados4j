package org.dcache.rados4j;

import java.util.Random;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author tigran
 */
public class RbdTest {

    private String getEnvOrDefault(String env, String value) {
        String v = System.getenv(env);
        if (v == null) {
            v = value;
        }
        return v;
    }

    private String id;
    private String configFile;
    private String testPool;
    private Rados rados;
    private Rbd rbd;

    @Before
    public void setUp() throws RadosException {
        id = getEnvOrDefault("RADOS_TEST_CLUSTER", "admin");
        configFile = getEnvOrDefault("RADOS_TEST_CONF", "/etc/ceph/ceph.conf");
        testPool = getEnvOrDefault("RADOS_TEST_POOL", "test-pool");

        rados = new Rados(id, configFile);
        rados.connect();
        rados.createPool(testPool);
        rbd = rados.createIoContext(testPool).createRbd();
    }

    @After
    public void tearDown() throws RadosException {
        rados.deletePool(testPool);
        rados.shutdown();
    }

    @Test
    public void testCreateImage() throws RadosException {
        rbd.create("test-image", 0);
        rbd.remove("test-image");
    }

    @Test
    public void testOpenImage() throws RadosException {
        rbd.create("test-image", 0);
        RbdImage image = rbd.open("test-image");
        image.close();
        rbd.remove("test-image");
    }

    @Test
    public void testOpenReadOnlyImage() throws RadosException {
        rbd.create("test-image", 0);
        RbdImage image = rbd.openReadOnly("test-image");
        image.close();
        rbd.remove("test-image");
    }

    @Test
    public void testWriteImage() throws RadosException {
        rbd.create("test-image", 0);
        try (RbdImage image = rbd.open("test-image") ) {
            byte[] data = new byte[1024];
            new Random().nextBytes(data);

            for(int i = 0; i < 1024; i++) {
                image.write(data, i*1024L, data.length);
            }
        }

        rbd.remove("test-image");
    }

    @Test
    public void testReadImage() throws RadosException {
        rbd.create("test-image", 0);
        try (RbdImage image = rbd.open("test-image")) {
            byte[] data = new byte[1024];
            int n = image.read(data, 0L, data.length);
            System.out.println(n);
        }

        rbd.remove("test-image");
    }
}
