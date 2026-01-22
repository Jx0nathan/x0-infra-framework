package com.supercode.framework.util;

import com.supercode.framework.utils.ExcelUtil;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :brave
 * @date : 2023/7/18
 */
public class ExcelUtilTest {
    String filePath = "/Users/wangyu/test.xlsx";

    @Test
    public void testExportFromMap() {
        List<Map> data = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            Map map = new HashMap();
            map.put("name", "name" + i);
            map.put("age", i);
            data.add(map);
        }
        try {
            ExcelUtil.downloadToExcelFromMap(new File(filePath), data,
                    new String[]{"name", "age"}, new String[]{"姓名", "年龄"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadToObject() throws IOException {
        ExcelUtil.readExcelToList(new File(filePath)).forEach(System.out::println);
    }

    @Test
    public void testExportFromObject() {
        List<TestAAA> data = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            TestAAA map = new TestAAA();
            map.setAge(i);
            map.setName("name" + i);
            data.add(map);
        }
        try {
            ExcelUtil.downloadToExcelFromObject(new File(filePath), data,
                    new String[]{"姓名", "年龄"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadToMap() throws IOException {
        ExcelUtil.readExcelToMap(new File(filePath), new String[]{"姓名", "年龄"}).forEach(System.out::println);
    }


    @Data
    static class TestAAA {
        private String name;
        private Integer age;
    }
}
