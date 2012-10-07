/*
 * Copyright 2012 University of California, San Diego.
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
package edu.ucsd.hep.roofitexplorer.view.textsearch;

import java.util.SortedSet;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author holzner
 */
public class TextSearchRowFilterUtil
{
  private final TextSearchRowFilter rowFilter = new TextSearchRowFilter();
  
  
  private JButton clearTextFieldButton;
  
  private final JTextField searchTextField;
  // private final TableRowSorter tableRowSorter;
  private final AbstractTableModel tableModel;

  //----------------------------------------------------------------------

  public TextSearchRowFilterUtil(AbstractTableModel tableModel, JTextField searchTextField, TableRowSorter tableRowSorter)
  {
    this.searchTextField = searchTextField;
    this.tableModel = tableModel;
    
    // this.tableRowSorter = tableRowSorter;
    addDocumentListener(searchTextField);
    
    // also set the filter to the table row sorter 
    tableRowSorter.setRowFilter(rowFilter);
  }

  //----------------------------------------------------------------------

  /** constructor which does NOT set a table row filter. This must be 
   *  done manually by the calling code */
  public TextSearchRowFilterUtil(AbstractTableModel tableModel, JTextField searchTextField)
  {
    this.searchTextField = searchTextField;
    this.tableModel = tableModel;
      
    // this.tableRowSorter = tableRowSorter;
    addDocumentListener(searchTextField);
    
  }

  //----------------------------------------------------------------------

  /** adds the necessary DocumentListener to the given text field */
  private void addDocumentListener(JTextField searchTextField)
  {
    // add controls for filtering rows by text
    searchTextField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent de) { textFieldSearchTextChanged(de); }
      public void removeUpdate(DocumentEvent de) { textFieldSearchTextChanged(de); }
      public void changedUpdate(DocumentEvent de) { textFieldSearchTextChanged(de); }
    });
  }

  //----------------------------------------------------------------------
  
  public TextSearchRowFilter getRowFilter()
  {
    return rowFilter;
  }

  //----------------------------------------------------------------------
  
  private void clearTextFieldButtonActionPerformed(java.awt.event.ActionEvent evt)
  {
    // looks like there is no need to call another function
    // to update the selection, this seems to happen automatically
    this.searchTextField.setText("");
  }
  
  //----------------------------------------------------------------------

 /** this is called whenever the text in the search field changed */
  private void textFieldSearchTextChanged(DocumentEvent de)
  {
    // System.out.println("KEY TYPED: " + evt);

    // break the typed text into words
    String search_words[] = this.searchTextField.getText().split("\\s+");

    if (search_words.length < 1)
      return;

    setSearchFilter(search_words);
  }

  //----------------------------------------------------------------------

  private void setSearchFilter(String[] search_words)
  {
    this.rowFilter.setWords(search_words);

    // is there another way of making the sorter aware of the filter changing ?
    // this.tableRowSorter.setRowFilter(this.rowFilter);
  
    // this is a bit cheating, we pretend that the tablemodel has
    // changed when in fact the filter criterion has changed
    this.tableModel.fireTableDataChanged();
  }

  //----------------------------------------------------------------------

  public JButton getClearTextFieldButton()
  {
    if (clearTextFieldButton == null)
    {
      // clear button
      clearTextFieldButton = new JButton("Clear");
      clearTextFieldButton.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
          clearTextFieldButtonActionPerformed(evt);
        }
      });
    }

    return clearTextFieldButton;
  }
 
  //----------------------------------------------------------------------

  /** @see TextSearchRowFilter#getSelectedRows() */
  public SortedSet<Integer> getSelectedRows()
  {
    return rowFilter.getSelectedRows();
  }

  //----------------------------------------------------------------------

}
