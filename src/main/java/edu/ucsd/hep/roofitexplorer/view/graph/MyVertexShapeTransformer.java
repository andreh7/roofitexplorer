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
package edu.ucsd.hep.roofitexplorer.view.graph;

import java.awt.Rectangle;
import java.awt.Shape;
import org.apache.commons.collections15.Transformer;

/**
 * Defines how to draw a vertex.
 * 
 * The source code of the default transformer seems to be here:
 * http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-visualization/src/main/java/edu/uci/ics/jung/visualization/decorators/EllipseVertexShapeTransformer.java?view=markup
 * @author holzner
 */
class MyVertexShapeTransformer implements Transformer<VertexType, Shape>
{
  //----------------------------------------------------------------------

  private final int HALF_WIDTH = 25;
  private final int HALF_HEIGHT = 10;

  //----------------------------------------------------------------------
  
  public MyVertexShapeTransformer()
  {
  }
  //----------------------------------------------------------------------

  public Shape transform(VertexType vertex)
  {
    // TODO: make sure we don't re-create a new object
    // each time this is called;
    return new Rectangle(-HALF_WIDTH, -HALF_HEIGHT, 2 * HALF_WIDTH, 2 * HALF_HEIGHT);
  }
  
  //----------------------------------------------------------------------

}
