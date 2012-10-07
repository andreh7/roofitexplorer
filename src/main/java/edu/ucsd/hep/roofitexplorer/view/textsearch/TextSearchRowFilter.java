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

import java.util.*;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.table.TableModel;

/**
 * used to filter rows based on words the user types in
 * 
 * Note that the given table model class must implement TableModel
 * AND TextSearchTableModel
 * 
 * 
 * @author holzner
 */
public class TextSearchRowFilter extends RowFilter<TextSearchTableModel, Integer>
{
  /** contains model row numbers */
  private SortedSet<Integer> selectedRows;

  private String[] search_words;

  /** if true, only rows which match the search words AND are enabled
   *  are shown.
   */
  private boolean showEnabledOnly = false;
  
  //----------------------------------------------------------------------

  void setWords(String[] search_words)
  {
    // update the search words and invalidate the pre-calculated
    // selected rows
    this.search_words = search_words;
    selectedRows = null;

  }
  //----------------------------------------------------------------------

  /** this is called by the 'framework' to find out whether this 
   *  row should be included or not.
   * @param entry
   * @return 
   */
  @Override
  public boolean include(Entry<? extends TextSearchTableModel, ? extends Integer> entry)
  {

    // calculate the selected rows on demand (we have the tableModel here)
    if (this.selectedRows == null)
      calculateSelectedRows(entry.getModel());

    int row = entry.getIdentifier();
    
    if (showEnabledOnly && ! entry.getModel().isRowEnabled(row))
      return false;
      
    return this.selectedRows.contains(row);

  }
  //----------------------------------------------------------------------

  private void calculateSelectedRows(TextSearchTableModel tableModel)
  {
    selectedRows = new TreeSet<Integer>();

    int num_rows = ((TableModel)tableModel).getRowCount();

    // if no words specified, all rows will be selected
    if (this.search_words == null || this.search_words.length == 0)
    {
      for (int i = 0; i < num_rows; ++i)
        selectedRows.add(i);
      return;
    }

    // use regexps instead of plain String.contains(..): regexps
    // can be made case-insensitive (and maybe are faster, hoping
    // for a DFA to be compiled based on the pattern)
    List<Pattern> search_patterns = new ArrayList<Pattern>();

    // patterns which MUST NOT be present. Is there a way
    // to anti-match a regex ? This probably would also require
    // a modification in the 'and' logic when searching for
    // all fields..
    List<Pattern> exclude_patterns = new ArrayList<Pattern>();

    for (int i = 0; i < search_words.length; ++i)
    {
      String search_word = search_words[i];

      List<Pattern> list_to_append_to;

      if (search_word.startsWith("-"))
      {
        // exclude
        search_word = search_word.substring(1).trim();
        if (search_word.isEmpty())
          continue;

        list_to_append_to = exclude_patterns;
      }
      else
        list_to_append_to = search_patterns;

      Pattern pattern = Pattern.compile(Pattern.quote(search_word), Pattern.CASE_INSENSITIVE);

      list_to_append_to.add(pattern);
    }

    for (int row = 0; row < num_rows; ++row)
    {
      
      String fname = tableModel.getRowTextForSearching(row);

      if (containsAllWords(fname, search_patterns, exclude_patterns))
        selectedRows.add(row);
    }
  }

  //----------------------------------------------------------------------

  int getNumSelectedRows(TextSearchTableModel table_model)
  {
    if (this.selectedRows == null)
      calculateSelectedRows(table_model);

    return this.selectedRows.size();
  }
  //----------------------------------------------------------------------

  /** note that this does not take care of overlaps... */
  private boolean containsAllWords(String haystack, Collection<Pattern> search_patterns, 
    Collection<Pattern> exclude_patterns)
  {
    // first check for matching patterns
    for (Pattern pattern : search_patterns)
      if (! pattern.matcher(haystack).find())
        return false;

    // then for patterns which should NOT match
    for (Pattern pattern : exclude_patterns)
      if (pattern.matcher(haystack).find())
        return false;

    return true;
  }

  //----------------------------------------------------------------------

  public void setShowEnabledOnly(boolean showEnabledOnly)
  {
    this.showEnabledOnly = showEnabledOnly;
    // TODO: how to trigger an update of the table view ?
  }

  //----------------------------------------------------------------------

  /** @return the selected rows in model space */
  public SortedSet<Integer> getSelectedRows()
  {
    return Collections.unmodifiableSortedSet(selectedRows);
  }

  //----------------------------------------------------------------------
  
}
