/*
 *   Copyright 2007-2008 panFMP Developers Team c/o Uwe Schindler
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

import java.util.Properties;

/**
 * This class extends Properties by a method to set the protected parent Properties
 * @author Uwe Schindler
 */
public final class InheritedProperties extends Properties {

	/** Sets the parent <code>Properties</code> object with the default values. */
	public void setParentProperties(Properties props) {
		this.defaults=props;
	}

}