/*
 * Copyright 2013 University of California, San Diego.
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
package edu.ucsd.hep.roofitexplorer.view.graph;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author holzner
 */
public class TransferableBufferedImage implements Transferable
{
  final private BufferedImage image;

  public TransferableBufferedImage(BufferedImage image)
  {
    this.image = image;
  }
  
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]{ DataFlavor.imageFlavor };
  }

  public boolean isDataFlavorSupported(DataFlavor df)
  {
    return df.equals(DataFlavor.imageFlavor);
  }

  public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException
  {
    if (df.equals(DataFlavor.imageFlavor))
      return image;
    
    throw new UnsupportedFlavorException(df);
  }
  
}
