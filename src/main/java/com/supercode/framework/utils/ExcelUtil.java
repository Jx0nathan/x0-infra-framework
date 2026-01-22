package com.supercode.framework.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jonathan.ji
 */
public class ExcelUtil {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 读取Excel，返回Map
     *
     * @param file
     * @param column 列名集合
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> readExcelToMap(File file, String[] column) throws IOException {
        return readExcelToMap(file, column, true);
    }

    /**
     * 读取Excel文件，返回Object
     */
    public static List<List<Object>> readExcelToList(File file) throws IOException {
        return readExcelToList(file, true);
    }

    /**
     * 读取Excel文件生成列表对象
     */
    public static List<List<Object>> readExcelToList(File file, boolean ignoreFirstRow) throws IOException {
        List<List<Object>> result = new ArrayList<>();
        Workbook wb = new HSSFWorkbook(new FileInputStream(file));
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            for (int j = ignoreFirstRow ? 1 : 0; j <= sheet.getLastRowNum(); j++) {
                Row row = sheet.getRow(j);
                List<Object> item = new ArrayList<>(row.getLastCellNum());
                for (int k = 0; k < row.getLastCellNum(); k++) {
                    item.add(getValue(row.getCell(k)));
                }
                if (item.size() > 0) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * @param file
     * @param column         列名集合
     * @param ignoreFirstRow
     * @return
     * @throws IOException
     */
    public static List<Map<String, Object>> readExcelToMap(File file, String[] column, boolean ignoreFirstRow) throws IOException {
        if (column.length < 1) {
            throw new IllegalArgumentException("column must not be empty!");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Workbook wb = new HSSFWorkbook(new FileInputStream(file));
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            int rows = sheet.getLastRowNum();
            for (int j = ignoreFirstRow ? 1 : 0; j <= rows; j++) {
                Row row = sheet.getRow(j);
                Map<String, Object> item = new HashMap<>();
                for (int k = 0; k < column.length; k++) {
                    item.put(column[k], getValue(row.getCell(k)));
                }
                if (item.size() > 0) {
                    result.add(item);
                }
            }
        }
        return result;
    }


    /**
     * 得到一个单元格的数据
     *
     * @param cell
     * @return
     */
    private static String getValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(cell.getDateCellValue());
                }
                return new BigDecimal(String.valueOf(cell.getNumericCellValue())).toPlainString();
            case FORMULA:
                return cell.getCellFormula();
            case ERROR:
                return "数据类型错误";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
            case BLANK:
                return null;
            default:
                return "未知数据类型";
        }
    }


    public static <T> void createWorkBook(Workbook wb, Sheet sheet, List<T> list,
                                          String header[], String keys[]) throws IllegalAccessException {
        for (int i = 0; i < header.length; i++) {
            sheet.setColumnWidth((short) i, (short) (35.7 * 150));
        }
        Row row = sheet.createRow((short) 0);
        CellStyle csHeader = handleHeaderStyle(wb);
        CellStyle csBody = handleBodyStyle(wb);
        // 设置列名
        for (int i = 0; i < header.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(csHeader);
        }
        // 设置每行每列的值
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Row row1 = sheet.createRow(i + 1);
            T t = list.get(i);
            dealData(keys, csBody, row1, t);
        }
    }

    private static CellStyle handleBodyStyle(Workbook wb) {
        Font f2 = wb.createFont();
        // 创建第二种字体样式（用于值）
        f2.setFontHeightInPoints((short) 10);
        f2.setColor(IndexedColors.BLACK.getIndex());
        CellStyle cs2 = wb.createCellStyle();
        // 设置第二种单元格的样式（用于值）
        cs2.setFont(f2);
        cs2.setBorderLeft(BorderStyle.THIN);
        cs2.setBorderRight(BorderStyle.THIN);
        cs2.setBorderTop(BorderStyle.THIN);
        cs2.setBorderBottom(BorderStyle.THIN);
        cs2.setAlignment(HorizontalAlignment.CENTER);
        return cs2;
    }

    private static CellStyle handleHeaderStyle(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        cs.setFont(f);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        return cs;
    }


    private static <T> void dealData(String[] keys, CellStyle cs2, Row row1, T t) throws IllegalAccessException {
        if (t instanceof Map) {
            if (keys == null || keys.length == 0) {
                throw new IllegalAccessException("keys must not be empty!");
            }
            Map map = (Map) t;
            for (short j = 0; j < keys.length; j++) {
                Cell cell = row1.createCell(j);
                cell.setCellValue(map.get(keys[j]) == null ? " " : map.get(keys[j]).toString());
                cell.setCellStyle(cs2);
            }
        } else {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (int j = 0; j < declaredFields.length; j++) {
                Cell cell = row1.createCell(j);
                Field field = declaredFields[j];
                field.setAccessible(true);
                Object value = field.get(t);
                cell.setCellValue(value == null ? " " : value.toString());
                cell.setCellStyle(cs2);
            }
        }
    }

    /**
     * 创建excel文档，
     *
     * @param list   数据
     * @param header excel的列名
     */
    public static <T> void downloadToExcelFromObject(File file, List<T> list, String header[]) throws IOException, IllegalAccessException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        createWorkBook(wb, sheet, list, header, null);
        wb.write(new FileOutputStream(file));
    }

    /**
     * 创建excel文档，
     *
     * @param list   数据
     * @param header excel的列名
     */
    public static <T> void downloadToExcelFromMap(File file, List<Map> list, String[] keys, String header[]) throws IOException, IllegalAccessException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        createWorkBook(wb, sheet, list, header, keys);
        wb.write(new FileOutputStream(file));
    }

}
