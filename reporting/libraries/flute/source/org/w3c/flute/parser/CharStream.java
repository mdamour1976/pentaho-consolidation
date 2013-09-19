/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.w3c.flute.parser;

/**
 * This interface describes a character stream that maintains line and
 * column number positions of the characters.  It also has the capability
 * to backup the stream to some extent.  An implementation of this
 * interface is used in the TokenManager implementation generated by
 * JavaCCParser.
 *
 * All the methods except backup can be implemented in any fashion. backup
 * needs to be implemented correctly for the correct operation of the lexer.
 * Rest of the methods are all used to get information like line number,
 * column number and the String that constitutes a token and are not used
 * by the lexer. Hence their implementation won't affect the generated lexer's
 * operation.
 */

public interface CharStream {

  /**
   * Returns the next character from the selected input.  The method
   * of selecting the input is the responsibility of the class
   * implementing this interface.  Can throw any java.io.IOException.
   */
  char readChar() throws java.io.IOException;

  /**
   * Returns the column position of the character last read.
   * @deprecated 
   * @see #getEndColumn
   */
  int getColumn();

  /**
   * Returns the line number of the character last read.
   * @deprecated 
   * @see #getEndLine
   */
  int getLine();

  /**
   * Returns the column number of the last character for current token (being
   * matched after the last call to BeginTOken).
   */
  int getEndColumn();

  /**
   * Returns the line number of the last character for current token (being
   * matched after the last call to BeginTOken).
   */
  int getEndLine();

  /**
   * Returns the column number of the first character for current token (being
   * matched after the last call to BeginTOken).
   */
  int getBeginColumn();

  /**
   * Returns the line number of the first character for current token (being
   * matched after the last call to BeginTOken).
   */
  int getBeginLine();

  /**
   * Backs up the input stream by amount steps. Lexer calls this method if it
   * had already read some characters, but could not use them to match a
   * (longer) token. So, they will be used again as the prefix of the next
   * token and it is the implemetation's responsibility to do this right.
   */
  void backup(int amount);

  /**
   * Returns the next character that marks the beginning of the next token.
   * All characters must remain in the buffer between two successive calls
   * to this method to implement backup correctly.
   */
  char BeginToken() throws java.io.IOException;

  /**
   * Returns a string made up of characters from the marked token beginning 
   * to the current buffer position. Implementations have the choice of returning
   * anything that they want to. For example, for efficiency, one might decide
   * to just return null, which is a valid implementation.
   */
  String GetImage();

  /**
   * Returns an array of characters that make up the suffix of length 'len' for
   * the currently matched token. This is used to build up the matched string
   * for use in actions in the case of MORE. A simple and inefficient
   * implementation of this is as follows :
   *
   *   {
   *      String t = GetImage();
   *      return t.substring(t.length() - len, t.length()).toCharArray();
   *   }
   */
  char[] GetSuffix(int len);

  /**
   * The lexer calls this function to indicate that it is done with the stream
   * and hence implementations can free any resources held by this class.
   * Again, the body of this function can be just empty and it will not
   * affect the lexer's operation.
   */
  void Done();

}
