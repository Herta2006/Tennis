package tennis;

import javafx.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static tennis.NormalDistributionCalculator.splitByGroups;

public class AtpPlayerStatsSaverServiceImpl implements SaverService {
    private static final String EXCEL_FILE_PATH = "src/main/resources/AtpPlayerStatistics.xlsx";
    private static final int TABLE_DATA_OFFSET = 3;
    private static final short DEFAULT_ROW_HEIGHT = 15;
    public static final short DEFAULT_RECORDS_SHEET_COLUMN_WIDTH = 6;
    public static final short DEFAULT_NORMAL_DISTRIBUTION_SHEET_COLUMN_WIDTH = 3;
    private static final String RECORDS_SHEET_NAME = "Records";
    private static final String NORMAL_DISTRIBUTION_SHEET_NAME = "NormalDistribution";
    private static final int NUMBER_COLUMN_INDEX = 0;
    private static final int NAME_COLUMN_INDEX = 1;
    private static final int SURNAME_COLUMN_INDEX = 2;
    public static final int LEVELS_AMOUNT = 5;
    private static int RETURN_SECTION_FIRST_INDEX;
    private static int TABLE_LAST_COLUMN_INDEX;
    private DataSource<AtpTennisPlayer> dataSource;
    private Sheet recordsSheet;
    private Sheet normalDistributionSheet;

    @Override
    public void saveInfo() {
        tryApachePoiApi(EXCEL_FILE_PATH);
    }

    private void tryApachePoiApi(String filePath) {
        File file = new File(filePath);
        setSheets(file);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            fillStatistics(recordsSheet);
            fillNormalDistribution(normalDistributionSheet);
            recordsSheet.getWorkbook().write(outputStream);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "cannot open output stream by file path " + filePath);
        }
    }

    private void setSheets(File file) {
        if (file.exists()) {
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                recordsSheet = workbook.getSheet(RECORDS_SHEET_NAME);
                normalDistributionSheet = workbook.getSheet(NORMAL_DISTRIBUTION_SHEET_NAME);
            } catch (IOException e) {
                Logger.getGlobal().log(Level.WARNING, "cannot create inout stream by file path " + file.getPath());
            }
        } else {
            XSSFWorkbook workbook = new XSSFWorkbook();
            recordsSheet = workbook.createSheet(RECORDS_SHEET_NAME);
            normalDistributionSheet = workbook.createSheet(NORMAL_DISTRIBUTION_SHEET_NAME);
        }
    }

    private void fillNormalDistribution(Sheet sheet) {
        List<AtpTennisPlayer> players = dataSource.getList(AtpTennisPlayer.class);
        AtpTennisPlayer firstPlayer = players.iterator().next();
        List<Pair<String, List<ValuesGroup>>> returnRecordLevelsGroup = splitAbstractRecordByLevels(LEVELS_AMOUNT, firstPlayer.getReturnRecord());
        List<Pair<String, List<ValuesGroup>>> serviceRecordLevelsGroup = splitServiceRecordByLevels(LEVELS_AMOUNT);
        setHeaderRow2(sheet, returnRecordLevelsGroup, serviceRecordLevelsGroup);
        sheet.setDefaultColumnWidth(DEFAULT_NORMAL_DISTRIBUTION_SHEET_COLUMN_WIDTH);
        sheet.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT);

        AtomicInteger rowCounter = new AtomicInteger(TABLE_DATA_OFFSET + 1);;
        AtomicInteger columnCounter = new AtomicInteger(TABLE_DATA_OFFSET);
        returnRecordLevelsGroup.stream().forEach(groups -> {
            groups.getValue().stream().forEach(level -> {
                level.getValues().entrySet().stream().forEach(g -> {
                    Row row = sheet.getRow(rowCounter.getAndIncrement());
                    if (row == null) {
                        row = sheet.createRow(rowCounter.getAndIncrement());
                    }
                    row.createCell(columnCounter.get()).setCellValue(g.getKey());
                    row.createCell(columnCounter.get() + 1).setCellValue(g.getValue());
                });
                rowCounter.set(rowCounter.get() + 1);
            });
            rowCounter.set(TABLE_DATA_OFFSET + 1);
            columnCounter.set(columnCounter.get() + 2);
        });
    }

    private void setHeaderRow2(Sheet sheet, List<Pair<String, List<ValuesGroup>>> serviceRecordGroupedLevels, List<Pair<String, List<ValuesGroup>>> returnRecordGroupedLevels) {
        Row row = sheet.createRow(TABLE_DATA_OFFSET);
        final AtomicInteger cellIndex = new AtomicInteger(TABLE_DATA_OFFSET);
        serviceRecordGroupedLevels.stream().forEach(g -> addParameter(row, cellIndex, g));
        returnRecordGroupedLevels.stream().forEach(g -> addParameter(row, cellIndex, g));
        CellStyle style = createHeaderRowStyle(row);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        applyStyleToRow(style, row);
        row.setHeightInPoints(72);
    }

    private void addParameter(Row row, AtomicInteger cellIndex, Pair<String, List<ValuesGroup>> g) {
        Cell cell1 = row.createCell(cellIndex.getAndIncrement());
        Cell cell2 = row.createCell(cellIndex.getAndIncrement());
        row.getSheet().addMergedRegion(new CellRangeAddress(
                        TABLE_DATA_OFFSET,
                        TABLE_DATA_OFFSET,
                        cell1.getColumnIndex(),
                        cell2.getColumnIndex())
        );
        cell1.setCellValue(g.getKey());
    }

    private void fillStatistics(Sheet sheet) {
        Row headerRow = sheet.createRow(TABLE_DATA_OFFSET);
        setHeaderRow(headerRow);
        CellStyle style = createHeaderRowStyle(headerRow);
        List<AtpTennisPlayer> players = dataSource.getList(AtpTennisPlayer.class);
        for (AtpTennisPlayer atpTennisPlayer : players) {
            Row row = sheet.createRow(TABLE_DATA_OFFSET + atpTennisPlayer.getRank());
            Cell numberCell = row.createCell(TABLE_DATA_OFFSET + NUMBER_COLUMN_INDEX);
            numberCell.setCellValue(atpTennisPlayer.getRank());

            Cell nameCell = row.createCell(TABLE_DATA_OFFSET + NAME_COLUMN_INDEX);
            nameCell.setCellValue(atpTennisPlayer.getFirstName());

            Cell surnameCell = row.createCell(TABLE_DATA_OFFSET + SURNAME_COLUMN_INDEX);
            surnameCell.setCellValue(atpTennisPlayer.getSurname());

            addRecordsCells(row, atpTennisPlayer);

            applyStyleToRow(style, row);
        }
        setSheetStyle(sheet);
        int firstRowIndex = headerRow.getRowNum() + 1;
        int lastRowIndex = headerRow.getRowNum() + 1 + players.get(players.size() - 1).getRank();
        short lastColumnIndex = headerRow.getLastCellNum();
        drawVerticalLine(sheet, headerRow.getFirstCellNum(), firstRowIndex, lastRowIndex, CellSide.LEFT, CellStyle.BORDER_THIN);
        drawVerticalLine(sheet, headerRow.getFirstCellNum(), firstRowIndex, lastRowIndex, CellSide.RIGHT, CellStyle.BORDER_THIN);
        drawVerticalLine(sheet, TABLE_DATA_OFFSET + SURNAME_COLUMN_INDEX, firstRowIndex, lastRowIndex, CellSide.RIGHT, CellStyle.BORDER_THIN);
        drawVerticalLine(sheet, TABLE_DATA_OFFSET + RETURN_SECTION_FIRST_INDEX, firstRowIndex, lastRowIndex, CellSide.LEFT, CellStyle.BORDER_THIN);
        drawVerticalLine(sheet, TABLE_DATA_OFFSET + TABLE_LAST_COLUMN_INDEX, firstRowIndex, lastRowIndex, CellSide.RIGHT, CellStyle.BORDER_THIN);
        drawHorizontalLine(sheet, lastRowIndex - 1, headerRow.getFirstCellNum(), lastColumnIndex, CellSide.BOTTOM, CellStyle.BORDER_THIN);
    }

    private void drawVerticalLine(Sheet sheet, int columnIndex, int startIndex, int endIndex, CellSide cellSide, short borderStyle) {
        int i = startIndex;
        try {
            CellStyle style = createBorderStyle(sheet, cellSide, borderStyle);
            for (; i < endIndex; i++) {
                if (sheet.getRow(i) == null || sheet.getRow(i).getCell(columnIndex) == null) continue;
                sheet.getRow(i).getCell(columnIndex).setCellStyle(style);
            }
        } catch (Throwable e) {
            System.err.println("drawVerticalLine. Cell NPE, index: " + i);
        }
    }

    private void drawHorizontalLine(Sheet sheet, int rowIndex, int startIndex, int endIndex, CellSide cellSide, short borderStyle) {
        CellStyle style = createBorderStyle(sheet, cellSide, borderStyle);
        int i = startIndex;
        try {
            for (; i < endIndex; i++) {
                if (sheet.getRow(rowIndex).getCell(i) == null) continue;
                sheet.getRow(rowIndex).getCell(i).setCellStyle(style);
            }
        } catch (Throwable e) {
            System.err.println("drawHorizontalLine. Cell NPE, index: " + i);
        }
    }

    private CellStyle createBorderStyle(Sheet sheet, CellSide cellSide, short borderStyle) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        switch (cellSide) {
            case TOP:
                style.setBorderTop(borderStyle);
                break;
            case RIGHT:
                style.setBorderRight(borderStyle);
                break;
            case BOTTOM:
                style.setBorderBottom(borderStyle);
                break;
            case LEFT:
                style.setBorderLeft(borderStyle);
        }
        return style;
    }

    private CellStyle createHeaderRowStyle(Row row) {
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        return style;
    }

    private void applyStyleToRow(CellStyle style, Row row) {
        int i = 0;
        for (; i < row.getLastCellNum(); i++) {
            try {
                Cell cell = row.getCell(i);
                if (cell == null) continue;
                cell.setCellStyle(style);
            } catch (Throwable e) {
                System.err.println("applyStyleToRow. Cell NPE, index: " + i);
            }
        }
    }

    private void setSheetStyle(Sheet records) {
        records.setColumnWidth(TABLE_DATA_OFFSET, calculateWidth(4));
        records.setColumnWidth(TABLE_DATA_OFFSET + 1, calculateWidth(10));
        records.setColumnWidth(TABLE_DATA_OFFSET + 2, calculateWidth(13));
        records.setDefaultColumnWidth(DEFAULT_RECORDS_SHEET_COLUMN_WIDTH);
        records.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT);
    }

    private void addRecordsCells(Row row, AtpTennisPlayer atpTennisPlayer) {
        int cellIndex = TABLE_DATA_OFFSET + 2;
        AtpTennisPlayer.ServiceRecord serviceRecord = atpTennisPlayer.getServiceRecord();
        int serviceFirstServePoints = serviceRecord.getFirstServePointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceFirstServePoints);
        int serviceSecondServePointsWon = serviceRecord.getSecondServePointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceSecondServePointsWon);
        int aces = serviceRecord.getAces().getValue();
        int doubleFaults = serviceRecord.getDoubleFaults().getValue();
        double doubleFaultsToAces = Math.round((doubleFaults / (double) aces) * 100);
        row.createCell(++cellIndex).setCellValue(doubleFaultsToAces);
        int serviceBreakPointsFaced = serviceRecord.getBreakPointsFaced().getValue();
        int serviceGamesPlayed = serviceRecord.getGamesPlayed().getValue();
        double serviceBreakPointsFacedPercentage = Math.round(((double) serviceBreakPointsFaced) / serviceGamesPlayed * 100);
        row.createCell(++cellIndex).setCellValue(serviceBreakPointsFacedPercentage);
        int serviceBreakPointsWon = serviceRecord.getBreakPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceBreakPointsWon);
        int serviceGamesWon = serviceRecord.getGamesWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceGamesWon);
        int serviceFirstServe = serviceRecord.getPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceFirstServe);
        int serviceTotalPointsWon = serviceRecord.getTotalPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(serviceTotalPointsWon);
        row.createCell(++cellIndex).setCellValue(aces);
        row.createCell(++cellIndex).setCellValue(doubleFaults);
        row.createCell(++cellIndex).setCellValue(serviceBreakPointsFaced);
        row.createCell(++cellIndex).setCellValue(serviceGamesPlayed);

        AtpTennisPlayer.AbstractRecord returnRecord = atpTennisPlayer.getReturnRecord();
        int returnFirstServePoints = returnRecord.getFirstServePointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnFirstServePoints);
        int returnSecondServePointsWon = returnRecord.getSecondServePointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnSecondServePointsWon);
        int returnBreakPointsFaced = returnRecord.getBreakPointsFaced().getValue();
        int returnGamesPlayed = returnRecord.getGamesPlayed().getValue();
        double returnBreakPointsFacedPercentage = Math.round(((double) returnBreakPointsFaced) / returnGamesPlayed * 100);
        row.createCell(++cellIndex).setCellValue(returnBreakPointsFacedPercentage);
        int returnBreakPointsWon = returnRecord.getBreakPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnBreakPointsWon);
        int returnGamesWon = returnRecord.getGamesWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnGamesWon);
        int returnPointsWon = returnRecord.getPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnPointsWon);
        int returnTotalPointsWon = returnRecord.getTotalPointsWon().getValue();
        row.createCell(++cellIndex).setCellValue(returnTotalPointsWon);
        row.createCell(++cellIndex).setCellValue(returnBreakPointsFaced);
        row.createCell(++cellIndex).setCellValue(returnGamesPlayed);
    }

    private void setHeaderRow(Row headerRow) {
        int cellIndex = TABLE_DATA_OFFSET;
        AtpTennisPlayer atpTennisPlayer = dataSource.getList(AtpTennisPlayer.class).iterator().next();
        headerRow.createCell(cellIndex).setCellValue("№");
        headerRow.createCell(++cellIndex).setCellValue("Name");
        headerRow.createCell(++cellIndex).setCellValue("Surname");
        addRecordsHeaderCells(headerRow, atpTennisPlayer, cellIndex);
        headerRow.setHeightInPoints(72);
        CellStyle headerRowStyle = createHeaderRowStyle(headerRow);
        headerRowStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderLeft(CellStyle.BORDER_THIN);
        applyStyleToRow(headerRowStyle, headerRow);
    }

    private void addRecordsHeaderCells(Row row, AtpTennisPlayer atpTennisPlayer, int cellIndex) {
        AtpTennisPlayer.ServiceRecord serviceRecord = atpTennisPlayer.getServiceRecord();
        row.createCell(++cellIndex).setCellValue(serviceRecord.getFirstServePointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(serviceRecord.getSecondServePointsWon().getKey());
        String aces = serviceRecord.getAces().getKey();
        String doubleFaults = serviceRecord.getDoubleFaults().getKey();
        row.createCell(++cellIndex).setCellValue(doubleFaults + "/" + aces);
        String serviceBreakPointFaced = serviceRecord.getBreakPointsFaced().getKey();
        row.createCell(++cellIndex).setCellValue(serviceBreakPointFaced);
        row.createCell(++cellIndex).setCellValue(serviceRecord.getBreakPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(serviceRecord.getGamesWon().getKey());
        row.createCell(++cellIndex).setCellValue(serviceRecord.getPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(serviceRecord.getTotalPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(aces);
        row.createCell(++cellIndex).setCellValue(doubleFaults);
        row.createCell(++cellIndex).setCellValue(serviceBreakPointFaced);
        row.createCell(++cellIndex).setCellValue(serviceRecord.getGamesPlayed().getKey());

        AtpTennisPlayer.AbstractRecord returnRecord = atpTennisPlayer.getReturnRecord();
        row.createCell(++cellIndex).setCellValue(returnRecord.getFirstServePointsWon().getKey());
        RETURN_SECTION_FIRST_INDEX = cellIndex - TABLE_DATA_OFFSET;
        row.createCell(++cellIndex).setCellValue(returnRecord.getSecondServePointsWon().getKey());
        String returnBreakPointFaced = returnRecord.getBreakPointsFaced().getKey();
        row.createCell(++cellIndex).setCellValue(returnBreakPointFaced);
        row.createCell(++cellIndex).setCellValue(returnRecord.getBreakPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(returnRecord.getGamesWon().getKey());
        row.createCell(++cellIndex).setCellValue(returnRecord.getPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(returnRecord.getTotalPointsWon().getKey());
        row.createCell(++cellIndex).setCellValue(returnBreakPointFaced);
        row.createCell(++cellIndex).setCellValue(returnRecord.getGamesPlayed().getKey());
        TABLE_LAST_COLUMN_INDEX = cellIndex - TABLE_DATA_OFFSET;
    }

    public List<Pair<String, List<ValuesGroup>>> splitServiceRecordByLevels(int levelsAmount) {
        AtpTennisPlayer.ServiceRecord record = dataSource.getList(AtpTennisPlayer.class).iterator().next().getServiceRecord();
        String doubleFaultName = record.getDoubleFaults().getKey();
        String acesName = record.getAces().getKey();
        List<Pair<String, List<ValuesGroup>>> recordGroups = splitAbstractRecordByLevels(levelsAmount, record);
        recordGroups.add(2, new Pair<>(doubleFaultName + "/" + acesName, splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> {
                    double aces = p.getServiceRecord().getAces().getValue();
                    int doubleFaults = p.getServiceRecord().getDoubleFaults().getValue();
                    return (int) Math.round(doubleFaults / aces * 100);
                })
                .collect(Collectors.toList()), levelsAmount)));
        return recordGroups;
    }

    public List<Pair<String, List<ValuesGroup>>> splitAbstractRecordByLevels(int levelsAmount, AtpTennisPlayer.AbstractRecord record) {
        List<Pair<String, List<ValuesGroup>>> recordGroups = new ArrayList<>();

        recordGroups.add(new Pair<>(record.getFirstServePointsWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> {
                    return p.getReturnRecord().getFirstServePointsWon().getValue();
                })
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getSecondServePointsWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> p.getReturnRecord().getSecondServePointsWon().getValue())
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getBreakPointsFaced().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> {
                    int breakPointsFaced = p.getReturnRecord().getBreakPointsFaced().getValue();
                    double gamesPlayed = p.getReturnRecord().getGamesPlayed().getValue();
                    return (int) Math.round(breakPointsFaced / gamesPlayed * 100);
                })
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getBreakPointsWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> p.getReturnRecord().getBreakPointsWon().getValue())
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getGamesWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> p.getReturnRecord().getGamesWon().getValue())
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getPointsWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> p.getReturnRecord().getPointsWon().getValue())
                .collect(Collectors.toList()), levelsAmount)));

        recordGroups.add(new Pair<>(record.getTotalPointsWon().getKey(), splitByGroups(dataSource.getList(AtpTennisPlayer.class).stream()
                .map(p -> p.getReturnRecord().getTotalPointsWon().getValue())
                .collect(Collectors.toList()), levelsAmount)));

        return recordGroups;
    }


//    private void tryJxlApi(String filePath) {
//        try {
//            File file = new File(filePath);
//            jxl.Workbook workbook;
//            if (!file.exists()) {
//                jxl.write.WritableWorkbook writableWorkbook = jxl.Workbook.createWorkbook(file);
//                jxl.write.WritableSheet recordsSheet = writableWorkbook.createSheet("Records", 0);
//                int row = 0;
//                int column = 0;
//                jxl.write.Label cellA1 = new jxl.write.Label(row, column, "Value");
//                try {
//                    recordsSheet.addCell(cellA1);
//                } catch (jxl.write.WriteException e) {
//                    Logger.getGlobal().log(Level.WARNING, "cannot add cell to newly created workbook. " +
//                            "Filepath is " + filePath + ". Cell coordinates =[" + row + ", " + column + "]");
//                }
//                writableWorkbook.write();
//                try {
//                    writableWorkbook.close();
//                } catch (jxl.write.WriteException e) {
//                    Logger.getGlobal().log(Level.WARNING, "cannot close newly created workbook. Filepath is " + filePath);
//                }
//            }
//            workbook = jxl.Workbook.getWorkbook(file);
//            jxl.Sheet sheet = workbook.getSheet(0);
//            jxl.Cell cellA1 = sheet.getCell(0, 0);
//            System.out.println(cellA1);
//        } catch (IOException | jxl.read.biff.BiffException e) {
//            Logger.getGlobal().log(Level.WARNING, "cannot open/create workbook by file path " + filePath);
//        }
//    }

    private static int calculateWidth(int unitWidth) {
        double value = 441.3793d + 256d * (unitWidth - 1d);
        return (int) Math.round(value);
    }

    @Override
    public void configure(ConfigContext configContext) {
        dataSource = configContext.getDataSource();
    }

    enum CellSide {
        TOP, RIGHT, BOTTOM, LEFT
    }
}
