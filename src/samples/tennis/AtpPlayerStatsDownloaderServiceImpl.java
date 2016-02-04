package samples.tennis;

// downloads page with tennis player ranking
// parses above page and get url for each player
// requests pages with stats for each player using above url
// parses each received above page and get stats data
// saves stats data to excel

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import samples.tennis.AtpTennisPlayer.AbstractRecord;
import samples.tennis.AtpTennisPlayer.ServiceRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AtpPlayerStatsDownloaderServiceImpl implements DownloaderService {
    private static final String ATP_WORLD_TOUR_SITE_URL = "http://www.atpworldtour.com";
    private static final String RANKINGS_SINGLES_PAGE_PATH = "/en/rankings/singles";
    private static final String OUTPUT_FILE_NAME = "SingleAtpTop100RankingPage.xml";
    private static final String EXCEL_FILE_PATH = "AtpPlayerStatistics.xlsx";
    private static final int TABLE_DATA_OFFSET = 3;
    private static final short DEFAULT_ROW_HEIGHT = 15;
    public static final short DEFAULT_COLUMN_WIDTH = 6;
    private static final String SHEET_NAME = "Records";
    public static final int NUMBER_COLUMN_INDEX = 0;
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int SURNAME_COLUMN_INDEX = 2;
    public static int RETURN_SECTION_FIRST_INDEX;
    public static int TABLE_LAST_COLUMN_INDEX;

    @Override
    public void doIt() throws Exception {
        Callable<byte[]> mainPageDownloader = new PageDownloader(ATP_WORLD_TOUR_SITE_URL + RANKINGS_SINGLES_PAGE_PATH);
        byte[] content = Executors.newFixedThreadPool(1).submit(mainPageDownloader).get();
//        saveToFile(content, OUTPUT_FILE_NAME);
        Document parsedPage = Jsoup.parse(new BufferedInputStream(new ByteArrayInputStream(content)), Charset.defaultCharset().name(), ATP_WORLD_TOUR_SITE_URL);
        Element body = parsedPage.body();
        Elements tds = body.getElementsByClass("player-cell");
        List<Thread> threads = new ArrayList<>();
        for (Element td : tds) {
            Element a = td.children().first();
            if (a == null) continue;
            Attribute href = a.attributes().iterator().next();
            AtpTennisPlayer atpTennisPlayer = new AtpTennisPlayer(ATP_WORLD_TOUR_SITE_URL + href.getValue());
            Thread thread = new Thread(new AtpPlayerInfoPageDownloader(atpTennisPlayer, PLAYERS, TIMES_PER_PAGE));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private void saveToFile(byte[] content, String filePath) throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
        outputStream.write(content);
        outputStream.close();
    }

    @Override
    public void saveInfoToExcel() {
        tryApachePoiApi(EXCEL_FILE_PATH);
    }

    private void tryApachePoiApi(String filePath) {
        File file = new File(filePath);
        Sheet sheet = getSheet(file);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            handlePlayers(sheet);
            sheet.getWorkbook().write(outputStream);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "cannot open output stream by file path " + filePath);
        }
    }

    private Sheet getSheet(File file) {
        if (file.exists()) {
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                return new XSSFWorkbook(inputStream).getSheet(SHEET_NAME);
            } catch (IOException e) {
                Logger.getGlobal().log(Level.WARNING, "cannot create inout stream by file path " + file.getPath());
            }
        }
        return new XSSFWorkbook().createSheet(SHEET_NAME);
    }

    private void handlePlayers(Sheet sheet) {
        Row headerRow = sheet.createRow(TABLE_DATA_OFFSET);
        setHeaderRow(headerRow);
        CellStyle style = createRowStyle(headerRow);
        for (AtpTennisPlayer atpTennisPlayer : PLAYERS) {
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
        int lastRowIndex = headerRow.getRowNum() + 1 + PLAYERS.get(PLAYERS.size() - 1).getRank();
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


    private CellStyle createRowStyle(Row row) {
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
        records.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);
        records.setDefaultRowHeightInPoints(DEFAULT_ROW_HEIGHT);
    }

    private void addRecordsCells(Row row, AtpTennisPlayer atpTennisPlayer) {
        int cellIndex = 2;
        ServiceRecord serviceRecord = atpTennisPlayer.getServiceRecord();
        int serviceFirstServePoints = serviceRecord.getFirstServePointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceFirstServePoints);
        int serviceSecondServePointsWon = serviceRecord.getSecondServePointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceSecondServePointsWon);
        int aces = serviceRecord.getAces().getValue();
        int doubleFaults = serviceRecord.getDoubleFaults().getValue();
        double doubleFaultsToAces = Math.round((doubleFaults / (double) aces) * 100);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(doubleFaultsToAces);
        int serviceBreakPointsFaced = serviceRecord.getBreakPointsFaced().getValue();
        int serviceGamesPlayed = serviceRecord.getGamesPlayed().getValue();
        double serviceBreakPointsFacedPercentage = Math.round(((double) serviceBreakPointsFaced) / serviceGamesPlayed * 100);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceBreakPointsFacedPercentage);
        int serviceBreakPointsWon = serviceRecord.getBreakPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceBreakPointsWon);
        int serviceGamesWon = serviceRecord.getGamesWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceGamesWon);
        int serviceFirstServe = serviceRecord.getPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceFirstServe);
        int serviceTotalPointsWon = serviceRecord.getTotalPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceTotalPointsWon);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(aces);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(doubleFaults);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceBreakPointsFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceGamesPlayed);

        AbstractRecord returnRecord = atpTennisPlayer.getReturnRecord();
        int returnFirstServePoints = returnRecord.getFirstServePointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnFirstServePoints);
        int returnSecondServePointsWon = returnRecord.getSecondServePointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnSecondServePointsWon);
        int returnBreakPointsFaced = returnRecord.getBreakPointsFaced().getValue();
        int returnGamesPlayed = returnRecord.getGamesPlayed().getValue();
        double returnBreakPointsFacedPercentage = Math.round(((double) returnBreakPointsFaced) / returnGamesPlayed * 100);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnBreakPointsFacedPercentage);
        int returnBreakPointsWon = returnRecord.getBreakPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnBreakPointsWon);
        int returnGamesWon = returnRecord.getGamesWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnGamesWon);
        int returnPointsWon = returnRecord.getPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnPointsWon);
        int returnTotalPointsWon = returnRecord.getTotalPointsWon().getValue();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnTotalPointsWon);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnBreakPointsFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnGamesPlayed);
    }

    private void setHeaderRow(Row headerRow) {
        int cellIndex = 0;
        AtpTennisPlayer atpTennisPlayer = PLAYERS.iterator().next();
        headerRow.createCell(TABLE_DATA_OFFSET + cellIndex).setCellValue("№");
        headerRow.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue("Name");
        headerRow.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue("Surname");
        addRecordsHeaderCells(headerRow, atpTennisPlayer, cellIndex);
        headerRow.setHeightInPoints(72);
        CellStyle headerRowStyle = createRowStyle(headerRow);
        headerRowStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerRowStyle.setBorderLeft(CellStyle.BORDER_THIN);
        applyStyleToRow(headerRowStyle, headerRow);
    }

    private void addRecordsHeaderCells(Row row, AtpTennisPlayer atpTennisPlayer, int cellIndex) {
        ServiceRecord serviceRecord = atpTennisPlayer.getServiceRecord();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getFirstServePointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getSecondServePointsWon().getKey());
        String aces = serviceRecord.getAces().getKey();
        String doubleFaults = serviceRecord.getDoubleFaults().getKey();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(aces + "/" + doubleFaults);
        String serviceBreakPointFaced = serviceRecord.getBreakPointsFaced().getKey();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceBreakPointFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getBreakPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getGamesWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getTotalPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(aces);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(doubleFaults);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceBreakPointFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(serviceRecord.getGamesPlayed().getKey());

        AbstractRecord returnRecord = atpTennisPlayer.getReturnRecord();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getFirstServePointsWon().getKey());
        RETURN_SECTION_FIRST_INDEX = cellIndex;
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getSecondServePointsWon().getKey());
        String returnBreakPointFaced = returnRecord.getBreakPointsFaced().getKey();
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnBreakPointFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getBreakPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getGamesWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getTotalPointsWon().getKey());
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnBreakPointFaced);
        row.createCell(TABLE_DATA_OFFSET + ++cellIndex).setCellValue(returnRecord.getGamesPlayed().getKey());
        TABLE_LAST_COLUMN_INDEX = cellIndex;
    }


    private void tryJxlApi(String filePath) {
        try {
            File file = new File(filePath);
            jxl.Workbook workbook;
            if (!file.exists()) {
                jxl.write.WritableWorkbook writableWorkbook = jxl.Workbook.createWorkbook(file);
                jxl.write.WritableSheet recordsSheet = writableWorkbook.createSheet("Records", 0);
                int row = 0;
                int column = 0;
                jxl.write.Label cellA1 = new jxl.write.Label(row, column, "Value");
                try {
                    recordsSheet.addCell(cellA1);
                } catch (jxl.write.WriteException e) {
                    Logger.getGlobal().log(Level.WARNING, "cannot add cell to newly created workbook. " +
                            "Filepath is " + filePath + ". Cell coordinates =[" + row + ", " + column + "]");
                }
                writableWorkbook.write();
                try {
                    writableWorkbook.close();
                } catch (jxl.write.WriteException e) {
                    Logger.getGlobal().log(Level.WARNING, "cannot close newly created workbook. Filepath is " + filePath);
                }
            }
            workbook = jxl.Workbook.getWorkbook(file);
            jxl.Sheet sheet = workbook.getSheet(0);
            jxl.Cell cellA1 = sheet.getCell(0, 0);
            System.out.println(cellA1);
        } catch (IOException | jxl.read.biff.BiffException e) {
            Logger.getGlobal().log(Level.WARNING, "cannot open/create workbook by file path " + filePath);
        }
    }

    private static int calculateWidth(int unitWidth) {
        double value = 441.3793d + 256d * (unitWidth - 1d);
        return (int) Math.round(value);
    }

    enum CellSide {
        TOP, RIGHT, BOTTOM, LEFT
    }
}


