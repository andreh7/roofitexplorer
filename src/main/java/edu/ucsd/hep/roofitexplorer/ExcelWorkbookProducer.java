/*
 * Copyright 2011-2012 University of California, San Diego.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucsd.hep.roofitexplorer;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Class taking a TableModel and producing a spreadsheet file.
 * (note that this is not specific to this project and could be 
 *  separated out into a different library)
 * 
 * @author holzner
 */
public class ExcelWorkbookProducer
{
   //----------------------------------------------------------------------

  /** make a spreadsheet from the table model. Note that this does NOT
   *  take into account any sorting / filtering applied when displaying
   *  the table.
   * @param model
   * @param sheet_name
   * @return
   */
  public static Workbook makeHSSFWorkbook(TableModel model, String sheet_name)
  {
    Workbook wb = new HSSFWorkbook();
    
    Sheet sheet = wb.createSheet();
    
    wb.setSheetName(0, sheet_name);
    
    Cell cell = null;
    
    int numCols = model.getColumnCount();
    
    // set title row
    Row row = sheet.createRow(0);
    for (int col = 0; col < numCols; ++col)
    {
      cell = row.createCell(col);
      cell.setCellValue(model.getColumnName(col));
    }
    
    // now fill in data content
    int first_data_row_num = 2;
    
    int num_rows = model.getRowCount();
    
    for (int rowIndex = 0; rowIndex < num_rows; rowIndex++)
    {
      row = sheet.createRow(rowIndex + first_data_row_num);
      for (int col = 0; col < numCols; ++col)
      {
        cell = row.createCell(col);
    
        Object value = model.getValueAt(rowIndex, col);
        if (value == null)
          continue;
        
        if (value instanceof Double)
          cell.setCellValue((Double) value);
        else
          cell.setCellValue(value.toString());
      } // loop over all columns of this row
      
    } // loop over all data rows
    
    return wb;
  }
  
  //----------------------------------------------------------------------

  /** similar to the above but running on the table itself (i.e. should
   *  take into account filtering and sorting)
   */
  public static Workbook makeHSSFWorkbook(JTable table, String sheetName)
  {
    Workbook wb = new HSSFWorkbook();
  
    Sheet sheet = wb.createSheet();
    wb.setSheetName(0, sheetName);
    
    Cell cell = null;
    
    int num_columns = table.getColumnCount();
    
    // set title row
    Row row = sheet.createRow(0);
    for (int col = 0; col < num_columns; ++col)
    {
      cell = row.createCell(col);
      cell.setCellValue(table.getColumnName(col));
    }
    
    // now fill in data content
    int first_data_row_num = 2;
    int numRows = table.getRowCount();
    
    for (int rowIndex = 0; rowIndex < numRows; ++rowIndex)
    {
      row = sheet.createRow(rowIndex + first_data_row_num);
      for (int col = 0; col < num_columns; ++col)
      {
        cell = row.createCell(col);
        Object value = table.getValueAt(rowIndex, col);
        if (value == null)
          continue;
        if (value instanceof Double)
          cell.setCellValue((Double) value);
        else
          cell.setCellValue(value.toString());
      } // loop over all columns of this row
    
    } // loop over all data rows
    
    return wb;
  }
  //----------------------------------------------------------------------

}
