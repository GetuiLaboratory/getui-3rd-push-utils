package com.getui.gtps;

import com.getui.gtps.manufacturer.ManufacturerFactory;
import com.getui.gtps.manufacturer.ManufacturerFile;
import com.getui.gtps.manufacturer.Result;
import com.getui.gtps.manufacturer.constant.ManufacturerConstants;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author zhourh@getui.com
 * Date: 2021-01-06
 */
public class TestUtils {


    @Before
    public void init() throws IOException {
        GtSDKStarter.getInstance().loadPropertyFile("/config/application.properties").init();
    }

    @Test
    public void testUploadIcon() throws FileNotFoundException {
        Map<String, Result> resultMap = ManufacturerFactory.uploadIcon(new File("image/icon.png"));

        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertNotNull(resultMap.get("XM"));
        assertEquals(0, resultMap.get("XM").getCode());
        assertEquals("success", resultMap.get("XM").getMessage());
        assertThat(resultMap.get("XM").getData(), new StringStartsWith("http://"));

        assertNotNull(resultMap.get("OPPO"));
        assertEquals(resultMap.get("OPPO").getCode(), 0);
        assertEquals(resultMap.get("OPPO").getMessage(), "success");
        assertThat(resultMap.get("OPPO").getData(), new StringContains("_"));
    }

    @Test
    public void testUploadPic() throws FileNotFoundException {
        Map<String, Result> resultMap = ManufacturerFactory.uploadPic(new File("image/pic.jpeg"));

        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertNotNull(resultMap.get("XM"));
        assertEquals(0, resultMap.get("XM").getCode());
        assertEquals("success", resultMap.get("XM").getMessage());
        assertThat(resultMap.get("XM").getData(), new StringStartsWith("http://"));

        assertNotNull(resultMap.get("OPPO"));
        assertEquals(resultMap.get("OPPO").getCode(), 0);
        assertEquals(resultMap.get("OPPO").getMessage(), "success");
        assertThat(resultMap.get("OPPO").getData(), new StringContains("_"));
    }

    @Test
    public void testUploadIconSeparately() {
        ManufacturerFile file1 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.OPPO, "image/icon.png");
        ManufacturerFile file2 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.XM, "image/icon.png");
        ManufacturerFile[] manufacturerFiles = new ManufacturerFile[]{file1, file2};
        Map<String, Result> resultMap = ManufacturerFactory.uploadIcon(manufacturerFiles);

        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertNotNull(resultMap.get("XM"));
        assertEquals(0, resultMap.get("XM").getCode());
        assertEquals("success", resultMap.get("XM").getMessage());
        assertThat(resultMap.get("XM").getData(), new StringStartsWith("http://"));

        assertNotNull(resultMap.get("OPPO"));
        assertEquals(resultMap.get("OPPO").getCode(), 0);
        assertEquals(resultMap.get("OPPO").getMessage(), "success");
        assertThat(resultMap.get("OPPO").getData(), new StringContains("_"));
    }

    @Test
    public void testUploadPicSeparately() {
        ManufacturerFile file1 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.OPPO, "image/pic.jpeg");
        ManufacturerFile file2 = new ManufacturerFile(ManufacturerConstants.ManufacturerName.XM, "image/pic.jpeg");
        ManufacturerFile[] manufacturerFiles = new ManufacturerFile[]{file1, file2};
        Map<String, Result> resultMap = ManufacturerFactory.uploadPic(manufacturerFiles);

        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertNotNull(resultMap.get("XM"));
        assertEquals(0, resultMap.get("XM").getCode());
        assertEquals("success", resultMap.get("XM").getMessage());
        assertThat(resultMap.get("XM").getData(), new StringStartsWith("http://"));

        assertNotNull(resultMap.get("OPPO"));
        assertEquals(resultMap.get("OPPO").getCode(), 0);
        assertEquals(resultMap.get("OPPO").getMessage(), "success");
        assertThat(resultMap.get("OPPO").getData(), new StringContains("_"));
    }

    @Test
    public void testMemoryCache() throws FileNotFoundException {
        Map<String, Result> resultMap = ManufacturerFactory.uploadIcon(new File("image/icon.png"));

        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());

        assertNotNull(resultMap.get("XM"));
        assertEquals(0, resultMap.get("XM").getCode());
        assertEquals("success", resultMap.get("XM").getMessage());
        assertThat(resultMap.get("XM").getData(), new StringStartsWith("http://"));

        assertNotNull(resultMap.get("OPPO"));
        assertEquals(resultMap.get("OPPO").getCode(), 0);
        assertEquals(resultMap.get("OPPO").getMessage(), "success");
        assertThat(resultMap.get("OPPO").getData(), new StringContains("_"));

        Map<String, Result> resultMap2 = ManufacturerFactory.uploadIcon(new File("image/icon.png"));
        assertEquals(resultMap.size(), resultMap2.size());
        assertEquals(resultMap.get("XM").getData(), resultMap2.get("XM").getData());
        assertEquals(resultMap.get("OPPO").getData(), resultMap2.get("OPPO").getData());
    }
}
