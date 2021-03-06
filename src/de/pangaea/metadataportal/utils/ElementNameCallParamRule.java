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

package de.pangaea.metadataportal.utils;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * Similar to digester's {@link org.apache.commons.digester.PathCallParamRule},
 * but puts only the element name of the match onto the parameter stack.
 */
public final class ElementNameCallParamRule extends Rule {
  
  public ElementNameCallParamRule(int paramIndex) {
    this.paramIndex = paramIndex;
  }
  
  @Override
  public void begin(String namespace, String name, Attributes attributes) {
    final String param = getDigester().getCurrentElementName();
    if (param != null) {
      final Object parameters[] = (Object[]) digester.peekParams();
      parameters[paramIndex] = param;
    }
  }

  private final int paramIndex;
}
