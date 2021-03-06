/*
 *   Copyright panFMP Developers Team c/o Uwe Schindler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package de.pangaea.metadataportal.config;

import java.util.Arrays;
import java.util.Locale;

import de.pangaea.metadataportal.utils.PublicForDigesterUse;

/**
 * Config element that contains the definition of a field. It contains its name
 * and some properties like name or datatype.
 * 
 * @author Uwe Schindler
 */
public final class FieldConfig extends ExpressionConfig {
  
  public void setName(String v) {
    name = v;
  }
  
  public String getName() {
    return name;
  }
  
  /*
   * NOT YET USED public void setDataType(DataType v) { datatype=v; } public
   * DataType getDataType() { return datatype; }
   */
  
  @PublicForDigesterUse
  @Deprecated
  public void setDataType(String v) {
    try {
      datatype = DataType.valueOf(v.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid value '" + v
          + "' for attribute datatype, valid ones are: "
          + Arrays.toString(DataType.values()));
    }
  }
  
  public void setDefault(String v) {
    defaultValue = v;
  }
  
  public String getDefault() {
    return defaultValue;
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  // members "the configuration"
  public String name = null;
  public String defaultValue = null;
  public DataType datatype = DataType.STRING;
  
  public static enum DataType {
    STRING, NUMBER, DATETIME, INTEGER, BOOLEAN, XML, XHTML, JSON
  };
}
