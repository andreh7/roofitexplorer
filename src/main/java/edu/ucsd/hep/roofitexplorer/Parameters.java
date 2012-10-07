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
package edu.ucsd.hep.roofitexplorer;

/**
 * Some parameters which currently are not stored elsewhere.
 * @author holzner
 */
public class Parameters
{
  /** the addresses of the objects seem to change between 
   *  dumping different objects (at least on some platforms).
   *  If this is set to true, the address is used, if set
   *  to false, the name is used instead (which should be unique
   *  within the workspace).
   */
  public static final boolean useAddress = false;
  
  /** we've seen occurrences where the client does not point
   *  to the server but the server points to the client according
   *  to the Print("V") output of the workspace members (not clear
   *  to me why this happens).
   * 
   *  Setting this flag to true, the code will add pointers 
   *  back to the server of clients to which servers point and
   *  vice versa add pointers back to the client to servers 
   *  pointed to by a client.
   */ 
  public static final boolean addMissingServerClientLinks = true;
  
  
}
